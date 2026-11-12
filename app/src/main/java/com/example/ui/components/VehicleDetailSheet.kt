package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Vehicle
import com.example.ui.PriceQuote
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.ChipBackground
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailSheet(
  vehicle: Vehicle,
  quote: PriceQuote,
  pickupEpochMs: Long,
  returnEpochMs: Long,
  isProcessing: Boolean,
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onRequestBooking: () -> Unit
) {
  val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    modifier = Modifier.testTag("vehicle_detail_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Maruti Suzuki Ertiga",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          Text(
            text = "${vehicle.variant} • ${vehicle.registrationYear} (${vehicle.calculatedAge} years old)",
            fontSize = 14.sp,
            color = TextMuted
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      // Banner Visual
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(NavyPrimary),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "7-SEATER SMART HYBRID MPV",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.sp
          )
        }

        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(EmeraldLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("Admin Verified", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
        }
      }

      // Specs Matrix
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(ChipBackground)
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Transmission", fontSize = 11.sp, color = TextMuted)
          Text(vehicle.transmission, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Fuel Type", fontSize = 11.sp, color = TextMuted)
          Text(vehicle.fuelType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Seats", fontSize = 11.sp, color = TextMuted)
          Text("${vehicle.seatingCapacity} Adult Seats", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Odometer", fontSize = 11.sp, color = TextMuted)
          Text("${vehicle.odometer} km", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
      }

      // Rental Conditions & Limits
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Rental Policies", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          PolicyRow("General Pickup Area", "${vehicle.pickupArea}, Jamshedpur")
          PolicyRow("Included Mileage", "${vehicle.includedKmPerDay} km/day (Extra: ₹${vehicle.extraKmCharge.toInt()}/km)")
          PolicyRow("Fuel Policy", "Same-to-same (Return with equal fuel)")
          PolicyRow("Min / Max Duration", "${vehicle.minDays} day(s) minimum • ${vehicle.maxDays} day(s) max")
          if (vehicle.conditionNotes.isNotBlank()) {
            PolicyRow("Owner Notes", vehicle.conditionNotes)
          }
        }
      }

      // Itemized Price Breakdown (Transparent, Owner-set rate)
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Price Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text("${quote.daysCount} Day(s) Rental", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AmberAccent)
          }

          Text(
            text = "Pickup: ${dateFormatter.format(Date(pickupEpochMs))}  →  Return: ${dateFormatter.format(Date(returnEpochMs))}",
            fontSize = 12.sp,
            color = TextMuted
          )

          HorizontalDivider(color = SurfaceBorder)

          PriceLine(
            label = "Base Rental Charge (₹${vehicle.dailyRate.toInt()} × ${quote.daysCount} days)",
            amount = "₹${quote.baseTotal.toInt()}"
          )

          if (quote.deliveryFee > 0) {
            PriceLine(label = "Delivery Charge", amount = "₹${quote.deliveryFee.toInt()}")
          }

          if (quote.deposit > 0) {
            PriceLine(
              label = "Refundable Security Deposit (Returned post-trip)",
              amount = "₹${quote.deposit.toInt()}"
            )
          }

          PriceLine(
            label = "Platform Service Fee",
            amount = "₹${quote.platformFee.toInt()}"
          )

          HorizontalDivider(color = SurfaceBorder)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Total Payable", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text("₹${quote.totalPayable.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
          }
        }
      }

      // Privacy Notice
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ChipBackground)
          .padding(10.dp)
      ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Vehicle registration number and exact handover location are disclosed only after owner acceptance.",
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )
      }

      // Request to Book Action
      Button(
        onClick = onRequestBooking,
        enabled = !isProcessing,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("request_to_book_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
      ) {
        if (isProcessing) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
          Text("Request to book", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun PolicyRow(title: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(title, fontSize = 13.sp, color = TextMuted)
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
  }
}

@Composable
private fun PriceLine(label: String, amount: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, fontSize = 13.sp, color = TextPrimary)
    Text(amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
  }
}
