package com.github.brugapp.brug.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Class handling the graphical part when swiping left or right on a list item.
 */
class CallbackUI(private val c: Canvas, private val viewHolder: RecyclerView.ViewHolder, private val dX: Float) {

    /**
     * Defines what is drawn in the window in the case of the swipe left (resp. swipe right) events.
     */
    fun setSwipeUI(
        swipePair: Pair<Drawable, Int>
    ){
        val chatView = viewHolder.itemView

        val icon: Drawable = swipePair.first
        val swipeBGColor: Int = swipePair.second


        val iconMargin = (chatView.height - icon.intrinsicHeight) / 2
        icon.setTint(Color.WHITE)

        val swipeBG = ColorDrawable(swipeBGColor)

        swipeBG.bounds = computeBGBounds(dX, chatView)
        icon.bounds = computeIconBounds(icon, dX, iconMargin, chatView)

        swipeBG.draw(c)
        c.save()
        c.clipRect(swipeBG.bounds)
        icon.draw(c)
        c.restore()
    }


    private fun computeBGBounds(dX: Float, chatView: View): Rect {
        val leftOffset: Int
        val rightOffset: Int
        if(dX > 0){
            leftOffset = chatView.left
            rightOffset = dX.toInt()
        } else {
            leftOffset = chatView.right + dX.toInt()
            rightOffset = chatView.right
        }

        return Rect(leftOffset, chatView.top, rightOffset, chatView.bottom)
    }

    private fun computeIconBounds(icon: Drawable, dX: Float, iconMargin: Int, chatView: View): Rect {
        val leftOffset: Int
        val rightOffset: Int
        if(dX > 0){
            leftOffset = chatView.left + iconMargin
            rightOffset = chatView.left + iconMargin + icon.intrinsicWidth
        } else {
            leftOffset = chatView.right - iconMargin - icon.intrinsicWidth
            rightOffset = chatView.right - iconMargin
        }

        return Rect(leftOffset, chatView.top + iconMargin, rightOffset, chatView.bottom - iconMargin)
    }
}