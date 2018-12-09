package com.germanhc.filmica.view.trendlist


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.germanhc.filmica.R
import com.germanhc.filmica.data.Film

class TrendlistFragment : Fragment() {
    lateinit var listener: TrendlistFragment.OnItemClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trendlist, container, false)
    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film)
    }

}
