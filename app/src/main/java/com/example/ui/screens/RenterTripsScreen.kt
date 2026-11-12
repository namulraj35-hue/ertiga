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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun RenterTripsScreen(
  viewModel: MarketplaceViewModel,
  onOpenPaymentDialog: (Booking) -> Unit,
  onOpenPickupDialog: (Booking) -> Unit
) {
  val renterBookings by viewModel.renterBookings.collectAsState()
  val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "My Trips",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = NavyPrimary,
      modifier = Modifier.testTag("heading_my_trips")
    )
    Text(
      text = "Track your self-drive bookings, verification, handover OTP, and trip progress.",
      fontSize = 13.sp,
      color = TextMuted
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (renterBookings.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Luggage,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "No trips requested yet",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Explore approved Ertigas in Jamshedpur and submit your first booking request.",
              fontSize = 13.sp,
              color = TextMuted,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(renterBookings, key = { it.id }) { booking ->
          RenterBookingCard(
            booking = booking,
            dateFormatter = dateFormatter,
            onPayClick = { onOpenPaymentDialog(booking) },
            onPickupClick = { onOpenPickupDialog(booking) },
            onCancelClick = {
              viewModel.cancelBooking(booking.id, "Cancelled by renter before trip")
            }
          )
        }
        item {
          Spacer(modifier = Modifier.height(80.dp))
        }
      }
    }
  }
}

@Composable
private fun RenterBookingCard(
  booking: Booking,
  dateFormatter: SimpleDateFormat,
  onPayClick: () -> Unit,
  onPickupClick: () -> Unit,
  onCancelClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("renter_booking_${booking.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top status row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = booking.vehicleTitle,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        BookingStatusBadge(status = booking.status)
      }

      // Pickup Area & Dates
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${booking.pickupArea}, Jamshedpur",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = NavyPrimary
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ChipBackground)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Pickup", fontSize = 11.sp, color = TextMuted)
          Text(dateFormatter.format(Date(booking.startEpochMs)), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("Return", fontSize = 11.sp, color = TextMuted)
          Text(dateFormatter.format(Date(booking.endEpochMs)), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
        }
      }

      // Price Snapshot breakdown
      HorizontalDivider(color = SurfaceBorder)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${booking.daysCount} Day(s) • Total: ₹${booking.totalAmount.toInt()}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Text(
          text = "Payment: ${booking.paymentStatus.replace('_', ' ')}",
          fontSize = 12.sp,
          color = if (booking.paymentStatus == "PAID") EmeraldDark else AmberAccent,
          fontWeight = FontWeight.SemiBold
        )
      }

      // Secret Handover OTP (Only shown once confirmed!)
      if (booking.status in listOf("CONFIRMED", "IN_PROGRESS") && booking.handoverOtp.isNotBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldLight)
            .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Secret Handover OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                Text("Show to owner at vehicle pickup", fontSize = 11.sp, color = EmeraldDark.copy(alpha = 0.8f))
              }
            }
            Text(
              text = booking.handoverOtp,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldDark,
              letterSpacing = 2.sp
            )
          }
        }
      }

      // Next action buttons based on status
      when (booking.status) {
        "AWAITING_PAYMENT" -> {
          Button(
            onClick = onPayClick,
            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("pay_confirm_btn_${booking.id}")
          ) {
            Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Payment (Cash / UPI)", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
        "CONFIRMED" -> {
          Button(
            onClick = onPickupClick,
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("pickup_checklist_btn_${booking.id}")
          ) {
            Text("Complete Pickup Checklist", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
        "REQUESTED" -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Temporary hold active pending owner acceptance",
              fontSize = 11.sp,
              color = TextMuted,
              modifier = Modifier.weight(1f)
            )
            OutlinedButton(
              onClick = onCancelClick,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Cancel", fontSize = 12.sp, color = CrimsonRed)
            }
          }
        }
      }
    }
  }
}

@Composable
fun BookingStatusBadge(status: String) {
  val (bgColor, textColor, label) = when (status) {
    "REQUESTED" -> Triple(AmberContainer, AmberAccent, "Waiting for owner")
    "AWAITING_PAYMENT" -> Triple(AmberContainer, AmberAccent, "Payment needed")
    "CONFIRMED" -> Triple(EmeraldLight, EmeraldDark, "Booking confirmed")
    "IN_PROGRESS" -> Triple(ChipBackground, NavyPrimary, "Trip in progress")
    "COMPLETED" -> Triple(EmeraldLight, EmeraldDark, "Rental completed")
    "CANCELLED" -> Triple(CrimsonLight, CrimsonRed, "Cancelled")
    "DECLINED" -> Triple(CrimsonLight, CrimsonRed, "Declined")
    "EXPIRED" -> Triple(ChipBackground, TextMuted, "Expired")
    else -> Triple(ChipBackground, TextMuted, status)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}
