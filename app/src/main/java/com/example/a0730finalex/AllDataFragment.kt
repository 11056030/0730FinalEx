package com.example.a0730finalex.ui.all_data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.a0730finalex.AppDatabase
import com.example.a0730finalex.R
import com.example.a0730finalex.User
import com.example.a0730finalex.UserAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AllDataFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var users = mutableListOf<User>()
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_data, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        userAdapter = UserAdapter(users) { user ->
            val bundle = Bundle()
            bundle.putSerializable("user", user)
            findNavController().navigate(R.id.action_navigation_home_to_navigation_edit, bundle)
        }
        recyclerView.adapter = userAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val user = users[position]
                deleteUser(user)
                userAdapter.removeAt(position)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)

        db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            val userList = db.userDao().getAll().toMutableList()
            launch(Dispatchers.Main) {
                users.clear()
                users.addAll(userList)
                userAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteUser(user: User) {
        GlobalScope.launch(Dispatchers.IO) {
            db.userDao().delete(user)
        }
    }
}
