package rc.MatchPointLite.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*

private val ColorGold    = Color(0xFFFFD700)
private val ColorOverlay = Color(0xF0101010)
private val ColorGreen   = Color(0xFF1D8A3A)

@Composable
fun PaywallScreen(
    onPurchase: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorOverlay),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                TennisBall(size = 32f)
            }
            item {
                Text(
                    text = "UNLOCK FULL APP",
                    color = ColorGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
            item {
                Text(
                    text = "You've used your 3 free games / undos",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FeatureRow("Unlimited games")
                    FeatureRow("Unlimited undos")
                    FeatureRow("Golden Point mode")
                    FeatureRow("Star Point mode")
                }
            }
            item {
                Chip(
                    onClick = onPurchase,
                    label = {
                        Text(
                            "Unlock for \$0.99",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.chipColors(backgroundColor = ColorGreen)
                )
            }
            item {
                CompactChip(
                    onClick = onDismiss,
                    label = { Text("Maybe Later", fontSize = 8.sp) },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color(0xFF333333)
                    )
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text("✓ ", color = ColorGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(text, color = Color.White, fontSize = 8.sp)
    }
}
