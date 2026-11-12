package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class MarketplaceSettings(
  val appName: String = "BookMyErtiga Jamshedpur",
  val serviceCity: String = "Jamshedpur, Jharkhand",
  val currency: String = "INR",
  val currencySymbol: String = "₹",
  val timezone: String = "Asia/Kolkata",
  val platformFeePercent: Double = 5.0,
  val minRentalDays: Int = 1,
  val maxRentalDays: Int = 14,
  val allowOutstation: Boolean = false,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
