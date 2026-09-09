package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary

/**
 * Standard sizes for the Rebuild unified button design system.
 * Touch target is strictly minimum 48.dp across all sizes.
 */
enum class RebuildButtonSize(
    val minHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp,
    val cornerRadius: Dp
) {
    SMALL(
        minHeight = 48.dp,
        horizontalPadding = 12.dp,
        verticalPadding = 8.dp,
        fontSize = 12.sp,
        iconSize = 16.dp,
        cornerRadius = 10.dp
    ),
    MEDIUM(
        minHeight = 48.dp,
        horizontalPadding = 16.dp,
        verticalPadding = 10.dp,
        fontSize = 13.sp,
        iconSize = 18.dp,
        cornerRadius = 12.dp
    ),
    LARGE(
        minHeight = 52.dp,
        horizontalPadding = 20.dp,
        verticalPadding = 12.dp,
        fontSize = 14.sp,
        iconSize = 20.dp,
        cornerRadius = 14.dp
    )
}

/**
 * Visual variants for the Rebuild button system.
 */
enum class RebuildButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    GHOST,
    DANGER,
    SUCCESS
}

/**
 * Production-ready unified Button Component.
 * - Minimum 48.dp interactive touch target.
 * - Eliminates text vertical stacking/clipping.
 * - Single-line text with ellipsis and automatic truncation protection.
 * - High-contrast accessible color styling.
 */
@Composable
fun RebuildButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: RebuildButtonSize = RebuildButtonSize.MEDIUM,
    variant: RebuildButtonVariant = RebuildButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor = when (variant) {
        RebuildButtonVariant.PRIMARY -> if (enabled) IceCyanPrimary else IceCyanPrimary.copy(alpha = 0.35f)
        RebuildButtonVariant.SECONDARY -> if (enabled) Color(0xFF1E355B) else Color(0xFF101B2E)
        RebuildButtonVariant.OUTLINE -> Color.Transparent
        RebuildButtonVariant.GHOST -> Color.Transparent
        RebuildButtonVariant.DANGER -> if (enabled) Color(0xFFE53935) else Color(0x55E53935)
        RebuildButtonVariant.SUCCESS -> if (enabled) Color(0xFF22C55E) else Color(0x4422C55E)
    }

    val contentColor = when (variant) {
        RebuildButtonVariant.PRIMARY -> if (enabled) DarkNavy else GlassWhiteMuted
        RebuildButtonVariant.SECONDARY -> if (enabled) IceCyanPrimary else GlassWhiteMuted
        RebuildButtonVariant.OUTLINE -> if (enabled) IceCyanPrimary else GlassWhiteMuted
        RebuildButtonVariant.GHOST -> if (enabled) GlassWhite else GlassWhiteMuted
        RebuildButtonVariant.DANGER -> Color.White
        RebuildButtonVariant.SUCCESS -> DarkNavy
    }

    val borderStroke = when (variant) {
        RebuildButtonVariant.OUTLINE -> BorderStroke(
            1.dp,
            if (enabled) IceCyanPrimary.copy(alpha = 0.7f) else Color(0x337C8CFF)
        )
        RebuildButtonVariant.SECONDARY -> BorderStroke(
            1.dp,
            if (enabled) Color(0x447C8CFF) else Color(0x227C8CFF)
        )
        else -> null
    }

    val shape = RoundedCornerShape(size.cornerRadius)

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = size.minHeight)
            .heightIn(min = size.minHeight)
            .clip(shape)
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        color = containerColor,
        border = borderStroke,
        shadowElevation = if (variant == RebuildButtonVariant.PRIMARY && enabled) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = size.horizontalPadding,
                vertical = size.verticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(size.iconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(size.iconSize)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontSize = size.fontSize,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (!isLoading && trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(size.iconSize)
                )
            }
        }
    }
}
