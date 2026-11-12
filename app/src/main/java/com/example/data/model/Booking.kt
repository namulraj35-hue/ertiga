package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Booking(
  @DocumentId val id: String = "",
  val vehicleId: String = "",
  val vehicleTitle: String = "",
  val vehicleVariant: String = "",
  val renterId: String = "",
  val renterName: String = "",
  val renterPhone: String = "",
  val ownerId: String = "",
  val pickupArea: String = "",
  val startEpochMs: Long = 0L,
  val endEpochMs: Long = 0L,
  val daysCount: Int = 1,
  val status: String = "REQUESTED", // REQUESTED, AWAITING_PAYMENT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, DECLINED, EXPIRED
  val holdExpiresAtEpochMs: Long = 0L,
  val dailyRate: Double = 0.0,
  val baseTotal: Double = 0.0,
  val platformFee: Double = 0.0,
  val deliveryFee: Double = 0.0,
  val deposit: Double = 0.0,
  val totalAmount: Double = 0.0,
  val paymentStatus: String = "UNPAID", // UNPAID, PAYMENT_UNVERIFIED, PAID
  val paymentMode: String = "", // CASH, UPI
  val paymentRef: String = "",
  val handoverOtp: String = "",
  val pickupOdometer: Int = 0,
  val pickupFuelPercent: Int = 100,
  val pickupNotes: String = "",
  val pickupCompletedAtEpochMs: Long = 0L,
  val returnOdometer: Int = 0,
  val returnFuelPercent: Int = 100,
  val returnNotes: String = "",
  val returnCompletedAtEpochMs: Long = 0L,
  val declineReason: String = "",
  val cancellationReason: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
