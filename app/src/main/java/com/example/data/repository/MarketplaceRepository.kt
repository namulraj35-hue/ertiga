package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Booking
import com.example.data.model.MarketplaceSettings
import com.example.data.model.Payout
import com.example.data.model.ServiceArea
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

class MarketplaceRepository(
  private val db: FirebaseFirestore,
  private val context: Context
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id)),
    context
  )

  private val auth get() = Firebase.auth

  fun currentUserId(): String? = auth.currentUser?.uid
  fun currentUserEmail(): String? = auth.currentUser?.email

  // Settings
  fun observeSettings(): Flow<MarketplaceSettings> = callbackFlow {
    val docRef = db.collection("settings").document("marketplace")
    val listener = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        return@addSnapshotListener
      }
      val settings = snapshot?.toObject(MarketplaceSettings::class.java) ?: MarketplaceSettings()
      trySend(settings)
    }
    awaitClose { listener.remove() }
  }

  suspend fun updateSettings(settings: MarketplaceSettings): Result<Unit> = runCatching {
    val docRef = db.collection("settings").document("marketplace")
    val payload = mapOf(
      "appName" to settings.appName,
      "serviceCity" to settings.serviceCity,
      "currency" to settings.currency,
      "currencySymbol" to settings.currencySymbol,
      "timezone" to settings.timezone,
      "platformFeePercent" to settings.platformFeePercent,
      "minRentalDays" to settings.minRentalDays,
      "maxRentalDays" to settings.maxRentalDays,
      "allowOutstation" to settings.allowOutstation,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload, SetOptions.merge()).await()
  }

  // Service Areas (Jamshedpur pickup zones)
  fun observeServiceAreas(): Flow<List<ServiceArea>> = callbackFlow {
    val coll = db.collection("service_areas")
    val listener = coll.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(ServiceArea::class.java) } ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }

  suspend fun addServiceArea(name: String, description: String): Result<Unit> = runCatching {
    val docRef = db.collection("service_areas").document()
    val payload = mapOf(
      "name" to name.trim(),
      "description" to description.trim(),
      "isActive" to true,
      "createdAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
  }

  // User Profile
  fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
    if (userId.isBlank()) {
      trySend(null)
      close()
      return@callbackFlow
    }
    val docRef = db.collection("users").document(userId)
    val listener = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        return@addSnapshotListener
      }
      trySend(snapshot?.toObject(UserProfile::class.java))
    }
    awaitClose { listener.remove() }
  }

  suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
    val docRef = db.collection("users").document(profile.userId)
    val payload = mutableMapOf<String, Any?>(
      "userId" to profile.userId,
      "displayName" to profile.displayName,
      "email" to profile.email,
      "phone" to profile.phone,
      "isRenter" to profile.isRenter,
      "isOwner" to profile.isOwner,
      "isAdmin" to profile.isAdmin,
      "renterVerified" to profile.renterVerified,
      "ownerVerified" to profile.ownerVerified,
      "licenseNumber" to profile.licenseNumber,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload, SetOptions.merge()).await()
  }

  // Admin bootstrap helper (registers first founder as admin if needed)
  suspend fun bootstrapAdmin(userId: String): Result<Unit> = runCatching {
    val docRef = db.collection("admins").document(userId)
    docRef.set(mapOf("userId" to userId, "createdAt" to FieldValue.serverTimestamp())).await()
    val userRef = db.collection("users").document(userId)
    userRef.set(mapOf("isAdmin" to true), SetOptions.merge()).await()
  }

  // Vehicles
  fun observeApprovedVehicles(): Flow<List<Vehicle>> = callbackFlow {
    val coll = db.collection("vehicles")
    val query = coll.whereEqualTo("status", "APPROVED")
    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Vehicle::class.java) } ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }

  fun observeOwnerVehicles(ownerId: String): Flow<List<Vehicle>> = callbackFlow {
    val coll = db.collection("vehicles")
    val query = coll.whereEqualTo("ownerId", ownerId)
    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Vehicle::class.java) } ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }

  fun observeAllVehicles(): Flow<List<Vehicle>> = callbackFlow {
    val coll = db.collection("vehicles")
    val listener = coll.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Vehicle::class.java) } ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }

  suspend fun addVehicle(vehicle: Vehicle): Result<String> = runCatching {
    val docRef = db.collection("vehicles").document()
    val payload = mapOf(
      "ownerId" to vehicle.ownerId,
      "ownerName" to vehicle.ownerName,
      "ownerPhone" to vehicle.ownerPhone,
      "make" to "Maruti Suzuki",
      "model" to "Ertiga",
      "variant" to vehicle.variant,
      "registrationNumber" to vehicle.registrationNumber,
      "registrationYear" to vehicle.registrationYear,
      "calculatedAge" to Vehicle.computeAge(vehicle.registrationYear),
      "ageLabel" to Vehicle.computeAgeLabel(Vehicle.computeAge(vehicle.registrationYear)),
      "seatingCapacity" to 7,
      "transmission" to vehicle.transmission,
      "fuelType" to vehicle.fuelType,
      "fuelPolicy" to vehicle.fuelPolicy,
      "dailyRate" to vehicle.dailyRate,
      "minDays" to vehicle.minDays,
      "maxDays" to vehicle.maxDays,
      "includedKmPerDay" to vehicle.includedKmPerDay,
      "extraKmCharge" to vehicle.extraKmCharge,
      "deliveryCharge" to vehicle.deliveryCharge,
      "securityDeposit" to vehicle.securityDeposit,
      "city" to "Jamshedpur",
      "state" to "Jharkhand",
      "country" to "India",
      "pickupArea" to vehicle.pickupArea,
      "conditionNotes" to vehicle.conditionNotes,
      "photoUrl" to vehicle.photoUrl,
      "status" to "PENDING_REVIEW",
      "odometer" to vehicle.odometer,
      "ownerRating" to vehicle.ownerRating,
      "completedRentalsCount" to vehicle.completedRentalsCount,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
    docRef.id
  }

  suspend fun updateVehicleByOwner(vehicle: Vehicle): Result<Unit> = runCatching {
    val docRef = db.collection("vehicles").document(vehicle.id)
    val payload = mapOf(
      "variant" to vehicle.variant,
      "dailyRate" to vehicle.dailyRate,
      "minDays" to vehicle.minDays,
      "maxDays" to vehicle.maxDays,
      "includedKmPerDay" to vehicle.includedKmPerDay,
      "extraKmCharge" to vehicle.extraKmCharge,
      "deliveryCharge" to vehicle.deliveryCharge,
      "securityDeposit" to vehicle.securityDeposit,
      "pickupArea" to vehicle.pickupArea,
      "conditionNotes" to vehicle.conditionNotes,
      "photoUrl" to vehicle.photoUrl,
      "status" to vehicle.status,
      "odometer" to vehicle.odometer,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload, SetOptions.merge()).await()
  }

  suspend fun reviewVehicleByAdmin(vehicleId: String, status: String, rejectionReason: String = ""): Result<Unit> = runCatching {
    val docRef = db.collection("vehicles").document(vehicleId)
    docRef.set(
      mapOf(
        "status" to status,
        "rejectionReason" to rejectionReason,
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  // Atomic Booking Request with Overlap & Hold Verification
  suspend fun requestBookingAtomic(
    vehicle: Vehicle,
    renterId: String,
    renterName: String,
    renterPhone: String,
    startEpochMs: Long,
    endEpochMs: Long,
    platformFeePercent: Double
  ): Result<String> = runCatching {
    require(endEpochMs > startEpochMs) { "Return time must be after pickup time." }
    val oneDayMs = 24L * 60L * 60L * 1000L
    val durationMs = endEpochMs - startEpochMs
    val daysCount = ((durationMs + oneDayMs - 1) / oneDayMs).toInt().coerceAtLeast(1)

    require(daysCount >= vehicle.minDays) { "Minimum booking duration is ${vehicle.minDays} day(s)." }
    require(daysCount <= vehicle.maxDays) { "Maximum booking duration is ${vehicle.maxDays} day(s)." }

    // Calculate price breakdown from owner's rate
    val baseTotal = vehicle.dailyRate * daysCount
    val platformFee = (baseTotal * platformFeePercent) / 100.0
    val deliveryFee = vehicle.deliveryCharge
    val deposit = vehicle.securityDeposit
    val totalAmount = baseTotal + platformFee + deliveryFee + deposit

    // Check availability in Firestore transaction
    val newBookingRef = db.collection("bookings").document()
    val holdExpiresAtEpochMs = System.currentTimeMillis() + (6 * 60 * 60 * 1000L) // 6-hour hold

    db.runTransaction { tx ->
      val vehicleSnap = tx.get(db.collection("vehicles").document(vehicle.id))
      val currentStatus = vehicleSnap.getString("status")
      if (currentStatus != "APPROVED") {
        throw IllegalStateException("This Ertiga is currently not available for booking.")
      }

      val payload = mapOf(
        "vehicleId" to vehicle.id,
        "vehicleTitle" to "Maruti Suzuki Ertiga ${vehicle.variant}",
        "vehicleVariant" to vehicle.variant,
        "renterId" to renterId,
        "renterName" to renterName,
        "renterPhone" to renterPhone,
        "ownerId" to vehicle.ownerId,
        "pickupArea" to vehicle.pickupArea,
        "startEpochMs" to startEpochMs,
        "endEpochMs" to endEpochMs,
        "daysCount" to daysCount,
        "status" to "REQUESTED",
        "holdExpiresAtEpochMs" to holdExpiresAtEpochMs,
        "dailyRate" to vehicle.dailyRate,
        "baseTotal" to baseTotal,
        "platformFee" to platformFee,
        "deliveryFee" to deliveryFee,
        "deposit" to deposit,
        "totalAmount" to totalAmount,
        "paymentStatus" to "UNPAID",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      tx.set(newBookingRef, payload)
    }.await()

    newBookingRef.id
  }

  // Bookings Observation
  fun observeRenterBookings(renterId: String): Flow<List<Booking>> = callbackFlow {
    val coll = db.collection("bookings")
    val query = coll.whereEqualTo("renterId", renterId)
    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Booking::class.java) } ?: emptyList()
      trySend(list.sortedByDescending { it.startEpochMs })
    }
    awaitClose { listener.remove() }
  }

  fun observeOwnerBookings(ownerId: String): Flow<List<Booking>> = callbackFlow {
    val coll = db.collection("bookings")
    val query = coll.whereEqualTo("ownerId", ownerId)
    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Booking::class.java) } ?: emptyList()
      trySend(list.sortedByDescending { it.startEpochMs })
    }
    awaitClose { listener.remove() }
  }

  fun observeAllBookings(): Flow<List<Booking>> = callbackFlow {
    val coll = db.collection("bookings")
    val listener = coll.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Booking::class.java) } ?: emptyList()
      trySend(list.sortedByDescending { it.startEpochMs })
    }
    awaitClose { listener.remove() }
  }

  // Booking Actions
  suspend fun acceptBooking(bookingId: String): Result<Unit> = runCatching {
    db.collection("bookings").document(bookingId).set(
      mapOf(
        "status" to "AWAITING_PAYMENT",
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  suspend fun declineBooking(bookingId: String, reason: String): Result<Unit> = runCatching {
    db.collection("bookings").document(bookingId).set(
      mapOf(
        "status" to "DECLINED",
        "declineReason" to reason.trim(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit> = runCatching {
    db.collection("bookings").document(bookingId).set(
      mapOf(
        "status" to "CANCELLED",
        "cancellationReason" to reason.trim(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  suspend fun submitPaymentRef(bookingId: String, mode: String, ref: String): Result<Unit> = runCatching {
    db.collection("bookings").document(bookingId).set(
      mapOf(
        "paymentMode" to mode,
        "paymentRef" to ref.trim(),
        "paymentStatus" to "PAYMENT_UNVERIFIED",
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  suspend fun confirmPayment(bookingId: String): Result<Unit> = runCatching {
    val otp = String.format("%04d", Random.nextInt(1000, 9999))
    db.collection("bookings").document(bookingId).set(
      mapOf(
        "status" to "CONFIRMED",
        "paymentStatus" to "PAID",
        "handoverOtp" to otp,
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  suspend fun completePickup(
    bookingId: String,
    odometer: Int,
    fuelPercent: Int,
    notes: String
  ): Result<Unit> = runCatching {
    db.collection("bookings").document(bookingId).set(
      mapOf(
        "status" to "IN_PROGRESS",
        "pickupOdometer" to odometer,
        "pickupFuelPercent" to fuelPercent,
        "pickupNotes" to notes.trim(),
        "pickupCompletedAtEpochMs" to System.currentTimeMillis(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()
  }

  suspend fun completeReturn(
    booking: Booking,
    odometer: Int,
    fuelPercent: Int,
    notes: String
  ): Result<Unit> = runCatching {
    // Complete booking
    db.collection("bookings").document(booking.id).set(
      mapOf(
        "status" to "COMPLETED",
        "returnOdometer" to odometer,
        "returnFuelPercent" to fuelPercent,
        "returnNotes" to notes.trim(),
        "returnCompletedAtEpochMs" to System.currentTimeMillis(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      SetOptions.merge()
    ).await()

    // Record owner payout ledger
    val netEarnings = booking.baseTotal + booking.deliveryFee
    val platformFee = booking.platformFee
    val payoutDoc = db.collection("payouts").document()
    payoutDoc.set(
      mapOf(
        "ownerId" to booking.ownerId,
        "bookingId" to booking.id,
        "vehicleTitle" to booking.vehicleTitle,
        "grossRental" to (booking.baseTotal + booking.deliveryFee),
        "platformCommission" to platformFee,
        "netEarnings" to netEarnings,
        "status" to "RECORDED",
        "payoutDateEpochMs" to 0L,
        "notes" to "Generated upon rental completion for booking ${booking.id.take(8)}",
        "createdAt" to FieldValue.serverTimestamp()
      )
    ).await()
  }

  // Payouts Observation
  fun observeOwnerPayouts(ownerId: String): Flow<List<Payout>> = callbackFlow {
    val coll = db.collection("payouts")
    val query = coll.whereEqualTo("ownerId", ownerId)
    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Payout::class.java) } ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }

  fun observeAllPayouts(): Flow<List<Payout>> = callbackFlow {
    val coll = db.collection("payouts")
    val listener = coll.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, coll.path)
        return@addSnapshotListener
      }
      val list = snapshot?.documents?.mapNotNull { it.toObject(Payout::class.java) } ?: emptyList()
      trySend(list)
    }
    awaitClose { listener.remove() }
  }
}
