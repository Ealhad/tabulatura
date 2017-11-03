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

data class TabInfo(
        val name: String,
        val url: String,
        val note: Float,
        val votes: Int
) {
    var inFavorites: Boolean = false

    companion object {
        val TABLE_NAME = "TabInfo"
        val COLUMN_NAME = "name"
        val COLUMN_URL = "url"
        val COLUMN_NOTE = "note"
        val COLUMN_VOTES = "votes"
    }
}

data class TabContent(
        val url: String,
        var content: String
) {
    companion object {
        val TABLE_NAME = "TabContent"
        val COLUMN_URL = "url"
        val COLUMN_CONTENT = "content"
    }
}

