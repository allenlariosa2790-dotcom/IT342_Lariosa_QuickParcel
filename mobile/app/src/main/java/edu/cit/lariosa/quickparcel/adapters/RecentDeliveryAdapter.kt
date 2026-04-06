package edu.cit.lariosa.quickparcel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.cit.lariosa.quickparcel.R
import edu.cit.lariosa.quickparcel.data.RecentDelivery

class RecentDeliveryAdapter(
        private val deliveries: List<RecentDelivery>
) : RecyclerView.Adapter<RecentDeliveryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recent_delivery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(deliveries[position])
    }

    override fun getItemCount() = deliveries.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTrackingNumber: TextView = itemView.findViewById(R.id.tvTrackingNumber)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvEta: TextView = itemView.findViewById(R.id.tvEta)
        private val btnTrack: Button = itemView.findViewById(R.id.btnTrack)

        fun bind(delivery: RecentDelivery) {
            tvTrackingNumber.text = delivery.trackingNumber
            tvAddress.text = delivery.address
            tvDate.text = delivery.date
            tvEta.text = "ETA: ${delivery.eta}"

            btnTrack.setOnClickListener {
                Toast.makeText(
                        itemView.context,
                        "Track ${delivery.trackingNumber}",
                        Toast.LENGTH_SHORT
                ).show()
                // TODO: Navigate to tracking screen
            }
        }
    }
}