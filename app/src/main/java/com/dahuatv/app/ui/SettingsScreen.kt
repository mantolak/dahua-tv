package com.dahuatv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.dahuatv.app.model.Camera
import com.dahuatv.app.model.CameraForm
import com.dahuatv.app.model.Field
import com.dahuatv.app.ui.components.FieldLabelWidth
import com.dahuatv.app.ui.components.TvTextField

@Composable
fun SettingsScreen(
    index: Int,
    camera: Camera,
    onSave: (Camera) -> Unit,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
) {
    var form by remember(camera) { mutableStateOf(CameraForm.from(camera)) }
    // Errors appear only after the first Save attempt, not while typing.
    var showErrors by remember { mutableStateOf(false) }
    val errors = if (showErrors) form.errors() else emptyMap()

    val firstField = remember { FocusRequester() }
    val saveButton = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstField.requestFocus() }

    Row(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            // TV overscan-safe margins.
            .padding(horizontal = 48.dp, vertical = 27.dp),
    ) {
        Column(Modifier.weight(0.3f).padding(end = 32.dp, top = 8.dp)) {
            Text("Camera ${index + 1}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(16.dp))
            Hint("Grid uses the sub-stream (subtype=1).")
            Hint("Fullscreen uses the 4K main stream (subtype=0).")
            Spacer(Modifier.height(16.dp))
            Hint("User and password are encrypted on this TV and never leave it.")
            Spacer(Modifier.height(16.dp))
            Hint("OK edits a field · Back cancels")
        }

        Column(
            Modifier.weight(0.7f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TvTextField(
                label = "Name",
                value = form.name,
                onValueChange = { form = form.copy(name = it) },
                error = errors[Field.NAME],
                focusRequester = firstField,
            )
            TvTextField(
                label = "IP address",
                value = form.host,
                onValueChange = { form = form.copy(host = it.trim()) },
                error = errors[Field.HOST],
                keyboardType = KeyboardType.Uri,
            )
            TvTextField(
                label = "RTSP port",
                value = form.port,
                onValueChange = { v -> form = form.copy(port = v.filter(Char::isDigit).take(5)) },
                error = errors[Field.PORT],
                keyboardType = KeyboardType.Number,
            )
            TvTextField(
                label = "Username",
                value = form.username,
                onValueChange = { form = form.copy(username = it) },
                error = errors[Field.USERNAME],
            )
            TvTextField(
                label = "Password",
                value = form.password,
                onValueChange = { form = form.copy(password = it) },
                error = errors[Field.PASSWORD],
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )
            TvTextField(
                label = "Channel",
                value = form.channel,
                onValueChange = { v -> form = form.copy(channel = v.filter(Char::isDigit).take(2)) },
                error = errors[Field.CHANNEL],
                keyboardType = KeyboardType.Number,
                // Without this, Down picks the button nearest the field's centre (Cancel), not Save.
                down = saveButton,
            )

            Row(
                Modifier.padding(start = FieldLabelWidth, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(modifier = Modifier.focusRequester(saveButton), onClick = {
                    showErrors = true
                    if (form.errors().isEmpty()) onSave(form.toCamera())
                }) { Text("Save") }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
                if (camera.isConfigured) {
                    OutlinedButton(onClick = onRemove) { Text("Remove camera") }
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
}
