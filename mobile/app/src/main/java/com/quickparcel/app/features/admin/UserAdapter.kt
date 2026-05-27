package com.quickparcel.app.features.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.quickparcel.app.R
import com.quickparcel.app.shared.models.User

class UserAdapter(
    private var users: List<User>,
    private val onStatusToggle: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], onStatusToggle)
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val btnToggle: TextView = itemView.findViewById(R.id.btn_toggle)
        private val cardUser: CardView = itemView.findViewById(R.id.card_user)

        fun bind(user: User, onStatusToggle: (User, Boolean) -> Unit) {
            tvName.text = "${user.firstName} ${user.lastName}"
            tvEmail.text = user.email
            tvType.text = user.userType

            val isActive = user.isActive
            tvStatus.text = if (isActive) "Active" else "Suspended"
            tvStatus.setTextColor(if (isActive) {
                itemView.context.getColor(R.color.quickparcel_green)
            } else {
                itemView.context.getColor(R.color.quickparcel_red)
            })

            btnToggle.text = if (isActive) "Suspend" else "Activate"
            btnToggle.setBackgroundColor(if (isActive) {
                itemView.context.getColor(R.color.quickparcel_red)
            } else {
                itemView.context.getColor(R.color.quickparcel_green)
            })

            cardUser.setOnClickListener {
                onStatusToggle(user, isActive)
            }

            btnToggle.setOnClickListener {
                onStatusToggle(user, isActive)
            }
        }
    }
}