package com.germanhc.filmica.view.detail

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.germanhc.filmica.R

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        if (savedInstanceState == null) {
            val id = intent.getStringExtra("id")

            val detailsFragment = DetailsFragment.newInstance(id)

            supportFragmentManager.beginTransaction()
                .add(R.id.container_details, detailsFragment)
                .commit()
        }
    }
}
