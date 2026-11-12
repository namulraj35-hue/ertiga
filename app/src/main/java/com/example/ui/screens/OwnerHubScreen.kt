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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Payout
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
fun OwnerHubScreen(
  viewModel: MarketplaceViewModel,
  onOpenAddErtigaDialog: () -> Unit,
  onOpenPickupDialog: (Booking) -> Unit,
  onOpenReturnDialog: (Booking) -> Unit
) {
  val ownerVehicles by viewModel.ownerVehicles.collectAsState()
  val ownerBookings by viewModel.ownerBookings.collectAsState()
  val ownerPayouts by viewModel.ownerPayouts.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Booking Requests", "My Ertigas", "Earnings Ledger")
  val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

  val pendingRequests = ownerBookings.filter { it.status == "REQUESTED" }
  val totalEarnings = ownerPayouts.sumOf { it.netEarnings }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Owner Dashboard",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary,
        modifier = Modifier.testTag("heading_owner_hub")
      )
      Text(
        text = "Manage your Ertiga vehicles, set your daily rates, and respond to renter requests.",
        fontSize = 13.sp,
        color = TextMuted
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Metric Summary Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatCard(
          title = "My Ertigas",
          value = ownerVehicles.size.toString(),
          modifier = Modifier.weight(1f)
        )
        StatCard(
          title = "Requests",
          value = pendingRequests.size.toString(),
          isHighlighted = pendingRequests.isNotEmpty(),
          modifier = Modifier.weight(1f)
        )
        StatCard(
          title = "Net Earnings",
          value = "₹${totalEarnings.toInt()}",
          modifier = Modifier.weight(1.2f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tab selector
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
              Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Tab content
      when (selectedTab) {
        0 -> { // Booking Requests & Rentals
          if (ownerBookings.isEmpty()) {
            EmptyOwnerState(
              title = "No booking requests yet",
              desc = "When renters in Jamshedpur request your approved Ertigas, they will appear here with instant accept/decline actions."
            )
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(ownerBookings, key = { it.id }) { booking ->
                OwnerBookingCard(
                  booking = booking,
                  dateFormatter = dateFormatter,
                  onAccept = { viewModel.acceptBooking(booking.id) },
                  onDecline = { viewModel.declineBooking(booking.id, "Owner unavailable for selected dates") },
                  onConfirmHandover = { onOpenPickupDialog(booking) },
                  onRecordReturn = { onOpenReturnDialog(booking) }
                )
              }
              item { Spacer(modifier = Modifier.height(80.dp)) }
            }
          }
        }
        1 -> { // My Ertigas
          if (ownerVehicles.isEmpty()) {
            EmptyOwnerState(
              title = "No Ertigas listed yet",
              desc = "Tap 'List your Ertiga' below to enter your vehicle details, custom daily rate, mileage limits, and Jamshedpur pickup area.",
              actionButton = {
                Button(
                  onClick = onOpenAddErtigaDialog,
                  colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("List your Ertiga", color = Color.White, fontWeight = FontWeight.Bold)
                }
              }
            )
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(ownerVehicles, key = { it.id }) { vehicle ->
                OwnerVehicleCard(
                  vehicle = vehicle,
                  onTogglePause = {
                    val newStatus = if (vehicle.status == "PAUSED") "APPROVED" else "PAUSED"
                    viewModel.updateVehicle(vehicle.copy(status = newStatus))
                  }
                )
              }
              item { Spacer(modifier = Modifier.height(80.dp)) }
            }
          }
        }
        2 -> { // Earnings Ledger
          if (ownerPayouts.isEmpty()) {
            EmptyOwnerState(
              title = "No earnings recorded yet",
              desc = "Once a confirmed rental return checklist is completed, your net earnings ledger will be calculated and displayed here."
            )
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(ownerPayouts, key = { it.id }) { payout ->
                PayoutCard(payout = payout, dateFormatter = dateFormatter)
              }
              item { Spacer(modifier = Modifier.height(80.dp)) }
            }
          }
        }
      }
    }

    // FAB to Add an Ertiga
    if (selectedTab == 1) {
      FloatingActionButton(
        onClick = onOpenAddErtigaDialog,
        containerColor = AmberAccent,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(bottom = 76.dp, end = 20.dp)
          .testTag("add_ertiga_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add an Ertiga")
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  isHighlighted: Boolean = false,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isHighlighted) AmberContainer else Color.White
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(title, fontSize = 11.sp, color = TextMuted)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = if (isHighlighted) AmberAccent else NavyPrimary
      )
    }
  }
}

@Composable
private fun OwnerBookingCard(
  booking: Booking,
  dateFormatter: SimpleDateFormat,
  onAccept: () -> Unit,
  onDecline: () -> Unit,
  onConfirmHandover: () -> Unit,
  onRecordReturn: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("owner_booking_card_${booking.id}")
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
        Text(
          text = booking.vehicleTitle,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        BookingStatusBadge(status = booking.status)
      }

      Text(
        text = "Renter: ${booking.renterName} • ${booking.pickupArea}, Jamshedpur",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = TextMuted
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ChipBackground)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${dateFormatter.format(Date(booking.startEpochMs))} → ${dateFormatter.format(Date(booking.endEpochMs))}",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = NavyPrimary
        )
        Text(
          text = "₹${booking.totalAmount.toInt()} Total",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = AmberAccent
        )
      }

      // Handover OTP Display
      if (booking.status in listOf("CONFIRMED", "IN_PROGRESS") && booking.handoverOtp.isNotBlank()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldLight)
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Handover OTP (match with renter):", fontSize = 12.sp, color = EmeraldDark)
          Text(booking.handoverOtp, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
        }
      }

      // Action Buttons
      when (booking.status) {
        "REQUESTED" -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onDecline,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Decline", color = CrimsonRed, fontSize = 13.sp)
            }
            Button(
              onClick = onAccept,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Accept", color = Color.White, fontSize = 13.sp)
            }
          }
        }
        "CONFIRMED" -> {
          Button(
            onClick = onConfirmHandover,
            modifier = Modifier
              .fillMaxWidth()
              .height(42.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
          ) {
            Text("Complete Handover at Pickup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
        "IN_PROGRESS" -> {
          Button(
            onClick = onRecordReturn,
            modifier = Modifier
              .fillMaxWidth()
              .height(42.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
          ) {
            Text("Complete Return & Record Ledger", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun OwnerVehicleCard(
  vehicle: Vehicle,
  onTogglePause: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("owner_vehicle_card_${vehicle.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
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
            text = "${vehicle.registrationYear} • ${vehicle.pickupArea}, Jamshedpur",
            fontSize = 12.sp,
            color = TextMuted
          )
        }
        VehicleStatusBadge(status = vehicle.status)
      }

      // Owner-Set Pricing Snapshot
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ChipBackground)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Daily Price (Owner-set):", fontSize = 12.sp, color = TextMuted)
        Text("₹${vehicle.dailyRate.toInt()}/day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Included: ${vehicle.includedKmPerDay} km/day", fontSize = 11.sp, color = TextMuted)
        Text("Deposit: ₹${vehicle.securityDeposit.toInt()}", fontSize = 11.sp, color = TextMuted)
      }

      if (vehicle.status == "REJECTED" && vehicle.rejectionReason.isNotBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CrimsonLight)
            .padding(8.dp)
        ) {
          Text(
            text = "Admin Feedback: ${vehicle.rejectionReason}",
            fontSize = 11.sp,
            color = CrimsonRed
          )
        }
      }

      // Pause / Resume control
      if (vehicle.status in listOf("APPROVED", "PAUSED")) {
        OutlinedButton(
          onClick = onTogglePause,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(
            imageVector = if (vehicle.status == "PAUSED") Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (vehicle.status == "PAUSED") "Resume Listing" else "Pause Listing (Maintenance / Personal Use)")
        }
      }
    }
  }
}

@Composable
fun VehicleStatusBadge(status: String) {
  val (bg, fg, label) = when (status) {
    "APPROVED" -> Triple(EmeraldLight, EmeraldDark, "Approved")
    "PENDING_REVIEW" -> Triple(AmberContainer, AmberAccent, "Pending Review")
    "REJECTED" -> Triple(CrimsonLight, CrimsonRed, "Rejected")
    "PAUSED" -> Triple(ChipBackground, TextMuted, "Paused")
    else -> Triple(ChipBackground, TextMuted, status)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
  }
}

@Composable
private fun PayoutCard(payout: Payout, dateFormatter: SimpleDateFormat) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(payout.vehicleTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text(
          text = "+ ₹${payout.netEarnings.toInt()}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldDark
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Gross: ₹${payout.grossRental.toInt()} • Commission: ₹${payout.platformCommission.toInt()}", fontSize = 12.sp, color = TextMuted)
        Text(payout.status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
      }
    }
  }
}

@Composable
private fun EmptyOwnerState(
  title: String,
  desc: String,
  actionButton: (@Composable () -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(48.dp))
      Spacer(modifier = Modifier.height(12.dp))
      Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, textAlign = TextAlign.Center)
      Spacer(modifier = Modifier.height(6.dp))
      Text(desc, fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 18.sp)
      if (actionButton != null) {
        Spacer(modifier = Modifier.height(16.dp))
        actionButton()
      }
    }
  }
}
