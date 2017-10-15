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

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.verticalLayout

class FavoritesActivity : AppCompatActivity() {

    private val ui = FavoritesActivityUI(TabListAdapter(emptyList()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabList = database.use {
            select(
                    TabInfo.TABLE_NAME,
                    TabInfo.COLUMN_NAME,
                    TabInfo.COLUMN_URL,
                    TabInfo.COLUMN_NOTE,
                    TabInfo.COLUMN_VOTES
            )
                    .exec { parseList(classParser<TabInfo>()) }
        }

        ui.setContentView(this)
        ui.tabList.swapAdapter(TabListAdapter(tabList), true)

        ui.tabList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }

    override fun onResume() {
        super.onResume()

        ui.tabList.swapAdapter(TabListAdapter(database.use {
            select(
                    TabInfo.TABLE_NAME,
                    TabInfo.COLUMN_NAME,
                    TabInfo.COLUMN_URL,
                    TabInfo.COLUMN_NOTE,
                    TabInfo.COLUMN_VOTES
            )
                    .exec { parseList(classParser<TabInfo>()) }
        }), true)
    }
}

class FavoritesActivityUI(private val listAdapter: TabListAdapter) : AnkoComponent<FavoritesActivity> {
    lateinit var tabList: RecyclerView

    override fun createView(ui: AnkoContext<FavoritesActivity>) = with(ui) {
        verticalLayout {
            tabList = recyclerView {
                adapter = listAdapter
            }
        }
    }
}
