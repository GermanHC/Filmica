package com.germanhc.filmica.view.watchlist

import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.View
import com.germanhc.filmica.R
import com.germanhc.filmica.data.Film
import com.germanhc.filmica.view.util.BaseFilmAdapter
import com.germanhc.filmica.view.util.BaseFilmHolder
import com.germanhc.filmica.view.util.SimpleTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_watchlist.view.*

class WatchlistAdapter :
    BaseFilmAdapter<WatchlistAdapter.WatchlistHolder>(
        layoutItem = R.layout.item_watchlist,
        holderCreator = { view -> WatchlistHolder(view) }
    ) {

    class WatchlistHolder(itemView: View) : BaseFilmHolder(itemView) {

        override fun bindFilm(film: Film) {
            super.bindFilm(film)

            with(itemView) {
                labelTitle.text = film.title
                labelOverview.text = film.overview
                labelVotes.text = film.voteRating.toString()
                loadImage()
            }
        }

        private fun loadImage() {
            val target = SimpleTarget(
                successCallback = { bitmap, from ->
                    itemView.imgPoster.setImageBitmap(bitmap)
                    setColorFrom(bitmap)
                }
            )

            itemView.imgPoster.tag = target

            Picasso.get()
                .load(film.getPosterUrl())
                .error(R.drawable.placeholder)
                .into(target)
        }

        private fun setColorFrom(bitmap: Bitmap) {
            Palette.from(bitmap).generate { palette ->
                val defaultColor = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                val swatch = palette?.vibrantSwatch ?: palette?.dominantSwatch
                val color = swatch?.rgb ?: defaultColor
                val overlayColor = Color.argb(
                    (Color.alpha(color) * 0.5).toInt(),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
                itemView.imgOverlay.setBackgroundColor(overlayColor)
            }
        }
    }
}