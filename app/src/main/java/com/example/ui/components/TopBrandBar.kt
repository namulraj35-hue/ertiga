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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted

@Composable
fun TopBrandBar(
  appName: String,
  serviceCity: String,
  isSignedIn: Boolean,
  userDisplayName: String,
  isAdmin: Boolean,
  onSignInClick: () -> Unit,
  onSignOut: () -> Unit,
  onListErtigaClick: () -> Unit,
  onAdminBootstrap: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = Color.White,
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Brand Logo & City
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(NavyPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "BookMyErtiga Logo",
            tint = AmberAccent,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
          Text(
            text = "BookMyErtiga",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = serviceCity,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = TextMuted
            )
          }
        }
      }

      // Action Buttons (Header buttons from spec)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (!isSignedIn) {
          // Visitor Header: "List your Ertiga", "Sign in"
          OutlinedButton(
            onClick = onListErtigaClick,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
          ) {
            Text("List your Ertiga", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
          }

          Button(
            onClick = onSignInClick,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
          ) {
            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sign in", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        } else {
          // Signed-in header
          if (isAdmin) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(EmeraldLight)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "ADMIN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldDark
              )
            }
          } else {
            // Affordance to initialize admin if this is the founder/admin testing
            IconButton(
              onClick = onAdminBootstrap,
              modifier = Modifier
                .size(32.dp)
                .testTag("admin_bootstrap_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Initialize Admin Access",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          IconButton(
            onClick = onSignOut,
            modifier = Modifier
              .size(36.dp)
              .testTag("sign_out_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = "Sign out",
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
