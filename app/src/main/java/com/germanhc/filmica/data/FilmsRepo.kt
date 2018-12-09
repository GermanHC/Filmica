package com.germanhc.filmica.data

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.*
import com.germanhc.filmica.view.util.VolleyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object FilmsRepo {
    private val filmsDiscover: MutableList<Film> = mutableListOf()
    private val filmsTrend: MutableList<Film> = mutableListOf()
    private val filmsSearch: MutableList<Film> = mutableListOf()

    @Volatile
    private var db: AppDatabase? = null

    // Instantiate the RequestQueue with the cache and network. Start the queue.
    lateinit var requestSearchQueue: RequestQueue

    private fun getDbInstance(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "filmica-db"
            ).build()
        }
        return db as AppDatabase
    }

    fun findFilmById(id: String, listType: String): Film? {
        var film: Film? = null
        if (listType == TAG_FILMS || listType == TAG_WATCHLIST)
            film = filmsDiscover.find { film -> film.id == id }
        if (film == null)
            film = filmsTrend.find { film -> film.id == id }
        if (film == null)
            film = filmsSearch.find { film -> film.id == id }

        return film
    }

    fun getListFilms(
        listType: String,
        context: Context,
        callbackSuccess: ((MutableList<Film>) -> Unit),
        callbackError: ((VolleyError) -> Unit)
    ) {
        val films = if (listType == TAG_FILMS) filmsDiscover else filmsTrend
        if (films.isEmpty()) {
            val url: String = when (listType) {
                TAG_TRENDLIST -> ApiRoutes.trendUrl()
                TAG_SEARCHLIST -> ApiRoutes.searchUrl()
                else -> ApiRoutes.discoverUrl()
            }
            requestFilms(listType, url, callbackSuccess, callbackError, context)
        } else {
            callbackSuccess.invoke(films)
        }
    }

    fun saveFilm(
        context: Context,
        film: Film,
        callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().insertFilm(film)
            }

            async.await()
            callbackSuccess.invoke(film)
        }
    }

    fun watchlist(
        context: Context,
        callbackSuccess: (List<Film>) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().getFilms()
            }

            val films: List<Film> = async.await()
            callbackSuccess.invoke(films)
        }
    }

    fun deleteFilm(
        context: Context,
        film: Film,
        callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().deleteFilm(film)
            }
            async.await()
            callbackSuccess.invoke(film)
        }
    }

    private fun requestFilms(
        listType: String,
        url: String,
        callbackSuccess: (MutableList<Film>) -> Unit,
        callbackError: (VolleyError) -> Unit,
        context: Context
    ) {
        val films = if (listType == TAG_FILMS) filmsDiscover else filmsTrend
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val newFilms = Film.parseFilms(response)
                films.addAll(newFilms)
                callbackSuccess.invoke(films)
            },
            { error ->
                callbackError.invoke(error)
            })
        request.tag = listType
        Volley.newRequestQueue(context)
            .add(request)
    }

    fun searchFilms(
        queryText: String,
        callbackSuccess: (MutableList<Film>) -> Unit,
        callbackError: (VolleyError) -> Unit
    ) {
        filmsSearch.clear()
        VolleyService.cancelAllTag(TAG_SEARCHLIST)
        val url: String = ApiRoutes.searchUrl(queryText)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val newFilms = Film.parseFilms(response)
                filmsSearch.addAll(newFilms.take(10))
                callbackSuccess.invoke(filmsSearch)
            },
            { error ->
                Log.d("errorVolley", error.toString())
                callbackError.invoke(error)
            })

        request.tag = TAG_SEARCHLIST
        VolleyService.requestQueue.add(request)
        VolleyService.requestQueue.start()
    }

    fun dummyFilms(): List<Film> {
        return (0..9).map { i ->
            Film(
                title = "Film $i",
                overview = "Overview $i",
                genre = "Genre $i",
                voteRating = i.toDouble(),
                release = "200$i-0$i-0$i"
            )
        }
    }

}