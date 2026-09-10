package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * REBUILD UNIFIED DESIGN SYSTEM
 * Production-grade components for input fields, keyboards, safe-areas, dialogs,
 * bottom sheets, selectors, and cards.
 * ============================================================================
 */

// Custom selection colors to prevent cursor/selection handle overflow issues
val RebuildTextSelectionColors = TextSelectionColors(
    handleColor = IceCyanPrimary,
    backgroundColor = IceCyanPrimary.copy(alpha = 0.35f)
)

/**
 * 1. REBUILD TEXT FIELD
 * - Guaranteed minimum touch target of 48dp+
 * - Integrated BringIntoViewRequester: automatically scrolls focused field into view above keyboard
 * - Cursor handles and selection handles styled cleanly without bleeding outside bounds
 * - Consistent border radius (14.dp), height (52.dp+), typography, and luxury color tokens
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RebuildTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color = Color(0xFF090E1E),
    focusedBorderColor: Color = IceCyanPrimary,
    unfocusedBorderColor: Color = FrostBlueAccent.copy(alpha = 0.35f),
    testTag: String? = null
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        CompositionLocalProvider(LocalTextSelectionColors provides RebuildTextSelectionColors) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    }
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
                label = label?.let {
                    { Text(it, color = GlassWhiteMuted, style = MaterialTheme.typography.bodyMedium) }
                },
                placeholder = placeholder?.let {
                    { Text(it, color = GlassWhiteMuted.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium) }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                enabled = enabled,
                readOnly = readOnly,
                shape = shape,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = GlassWhite),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GlassWhite,
                    unfocusedTextColor = GlassWhite,
                    disabledTextColor = GlassWhiteMuted,
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor.copy(alpha = 0.5f),
                    focusedBorderColor = focusedBorderColor,
                    unfocusedBorderColor = unfocusedBorderColor,
                    disabledBorderColor = unfocusedBorderColor.copy(alpha = 0.3f),
                    errorBorderColor = Color(0xFFEF4444),
                    cursorColor = IceCyanPrimary,
                    errorCursorColor = Color(0xFFEF4444)
                )
            )
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * 2. REBUILD DIALOG
 * - Production-grade dialog architecture replacing raw AlertDialog
 * - Constrained for small phones, large phones, tablets, and landscape
 * - imePadding(): dialog automatically shrinks & lifts above keyboard
 * - Sticky Header + Scrollable Form Body (verticalScroll) + Sticky Footer
 * - Save/Confirm CTA buttons remain 100% visible and reachable above keyboard
 * - Touch targets >= 48dp throughout
 */
@Composable
fun RebuildDialog(
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = IceCyanPrimary,
    confirmButtonText: String = "Confirm",
    confirmButtonEnabled: Boolean = true,
    isProcessing: Boolean = false,
    onConfirm: () -> Unit,
    dismissButtonText: String? = "Cancel",
    onDismissClick: (() -> Unit)? = onDismiss,
    headerAccentColor: Color = IceCyanPrimary,
    testTag: String = "rebuild_dialog",
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
                .imePadding()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 540.dp)
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 0.90f)
                    .testTag(testTag),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF090E1E),
                border = BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(
                            headerAccentColor.copy(alpha = 0.7f),
                            GlassBorder,
                            headerAccentColor.copy(alpha = 0.2f)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // --- STICKY HEADER ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (icon != null) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(iconTint.copy(alpha = 0.15f))
                                        .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassWhiteMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Close button (48dp touch target)
                        IconButton(
                            onClick = { if (!isProcessing) onDismiss() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("${testTag}_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = GlassWhiteMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    // --- SCROLLABLE FORM BODY ---
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            content()
                        }
                    }

                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    // --- STICKY FOOTER (CTAs always visible & reachable) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissButtonText != null && onDismissClick != null) {
                            OutlinedButton(
                                onClick = onDismissClick,
                                enabled = !isProcessing,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("${testTag}_cancel_btn"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GlassBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassWhiteMuted)
                            ) {
                                Text(
                                    text = dismissButtonText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Button(
                            onClick = onConfirm,
                            enabled = confirmButtonEnabled && !isProcessing,
                            modifier = Modifier
                                .weight(if (dismissButtonText != null) 1.3f else 1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("${testTag}_confirm_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = headerAccentColor,
                                disabledContainerColor = headerAccentColor.copy(alpha = 0.3f)
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = DarkNavy,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Saving...",
                                    color = DarkNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = confirmButtonText,
                                    color = DarkNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. REBUILD BOTTOM SHEET
 * Modal bottom sheet with imePadding, responsive max-width for tablets,
 * safe area support, and scrollable container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RebuildBottomSheet(
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    testTag: String = "rebuild_bottom_sheet",
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0F22),
        dragHandle = { BottomSheetDefaults.DragHandle(color = GlassWhiteMuted.copy(alpha = 0.4f)) },
        modifier = modifier
            .imePadding()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = GlassWhiteMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 4. REBUILD SELECTOR CHIP
 * - Guarantees 48dp minimum touch target
 * - High contrast visual active glow
 * - Text overflow protection
 */
@Composable
fun RebuildSelectorChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    selectedColor: Color = IceCyanPrimary,
    testTag: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) selectedColor else Color(0x33102A45),
        border = BorderStroke(
            1.dp,
            if (isSelected) selectedColor else GlassBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isSelected) DarkNavy else GlassWhiteMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) DarkNavy else GlassWhite,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 5. REBUILD CARD
 * Production-grade card container with consistent border and padding
 */
@Composable
fun RebuildCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = Color(0xFF0B1226),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
