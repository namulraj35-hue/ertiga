package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Booking
import com.example.data.model.MarketplaceSettings
import com.example.data.model.Payout
import com.example.data.model.ServiceArea
import com.example.data.model.UserProfile
import com.example.data.model.Vehicle
import com.example.data.repository.MarketplaceRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class PriceQuote(
  val daysCount: Int,
  val baseTotal: Double,
  val platformFee: Double,
  val deliveryFee: Double,
  val deposit: Double,
  val totalPayable: Double
)

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = MarketplaceRepository(application.applicationContext)
  private val auth = Firebase.auth

  val currentUser get() = auth.currentUser

  val settings: StateFlow<MarketplaceSettings> = repository.observeSettings()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MarketplaceSettings())

  val serviceAreas: StateFlow<List<ServiceArea>> = repository.observeServiceAreas()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _userProfile = MutableStateFlow<UserProfile?>(null)
  val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

  val approvedVehicles: StateFlow<List<Vehicle>> = repository.observeApprovedVehicles()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _ownerVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
  val ownerVehicles: StateFlow<List<Vehicle>> = _ownerVehicles.asStateFlow()

  val allVehicles: StateFlow<List<Vehicle>> = repository.observeAllVehicles()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _renterBookings = MutableStateFlow<List<Booking>>(emptyList())
  val renterBookings: StateFlow<List<Booking>> = _renterBookings.asStateFlow()

  private val _ownerBookings = MutableStateFlow<List<Booking>>(emptyList())
  val ownerBookings: StateFlow<List<Booking>> = _ownerBookings.asStateFlow()

  val allBookings: StateFlow<List<Booking>> = repository.observeAllBookings()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _ownerPayouts = MutableStateFlow<List<Payout>>(emptyList())
  val ownerPayouts: StateFlow<List<Payout>> = _ownerPayouts.asStateFlow()

  val allPayouts: StateFlow<List<Payout>> = repository.observeAllPayouts()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Search parameters
  val selectedArea = MutableStateFlow("All Jamshedpur")

  private val calendar = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 10)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  val pickupEpochMs = MutableStateFlow(calendar.timeInMillis)

  private val returnCalendar = (calendar.clone() as Calendar).apply {
    add(Calendar.DAY_OF_YEAR, 3)
  }
  val returnEpochMs = MutableStateFlow(returnCalendar.timeInMillis)

  // Filtered available vehicles (checking zone and overlapping active bookings)
  val availableVehicles: StateFlow<List<Vehicle>> = combine(
    approvedVehicles,
    allBookings,
    selectedArea,
    pickupEpochMs,
    returnEpochMs
  ) { vehicles, bookings, area, start, end ->
    vehicles.filter { vehicle ->
      // Area match
      val areaMatches = area == "All Jamshedpur" || vehicle.pickupArea.equals(area, ignoreCase = true)
      if (!areaMatches) return@filter false

      // Overlap match: check if any active booking conflicts with requested [start, end]
      val hasOverlap = bookings.any { b ->
        b.vehicleId == vehicle.id &&
          b.status in listOf("REQUESTED", "AWAITING_PAYMENT", "CONFIRMED", "IN_PROGRESS") &&
          b.startEpochMs < end && b.endEpochMs > start
      }
      !hasOverlap
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected vehicle for details
  val selectedVehicle = MutableStateFlow<Vehicle?>(null)

  // Status and feedback messaging
  val userMessage = MutableStateFlow<String?>(null)
  val isProcessing = MutableStateFlow(false)

  init {
    val uid = auth.currentUser?.uid
    if (uid != null) {
      loadUserData(uid)
    }
  }

  fun loadUserData(uid: String) {
    viewModelScope.launch {
      repository.observeUserProfile(uid).collect { profile ->
        if (profile == null) {
          // Initialize user profile
          val email = auth.currentUser?.email.orEmpty()
          val name = auth.currentUser?.displayName.orEmpty().ifBlank { "User" }
          val isFounderAdmin = email.equals("namulraj35@gmail.com", ignoreCase = true)
          val newProfile = UserProfile(
            userId = uid,
            displayName = name,
            email = email,
            isAdmin = isFounderAdmin,
            isRenter = true,
            isOwner = isFounderAdmin // founder owns the first Ertiga
          )
          repository.saveUserProfile(newProfile)
          _userProfile.value = newProfile
        } else {
          _userProfile.value = profile
        }
      }
    }

    viewModelScope.launch {
      repository.observeOwnerVehicles(uid).collect { _ownerVehicles.value = it }
    }
    viewModelScope.launch {
      repository.observeRenterBookings(uid).collect { _renterBookings.value = it }
    }
    viewModelScope.launch {
      repository.observeOwnerBookings(uid).collect { _ownerBookings.value = it }
    }
    viewModelScope.launch {
      repository.observeOwnerPayouts(uid).collect { _ownerPayouts.value = it }
    }
  }

  fun calculateQuote(vehicle: Vehicle, startMs: Long, endMs: Long): PriceQuote {
    val oneDayMs = 24L * 60L * 60L * 1000L
    val durationMs = endMs - startMs
    val daysCount = ((durationMs + oneDayMs - 1) / oneDayMs).toInt().coerceAtLeast(1)
    val baseTotal = vehicle.dailyRate * daysCount
    val platformFee = (baseTotal * settings.value.platformFeePercent) / 100.0
    val deliveryFee = vehicle.deliveryCharge
    val deposit = vehicle.securityDeposit
    val total = baseTotal + platformFee + deliveryFee + deposit
    return PriceQuote(
      daysCount = daysCount,
      baseTotal = baseTotal,
      platformFee = platformFee,
      deliveryFee = deliveryFee,
      deposit = deposit,
      totalPayable = total
    )
  }

  fun saveProfile(profile: UserProfile, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.saveUserProfile(profile).onSuccess {
        _userProfile.value = profile
        userMessage.value = "Profile updated successfully."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Failed to update profile: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun bootstrapAdmin() {
    val uid = auth.currentUser?.uid ?: return
    viewModelScope.launch {
      repository.bootstrapAdmin(uid).onSuccess {
        userMessage.value = "Admin privileges initialized for your account."
        loadUserData(uid)
      }.onFailure { err ->
        userMessage.value = "Admin initialization error: ${err.message}"
      }
    }
  }

  fun addServiceArea(name: String, description: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.addServiceArea(name, description).onSuccess {
        userMessage.value = "Added Jamshedpur pickup area: $name"
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Failed to add service area: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun updateSettings(newSettings: MarketplaceSettings, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.updateSettings(newSettings).onSuccess {
        userMessage.value = "Marketplace settings saved."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Settings update failed: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun addVehicle(vehicle: Vehicle, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.addVehicle(vehicle).onSuccess { id ->
        userMessage.value = "Ertiga submitted for admin review."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Failed to submit vehicle: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun updateVehicle(vehicle: Vehicle, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.updateVehicleByOwner(vehicle).onSuccess {
        userMessage.value = "Vehicle details updated."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Failed to update vehicle: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun reviewVehicle(vehicleId: String, status: String, reason: String = "") {
    viewModelScope.launch {
      isProcessing.value = true
      repository.reviewVehicleByAdmin(vehicleId, status, reason).onSuccess {
        userMessage.value = "Vehicle status updated to $status."
      }.onFailure { err ->
        userMessage.value = "Review failed: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun requestBooking(vehicle: Vehicle, onComplete: () -> Unit = {}) {
    val uid = auth.currentUser?.uid ?: return
    val name = userProfile.value?.displayName ?: "Renter"
    val phone = userProfile.value?.phone ?: ""

    viewModelScope.launch {
      isProcessing.value = true
      repository.requestBookingAtomic(
        vehicle = vehicle,
        renterId = uid,
        renterName = name,
        renterPhone = phone,
        startEpochMs = pickupEpochMs.value,
        endEpochMs = returnEpochMs.value,
        platformFeePercent = settings.value.platformFeePercent
      ).onSuccess { bookingId ->
        userMessage.value = "Booking request submitted! Vehicle is held pending owner response."
        onComplete()
      }.onFailure { err ->
        userMessage.value = err.message ?: "Booking request failed"
      }
      isProcessing.value = false
    }
  }

  fun acceptBooking(bookingId: String) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.acceptBooking(bookingId).onSuccess {
        userMessage.value = "Booking accepted! Waiting for renter payment confirmation."
      }.onFailure { err ->
        userMessage.value = "Failed to accept: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun declineBooking(bookingId: String, reason: String) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.declineBooking(bookingId, reason).onSuccess {
        userMessage.value = "Booking declined. Availability hold released."
      }.onFailure { err ->
        userMessage.value = "Failed to decline: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun cancelBooking(bookingId: String, reason: String) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.cancelBooking(bookingId, reason).onSuccess {
        userMessage.value = "Booking cancelled. Availability hold released."
      }.onFailure { err ->
        userMessage.value = "Failed to cancel: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun submitPayment(bookingId: String, mode: String, ref: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.submitPaymentRef(bookingId, mode, ref).onSuccess {
        userMessage.value = "Payment details submitted. Marked unverified until confirmed by admin/owner."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Payment submission failed: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun confirmPayment(bookingId: String) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.confirmPayment(bookingId).onSuccess {
        userMessage.value = "Payment confirmed! 4-digit Handover OTP generated."
      }.onFailure { err ->
        userMessage.value = "Payment confirmation failed: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun completePickup(bookingId: String, odometer: Int, fuelPercent: Int, notes: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.completePickup(bookingId, odometer, fuelPercent, notes).onSuccess {
        userMessage.value = "Pickup checklist recorded! Rental is now In Progress."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Pickup recording failed: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun completeReturn(booking: Booking, odometer: Int, fuelPercent: Int, notes: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      isProcessing.value = true
      repository.completeReturn(booking, odometer, fuelPercent, notes).onSuccess {
        userMessage.value = "Return checklist recorded! Rental completed and payout entry created."
        onComplete()
      }.onFailure { err ->
        userMessage.value = "Return recording failed: ${err.message}"
      }
      isProcessing.value = false
    }
  }

  fun clearUserMessage() {
    userMessage.value = null
  }
}
