package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import com.example.data.model.Booking
import com.example.data.model.Vehicle
import com.example.ui.MarketplaceViewModel
import com.example.ui.auth.SignInScreen
import com.example.ui.auth.signOutGoogle
import com.example.ui.components.AddErtigaDialog
import com.example.ui.components.AddServiceAreaDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ChecklistType
import com.example.ui.components.HandoverChecklistDialog
import com.example.ui.components.NavTab
import com.example.ui.components.PaymentDialog
import com.example.ui.components.TopBrandBar
import com.example.ui.components.VehicleDetailSheet
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.OwnerHubScreen
import com.example.ui.screens.RenterTripsScreen
import com.example.ui.theme.BookMyErtigaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  private val viewModel: MarketplaceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BookMyErtigaTheme {
        MainRoot(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoot(viewModel: MarketplaceViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
  var showSignInModal by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
      if (auth.currentUser != null) {
        viewModel.loadUserData(auth.currentUser!!.uid)
      }
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose { Firebase.auth.removeAuthStateListener(listener) }
  }

  var isGuestBrowsing by remember { mutableStateOf(false) }

  if (currentUser == null && !isGuestBrowsing) {
    SignInScreen(
      onAuthSuccess = {
        currentUser = Firebase.auth.currentUser
        currentUser?.uid?.let { viewModel.loadUserData(it) }
      },
      onContinueAsGuest = {
        isGuestBrowsing = true
      }
    )
  } else {
    val uid = currentUser?.uid ?: "guest"
    key(uid) {
      MarketplaceAppContent(
        viewModel = viewModel,
        isSignedIn = currentUser != null,
        onRequireSignIn = {
          isGuestBrowsing = false
        },
        onSignOut = {
          signOutGoogle(credentialManager, coroutineScope) {
            currentUser = null
            isGuestBrowsing = false
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceAppContent(
  viewModel: MarketplaceViewModel,
  isSignedIn: Boolean,
  onRequireSignIn: () -> Unit,
  onSignOut: () -> Unit
) {
  var currentTab by remember { mutableStateOf(NavTab.EXPLORE) }
  val settings by viewModel.settings.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val userMessage by viewModel.userMessage.collectAsState()
  val isProcessing by viewModel.isProcessing.collectAsState()
  val ownerBookings by viewModel.ownerBookings.collectAsState()
  val renterBookings by viewModel.renterBookings.collectAsState()
  val serviceAreas by viewModel.serviceAreas.collectAsState()

  val selectedVehicle by viewModel.selectedVehicle.collectAsState()
  val pickupEpochMs by viewModel.pickupEpochMs.collectAsState()
  val returnEpochMs by viewModel.returnEpochMs.collectAsState()

  val pendingRequestsCount = if (isSignedIn) ownerBookings.count { it.status == "REQUESTED" } else 0
  val activeTripsCount = if (isSignedIn) renterBookings.count { it.status in listOf("REQUESTED", "AWAITING_PAYMENT", "CONFIRMED", "IN_PROGRESS") } else 0
  val isAdmin = isSignedIn && (userProfile?.isAdmin == true)

  // Dialog & Sheet States
  val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showAddErtigaDialog by remember { mutableStateOf(false) }
  var showAddAreaDialog by remember { mutableStateOf(false) }
  var paymentBookingTarget by remember { mutableStateOf<Booking?>(null) }
  var checklistBookingTarget by remember { mutableStateOf<Pair<Booking, ChecklistType>?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    topBar = {
      TopBrandBar(
        appName = settings.appName,
        serviceCity = settings.serviceCity,
        isSignedIn = isSignedIn,
        userDisplayName = userProfile?.displayName ?: "User",
        isAdmin = isAdmin,
        onSignInClick = onRequireSignIn,
        onSignOut = onSignOut,
        onListErtigaClick = {
          if (!isSignedIn) {
            onRequireSignIn()
          } else {
            showAddErtigaDialog = true
          }
        },
        onAdminBootstrap = {
          if (!isSignedIn) {
            onRequireSignIn()
          } else {
            viewModel.bootstrapAdmin()
          }
        }
      )
    },
    bottomBar = {
      BottomNavBar(
        currentTab = currentTab,
        onTabSelected = { tab ->
          if (!isSignedIn && tab != NavTab.EXPLORE) {
            onRequireSignIn()
          } else {
            currentTab = tab
          }
        },
        isAdmin = isAdmin,
        pendingRequestsCount = pendingRequestsCount,
        activeTripsCount = activeTripsCount
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        NavTab.EXPLORE -> {
          ExploreScreen(
            viewModel = viewModel,
            onVehicleSelected = { vehicle ->
              viewModel.selectedVehicle.value = vehicle
            },
            onNavigateToOwnerHub = {
              if (!isSignedIn) {
                onRequireSignIn()
              } else {
                currentTab = NavTab.OWNER_HUB
              }
            }
          )
        }
        NavTab.MY_TRIPS -> {
          if (!isSignedIn) {
            ExploreScreen(
              viewModel = viewModel,
              onVehicleSelected = { viewModel.selectedVehicle.value = it },
              onNavigateToOwnerHub = onRequireSignIn
            )
          } else {
            RenterTripsScreen(
              viewModel = viewModel,
              onOpenPaymentDialog = { booking -> paymentBookingTarget = booking },
              onOpenPickupDialog = { booking ->
                checklistBookingTarget = Pair(booking, ChecklistType.PICKUP)
              }
            )
          }
        }
        NavTab.OWNER_HUB -> {
          if (!isSignedIn) {
            ExploreScreen(
              viewModel = viewModel,
              onVehicleSelected = { viewModel.selectedVehicle.value = it },
              onNavigateToOwnerHub = onRequireSignIn
            )
          } else {
            OwnerHubScreen(
              viewModel = viewModel,
              onOpenAddErtigaDialog = { showAddErtigaDialog = true },
              onOpenPickupDialog = { booking ->
                checklistBookingTarget = Pair(booking, ChecklistType.PICKUP)
              },
              onOpenReturnDialog = { booking ->
                checklistBookingTarget = Pair(booking, ChecklistType.RETURN)
              }
            )
          }
        }
        NavTab.ADMIN -> {
          if (isAdmin) {
            AdminDashboardScreen(
              viewModel = viewModel,
              onOpenAddAreaDialog = { showAddAreaDialog = true }
            )
          } else {
            ExploreScreen(
              viewModel = viewModel,
              onVehicleSelected = { viewModel.selectedVehicle.value = it },
              onNavigateToOwnerHub = { currentTab = NavTab.OWNER_HUB }
            )
          }
        }
      }

      // Vehicle Detail & Booking Bottom Sheet
      if (selectedVehicle != null) {
        val v = selectedVehicle!!
        val quote = viewModel.calculateQuote(v, pickupEpochMs, returnEpochMs)
        VehicleDetailSheet(
          vehicle = v,
          quote = quote,
          pickupEpochMs = pickupEpochMs,
          returnEpochMs = returnEpochMs,
          isProcessing = isProcessing,
          sheetState = detailSheetState,
          onDismiss = { viewModel.selectedVehicle.value = null },
          onRequestBooking = {
            if (!isSignedIn) {
              viewModel.selectedVehicle.value = null
              onRequireSignIn()
            } else {
              viewModel.requestBooking(v) {
                viewModel.selectedVehicle.value = null
                currentTab = NavTab.MY_TRIPS
              }
            }
          }
        )
      }

      // Add Ertiga Dialog
      if (showAddErtigaDialog) {
        AddErtigaDialog(
          serviceAreas = serviceAreas,
          currentUserId = viewModel.currentUser?.uid.orEmpty(),
          currentUserName = userProfile?.displayName ?: "Owner",
          currentUserPhone = userProfile?.phone ?: "",
          onDismiss = { showAddErtigaDialog = false },
          onSubmit = { vehicle ->
            viewModel.addVehicle(vehicle) {
              showAddErtigaDialog = false
            }
          }
        )
      }

      // Add Service Area Dialog (Admin)
      if (showAddAreaDialog) {
        AddServiceAreaDialog(
          onDismiss = { showAddAreaDialog = false },
          onAddArea = { name, desc ->
            viewModel.addServiceArea(name, desc) {
              showAddAreaDialog = false
            }
          }
        )
      }

      // Payment Confirmation Dialog (Renter)
      if (paymentBookingTarget != null) {
        PaymentDialog(
          booking = paymentBookingTarget!!,
          onDismiss = { paymentBookingTarget = null },
          onSubmitPayment = { mode, ref ->
            val bId = paymentBookingTarget!!.id
            viewModel.submitPayment(bId, mode, ref) {
              paymentBookingTarget = null
            }
          }
        )
      }

      // Handover Checklist Dialog (Pickup or Return)
      if (checklistBookingTarget != null) {
        val (booking, type) = checklistBookingTarget!!
        HandoverChecklistDialog(
          booking = booking,
          type = type,
          onDismiss = { checklistBookingTarget = null },
          onConfirm = { odometer, fuel, notes ->
            if (type == ChecklistType.PICKUP) {
              viewModel.completePickup(booking.id, odometer, fuel, notes) {
                checklistBookingTarget = null
              }
            } else {
              viewModel.completeReturn(booking, odometer, fuel, notes) {
                checklistBookingTarget = null
              }
            }
          }
        )
      }
    }
  }
}
