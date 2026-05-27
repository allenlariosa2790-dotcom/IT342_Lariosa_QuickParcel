package com.quickparcel.app.features.sender

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickparcel.app.R
import com.quickparcel.app.shared.models.Delivery
import java.text.SimpleDateFormat
import java.util.*

class DeliveryAdapter(
    private var deliveries: List<Delivery>,
    private val onItemClick: (Delivery) -> Unit,
    private val isRiderMode: Boolean = false,
    private val onStatusUpdate: ((Delivery, String) -> Unit)? = null
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    var currentList: List<Delivery> = deliveries
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_card, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val delivery = deliveries[position]
        holder.bind(delivery, onItemClick, isRiderMode, onStatusUpdate)
    }

    override fun getItemCount(): Int = deliveries.size

    fun updateDeliveries(newDeliveries: List<Delivery>) {
        deliveries = newDeliveries
        currentList = newDeliveries
        notifyDataSetChanged()
    }

    class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTrackingNumber: TextView = itemView.findViewById(R.id.tv_tracking_number)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvPickup: TextView = itemView.findViewById(R.id.tv_pickup)
        private val tvDropoff: TextView = itemView.findViewById(R.id.tv_dropoff)
        private val tvCost: TextView = itemView.findViewById(R.id.tv_cost)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val btnPickedUp: Button? = itemView.findViewById(R.id.btn_picked_up)
        private val btnInTransit: Button? = itemView.findViewById(R.id.btn_in_transit)
        private val btnDelivered: Button? = itemView.findViewById(R.id.btn_delivered)
        private val statusButtonsContainer: View? = itemView.findViewById(R.id.status_buttons_container)

        fun bind(delivery: Delivery, onItemClick: (Delivery) -> Unit, isRiderMode: Boolean, onStatusUpdate: ((Delivery, String) -> Unit)?) {
            tvTrackingNumber.text = delivery.trackingNumber
            tvStatus.text = delivery.status
            tvPickup.text = "📍 ${delivery.pickupAddress.take(50)}"
            tvDropoff.text = "🏁 ${delivery.dropoffAddress.take(50)}"
            tvCost.text = String.format("₱%.2f", delivery.estimatedCost)

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(delivery.createdAt)
                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDate.text = displayFormat.format(date)
            } catch (e: Exception) {
                tvDate.text = delivery.createdAt.take(10)
            }

            val bgRes = when (delivery.status) {
                "PENDING" -> R.drawable.bg_status_pending
                "ACCEPTED" -> R.drawable.bg_status_accepted
                "PICKED_UP", "IN_TRANSIT" -> R.drawable.bg_status_accepted
                "DELIVERED" -> R.drawable.bg_status_delivered
                else -> R.drawable.bg_status_pending
            }
            tvStatus.setBackgroundResource(bgRes)

            // Only show status update buttons for Rider mode and only for active deliveries
            if (isRiderMode && onStatusUpdate != null && delivery.status !in listOf("DELIVERED", "CANCELLED")) {
                statusButtonsContainer?.visibility = View.VISIBLE

                // Enable/disable based on current status
                btnPickedUp?.isEnabled = delivery.status == "ACCEPTED"
                btnPickedUp?.alpha = if (delivery.status == "ACCEPTED") 1.0f else 0.5f

                btnInTransit?.isEnabled = delivery.status == "PICKED_UP"
                btnInTransit?.alpha = if (delivery.status == "PICKED_UP") 1.0f else 0.5f

                btnDelivered?.isEnabled = delivery.status == "IN_TRANSIT"
                btnDelivered?.alpha = if (delivery.status == "IN_TRANSIT") 1.0f else 0.5f

                btnPickedUp?.setOnClickListener { onStatusUpdate(delivery, "PICKED_UP") }
                btnInTransit?.setOnClickListener { onStatusUpdate(delivery, "IN_TRANSIT") }
                btnDelivered?.setOnClickListener { onStatusUpdate(delivery, "DELIVERED") }
            } else {
                statusButtonsContainer?.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClick(delivery) }
        }
    }
}