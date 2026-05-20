package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class UserReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val size: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "favorites")
data class FavoriteProduct(
    @PrimaryKey val productId: String
)

@Entity(tableName = "promo_notifications")
data class PromoNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val badge: String, // "NEW ARRIVAL", "PROMOTION", "EXCLUSIVE"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
