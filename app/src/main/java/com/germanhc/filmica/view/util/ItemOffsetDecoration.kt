package com.germanhc.filmica.view.util

import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class ItemOffsetDecoration(@DimenRes val offsetId: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val offset = view.context.resources.getDimensionPixelSize(offsetId)
        val position = parent.getChildAdapterPosition(view)

        val items: Int = parent.adapter?.itemCount ?: 0

        if (parent.layoutManager is GridLayoutManager) {
            val columns = (parent.layoutManager as GridLayoutManager).spanCount
            val rows = (items + 1 / columns)

            val column = getColumn(position, columns)
            val row = getRow(position, columns)

            val topOffset = if (row == 1) offset else offset / 2
            val leftOffset = if (column == 1) offset else offset / 2

            val bottomOffset = if (row == rows) offset else offset / 2
            val rightOffset = if (column == columns) offset else offset / 2

            outRect.set(leftOffset, topOffset, rightOffset, bottomOffset)
        } else if (parent.layoutManager is LinearLayoutManager) {
            val top = if (position == 0) offset else 0
            outRect.set(offset, top, offset, offset)
        }


    }

    private fun getRow(position: Int, columns: Int) =
        Math.ceil((position.toDouble() + 1) / columns.toDouble()).toInt()

    private fun getColumn(position: Int, columns: Int) = (position % columns) + 1
}