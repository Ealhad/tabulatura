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

object Cache {
    private val tabContentMap: HashMap<String, String> = HashMap()
    private val tabInfosMap: HashMap<String, TabInfo> = HashMap()

    fun hasContent(url: String) = tabContentMap.containsKey(url)

    fun getContent(url: String) = tabContentMap.getValue(url)

    fun getInfos(url: String) = tabInfosMap.getValue(url)

    fun saveContent(url: String, content: String) {
        tabContentMap.put(url, content)
    }

    fun saveInfos(infos: TabInfo) {
        tabInfosMap.put(infos.url, infos)
    }
}
