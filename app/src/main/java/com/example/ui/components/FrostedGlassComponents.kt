package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.GlowBorderBrush
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderBrush: Brush = GlowBorderBrush,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = FrostedNavyCard,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = shape, spotColor = IceCyanGlow.copy(alpha = 0.25f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.85f),
                        backgroundColor.copy(alpha = 0.65f),
                        DeepNavySurface.copy(alpha = 0.9f)
                    )
                )
            )
            .border(BorderStroke(borderWidth, borderBrush), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun HeroGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF132B52).copy(alpha = 0.9f),
            Color(0xFF0E1A38).copy(alpha = 0.85f),
            Color(0xFF070E22).copy(alpha = 0.95f)
        )
    )
    val heroBorder = Brush.linearGradient(
        colors = listOf(
            IceCyanPrimary.copy(alpha = 0.8f),
            FrostBlueAccent.copy(alpha = 0.4f),
            ElectricBlue.copy(alpha = 0.7f)
        )
    )

    Box(
        modifier = modifier
            .shadow(elevation = 18.dp, shape = shape, spotColor = IceCyanPrimary.copy(alpha = 0.35f))
            .clip(shape)
            .background(heroGradient)
            .border(BorderStroke(1.5.dp, heroBorder), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(20.dp),
        content = content
    )
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    icon: ImageVector? = null,
    accentColor: Color = IceCyanPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0x331E355B),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = GlassWhiteMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = GlassWhite,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun GlowPill(
    text: String,
    color: Color = IceCyanPrimary,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CompactScreenHeader(
    title: String,
    subtitle: String? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onOpenDrawer != null) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FrostedNavyCard)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = GlassWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        } else if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FrostedNavyCard)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = GlassWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GlassWhite,
                letterSpacing = 0.3.sp,
                maxLines = 1
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = GlassWhiteMuted,
                    maxLines = 1
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun CompactLevelXpBadge(
    level: Int,
    xp: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = LuxuryCard,
        border = BorderStroke(1.dp, Color(0x337C8CFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEVEL $level",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PurpleArc,
                fontSize = 11.sp
            )
            Text(
                text = " • ",
                color = GlassWhiteMuted,
                fontSize = 11.sp
            )
            Text(
                text = "${String.format("%,d", xp)} XP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = IceCyanPrimary,
                fontSize = 11.sp
            )
        }
    }
}

