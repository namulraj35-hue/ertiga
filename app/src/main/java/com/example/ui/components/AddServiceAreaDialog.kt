package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextMuted

@Composable
fun AddServiceAreaDialog(
  onDismiss: () -> Unit,
  onAddArea: (name: String, desc: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("add_service_area_dialog")
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
          Text("Add Jamshedpur Pickup Zone", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = "Specify a pickup area strictly inside Jamshedpur, Jharkhand (e.g., Bistupur, Sakchi, Sonari, Kadma, Telco, Golmuri).",
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Pickup Area Name") },
          placeholder = { Text("e.g. Bistupur") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Area Description / Landmark (Optional)") },
          placeholder = { Text("e.g. Near Regal Ground / Main Road") },
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onAddArea(name.trim(), description.trim())
            }
          },
          enabled = name.isNotBlank(),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
        ) {
          Text("Save Jamshedpur Zone", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
