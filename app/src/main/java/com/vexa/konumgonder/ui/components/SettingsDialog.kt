package com.vexa.konumgonder.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vexa.konumgonder.R
import com.vexa.konumgonder.data.PreferencesManager
import com.vexa.konumgonder.ui.theme.CardBackground
import com.vexa.konumgonder.ui.theme.GreenPrimary
import com.vexa.konumgonder.ui.theme.TextMuted
import com.vexa.konumgonder.ui.theme.TextWhite

@Composable
fun SettingsDialog(
    initialTemplate: String,
    onDismiss: () -> Unit,
    onSaveTemplate: (String) -> Unit
) {
    var templateText by remember { mutableStateOf(initialTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = stringResource(R.string.settings),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.msg_template_desc),
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = templateText,
                    onValueChange = { templateText = it },
                    label = { Text(stringResource(R.string.msg_template_label), color = TextMuted) },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = TextMuted,
                        cursorColor = GreenPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { templateText = PreferencesManager.DEFAULT_MESSAGE_TEMPLATE },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_reset_default), color = GreenPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveTemplate(templateText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.btn_save), color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close), color = TextMuted)
            }
        }
    )
}
