package com.vexa.konumgonder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vexa.konumgonder.data.FavoriteContact
import com.vexa.konumgonder.ui.theme.AppBackground
import com.vexa.konumgonder.ui.theme.CardBackground
import com.vexa.konumgonder.ui.theme.CardBorder
import com.vexa.konumgonder.ui.theme.GreenAccent
import com.vexa.konumgonder.ui.theme.GreenPrimary
import com.vexa.konumgonder.ui.theme.TextMuted
import com.vexa.konumgonder.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteContactsSheet(
    contacts: List<FavoriteContact>,
    selectedContactId: String?,
    onDismiss: () -> Unit,
    onSelectContact: (id: String?) -> Unit,
    onDeleteContact: (id: String) -> Unit,
    onAddNewContactClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favori Kişilerim",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Button(
                    onClick = onAddNewContactClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ekle",
                        tint = TextWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yeni Ekle", color = TextWhite, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty()) {
                Text(
                    text = "Henüz kayıtlı favori kişi yok.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Option for "No selection / Anyone"
                    item {
                        val isNoneSelected = selectedContactId == null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .border(
                                    1.dp,
                                    if (isNoneSelected) GreenPrimary else CardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectContact(null) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isNoneSelected,
                                onClick = { onSelectContact(null) },
                                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Herhangi Biri / Seçim Yok",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "WhatsApp içinden kişi seçeceğim",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    items(contacts, key = { it.id }) { contact ->
                        val isSelected = contact.id == selectedContactId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) GreenPrimary else CardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectContact(contact.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectContact(contact.id) },
                                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = contact.phoneNumber,
                                    color = TextAccentGreenOrMuted(isSelected),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { onDeleteContact(contact.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TextAccentGreenOrMuted(isSelected: Boolean) = if (isSelected) GreenAccent else TextMuted
