package com.quickparcel.app.shared.components

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.quickparcel.app.R

class StatusBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    fun setStatus(status: String) {
        text = status
        val (bgRes, textColor) = when (status) {
            "PENDING" -> R.drawable.bg_status_pending to android.R.color.white
            "ACCEPTED" -> R.drawable.bg_status_accepted to android.R.color.white
            "PICKED_UP", "IN_TRANSIT" -> R.drawable.bg_status_accepted to android.R.color.white
            "DELIVERED" -> R.drawable.bg_status_delivered to android.R.color.white
            "CANCELLED" -> R.drawable.bg_status_pending to android.R.color.white
            else -> R.drawable.bg_status_pending to android.R.color.white
        }
        setBackgroundResource(bgRes)
        setTextColor(ContextCompat.getColor(context, textColor))
    }
}