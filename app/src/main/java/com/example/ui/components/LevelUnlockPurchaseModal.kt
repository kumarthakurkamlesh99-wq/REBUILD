package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RankLevel
import com.example.ui.theme.DarkLuxuryBackground
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

/**
 * Premium Level Unlock Purchase Modal
 * XP-based simulated high-end checkout experience.
 */
@Composable
fun LevelUnlockPurchaseModal(
    rank: RankLevel,
    currentXpBalance: Int,
    isPurchasing: Boolean = false,
    onConfirmUnlock: () -> Unit,
    onDismiss: () -> Unit
) {
    val xpRequired = rank.unlockXpCost
    val isSufficient = currentXpBalance >= xpRequired
    val remainingBalance = currentXpBalance - xpRequired
    val deficit = xpRequired - currentXpBalance

    Dialog(
        onDismissRequest = { if (!isPurchasing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("level_unlock_modal")
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                IceCyanPrimary,
                                PurpleArc.copy(alpha = 0.6f),
                                GlassBorder
                            )
                        )
                    ),
                    RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(IceCyanPrimary.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(1.5.dp, IceCyanPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSufficient) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isSufficient) IceCyanPrimary else WarningAmber,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "LEVEL UNLOCK",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = IceCyanPrimary
                )

                Text(
                    text = "Level ${rank.level} • ${rank.title}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = rank.teaserLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Economy Breakdown Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF040711),
                    border = BorderStroke(1.dp, Color(0x337C8CFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EconomyRow(
                            label = "XP Required to Unlock",
                            value = "${String.format("%,d", xpRequired)} XP",
                            valueColor = PurpleArc,
                            isBold = true
                        )

                        HorizontalDivider(color = Color(0x18FFFFFF), thickness = 0.5.dp)

                        EconomyRow(
                            label = "Current XP Balance",
                            value = "${String.format("%,d", currentXpBalance)} XP",
                            valueColor = GlassWhite
                        )

                        HorizontalDivider(color = Color(0x18FFFFFF), thickness = 0.5.dp)

                        EconomyRow(
                            label = "Remaining Balance After Unlock",
                            value = if (isSufficient) "${String.format("%,d", remainingBalance)} XP" else "Deficit: -${String.format("%,d", deficit)} XP",
                            valueColor = if (isSufficient) SuccessGreen else DangerRed,
                            isBold = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Insufficient XP Warning / Incentive
                if (!isSufficient) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarningAmber.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "You need ${String.format("%,d", deficit)} more XP.\nComplete missions, study sessions, and flashcard sweeps to earn XP.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningAmber,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "Instant deduction from your XP Ledger with permanent cryptographic transaction ID.",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassWhiteMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isPurchasing,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_unlock_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassWhiteMuted)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onConfirmUnlock,
                        enabled = isSufficient && !isPurchasing,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("confirm_unlock_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyanPrimary,
                            disabledContainerColor = Color(0xFF1B2338)
                        )
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DarkLuxuryBackground,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (isSufficient) DarkLuxuryBackground else GlassWhiteMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSufficient) "Unlock Level" else "Locked",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSufficient) DarkLuxuryBackground else GlassWhiteMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomyRow(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GlassWhiteMuted,
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    }
}
