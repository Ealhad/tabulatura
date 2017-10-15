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

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import org.jetbrains.anko.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jsoup.Jsoup

class DisplayTabActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ui = DisplayTabActivityUI()
        ui.setContentView(this)

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
            ui.tabContent.text = Cache.getContent(url)
        } else {
            Fuel.get(url).response { _, response, _ ->
                val doc = Jsoup.parse(response.toString())
                val content = doc.select(".js-tab-content").text()
                Cache.saveContent(url, content)
                tabContent.content = content
                ui.tabContent.text = content
            }
        }

        ui.tabName.text = tabInfos.name

        fun setFontSize(size: Float) {
            ui.tabContent.textSize = size
        }

        ui.incFont.onClick {
            prefs.fontSize += 1
            setFontSize(prefs.fontSize)
        }

        ui.decFont.onClick {
            prefs.fontSize -= 1
            setFontSize(prefs.fontSize)
        }

        ui.toggleFav.text = if (tabInfos.inFavorites) "unfav" else "fav"

        ui.toggleFav.onClick {
            database.use {
                if (tabInfos.inFavorites) {
                    delete(TabInfo.TABLE_NAME, TabInfo.COLUMN_URL + " = {url}", "url" to url)
                    tabInfos.inFavorites = false
                    ui.toggleFav.text = "fav"
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
                    ui.toggleFav.text = "unfav"
                }
            }
        }

    }
}

class DisplayTabActivityUI : AnkoComponent<DisplayTabActivity> {
    lateinit var tabName: TextView
    lateinit var tabContent: TextView
    lateinit var decFont: Button
    lateinit var incFont: Button
    lateinit var toggleFav: Button

    private val customStyle = { v: Any ->
        when (v) {
            is TextView -> v.padding = 5
            is Button -> v.width = wrapContent
        }
    }

    override fun createView(ui: AnkoContext<DisplayTabActivity>) = with(ui) {
        relativeLayout {
            tabName = textView {
                id = R.id.tabName
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
            }

            incFont = button("+") {
                id = R.id.incFont
            }.lparams {
                alignParentEnd()
            }

            val textFont = textView("Font") {
                id = R.id.textFont
            }.lparams {
                leftOf(incFont)
            }

            decFont = button("-") { id = R.id.decFont }.lparams {
                leftOf(textFont)
            }

            toggleFav = button("fav") {
            }.lparams { leftOf(decFont) }


            scrollView {
                horizontalScrollView {
                    tabContent = textView {
                        typeface = Typeface.MONOSPACE
                        textSize = prefs.fontSize
                    }

                }
            }
                    .lparams {
                        below(tabName)
                    }
        }
    }.applyRecursively(customStyle)
}
