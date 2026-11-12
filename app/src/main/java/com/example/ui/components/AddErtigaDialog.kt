package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ServiceArea
import com.example.data.model.Vehicle
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddErtigaDialog(
  serviceAreas: List<ServiceArea>,
  currentUserId: String,
  currentUserName: String,
  currentUserPhone: String,
  onDismiss: () -> Unit,
  onSubmit: (Vehicle) -> Unit
) {
  var variant by remember { mutableStateOf("ZXi Smart Hybrid") }
  var registrationYear by remember { mutableStateOf("2022") }
  var transmission by remember { mutableStateOf("Manual") }
  var fuelType by remember { mutableStateOf("Petrol") }
  var odometer by remember { mutableStateOf("25000") }
  var pickupArea by remember { mutableStateOf(serviceAreas.firstOrNull()?.name ?: "Bistupur") }
  var dailyRate by remember { mutableStateOf("2500") }
  var minDays by remember { mutableStateOf("1") }
  var maxDays by remember { mutableStateOf("14") }
  var includedKm by remember { mutableStateOf("200") }
  var extraKmCharge by remember { mutableStateOf("12") }
  var deliveryCharge by remember { mutableStateOf("0") }
  var securityDeposit by remember { mutableStateOf("2000") }
  var conditionNotes by remember { mutableStateOf("Well-maintained Ertiga, clean interior, dual AC, Bluetooth audio.") }

  var areaDropdownExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("add_ertiga_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("List an Ertiga", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = "As vehicle owner, enter your Ertiga specs and your own rental price. Jamshedpur admin reviews vehicle condition and registration before public listing.",
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )

        OutlinedTextField(
          value = "Maruti Suzuki Ertiga",
          onValueChange = {},
          readOnly = true,
          label = { Text("Make & Model (Fixed for launch)") },
          modifier = Modifier.fillMaxWidth(),
          enabled = false
        )

        OutlinedTextField(
          value = variant,
          onValueChange = { variant = it },
          label = { Text("Variant (e.g. ZXi, VXi, ZXi+ Smart Hybrid)") },
          modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = registrationYear,
            onValueChange = { registrationYear = it },
            label = { Text("Reg Year") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = transmission,
            onValueChange = { transmission = it },
            label = { Text("Transmission") },
            modifier = Modifier.weight(1f)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = fuelType,
            onValueChange = { fuelType = it },
            label = { Text("Fuel Type") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = odometer,
            onValueChange = { odometer = it },
            label = { Text("Odometer (km)") },
            modifier = Modifier.weight(1f)
          )
        }

        // Pickup area dropdown
        ExposedDropdownMenuBox(
          expanded = areaDropdownExpanded,
          onExpandedChange = { areaDropdownExpanded = !areaDropdownExpanded },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = pickupArea,
            onValueChange = {},
            readOnly = true,
            label = { Text("General Pickup Area in Jamshedpur") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaDropdownExpanded) },
            modifier = Modifier
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = areaDropdownExpanded,
            onDismissRequest = { areaDropdownExpanded = false }
          ) {
            val list = if (serviceAreas.isEmpty()) listOf("Bistupur", "Sakchi", "Kadma", "Sonari", "Telco") else serviceAreas.map { it.name }
            list.forEach { areaName ->
              DropdownMenuItem(
                text = { Text(areaName) },
                onClick = {
                  pickupArea = areaName
                  areaDropdownExpanded = false
                }
              )
            }
          }
        }

        // OWNER-SET PRICING SECTION
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Owner-Set Rental Rates", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            OutlinedTextField(
              value = dailyRate,
              onValueChange = { dailyRate = it },
              label = { Text("Daily Rental Price (₹ INR / day)") },
              modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              OutlinedTextField(
                value = includedKm,
                onValueChange = { includedKm = it },
                label = { Text("Included km/day") },
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = extraKmCharge,
                onValueChange = { extraKmCharge = it },
                label = { Text("Extra ₹/km") },
                modifier = Modifier.weight(1f)
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              OutlinedTextField(
                value = deliveryCharge,
                onValueChange = { deliveryCharge = it },
                label = { Text("Delivery Charge (₹)") },
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = securityDeposit,
                onValueChange = { securityDeposit = it },
                label = { Text("Refundable Deposit (₹)") },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        OutlinedTextField(
          value = conditionNotes,
          onValueChange = { conditionNotes = it },
          label = { Text("Condition & Feature Notes") },
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            val yearInt = registrationYear.toIntOrNull() ?: 2022
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val newVehicle = Vehicle(
              ownerId = currentUserId,
              ownerName = currentUserName,
              ownerPhone = currentUserPhone,
              make = "Maruti Suzuki",
              model = "Ertiga",
              variant = variant.trim(),
              registrationYear = yearInt,
              calculatedAge = (currentYear - yearInt).coerceAtLeast(0),
              seatingCapacity = 7,
              transmission = transmission.trim(),
              fuelType = fuelType.trim(),
              odometer = odometer.toIntOrNull() ?: 25000,
              pickupArea = pickupArea,
              dailyRate = dailyRate.toDoubleOrNull() ?: 2500.0,
              minDays = minDays.toIntOrNull() ?: 1,
              maxDays = maxDays.toIntOrNull() ?: 14,
              includedKmPerDay = includedKm.toIntOrNull() ?: 200,
              extraKmCharge = extraKmCharge.toDoubleOrNull() ?: 12.0,
              deliveryCharge = deliveryCharge.toDoubleOrNull() ?: 0.0,
              securityDeposit = securityDeposit.toDoubleOrNull() ?: 2000.0,
              conditionNotes = conditionNotes.trim(),
              status = "PENDING_REVIEW"
            )
            onSubmit(newVehicle)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
        ) {
          Text("Submit Ertiga for Admin Review", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
