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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.rx.rx_string
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.adapter_tab.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onEditorAction
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0"
        FuelManager.instance.baseHeaders = hashMapOf(Pair("user-agent", agent))

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        tabListView.adapter = TabListAdapter(emptyList())
        tabListView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val searchObservable: Observable<String> = Observable.create { emitter ->
            searchField.onEditorAction { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    emitter.onNext(v?.text.toString())
                }
            }
        }

        searchObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext { showProgress() }
                .observeOn(Schedulers.io())
                .flatMap {
                    val url = "https://www.ultimate-guitar.com/search.php?search_type=title&type=200&value=" + it
                    Fuel.get(url).rx_string().toObservable()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    hideProgress()
                    val tabs = getTabs(result.toString())
                    if (tabs.isEmpty()) {
                        toast("Search gave no results.")
                    }
                    tabListView.swapAdapter(TabListAdapter(tabs), true)
                }
    }

    private fun showProgress() {
        progressBar.visibility = VISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = GONE
    }
}

fun getTab(el: Element): TabInfo {
    val song = el.select(".search-version--link .song")
    val note = el.select(".rating").attr("title").toFloat()
    val votes = el.select(".ratdig").text().toInt()
    return TabInfo(
            song.text(),
            song.attr("href"),
            note,
            votes
    )
}

fun getTabs(html: String): List<TabInfo> =
        Jsoup.parse(html)
                .select(".tresults tr")
                .filter { it.getElementsByClass("tresults--rating").first()?.text()?.isNotBlank() == true }
                .map { getTab(it) }


class TabListAdapter(private val tabInfoList: List<TabInfo>) : RecyclerView.Adapter<TabViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val itemLayoutView: View = LayoutInflater.from(parent.context).inflate(R.layout.adapter_tab, null)
        return TabViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bindItems(tabInfoList[position])
    }

    override fun getItemCount(): Int = tabInfoList.size
}

class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindItems(tabInfo: TabInfo) {
        itemView.name.text = tabInfo.name

        itemView.rating.text = tabInfo.note.toString()
        itemView.votes.text = tabInfo.votes.toString()

        itemView.card.onClick {
            Cache.saveInfos(tabInfo)
            itemView.context.startActivity<DisplayTabActivity>("name" to tabInfo.name, "url" to tabInfo.url)
        }
    }
}

