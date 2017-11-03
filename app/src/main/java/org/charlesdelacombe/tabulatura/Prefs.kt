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

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

val prefs: Prefs by lazy {
    Tabulatura.prefs!!
}

class Tabulatura : Application() {
    companion object {
        var prefs: Prefs? = null
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        super.onCreate()
    }
}

class Prefs(context: Context) {
    private val _prefsFilename = "org.charlesdelacombe.tablatura.prefs"
    private val _fontSize = "_fontSize"
    private val prefs: SharedPreferences = context.getSharedPreferences(_prefsFilename, 0)

    var fontSize: Float
        get() = prefs.getFloat(_fontSize, 12f)
        set(value) = prefs.edit().putFloat(_fontSize, value).apply()
}

