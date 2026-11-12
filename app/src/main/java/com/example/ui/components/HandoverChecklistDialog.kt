package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Booking
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextMuted

enum class ChecklistType {
  PICKUP,
  RETURN
}

@Composable
fun HandoverChecklistDialog(
  booking: Booking,
  type: ChecklistType,
  onDismiss: () -> Unit,
  onConfirm: (odometer: Int, fuelPercent: Int, notes: String) -> Unit
) {
  val initialOdo = if (type == ChecklistType.PICKUP) "25000" else (booking.pickupOdometer.takeIf { it > 0 } ?: 25200).toString()
  var odometer by remember { mutableStateOf(initialOdo) }
  var fuelLevel by remember { mutableFloatStateOf(100f) }
  var notes by remember { mutableStateOf("") }
  var enteredOtp by remember { mutableStateOf("") }
  var otpError by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("handover_checklist_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (type == ChecklistType.PICKUP) "Pickup Inspection Checklist" else "Return Inspection Checklist",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = if (type == ChecklistType.PICKUP)
            "Document the Ertiga's odometer reading, fuel level, and verify the 4-digit Handover OTP before handing over keys."
          else
            "Document final odometer and fuel level, inspect for damages, and sign off the rental return.",
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )

        // Handover OTP verification at pickup
        if (type == ChecklistType.PICKUP && booking.handoverOtp.isNotBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(EmeraldLight)
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Expected Handover OTP:", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
              }
              Text(booking.handoverOtp, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
            }
          }
        }

        OutlinedTextField(
          value = odometer,
          onValueChange = { odometer = it },
          label = { Text("Odometer Reading (km)") },
          modifier = Modifier.fillMaxWidth()
        )

        // Fuel Level Slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Fuel Tank Level", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
            Text("${fuelLevel.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
          }
          Slider(
            value = fuelLevel,
            onValueChange = { fuelLevel = it },
            valueRange = 0f..100f,
            steps = 9,
            colors = SliderDefaults.colors(
              thumbColor = AmberAccent,
              activeTrackColor = AmberAccent
            )
          )
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (type == ChecklistType.PICKUP) "Condition / Existing Scratches Notes" else "Return Condition / Cleanliness Notes") },
          placeholder = { Text("e.g. Minor scratch on rear bumper, clean interior") },
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            val odoInt = odometer.toIntOrNull() ?: 25000
            onConfirm(odoInt, fuelLevel.toInt(), notes.trim())
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (type == ChecklistType.PICKUP) NavyPrimary else EmeraldGreen
          )
        ) {
          Text(
            text = if (type == ChecklistType.PICKUP) "Confirm Handover & Start Rental" else "Complete Return & Sign Off",
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
