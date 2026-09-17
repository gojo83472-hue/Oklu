package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.action.AndroidAction
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActionCard(
    action: AndroidAction,
    status: String,
    onExecuteAction: (AndroidAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (action) {
        is AndroidAction.OpenCamera -> Icons.Default.CameraAlt
        is AndroidAction.OpenGallery -> Icons.Default.PhotoLibrary
        is AndroidAction.MakeCall -> Icons.Default.Call
        is AndroidAction.SendMessage -> Icons.Default.Message
        is AndroidAction.OpenSettings -> Icons.Default.Settings
        is AndroidAction.OpenChrome -> Icons.Default.Language
        is AndroidAction.OpenMusic -> Icons.Default.MusicNote
        is AndroidAction.OpenYouTube -> Icons.Default.SmartDisplay
        is AndroidAction.RecordVideo -> Icons.Default.Videocam
        is AndroidAction.PlayVideo -> Icons.Default.PlayCircle
        is AndroidAction.ControlVolume -> Icons.Default.VolumeUp
        is AndroidAction.ToggleFlashlight -> Icons.Default.FlashlightOn
        is AndroidAction.Dictation -> Icons.Default.Edit
        is AndroidAction.OpenApp -> Icons.Default.Apps
    }

    val iconColor = when (action) {
        is AndroidAction.MakeCall -> AccentGreen
        is AndroidAction.OpenCamera -> SecondaryCyan
        is AndroidAction.OpenMusic -> Color(0xFFF43F5E)
        is AndroidAction.OpenChrome -> Color(0xFF38BDF8)
        is AndroidAction.OpenYouTube -> Color(0xFFFF1744)
        is AndroidAction.RecordVideo -> Color(0xFFFF5252)
        is AndroidAction.ToggleFlashlight -> Color(0xFFFFD600)
        is AndroidAction.ControlVolume -> Color(0xFF00E676)
        else -> PrimaryIndigo
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("action_card_${action.type.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceElevated
        ),
        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = action.title,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = action.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (status == "executed") {
                    Surface(
                        color = AccentGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "Launched",
                            color = AccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onExecuteAction(action) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryIndigo,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("action_launch_button_${action.type.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Execute",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (action) {
                            is AndroidAction.MakeCall -> "Call With Confirmation"
                            is AndroidAction.SendMessage -> "Send Message"
                            is AndroidAction.OpenCamera -> "Open Camera"
                            is AndroidAction.OpenGallery -> "Open Gallery"
                            is AndroidAction.OpenSettings -> "Open Settings"
                            is AndroidAction.OpenChrome -> "Open Chrome"
                            is AndroidAction.OpenMusic -> "Play Music"
                            is AndroidAction.OpenYouTube -> "Open YouTube"
                            is AndroidAction.RecordVideo -> "Record Video"
                            is AndroidAction.PlayVideo -> "Play Video"
                            is AndroidAction.ControlVolume -> "Set Volume"
                            is AndroidAction.ToggleFlashlight -> "Toggle Torch"
                            is AndroidAction.Dictation -> "Copy Dictation"
                            is AndroidAction.OpenApp -> "Launch App"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
