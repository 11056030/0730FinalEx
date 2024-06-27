package com.example.a0730finalex.ui.add_edit_user

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.a0730finalex.R
import com.example.a0730finalex.User
import com.example.a0730finalex.AppDatabase
import com.example.a0730finalex.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

class AddEditUserFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtDescription: EditText
    private lateinit var imageView: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private var selectedUser: User? = null
    private var selectedImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_edit_user, container, false)

        edtName = view.findViewById(R.id.edtName)
        edtPhone = view.findViewById(R.id.edtPhone)
        edtDescription = view.findViewById(R.id.edtDescription)
        imageView = view.findViewById(R.id.imageView)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnSave = view.findViewById(R.id.btnSave)
        btnDelete = view.findViewById(R.id.btnDelete)

        db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
        userDao = db.userDao()

        arguments?.let {
            selectedUser = it.getSerializable("user") as User?
            selectedUser?.let { user ->
                edtName.setText(user.name)
                edtPhone.setText(user.phoneNumber)
                edtDescription.setText(user.description)
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(user.photo, 0, user.photo.size))
                edtPhone.isEnabled = false
            } ?: run {
                edtPhone.isEnabled = true
            }
        }

        btnSelectImage.setOnClickListener {
            checkStoragePermission()
        }

        btnSave.setOnClickListener {
            if (validatePhone()) {
                saveUser()
            } else {
                Toast.makeText(requireContext(), "請輸入有效的 10 位數字電話號碼", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            deleteUser()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("AddEditUserFragment", "Back button pressed, navigating up")
                findNavController().navigateUp()
            }
        })

        return view
    }



    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            selectImage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage()
                } else {
                    Toast.makeText(requireContext(), "沒有存儲權限，無法選擇圖片", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            val inputStream = requireActivity().contentResolver.openInputStream(selectedImageUri!!)
            selectedImage = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(selectedImage)
        }
    }

    private fun saveUser() {
        val name = edtName.text.toString()
        val phone = edtPhone.text.toString()
        val description = edtDescription.text.toString()
        val photo = selectedImage?.let { bitmapToByteArray(it) } ?: getDefaultAvatar()

        if (selectedUser == null) {
            val user = User(phone, name, description, photo)
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { userDao.insert(user) }
                    Toast.makeText(requireContext(), "用戶已保存", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "保存用戶失敗", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val updatedUser = selectedUser!!.copy(name = name, description = description, photo = photo)
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { userDao.update(updatedUser) }
                    Toast.makeText(requireContext(), "用戶已更新", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "更新用戶失敗", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteUser() {
        selectedUser?.let {
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { userDao.delete(it) }
                    Toast.makeText(requireContext(), "用戶已刪除", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "刪除用戶失敗", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToHome() {
        Log.d("AddEditUserFragment", "Navigating to home")
        findNavController().navigate(R.id.navigation_home)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun getDefaultAvatar(): ByteArray {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_avatar)
        return bitmapToByteArray(bitmap)
    }

    private fun validatePhone(): Boolean {
        val phone = edtPhone.text.toString().trim()

        val phonePattern = Pattern.compile("^\\d{10}\$")
        val matcher = phonePattern.matcher(phone)

        return matcher.matches()
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 1
        private const val REQUEST_SELECT_IMAGE = 2
    }
}
