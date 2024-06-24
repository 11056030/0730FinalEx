package com.example.a0730finalex.ui.call

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a0730finalex.R
import com.example.a0730finalex.User

class CallFragment : Fragment() {

    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_call, container, false)

        user = arguments?.getSerializable("user") as User

        val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val phoneTextView: TextView = view.findViewById(R.id.phoneTextView)
        val hangupButton: Button = view.findViewById(R.id.hangupButton)

        nameTextView.text = user.name
        phoneTextView.text = user.phoneNumber
        photoImageView.setImageBitmap(BitmapFactory.decodeByteArray(user.photo, 0, user.photo.size))

        hangupButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }
}
