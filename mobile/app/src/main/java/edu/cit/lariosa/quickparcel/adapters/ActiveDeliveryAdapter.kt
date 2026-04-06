package edu.cit.lariosa.quickparcel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.cit.lariosa.quickparcel.R
import edu.cit.lariosa.quickparcel.data.ActiveDelivery

class ActiveDeliveryAdapter(
        private val deliveries: List<ActiveDelivery>
) : RecyclerView.Adapter<ActiveDeliveryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_active_delivery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(deliveries[position])
    }

    override fun getItemCount() = deliveries.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTrackingNumber: TextView = itemView.findViewById(R.id.tvTrackingNumber)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvPickup: TextView = itemView.findViewById(R.id.tvPickup)
        private val tvDropoff: TextView = itemView.findViewById(R.id.tvDropoff)
        private val tvRider: TextView = itemView.findViewById(R.id.tvRider)
        private val tvEta: TextView = itemView.findViewById(R.id.tvEta)
        private val btnTrack: Button = itemView.findViewById(R.id.btnTrack)

        fun bind(delivery: ActiveDelivery) {
            tvTrackingNumber.text = delivery.trackingNumber
            tvStatus.text = delivery.status
            tvPickup.text = delivery.pickup
            tvDropoff.text = delivery.dropoff
            tvRider.text = "Rider: ${delivery.rider}"
            tvEta.text = "ETA: ${delivery.eta}"

            // Set status color based on status
            when (delivery.status) {
                "Delivered" -> tvStatus.setBackgroundResource(R.drawable.bg_status_delivered)
                "In Transit" -> tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                "Picked Up" -> tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                else -> tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }

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