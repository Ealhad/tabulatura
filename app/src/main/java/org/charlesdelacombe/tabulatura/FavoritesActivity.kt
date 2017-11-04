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
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select

class FavoritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabList = getFavorites()

        setContentView(R.layout.activity_favorites)

        tabListView.swapAdapter(TabListAdapter(tabList), true)
        tabListView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }

    override fun onResume() {
        super.onResume()

        tabListView.swapAdapter(TabListAdapter(getFavorites()), true)
    }

    private fun getFavorites(): List<TabInfo> {
        return database.use {
            select(
                    TabInfo.TABLE_NAME,
                    TabInfo.COLUMN_NAME,
                    TabInfo.COLUMN_URL,
                    TabInfo.COLUMN_NOTE,
                    TabInfo.COLUMN_VOTES
            )
                    .exec { parseList(classParser<TabInfo>()) }
        }
    }
}
