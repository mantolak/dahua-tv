package com.dahuatv.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

val FieldLabelWidth = 160.dp
val ErrorColor = Color(0xFFFF8A80)

/**
 * A text field that behaves like a TV control:
 * - the D-pad moves focus between fields without opening the keyboard,
 * - OK starts editing (keyboard opens),
 * - Enter/Next on the keyboard saves the value and moves to the next field,
 * - Back stops editing and stays on the field.
 */
@Composable
fun TvTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    /** Where D-pad Down (and keyboard Next) goes from this field; default is Compose's nearest-neighbour search. */
    down: FocusRequester = FocusRequester.Default,
) {
    val inner = remember { FocusRequester() }
    var editing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(6.dp)

    fun stopEditing(moveNext: Boolean) {
        if (!editing) return
        editing = false
        keyboard?.hide()
        focusRequester.requestFocus()
        if (moveNext) focusManager.moveFocus(FocusDirection.Down)
    }

    LaunchedEffect(editing) {
        if (editing) {
            inner.requestFocus()
            keyboard?.show()
        }
    }

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                modifier = Modifier.width(FieldLabelWidth),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Surface(
                onClick = { editing = true },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .focusProperties { this.down = down },
                shape = ClickableSurfaceDefaults.shape(shape),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                border = ClickableSurfaceDefaults.border(
                    border = Border(BorderStroke(1.dp, error?.let { ErrorColor } ?: Color.Gray), shape = shape),
                    focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape),
                ),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color(0xFF1C2228),
                    focusedContainerColor = Color(0xFF2A333C),
                ),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .focusRequester(inner)
                        // Only focusable while editing, so D-pad navigation lands on the Surface instead.
                        .focusProperties {
                            canFocus = editing
                            this.down = down
                        }
                        .onFocusChanged { if (!it.isFocused && editing) editing = false }
                        .onPreviewKeyEvent { e ->
                            if (e.key == Key.Back) {
                                if (e.type == KeyEventType.KeyUp) stopEditing(moveNext = false)
                                true
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                    cursorBrush = SolidColor(Color.White),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { stopEditing(moveNext = true) },
                        onDone = { stopEditing(moveNext = false) },
                    ),
                )
            }
        }
        if (error != null) {
            Text(
                text = error,
                modifier = Modifier.padding(start = FieldLabelWidth, top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = ErrorColor,
            )
        }
    }
}
