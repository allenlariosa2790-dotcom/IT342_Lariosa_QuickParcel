package com.quickparcel.app.features.tracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickparcel.app.R
import com.quickparcel.app.shared.models.TrackingHistory
import java.text.SimpleDateFormat
import java.util.*

class TrackingHistoryAdapter(
    private var history: List<TrackingHistory>
) : RecyclerView.Adapter<TrackingHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracking_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount(): Int = history.size

    fun updateHistory(newHistory: List<TrackingHistory>) {
        history = newHistory
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(history: TrackingHistory) {
            tvStatus.text = history.status
            tvLocation.text = history.location ?: history.status
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = dateFormat.format(Date(history.timestamp))
        }
    }
}