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
import com.github.kittinunf.fuel.Fuel
import kotlinx.android.synthetic.main.activity_display_tab.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jsoup.Jsoup

class DisplayTabActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_display_tab)

        val url = intent.getStringExtra("url")

        val tabInfos = Cache.getInfos(url)
        val tabContent = TabContent(url, "")


        val dbContent = database.use {
            select(TabContent.TABLE_NAME, TabContent.COLUMN_CONTENT)
                    .whereArgs(TabContent.COLUMN_URL + " = {url}", "url" to url)
                    .limit(1)
                    .exec { parseOpt(StringParser) }
        }

        dbContent?.let {
            tabInfos.inFavorites = true
            Cache.saveContent(url, it)
        }

        if (Cache.hasContent(url)) {
            tabContentView.text = Cache.getContent(url)
        } else {
            Fuel.get(url).response { _, response, _ ->
                val doc = Jsoup.parse(response.toString())
                val content = doc.select(".js-tab-content").text()
                Cache.saveContent(url, content)
                tabContent.content = content
                tabContentView.text = content
            }
        }

        tabNameView.text = tabInfos.name

        fun setFontSize(size: Float) {
            tabContentView.textSize = size
        }

        incFontButton.onClick {
            prefs.fontSize += 1
            setFontSize(prefs.fontSize)
        }

        decFontButton.onClick {
            prefs.fontSize -= 1
            setFontSize(prefs.fontSize)
        }

        toggleFavButton.text = if (tabInfos.inFavorites) "unfav" else "fav"

        toggleFavButton.onClick {
            database.use {
                if (tabInfos.inFavorites) {
                    delete(TabInfo.TABLE_NAME, TabInfo.COLUMN_URL + " = {url}", "url" to url)
                    tabInfos.inFavorites = false
                    toggleFavButton.text = "fav"
                } else {
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

                    tabInfos.inFavorites = true
                    toggleFavButton.text = "unfav"
                }
            }
        }

    }
}
