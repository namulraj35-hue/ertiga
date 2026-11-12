package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceArea
import com.example.data.model.Vehicle
import com.example.ui.MarketplaceViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.ChipBackground
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
  viewModel: MarketplaceViewModel,
  onVehicleSelected: (Vehicle) -> Unit,
  onNavigateToOwnerHub: () -> Unit
) {
  val context = LocalContext.current
  val serviceAreas by viewModel.serviceAreas.collectAsState()
  val availableVehicles by viewModel.availableVehicles.collectAsState()
  val approvedVehicles by viewModel.approvedVehicles.collectAsState()
  val selectedArea by viewModel.selectedArea.collectAsState()
  val pickupEpochMs by viewModel.pickupEpochMs.collectAsState()
  val returnEpochMs by viewModel.returnEpochMs.collectAsState()

  val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Main Heading
      Text(
        text = "Book an Ertiga in Jamshedpur",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary,
        modifier = Modifier.testTag("heading_main")
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Choose your dates, compare available Ertigas, and request your booking online.",
        fontSize = 14.sp,
        color = TextMuted,
        lineHeight = 20.sp
      )
    }

    // Search Card
    item {
      SearchCard(
        context = context,
        selectedArea = selectedArea,
        serviceAreas = serviceAreas,
        pickupEpochMs = pickupEpochMs,
        returnEpochMs = returnEpochMs,
        dateFormatter = dateFormatter,
        onAreaChanged = { viewModel.selectedArea.value = it },
        onPickupDateChanged = { viewModel.pickupEpochMs.value = it },
        onReturnDateChanged = { viewModel.returnEpochMs.value = it }
      )
    }

    // Owner Callout Banner
    item {
      OwnerCalloutCard(onListErtigaClicked = onNavigateToOwnerHub)
    }

    // Trust & Transparency Strip
    item {
      TrustStrip()
    }

    // Search Results Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Available Ertigas",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Text(
          text = "${availableVehicles.size} available",
          fontSize = 13.sp,
          color = TextMuted
        )
      }
    }

    if (availableVehicles.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No Ertigas are available for these dates. Try different dates or check again later.",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = NavyPrimary,
              textAlign = TextAlign.Center
            )
            if (approvedVehicles.isEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Marketplace starts fresh. As the owner of the first Ertiga, head to Owner Hub to add your vehicle listing.",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onNavigateToOwnerHub,
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("List your Ertiga", color = Color.White, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    } else {
      items(availableVehicles, key = { it.id }) { vehicle ->
        VehicleCard(
          vehicle = vehicle,
          onViewCar = { onVehicleSelected(vehicle) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCard(
  context: Context,
  selectedArea: String,
  serviceAreas: List<ServiceArea>,
  pickupEpochMs: Long,
  returnEpochMs: Long,
  dateFormatter: SimpleDateFormat,
  onAreaChanged: (String) -> Unit,
  onPickupDateChanged: (Long) -> Unit,
  onReturnDateChanged: (Long) -> Unit
) {
  var areaExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("search_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(
        text = "Find Your Ertiga in Jamshedpur",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = NavyPrimary
      )

      // 1. Pickup Area Selector (Dropdown)
      ExposedDropdownMenuBox(
        expanded = areaExpanded,
        onExpandedChange = { areaExpanded = !areaExpanded },
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = selectedArea,
          onValueChange = {},
          readOnly = true,
          label = { Text("Pickup area in Jamshedpur") },
          leadingIcon = {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberAccent)
          },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
          ),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("pickup_area_input")
        )
        ExposedDropdownMenu(
          expanded = areaExpanded,
          onDismissRequest = { areaExpanded = false }
        ) {
          DropdownMenuItem(
            text = { Text("All Jamshedpur") },
            onClick = {
              onAreaChanged("All Jamshedpur")
              areaExpanded = false
            }
          )
          serviceAreas.filter { it.isActive }.forEach { area ->
            DropdownMenuItem(
              text = { Text(area.name) },
              onClick = {
                onAreaChanged(area.name)
                areaExpanded = false
              }
            )
          }
        }
      }

      // 2. Pickup Date & Time
      OutlinedTextField(
        value = dateFormatter.format(Date(pickupEpochMs)),
        onValueChange = {},
        readOnly = true,
        label = { Text("Pickup date and time") },
        leadingIcon = {
          Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AmberAccent)
        },
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            showDatePicker(context, pickupEpochMs) { newMs ->
              onPickupDateChanged(newMs)
              if (returnEpochMs <= newMs) {
                onReturnDateChanged(newMs + (24L * 60L * 60L * 1000L))
              }
            }
          }
          .testTag("pickup_date_input"),
        enabled = false,
        colors = TextFieldDefaults.colors(
          disabledContainerColor = Color.White,
          disabledTextColor = NavyPrimary,
          disabledLabelColor = TextMuted,
          disabledLeadingIconColor = AmberAccent
        )
      )

      // 3. Return Date & Time
      OutlinedTextField(
        value = dateFormatter.format(Date(returnEpochMs)),
        onValueChange = {},
        readOnly = true,
        label = { Text("Return date and time") },
        leadingIcon = {
          Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AmberAccent)
        },
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            showDatePicker(context, returnEpochMs) { newMs ->
              if (newMs > pickupEpochMs) {
                onReturnDateChanged(newMs)
              }
            }
          }
          .testTag("return_date_input"),
        enabled = false,
        colors = TextFieldDefaults.colors(
          disabledContainerColor = Color.White,
          disabledTextColor = NavyPrimary,
          disabledLabelColor = TextMuted,
          disabledLeadingIconColor = AmberAccent
        )
      )

      // 4. Action Button
      Button(
        onClick = { /* state flows reactively update results */ },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("find_ertigas_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
      ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Find available Ertigas",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}

@Composable
private fun OwnerCalloutCard(onListErtigaClicked: () -> Unit) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.45f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("owner_callout_section")
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "Own an Ertiga? Earn when you’re not using it.",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NavyPrimary
      )
      Text(
        text = "List your car, manage your availability, and respond to booking requests from one owner dashboard.",
        fontSize = 13.sp,
        color = TextPrimary,
        lineHeight = 18.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Button(
        onClick = onListErtigaClicked,
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .height(44.dp)
          .testTag("list_ertiga_btn")
      ) {
        Text("List your Ertiga", color = Color.White, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun TrustStrip() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color.White)
      .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
      .padding(12.dp),
    horizontalArrangement = Arrangement.SpaceAround
  ) {
    TrustItem(icon = Icons.Default.Verified, title = "Admin Verified")
    TrustItem(icon = Icons.Default.Shield, title = "Zero Hidden Fees")
    TrustItem(icon = Icons.Default.CheckCircle, title = "OTP Handover")
  }
}

@Composable
private fun TrustItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
  }
}

@Composable
private fun VehicleCard(
  vehicle: Vehicle,
  onViewCar: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("vehicle_card_${vehicle.id}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Vehicle Header Graphic Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .background(NavyPrimary.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(60.dp)
          )
          Text(
            text = "MARUTI SUZUKI ERTIGA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp
          )
        }

        // Verified Badge
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(EmeraldLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Verified listing", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
          }
        }
      }

      // Vehicle Details
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column {
            Text(
              text = "Maruti Suzuki Ertiga ${vehicle.variant}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Text(
              text = "${vehicle.registrationYear} • ${vehicle.calculatedAge} yr old • ${vehicle.transmission} • ${vehicle.fuelType}",
              fontSize = 13.sp,
              color = TextMuted
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "₹${vehicle.dailyRate.toInt()}",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = AmberAccent
            )
            Text(
              text = "per day",
              fontSize = 11.sp,
              color = TextMuted
            )
          }
        }

        // Specifications chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SpecChip(icon = Icons.Default.People, label = "${vehicle.seatingCapacity} Seats")
          SpecChip(icon = Icons.Default.LocationOn, label = vehicle.pickupArea)
          SpecChip(icon = Icons.Default.Speed, label = "${vehicle.includedKmPerDay} km/day")
        }

        // Action Button
        Button(
          onClick = onViewCar,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("view_car_btn_${vehicle.id}"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("View car", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun SpecChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(ChipBackground)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
    Spacer(modifier = Modifier.width(4.dp))
    Text(label, fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
  }
}

private fun showDatePicker(context: Context, initialEpochMs: Long, onDateSelected: (Long) -> Unit) {
  val cal = Calendar.getInstance().apply { timeInMillis = initialEpochMs }
  DatePickerDialog(
    context,
    { _, year, month, day ->
      val selectedCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
      }
      onDateSelected(selectedCal.timeInMillis)
    },
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH),
    cal.get(Calendar.DAY_OF_MONTH)
  ).apply {
    datePicker.minDate = System.currentTimeMillis() // prevent past dates
    show()
  }
}
