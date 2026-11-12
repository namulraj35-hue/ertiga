package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.Booking
import com.example.data.model.Vehicle
import com.example.data.repository.MarketplaceRepository
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * UI State for Ertiga Vehicles exploration in Jamshedpur, Jharkhand.
 */
sealed interface ErtigaVehiclesUiState {
  data object Loading : ErtigaVehiclesUiState
  data class Success(
    val allApprovedCars: List<Vehicle>,
    val filteredAvailableCars: List<Vehicle>,
    val selectedArea: String,
    val availableJamshedpurZones: List<String>,
    val pickupEpochMs: Long,
    val returnEpochMs: Long,
    val selectedTransmission: String?,
    val selectedFuelType: String?,
    val isSortedByPriceAsc: Boolean
  ) : ErtigaVehiclesUiState
  data class Empty(
    val reasonMessage: String,
    val selectedArea: String,
    val pickupEpochMs: Long,
    val returnEpochMs: Long
  ) : ErtigaVehiclesUiState
  data class Error(
    val errorMessage: String,
    val canRetry: Boolean = true
  ) : ErtigaVehiclesUiState
}

/**
 * Dedicated Jetpack Compose ViewModel to fetch, filter, and observe available
 * Maruti Suzuki Ertiga self-drive rental vehicles in Jamshedpur, Jharkhand.
 */
class ErtigaVehiclesViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = MarketplaceRepository(application.applicationContext)
  private val firestore = FirebaseFirestore.getInstance(
    application.getString(R.string.firestore_database_id)
  )

  // Search parameters for Jamshedpur
  private val _selectedArea = MutableStateFlow("All Jamshedpur")
  val selectedArea: StateFlow<String> = _selectedArea.asStateFlow()

  private val initialPickupCalendar = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 10)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  private val _pickupEpochMs = MutableStateFlow(initialPickupCalendar.timeInMillis)
  val pickupEpochMs: StateFlow<Long> = _pickupEpochMs.asStateFlow()

  private val initialReturnCalendar = (initialPickupCalendar.clone() as Calendar).apply {
    add(Calendar.DAY_OF_YEAR, 3)
  }
  private val _returnEpochMs = MutableStateFlow(initialReturnCalendar.timeInMillis)
  val returnEpochMs: StateFlow<Long> = _returnEpochMs.asStateFlow()

  private val _selectedTransmission = MutableStateFlow<String?>(null) // "Manual", "Automatic"
  val selectedTransmission: StateFlow<String?> = _selectedTransmission.asStateFlow()

  private val _selectedFuelType = MutableStateFlow<String?>(null) // "Petrol", "CNG", "Diesel"
  val selectedFuelType: StateFlow<String?> = _selectedFuelType.asStateFlow()

  private val _isSortedByPriceAsc = MutableStateFlow(true)
  val isSortedByPriceAsc: StateFlow<Boolean> = _isSortedByPriceAsc.asStateFlow()

  // Real-time flows from Firestore repository
  private val approvedVehiclesFlow = repository.observeApprovedVehicles()
  private val serviceAreasFlow = repository.observeServiceAreas()
  private val allBookingsFlow = repository.observeAllBookings()

  /**
   * Combined reactive UI State reflecting live availability, area filters,
   * date range overlap checks, and sorting.
   */
  val uiState: StateFlow<ErtigaVehiclesUiState> = combine(
    approvedVehiclesFlow,
    serviceAreasFlow,
    allBookingsFlow,
    _selectedArea,
    _pickupEpochMs,
    _returnEpochMs,
    _selectedTransmission,
    _selectedFuelType,
    _isSortedByPriceAsc
  ) { args ->
    @Suppress("UNCHECKED_CAST")
    val vehicles = args[0] as List<Vehicle>
    @Suppress("UNCHECKED_CAST")
    val areas = (args[1] as List<com.example.data.model.ServiceArea>).filter { it.isActive }.map { it.name }
    @Suppress("UNCHECKED_CAST")
    val bookings = args[2] as List<Booking>
    val area = args[3] as String
    val start = args[4] as Long
    val end = args[5] as Long
    val transmission = args[6] as String?
    val fuelType = args[7] as String?
    val priceAsc = args[8] as Boolean

    // Available Jamshedpur zones (with "All Jamshedpur" at head)
    val defaultZones = listOf("Bistupur", "Sakchi", "Sonari", "Kadma", "Telco", "Golmuri", "Mango", "Adityapur")
    val combinedZones = (listOf("All Jamshedpur") + (if (areas.isNotEmpty()) areas else defaultZones)).distinct()

    // Filter by city, status, zone, transmission, fuel, and booking holds
    val filtered = vehicles.filter { vehicle ->
      // 1. Must be an approved Ertiga
      if (vehicle.status != "APPROVED") return@filter false

      // 2. City must be Jamshedpur
      if (!vehicle.city.equals("Jamshedpur", ignoreCase = true)) return@filter false

      // 3. Pickup area filter
      if (area != "All Jamshedpur" && !vehicle.pickupArea.equals(area, ignoreCase = true)) {
        return@filter false
      }

      // 4. Transmission filter (optional)
      if (transmission != null && !vehicle.transmission.equals(transmission, ignoreCase = true)) {
        return@filter false
      }

      // 5. Fuel type filter (optional)
      if (fuelType != null && !vehicle.fuelType.equals(fuelType, ignoreCase = true)) {
        return@filter false
      }

      // 6. Overlap availability check: No active booking on this car during [start, end]
      val hasOverlap = bookings.any { b ->
        b.vehicleId == vehicle.id &&
          b.status in listOf("REQUESTED", "AWAITING_PAYMENT", "CONFIRMED", "IN_PROGRESS") &&
          b.startEpochMs < end && b.endEpochMs > start
      }
      !hasOverlap
    }.let { list ->
      if (priceAsc) list.sortedBy { it.dailyRate } else list.sortedByDescending { it.dailyRate }
    }

    if (filtered.isEmpty()) {
      ErtigaVehiclesUiState.Empty(
        reasonMessage = if (vehicles.isEmpty()) {
          "No approved Ertigas are currently listed in Jamshedpur. Owners can list their vehicles from the Owner Hub."
        } else {
          "No Ertigas match your selected dates or pickup area in Jamshedpur. Try selecting different dates or areas."
        },
        selectedArea = area,
        pickupEpochMs = start,
        returnEpochMs = end
      )
    } else {
      ErtigaVehiclesUiState.Success(
        allApprovedCars = vehicles,
        filteredAvailableCars = filtered,
        selectedArea = area,
        availableJamshedpurZones = combinedZones,
        pickupEpochMs = start,
        returnEpochMs = end,
        selectedTransmission = transmission,
        selectedFuelType = fuelType,
        isSortedByPriceAsc = priceAsc
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ErtigaVehiclesUiState.Loading
  )

  fun selectPickupArea(area: String) {
    _selectedArea.value = area
  }

  fun setDateRange(pickupMs: Long, returnMs: Long) {
    require(returnMs > pickupMs) { "Return date must be after pickup date" }
    _pickupEpochMs.value = pickupMs
    _returnEpochMs.value = returnMs
  }

  fun setTransmissionFilter(transmission: String?) {
    _selectedTransmission.value = transmission
  }

  fun setFuelTypeFilter(fuelType: String?) {
    _selectedFuelType.value = fuelType
  }

  fun togglePriceSort() {
    _isSortedByPriceAsc.value = !_isSortedByPriceAsc.value
  }

  fun clearFilters() {
    _selectedArea.value = "All Jamshedpur"
    _selectedTransmission.value = null
    _selectedFuelType.value = null
    _isSortedByPriceAsc.value = true
  }
}
