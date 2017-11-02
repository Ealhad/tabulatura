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
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType.TYPE_CLASS_TEXT
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.rx.rx_string
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onEditorAction
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0"
        FuelManager.instance.baseHeaders = hashMapOf(Pair("user-agent", agent))

        super.onCreate(savedInstanceState)

        val ui = SearchActivityUI(TabListAdapter(emptyList()))
        ui.setContentView(this)

        ui.tabList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val searchObservable: Observable<String> = Observable.create { emitter ->
            ui.searchField.onEditorAction { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    emitter.onNext(v?.text.toString())
                }
            }
        }

        searchObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext { ui.showProgress() }
                .observeOn(Schedulers.io())
                .flatMap {
                    val url = "https://www.ultimate-guitar.com/search.php?search_type=title&type=200&value=" + it
                    Fuel.get(url).rx_string().toObservable()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    ui.hideProgress()
                    val tabs = getTabs(result.toString())
                    if (tabs.isEmpty()) {
                        toast("Search gave no results.")
                    }
                    ui.tabList.swapAdapter(TabListAdapter(tabs), true)
                }
    }

}

class SearchActivityUI(private val listAdapter: TabListAdapter) : AnkoComponent<SearchActivity> {
    lateinit var searchField: EditText
    lateinit var tabList: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<SearchActivity>) = with(ui) {
        verticalLayout {
            searchField = editText {
                hint = "Song name"
                imeOptions = IME_ACTION_SEARCH
                inputType = TYPE_CLASS_TEXT
            }

            progressBar = progressBar { visibility = GONE }

            tabList = recyclerView {
                adapter = listAdapter
            }
        }
    }

    fun showProgress() {
        progressBar.visibility = VISIBLE
    }

    fun hideProgress() {
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
                .filter { it.getElementsByClass("tresults--rating").first()?.text()?.isNotBlank() == true}
                .map { getTab(it) }


class TabListAdapter(private val tabInfoList: List<TabInfo>) : RecyclerView.Adapter<TabViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        return TabUI().createView(AnkoContext.create(parent.context, parent)).tag as TabViewHolder
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bindItems(tabInfoList[position])
    }

    override fun getItemCount(): Int = tabInfoList.size
}

class TabViewHolder(itemView: View, private val ui: TabUI) : RecyclerView.ViewHolder(itemView) {
    fun bindItems(tabInfo: TabInfo) {
        ui.name.text = tabInfo.name

        ui.rating.text = tabInfo.note.toString()
        ui.votes.text = tabInfo.votes.toString()

        ui.card.onClick {
            Cache.saveInfos(tabInfo)
            itemView.context.startActivity<DisplayTabActivity>("name" to tabInfo.name, "url" to tabInfo.url)
        }
    }
}

class TabUI : AnkoComponent<ViewGroup> {
    lateinit var card: CardView
    lateinit var name: TextView
    lateinit var rating: TextView
    lateinit var votes: TextView

    override fun createView(ui: AnkoContext<ViewGroup>): View {
        val itemView = with(ui) {
            linearLayout {
                lparams {
                    width = matchParent
                    height = wrapContent
                    margin = dip(5)
                }

                card = cardView {
                    relativeLayout {
                        name = textView {
                            textSize = 18f
                        }

                        rating = textView {
                            id = R.id.rating
                            typeface = Typeface.DEFAULT_BOLD
                            textSize = 16f
                        }
                                .lparams {
                                    alignParentEnd()
                                    alignParentTop()
                                }

                        votes = textView {
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        }
                                .lparams {
                                    alignParentEnd()
                                    below(rating)
                                }
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }

            }
        }
        itemView.tag = TabViewHolder(itemView, this)
        return itemView
    }
}

