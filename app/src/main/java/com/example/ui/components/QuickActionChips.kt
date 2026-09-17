package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.action.AndroidAction
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryViolet
import com.example.ui.theme.TextPrimary

data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val action: AndroidAction,
    val tint: Color,
    val queryOverride: String? = null
)

@Composable
fun QuickActionChips(
    onActionSelected: (AndroidAction) -> Unit,
    onQuerySelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quickActions = listOf(
        QuickActionItem("Camera", Icons.Default.CameraAlt, AndroidAction.OpenCamera, SecondaryCyan),
        QuickActionItem("Record Video", Icons.Default.Videocam, AndroidAction.RecordVideo, Color(0xFFFF5252)),
        QuickActionItem("Play Video", Icons.Default.PlayCircle, AndroidAction.PlayVideo(), Color(0xFFFF4081)),
        QuickActionItem("Flashlight", Icons.Default.FlashlightOn, AndroidAction.ToggleFlashlight(true), Color(0xFFFFD600)),
        QuickActionItem("Volume Up", Icons.Default.VolumeUp, AndroidAction.ControlVolume(AndroidAction.VolumeAction.UP), Color(0xFF00E676)),
        QuickActionItem("YouTube", Icons.Default.SmartDisplay, AndroidAction.OpenYouTube(), Color(0xFFFF1744)),
        QuickActionItem("Music", Icons.Default.MusicNote, AndroidAction.OpenMusic(null), Color(0xFFE040FB)),
        QuickActionItem("Gallery", Icons.Default.PhotoLibrary, AndroidAction.OpenGallery, TertiaryViolet),
        QuickActionItem("Chrome", Icons.Default.Language, AndroidAction.OpenChrome(null), Color(0xFF38BDF8)),
        QuickActionItem("Call", Icons.Default.Call, AndroidAction.MakeCall(""), Color(0xFF34D399)),
        QuickActionItem("SMS", Icons.Default.Message, AndroidAction.SendMessage(""), Color(0xFF60A5FA)),
        QuickActionItem("Settings", Icons.Default.Settings, AndroidAction.OpenSettings(null), PrimaryIndigo),
        QuickActionItem("Good Morning", Icons.Default.WbSunny, AndroidAction.OpenSettings(null), Color(0xFFFFB300), queryOverride = "Good morning!"),
        QuickActionItem("வணக்கம்", Icons.Default.Translate, AndroidAction.OpenSettings(null), SecondaryCyan, queryOverride = "வணக்கம் எப்படி இருக்கீங்க?")
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_action_chips_row"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickActions) { item ->
            Surface(
                onClick = {
                    if (item.queryOverride != null) {
                        onQuerySelected(item.queryOverride)
                    } else {
                        onActionSelected(item.action)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, item.tint.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("quick_chip_${item.label.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = item.tint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
