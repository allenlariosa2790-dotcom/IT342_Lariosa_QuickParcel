package com.quickparcel.app.features.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.quickparcel.app.R
import com.quickparcel.app.shared.models.Delivery
import java.text.SimpleDateFormat
import java.util.*

class PaymentAdapter(
    private var deliveries: List<Delivery>,
    private val onItemClick: (Delivery) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private var allDeliveries: List<Delivery> = deliveries
    private var currentFilter = "ALL"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(deliveries[position], onItemClick)
    }

    override fun getItemCount(): Int = deliveries.size

    fun updateDeliveries(newDeliveries: List<Delivery>) {
        allDeliveries = newDeliveries
        filter(currentFilter)
    }

    fun filter(filter: String) {
        currentFilter = filter
        deliveries = when (filter) {
            "PAID" -> allDeliveries.filter { it.paymentStatus == "PAID" }
            "PENDING" -> allDeliveries.filter { it.paymentStatus in listOf("PENDING", "UNPAID") || it.paymentStatus == null }
            else -> allDeliveries
        }
        notifyDataSetChanged()
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardPayment: CardView = itemView.findViewById(R.id.card_payment)
        private val tvTrackingNumber: TextView = itemView.findViewById(R.id.tv_tracking_number)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tv_payment_method)
        private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tv_payment_status)

        fun bind(delivery: Delivery, onItemClick: (Delivery) -> Unit) {
            tvTrackingNumber.text = delivery.trackingNumber

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(delivery.createdAt)
            } catch (e: Exception) {
                null
            }
            tvDate.text = if (date != null) dateFormat.format(date) else "Unknown date"

            tvAmount.text = "₱${String.format("%.2f", delivery.estimatedCost)}"
            tvPaymentMethod.text = delivery.paymentMethod ?: "COD"

            val status = delivery.paymentStatus ?: "PENDING"
            val statusColor = when (status) {
                "PAID" -> itemView.context.getColor(R.color.quickparcel_green)
                "PENDING", "UNPAID" -> itemView.context.getColor(R.color.quickparcel_yellow)
                else -> itemView.context.getColor(R.color.quickparcel_gray)
            }
            tvPaymentStatus.setTextColor(statusColor)
            tvPaymentStatus.text = status

            cardPayment.setOnClickListener { onItemClick(delivery) }
        }
    }
}