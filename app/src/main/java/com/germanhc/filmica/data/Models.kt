package com.germanhc.filmica.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@Entity
data class Film(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "vote_rating") var voteRating: Double = 0.0,
    var title: String = "",
    var genre: String = "",
    var release: String = "",
    var overview: String = "",
    var poster: String = ""
) {

    @Ignore
    constructor() : this("")

    fun getPosterUrl(): String {
        return "$BASE_POSTER_URL$poster"
    }

    companion object {
        fun parseFilms(response: JSONObject): MutableList<Film> {
            val films = mutableListOf<Film>()
            val filmsArray = response.getJSONArray("results")

            for (i in 0..(filmsArray.length() - 1)) {
                val film = parseFilm(filmsArray.getJSONObject(i))
                films.add(film)
            }

            return films
        }

        fun parseFilm(jsonFilm: JSONObject): Film {
            return Film(
                id = jsonFilm.getInt("id").toString(),
                title = jsonFilm.getString("title"),
                overview = jsonFilm.getString("overview"),
                voteRating = jsonFilm.getDouble("vote_average"),
                release = jsonFilm.getString("release_date"),
                poster = jsonFilm.optString("poster_path", ""),
                genre = parseGenres(jsonFilm.getJSONArray("genre_ids"))
            )
        }

        private fun parseGenres(genresArray: JSONArray): String {
            val genres = mutableListOf<String>()

            for (i in 0..(genresArray.length() - 1)) {
                val genreId = genresArray.getInt(i)
                val genre = ApiConstants.genres[genreId] ?: ""
                genres.add(genre)
            }

            return genres.reduce { acc, genre -> "$acc | $genre" }
        }
    }
}

