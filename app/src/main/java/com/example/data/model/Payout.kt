package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Payout(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val bookingId: String = "",
  val vehicleTitle: String = "",
  val grossRental: Double = 0.0,
  val platformCommission: Double = 0.0,
  val netEarnings: Double = 0.0,
  val status: String = "RECORDED", // RECORDED, PAID
  val payoutDateEpochMs: Long = 0L,
  val notes: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)
