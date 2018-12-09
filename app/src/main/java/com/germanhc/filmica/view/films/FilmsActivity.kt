package com.germanhc.filmica.view.films

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.germanhc.filmica.R
import com.germanhc.filmica.data.*
import com.germanhc.filmica.view.detail.DetailsActivity
import com.germanhc.filmica.view.detail.DetailsFragment
import com.germanhc.filmica.view.util.VolleyService
import com.germanhc.filmica.view.watchlist.WatchlistFragment
import kotlinx.android.synthetic.main.activity_films.*

class FilmsActivity : AppCompatActivity(),
    FilmsFragment.OnItemClickListener,
    WatchlistFragment.OnItemClickListener,
    DetailsFragment.OnFilmSavedListener {

    private lateinit var filmsFragment: FilmsFragment
    private lateinit var watchlistFragment: WatchlistFragment
    private lateinit var trendlistFragment: FilmsFragment
    private lateinit var searchlistFragment: FilmsFragment
    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_films)


        if (savedInstanceState == null) {
            setupFragments()
        } else {
            val activeTag = savedInstanceState.getString("active", TAG_FILMS)
            restoreFragments(activeTag)
        }

        navigation?.setOnNavigationItemSelectedListener { item ->
            val id = item.itemId
            when (id) {
                R.id.action_discover -> showMainFragment(filmsFragment)
                R.id.action_trending -> showMainFragment(trendlistFragment)
                R.id.action_watchlist -> showMainFragment(watchlistFragment)
                R.id.action_search -> showMainFragment(searchlistFragment)
            }
            true
        }
        VolleyService.initialize(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("active", activeFragment.tag)
    }

    private fun setupFragments() {
        filmsFragment = FilmsFragment.newInstance(TAG_FILMS)
        watchlistFragment = WatchlistFragment()
        trendlistFragment = FilmsFragment.newInstance(TAG_TRENDLIST)
        searchlistFragment = FilmsFragment.newInstance(TAG_SEARCHLIST)

        supportFragmentManager.beginTransaction()
            .add(R.id.container_list, filmsFragment, TAG_FILMS)
            .add(R.id.container_list, watchlistFragment, TAG_WATCHLIST)
            .add(R.id.container_list, trendlistFragment, TAG_TRENDLIST)
            .add(R.id.container_list, searchlistFragment, TAG_SEARCHLIST)
            .hide(watchlistFragment)
            .hide(trendlistFragment)
            .hide(searchlistFragment)
            .commit()

        activeFragment = filmsFragment
        showDetails("")
    }

    private fun restoreFragments(tag: String) {
        filmsFragment = supportFragmentManager.findFragmentByTag(TAG_FILMS) as FilmsFragment
        watchlistFragment = supportFragmentManager.findFragmentByTag(TAG_WATCHLIST) as WatchlistFragment
        trendlistFragment = supportFragmentManager.findFragmentByTag(TAG_TRENDLIST) as FilmsFragment
        searchlistFragment = supportFragmentManager.findFragmentByTag(TAG_SEARCHLIST) as FilmsFragment

        activeFragment =
                when (tag) {
                    TAG_WATCHLIST -> watchlistFragment
                    TAG_TRENDLIST -> trendlistFragment
                    TAG_SEARCHLIST -> searchlistFragment
                    else -> filmsFragment
                }
    }

    override fun onItemClicked(film: Film) {
        showDetails(film.id)
    }

    override fun onFilmSaved(film: Film) {
        watchlistFragment.loadWatchlist()
    }

    private fun showMainFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()
        activeFragment = fragment
        showDetails("")
    }

    fun showDetails(id: String) {
        if (isTablet())
            showDetailsFragment(id)
        else
            launchDetailsActivity(id)
    }

    private fun isTablet() = this.containerDetails != null

    private fun showDetailsFragment(id: String) {
        val detailsFragment = DetailsFragment.newInstance(id, activeFragment.tag!!)

        supportFragmentManager.beginTransaction()
            .replace(R.id.containerDetails, detailsFragment)
            .commit()
    }

    private fun launchDetailsActivity(id: String) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("filmType", activeFragment.tag)
        startActivity(intent)

    }
}