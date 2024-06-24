package com.example.a0730finalex

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private var users: MutableList<User>, private val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val callButton: ImageView = itemView.findViewById(R.id.callButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.nameTextView.text = if (user.name.isNotEmpty()) user.name else user.phoneNumber
        holder.phoneTextView.text = user.phoneNumber
        holder.photoImageView.setImageBitmap(BitmapFactory.decodeByteArray(user.photo, 0, user.photo.size))
        holder.itemView.setOnClickListener { onItemClick(user) }
        holder.callButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("user", user)
            it.findNavController().navigate(R.id.navigation_call, bundle)
        }
    }

    override fun getItemCount() = users.size

    fun removeAt(position: Int) {
        users.removeAt(position)
        notifyItemRemoved(position)
    }
}
