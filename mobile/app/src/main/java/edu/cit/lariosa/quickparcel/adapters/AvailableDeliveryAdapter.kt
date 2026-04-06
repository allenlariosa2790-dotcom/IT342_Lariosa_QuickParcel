package edu.cit.lariosa.quickparcel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.cit.lariosa.quickparcel.R
import edu.cit.lariosa.quickparcel.data.AvailableDelivery

class AvailableDeliveryAdapter(
        private val deliveries: List<AvailableDelivery>
) : RecyclerView.Adapter<AvailableDeliveryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_available_delivery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(deliveries[position])
    }

    override fun getItemCount() = deliveries.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTrackingNumber: TextView = itemView.findViewById(R.id.tvTrackingNumber)
        private val tvPickup: TextView = itemView.findViewById(R.id.tvPickup)
        private val tvDropoff: TextView = itemView.findViewById(R.id.tvDropoff)
        private val tvEarnings: TextView = itemView.findViewById(R.id.tvEarnings)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)

        fun bind(delivery: AvailableDelivery) {
            tvTrackingNumber.text = delivery.trackingNumber
            tvPickup.text = "From: ${delivery.pickup}"
            tvDropoff.text = "To: ${delivery.dropoff}"
            tvEarnings.text = delivery.earnings
            tvDistance.text = delivery.distance

            btnAccept.setOnClickListener {
                Toast.makeText(
                        itemView.context,
                        "Accepted ${delivery.trackingNumber}",
                        Toast.LENGTH_SHORT
                ).show()
                // TODO: Call API to accept delivery
            }
        }
    }
}