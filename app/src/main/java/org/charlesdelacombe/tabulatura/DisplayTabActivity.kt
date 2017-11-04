/*
 * (c) Copyright 2017 Charles de Lacombe
 *
 * This file is part of Tabulatura (https://github.com/Ealhad/tabulatura).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package org.charlesdelacombe.tabulatura

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import com.github.kittinunf.fuel.Fuel
import kotlinx.android.synthetic.main.activity_display_tab.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jsoup.Jsoup


class DisplayTabActivity : AppCompatActivity() {
    private lateinit var tabContent: TabContent
    private lateinit var tabInfos: TabInfo
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_display_tab)

        val url = intent.getStringExtra("url")

        tabInfos = Cache.getInfos(url)
        tabContent = TabContent(url, "")

        // Always check the cache first
        if (Cache.hasContent(url)) {
            tabContentView.text = Cache.getContent(url)
        }

        // Query DB anyway, to find if it is a favorite
        val dbContent = database.use {
            select(TabContent.TABLE_NAME, TabContent.COLUMN_CONTENT)
                    .whereArgs(TabContent.COLUMN_URL + " = {url}", "url" to url)
                    .limit(1)
                    .exec { parseOpt(StringParser) }
        }

        // We have a favorite!
        dbContent?.let {
            Cache.saveContent(url, it)
            setFavorite(true)
            tabContentView.text = it
        }

        // Favorite or note, the content shouldn't be null.
        if (dbContent.isNullOrBlank()) {
            fetchTab(isFavorite)
        }

        tabNameView.text = tabInfos.name

        // TODO handle this with zoom instead
        fontSizeBar.progress = (prefs.fontSize.toInt() - 8) * 5
        fontSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val size = 8 + progress / 5f
                prefs.fontSize = size
                tabContentView.textSize = size
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        favoriteButton.isChecked = isFavorite

        favoriteButton.onCheckedChange { _, isChecked ->
            database.use {
                if (isChecked) {
                    insert(
                            TabInfo.TABLE_NAME,
                            TabInfo.COLUMN_NAME to tabInfos.name,
                            TabInfo.COLUMN_URL to url,
                            TabInfo.COLUMN_NOTE to tabInfos.note,
                            TabInfo.COLUMN_VOTES to tabInfos.votes
                    )

                    insert(
                            TabContent.TABLE_NAME,
                            TabContent.COLUMN_URL to url,
                            TabContent.COLUMN_CONTENT to tabContent.content
                    )
                } else {
                    delete(TabInfo.TABLE_NAME, TabInfo.COLUMN_URL + " = {url}", "url" to url)
                    delete(TabContent.TABLE_NAME, TabInfo.COLUMN_URL + " = {url}", "url" to url)
                }
                true
            }
            setFavorite(isChecked)
        }
    }

    private fun setFavorite(isFavorite: Boolean) {
        this.isFavorite = isFavorite
        favoriteButton.isChecked = isFavorite
        Cache.saveInfos(tabInfos)
    }

    private fun fetchTab(persist: Boolean = false) {
        Fuel.get(tabContent.url).response { _, response, _ ->
            val doc = Jsoup.parse(response.toString())
            val content = doc.select(".js-tab-content").text()
            Cache.saveContent(tabContent.url, content)
            tabContent.content = content
            tabContentView.text = content

            if (persist) {
                database.use {
                    replace(
                            TabContent.TABLE_NAME,
                            TabContent.COLUMN_URL to tabContent.url,
                            TabContent.COLUMN_CONTENT to tabContent.content
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_display_tab, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
            when (item?.itemId) {
                R.id.refresh -> {
                    fetchTab(isFavorite)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}
