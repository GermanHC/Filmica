package com.germanhc.filmica.view.films

import android.content.Context
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.germanhc.filmica.R
import com.germanhc.filmica.data.Film
import com.germanhc.filmica.data.FilmsRepo
import com.germanhc.filmica.data.TAG_SEARCHLIST
import com.germanhc.filmica.view.util.ItemOffsetDecoration
import com.germanhc.filmica.view.util.onChange
import kotlinx.android.synthetic.main.fragment_films.*
import kotlinx.android.synthetic.main.layout_error.*

class FilmsFragment : Fragment() {
    lateinit var listener: OnItemClickListener
    lateinit var listType: String
    private var searchQuery: String = ""
    private var actualPage: Int = 0

    companion object {
        fun newInstance(listType: String): FilmsFragment {
            val instance = FilmsFragment()
            val args = Bundle()
            args.putString("listType", listType)
            instance.arguments = args
            instance.actualPage = 1
            return instance
        }
    }

    val list: RecyclerView by lazy {
        val instance = view!!.findViewById<RecyclerView>(R.id.list_films)
        instance.addItemDecoration(ItemOffsetDecoration(R.dimen.offset_grid))
        instance.setHasFixedSize(true)
        instance
    }

    val adapter: FilmsAdapter by lazy {
        val instance = FilmsAdapter { film ->
            this.listener.onItemClicked(film)
        }

        instance
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnItemClickListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_films, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listType = arguments?.getString("listType") ?: ""

        searchBoxFunctionality()

        list.adapter = adapter
        list.addOnScrollListener(recyclerViewOnScrollListener)
        btnRetry?.setOnClickListener { reload() }
    }

    private fun searchBoxFunctionality() {
        search_box?.visibility = if (listType == TAG_SEARCHLIST) View.VISIBLE else View.GONE
        search_box?.let {
            this.search_box.onChange {
                if (it.trim().length > 3) {
                    searchQuery = it.trim()
                    searchFilms()
                }
            }

            this.search_box.setOnEditorActionListener { search_box, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchQuery = search_box.text.toString()
                    searchFilms()
                }
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when (listType) {
            TAG_SEARCHLIST -> searchFilms()
            else -> reload()
        }
    }

    fun reload() {
        FilmsRepo.getListFilms(listType,
            actualPage,
            context!!,
            { films ->
                progress?.visibility = View.INVISIBLE
                layoutError?.visibility = View.INVISIBLE
                list.visibility = View.VISIBLE
                adapter.setFilms(films)
            },
            { error ->
                progress?.visibility = View.INVISIBLE
                list.visibility = View.INVISIBLE
                layoutError?.visibility = View.VISIBLE
                error.printStackTrace()
            })
    }

    fun searchFilms() {
        if (searchQuery.length > 3) {
            FilmsRepo.searchFilms(searchQuery,
                { films ->
                    progress?.visibility = View.INVISIBLE
                    layoutError?.visibility = View.INVISIBLE
                    list.visibility = View.VISIBLE
                    adapter.setFilms(films)
                },
                { error ->
                    progress?.visibility = View.INVISIBLE
                    list.visibility = View.INVISIBLE
                    layoutError?.visibility = View.VISIBLE
                    error.printStackTrace()
                })
        }
    }

    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = (recyclerView.layoutManager as LinearLayoutManager).childCount
            val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
            val firstVisibleItemPosition =
                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

            //if (!isLoading && !isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= MifareUltralight.PAGE_SIZE
                ) {
                    actualPage +=1
                    reload()
                    adapter.notifyItemRangeInserted(visibleItemCount + firstVisibleItemPosition+1, adapter.itemCount)
                }
            //}
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film)
    }
}