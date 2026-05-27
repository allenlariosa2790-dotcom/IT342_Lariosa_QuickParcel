package com.quickparcel.app.features.rider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickparcel.app.R
import com.quickparcel.app.shared.models.Delivery
import java.text.SimpleDateFormat
import java.util.*

class EarningsAdapter(
    private var transactions: List<Delivery>
) : RecyclerView.Adapter<EarningsAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_earnings_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Delivery>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTrackingNumber: TextView = itemView.findViewById(R.id.tv_tracking_number)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvPickup: TextView = itemView.findViewById(R.id.tv_pickup)
        private val tvDropoff: TextView = itemView.findViewById(R.id.tv_dropoff)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)

        fun bind(delivery: Delivery) {
            tvTrackingNumber.text = delivery.trackingNumber

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = try {
                dateFormat.parse(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
            } catch (e: Exception) {
                null
            }
            tvDate.text = if (date != null) dateFormat.format(date) else "Unknown date"

            tvPickup.text = delivery.pickupAddress.split(",").firstOrNull() ?: delivery.pickupAddress
            tvDropoff.text = delivery.dropoffAddress.split(",").firstOrNull() ?: delivery.dropoffAddress
            tvAmount.text = "₱${String.format("%.2f", delivery.estimatedCost)}"
        }
    }
}