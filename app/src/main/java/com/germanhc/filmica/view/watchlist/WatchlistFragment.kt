package com.germanhc.filmica.view.watchlist


import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_watchlist.*
import com.germanhc.filmica.R
import com.germanhc.filmica.data.Film
import com.germanhc.filmica.data.FilmsRepo
import com.germanhc.filmica.view.util.SwipeToDeleteCallback


class WatchlistFragment : Fragment() {
    lateinit var listener: OnItemClickListener

    val adapter: WatchlistAdapter by lazy {
        val instance = WatchlistAdapter { film ->
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeHandler()
        watchlist.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        loadWatchlist()
    }

    fun loadWatchlist() {
        FilmsRepo.watchlist(context!!) { films ->
            adapter.setFilms(films.toMutableList())
        }
    }

    private fun setupSwipeHandler() {
        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val film = adapter.getFilm(holder.adapterPosition)
                deleteFilmAt(holder.adapterPosition, film)
                Snackbar.make(holder.itemView, getString(R.string.snackbarDeleteElement), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { restoreFilm(film) }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(watchlist)
    }


    private fun deleteFilmAt(position: Int, film: Film) {
        FilmsRepo.deleteFilm(context!!, film) {
            adapter.removeFilmAt(position)
        }
    }

    private fun restoreFilm(film: Film) {
        FilmsRepo.saveFilm(context!!, film) {
            loadWatchlist()
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film)
    }
}
