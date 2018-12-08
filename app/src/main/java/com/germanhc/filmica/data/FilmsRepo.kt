package com.germanhc.filmica.data

import android.arch.persistence.room.Room
import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object FilmsRepo {
    private val films: MutableList<Film> = mutableListOf()

    @Volatile
    private var db: AppDatabase? = null

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

    fun findFilmById(id: String): Film? {
        return films.find { film -> film.id == id }
    }

    fun discoverFilms(
        context: Context,
        callbackSuccess: ((MutableList<Film>) -> Unit),
        callbackError: ((VolleyError) -> Unit)
    ) {
        if (films.isEmpty()) {
            requestDiscoverFilms(callbackSuccess, callbackError, context)
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

    private fun requestDiscoverFilms(
        callbackSuccess: (MutableList<Film>) -> Unit,
        callbackError: (VolleyError) -> Unit,
        context: Context
    ) {
        val url = ApiRoutes.discoverUrl()
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val newFilms = Film.parseFilms(response)
                films.addAll(newFilms)
                callbackSuccess.invoke(films)
            },
            { error ->
                callbackError.invoke(error)
            })
        Volley.newRequestQueue(context)
            .add(request)
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