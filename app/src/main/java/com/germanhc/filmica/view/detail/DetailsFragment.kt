package com.germanhc.filmica.view.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.*
import android.widget.Toast
import com.germanhc.filmica.R
import com.germanhc.filmica.data.Film
import com.germanhc.filmica.data.FilmsRepo
import com.germanhc.filmica.view.util.SimpleTarget
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_details.*

class DetailsFragment : Fragment() {

    companion object {
        fun newInstance(id: String): DetailsFragment {
            val instance = DetailsFragment()
            val args = Bundle()
            args.putString("id", id)
            instance.arguments = args

            return instance
        }
    }

    private var film: Film? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_details, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id: String = arguments?.getString("id") ?: ""
        film = FilmsRepo.findFilmById(id)

        film?.let {
            with(it) {
                labelTitle.text = title
                labelOverview.text = overview
                labelGenre.text = genre
                labelRelease.text = release
                labelVotes.text = voteRating.toString()

                loadImage(it)
            }
        }

        btnAdd.setOnClickListener {
            film?.let {
                FilmsRepo.saveFilm(context!!, it){
                    Toast.makeText(context, "Added to list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item.takeIf { item?.itemId == R.id.action_share }?.let { menuItem ->
            shareFilm()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun shareFilm() {
        val intent = Intent(Intent.ACTION_SEND)

        film?.let {
            val text = getString(R.string.template_share, it.title, it.voteRating)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, text)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.title_share)))
    }

    private fun loadImage(film: Film) {
        val target = SimpleTarget(
            successCallback = { bitmap, from ->
                imgPoster.setImageBitmap(bitmap)
                setColorFrom(bitmap)
            }
        )

        imgPoster.tag = target

        Picasso.get()
            .load(film.getPosterUrl())
            .error(R.drawable.placeholder)
            .into(target)
    }

    private fun setColorFrom(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val defaultColor = ContextCompat.getColor(context!!, R.color.colorPrimary)
            val swatch = palette?.vibrantSwatch ?: palette?.dominantSwatch
            val color = swatch?.rgb ?: defaultColor
            val overlayColor = Color.argb(
                (Color.alpha(color) * 0.5).toInt(),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )

            overlay.setBackgroundColor(overlayColor)
            btnAdd.backgroundTintList = ColorStateList.valueOf(color)
        }
    }
}