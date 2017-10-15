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
import android.widget.Button
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ui = MainActivityUI()
        ui.setContentView(this)

        ui.searchButton.onClick {
            startActivity<SearchActivity>()
        }

        ui.favButton.onClick {
            startActivity<FavoritesActivity>()
        }
    }
}

class MainActivityUI : AnkoComponent<MainActivity> {
    lateinit var searchButton: Button
    lateinit var favButton: Button
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        verticalLayout {
            searchButton = button("Search")
            favButton = button("Favorites")
        }
    }
}
