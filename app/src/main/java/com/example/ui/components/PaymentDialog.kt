package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Booking
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted

@Composable
fun PaymentDialog(
  booking: Booking,
  onDismiss: () -> Unit,
  onSubmitPayment: (mode: String, ref: String) -> Unit
) {
  var selectedMode by remember { mutableStateOf("UPI") } // "UPI" or "CASH"
  var upiRef by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("payment_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Payment Confirmation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = "Total Rental Amount: ₹${booking.totalAmount.toInt()}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = AmberAccent
        )

        Text(
          text = "While commercial payment gateways are unconfigured, select Cash on Handover or submit your UPI reference number.",
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )

        // Mode 1: UPI
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = if (selectedMode == "UPI") AmberContainer.copy(alpha = 0.3f) else Color(0xFFF8FAFC)),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedMode == "UPI") AmberAccent else SurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMode = "UPI" }
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = selectedMode == "UPI",
              onClick = { selectedMode = "UPI" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("UPI Transfer (GPay / PhonePe / Paytm)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("Transfer to owner/admin UPI and enter UTR / Ref ID", fontSize = 11.sp, color = TextMuted)
            }
          }
        }

        if (selectedMode == "UPI") {
          OutlinedTextField(
            value = upiRef,
            onValueChange = { upiRef = it },
            label = { Text("UPI Ref / UTR Number (12 digits)") },
            placeholder = { Text("e.g. 431289128392") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Mode 2: Cash on Handover
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = if (selectedMode == "CASH") AmberContainer.copy(alpha = 0.3f) else Color(0xFFF8FAFC)),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedMode == "CASH") AmberAccent else SurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMode = "CASH" }
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = selectedMode == "CASH",
              onClick = { selectedMode = "CASH" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Cash on Handover", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("Pay cash to vehicle owner during vehicle pickup in Jamshedpur", fontSize = 11.sp, color = TextMuted)
            }
          }
        }

        // Transparency note
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .padding(10.dp)
        ) {
          Text(
            text = "Submitted payments are marked unverified until checked by the admin/owner. Once verified, the booking is confirmed and your 4-digit Handover OTP is generated.",
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 15.sp
          )
        }

        Button(
          onClick = {
            val refText = if (selectedMode == "UPI") upiRef.trim().ifBlank { "UPI-PENDING" } else "CASH-ON-HANDOVER"
            onSubmitPayment(selectedMode, refText)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
        ) {
          Text("Submit Payment Record", fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}
