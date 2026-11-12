package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.MarketplaceSettings
import com.example.data.model.ServiceArea
import com.example.data.model.Vehicle
import com.example.ui.MarketplaceViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.ChipBackground
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
  viewModel: MarketplaceViewModel,
  onOpenAddAreaDialog: () -> Unit
) {
  val allVehicles by viewModel.allVehicles.collectAsState()
  val allBookings by viewModel.allBookings.collectAsState()
  val serviceAreas by viewModel.serviceAreas.collectAsState()
  val settings by viewModel.settings.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Vehicle Review", "Pickup Zones", "Payment Verification", "Marketplace Settings")
  val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

  val pendingVehicles = allVehicles.filter { it.status == "PENDING_REVIEW" }
  val unverifiedPayments = allBookings.filter { it.paymentStatus == "PAYMENT_UNVERIFIED" || it.status == "AWAITING_PAYMENT" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Admin Console",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary,
          modifier = Modifier.testTag("heading_admin")
        )
        Text(
          text = "Jamshedpur service boundaries, vehicle approvals, and manual payments.",
          fontSize = 13.sp,
          color = TextMuted
        )
      }
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(NavyPrimary)
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("SUPER ADMIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Sub tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.White,
      contentColor = NavyPrimary,
      edgePadding = 0.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = AmberAccent
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            val count = when (index) {
              0 -> pendingVehicles.size
              2 -> unverifiedPayments.size
              else -> 0
            }
            Text(
              text = if (count > 0) "$title ($count)" else title,
              fontSize = 13.sp,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    when (selectedTab) {
      0 -> { // Vehicle Review Queue
        if (allVehicles.isEmpty()) {
          AdminEmptyCard("No vehicle listings submitted yet in the marketplace.")
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(AmberContainer.copy(alpha = 0.5f))
                  .padding(10.dp)
              ) {
                Text(
                  text = "Notice: Admin inspects vehicle condition and registration eligibility. You cannot modify the owner's price.",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = NavyPrimary
                )
              }
            }

            items(allVehicles, key = { it.id }) { vehicle ->
              AdminVehicleCard(
                vehicle = vehicle,
                onApprove = { viewModel.reviewVehicle(vehicle.id, "APPROVED") },
                onReject = { reason -> viewModel.reviewVehicle(vehicle.id, "REJECTED", reason) }
              )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
          }
        }
      }
      1 -> { // Jamshedpur Pickup Zones
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Configured Jamshedpur Zones (${serviceAreas.size})",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Button(
              onClick = onOpenAddAreaDialog,
              colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Zone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (serviceAreas.isEmpty()) {
            AdminEmptyCard("No pickup zones configured yet. Tap below to automatically initialize Jamshedpur's standard zones, or add custom zones manually.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = {
                listOf(
                  "Bistupur" to "Main Commercial Hub / Regal Ground",
                  "Sakchi" to "Central Market & Kalimati Road",
                  "Sonari" to "Aerodrome & Riverside Road",
                  "Kadma" to "Rankini Mandir & Inner Ring Road",
                  "Telco" to "Plaza Area & Tata Motors Township",
                  "Golmuri" to "Clock Tower & Tinplate Circle",
                  "Bariatu / Adityapur" to "Industrial & Station Access Corridor"
                ).forEach { (name, desc) ->
                  viewModel.addServiceArea(name, desc)
                }
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberAccent)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Seed Standard Jamshedpur Zones", fontWeight = FontWeight.Bold, color = Color.White)
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(serviceAreas, key = { it.id }) { area ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = Color.White),
                  border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text(area.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        if (area.description.isNotBlank()) {
                          Text(area.description, fontSize = 12.sp, color = TextMuted)
                        }
                      }
                    }
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text("Active", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
              item { Spacer(modifier = Modifier.height(80.dp)) }
            }
          }
        }
      }
      2 -> { // Payment Verification
        if (unverifiedPayments.isEmpty()) {
          AdminEmptyCard("No pending payment verification requests.")
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(unverifiedPayments, key = { it.id }) { booking ->
              AdminPaymentCard(
                booking = booking,
                dateFormatter = dateFormatter,
                onConfirmPayment = { viewModel.confirmPayment(booking.id) }
              )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
          }
        }
      }
      3 -> { // Marketplace Settings
        MarketplaceSettingsEditor(settings = settings, onSave = { viewModel.updateSettings(it) })
      }
    }
  }
}

@Composable
private fun AdminVehicleCard(
  vehicle: Vehicle,
  onApprove: () -> Unit,
  onReject: (String) -> Unit
) {
  var rejectReason by remember { mutableStateOf("") }
  var showRejectInput by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("admin_vehicle_card_${vehicle.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Maruti Suzuki Ertiga ${vehicle.variant}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          Text(
            text = "Owner: ${vehicle.ownerName} • Reg Year: ${vehicle.registrationYear} (${vehicle.calculatedAge} yr old)",
            fontSize = 12.sp,
            color = TextMuted
          )
        }
        VehicleStatusBadge(status = vehicle.status)
      }

      // Owner Pricing Highlight
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ChipBackground)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Owner-set rate:", fontSize = 12.sp, color = TextMuted)
        Text("₹${vehicle.dailyRate.toInt()}/day", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Area: ${vehicle.pickupArea}", fontSize = 12.sp, color = TextMuted)
        Text("Odometer: ${vehicle.odometer} km", fontSize = 12.sp, color = TextMuted)
      }

      if (vehicle.conditionNotes.isNotBlank()) {
        Text("Condition notes: ${vehicle.conditionNotes}", fontSize = 12.sp, color = NavyPrimary)
      }

      // Actions
      if (vehicle.status == "PENDING_REVIEW") {
        HorizontalDivider(color = SurfaceBorder)
        if (showRejectInput) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = rejectReason,
              onValueChange = { rejectReason = it },
              label = { Text("Rejection reason / Required corrections") },
              modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { onReject(rejectReason) },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Confirm Rejection", color = Color.White)
              }
              OutlinedButton(onClick = { showRejectInput = false }) {
                Text("Cancel")
              }
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { showRejectInput = true },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Reject with Notes", color = CrimsonRed, fontSize = 13.sp)
            }
            Button(
              onClick = onApprove,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Approve Listing", color = Color.White, fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminPaymentCard(
  booking: Booking,
  dateFormatter: SimpleDateFormat,
  onConfirmPayment: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Booking: ${booking.vehicleTitle}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text("₹${booking.totalAmount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
      }

      Text(
        text = "Renter: ${booking.renterName} (${booking.renterPhone})",
        fontSize = 12.sp,
        color = TextMuted
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ChipBackground)
          .padding(8.dp)
      ) {
        Column {
          Text("Payment Mode: ${booking.paymentMode.ifBlank { "Unspecified" }}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
          if (booking.paymentRef.isNotBlank()) {
            Text("Submitted Ref / UPI ID: ${booking.paymentRef}", fontSize = 12.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
          }
        }
      }

      Button(
        onClick = onConfirmPayment,
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
      ) {
        Text("Verify & Generate Handover OTP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun MarketplaceSettingsEditor(
  settings: MarketplaceSettings,
  onSave: (MarketplaceSettings) -> Unit
) {
  var appName by remember(settings) { mutableStateOf(settings.appName) }
  var feePercent by remember(settings) { mutableStateOf(settings.platformFeePercent.toString()) }
  var minDays by remember(settings) { mutableStateOf(settings.minRentalDays.toString()) }
  var maxDays by remember(settings) { mutableStateOf(settings.maxRentalDays.toString()) }
  var allowOutstation by remember(settings) { mutableStateOf(settings.allowOutstation) }

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      Text("Central Marketplace Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
      Text("Control app name, commission percentage, and service rules.", fontSize = 13.sp, color = TextMuted)
    }

    item {
      OutlinedTextField(
        value = appName,
        onValueChange = { appName = it },
        label = { Text("App Name") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = settings.serviceCity,
        onValueChange = {},
        readOnly = true,
        label = { Text("Service City (Fixed at launch)") },
        modifier = Modifier.fillMaxWidth(),
        enabled = false
      )
    }

    item {
      OutlinedTextField(
        value = feePercent,
        onValueChange = { feePercent = it },
        label = { Text("Platform Fee Percent (%)") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = minDays,
          onValueChange = { minDays = it },
          label = { Text("Min Rental Days") },
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = maxDays,
          onValueChange = { maxDays = it },
          label = { Text("Max Rental Days") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Allow Outstation Trips", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
          Text("Allow renters to drive outside Jamshedpur after pickup", fontSize = 12.sp, color = TextMuted)
        }
        Switch(
          checked = allowOutstation,
          onCheckedChange = { allowOutstation = it }
        )
      }
    }

    item {
      Button(
        onClick = {
          onSave(
            settings.copy(
              appName = appName.trim(),
              platformFeePercent = feePercent.toDoubleOrNull() ?: 5.0,
              minRentalDays = minDays.toIntOrNull() ?: 1,
              maxRentalDays = maxDays.toIntOrNull() ?: 14,
              allowOutstation = allowOutstation
            )
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
      ) {
        Text("Save Settings", fontWeight = FontWeight.Bold, color = Color.White)
      }
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun AdminEmptyCard(message: String) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(Icons.Default.Info, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(36.dp))
      Spacer(modifier = Modifier.height(10.dp))
      Text(message, fontSize = 13.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
  }
}
