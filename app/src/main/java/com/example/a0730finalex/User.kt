package com.example.a0730finalex

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val description: String,
    val photo: ByteArray
) : Serializable
