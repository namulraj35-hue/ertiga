package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Calendar

/**
 * Firestore data model representing a Maruti Suzuki Ertiga vehicle listed for
 * self-drive rental in Jamshedpur, Jharkhand.
 */
@IgnoreExtraProperties
data class Vehicle(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val ownerName: String = "",
  val ownerPhone: String = "",
  val make: String = "Maruti Suzuki",
  val model: String = "Ertiga",
  val variant: String = "ZXi Smart Hybrid",
  val registrationNumber: String = "",
  val registrationYear: Int = 2022,
  val calculatedAge: Int = 2,
  val ageLabel: String = "Used", // "New" (<2 yrs), "Used" (2-5 yrs), "Older" (>5 yrs)
  val seatingCapacity: Int = 7,
  val transmission: String = "Manual", // "Manual", "Automatic"
  val fuelType: String = "Petrol",     // "Petrol", "CNG", "Diesel"
  val fuelPolicy: String = "Same-to-same",
  val dailyRate: Double = 2500.0,      // Owner-set price in INR
  val minDays: Int = 1,
  val maxDays: Int = 14,
  val includedKmPerDay: Int = 200,
  val extraKmCharge: Double = 12.0,
  val deliveryCharge: Double = 0.0,
  val securityDeposit: Double = 2000.0,
  val city: String = "Jamshedpur",
  val state: String = "Jharkhand",
  val country: String = "India",
  val pickupArea: String = "Bistupur", // Area inside Jamshedpur
  val conditionNotes: String = "",
  val photoUrl: String = "",
  val status: String = "PENDING_REVIEW", // PENDING_REVIEW, APPROVED, REJECTED, PAUSED
  val rejectionReason: String = "",
  val odometer: Int = 25000,
  val ownerRating: Double = 4.9,
  val completedRentalsCount: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  val isApproved: Boolean get() = status == "APPROVED"
  val isAvailable: Boolean get() = status == "APPROVED"
  val displayTitle: String get() = "$make $model $variant"
  val displayAge: String get() = if (calculatedAge <= 1) "1 year old" else "$calculatedAge years old"
  val formattedDailyPrice: String get() = "₹${dailyRate.toInt()}/day"

  companion object {
    fun computeAge(registrationYear: Int): Int {
      val currentYear = Calendar.getInstance().get(Calendar.YEAR)
      return (currentYear - registrationYear).coerceAtLeast(0)
    }

    fun computeAgeLabel(ageYears: Int): String {
      return when {
        ageYears < 2 -> "New"
        ageYears <= 5 -> "Used"
        else -> "Older"
      }
    }
  }
}
