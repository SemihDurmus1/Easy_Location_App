package com.vexa.konumgonder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vexa.konumgonder.data.FavoriteContact
import com.vexa.konumgonder.data.PreferencesManager
import com.vexa.konumgonder.ui.components.AddContactDialog
import com.vexa.konumgonder.ui.components.FavoriteContactsSheet
import com.vexa.konumgonder.ui.components.SettingsDialog
import com.vexa.konumgonder.ui.theme.AppBackground
import com.vexa.konumgonder.ui.theme.CardBackground
import com.vexa.konumgonder.ui.theme.CardBorder
import com.vexa.konumgonder.ui.theme.GlowColor
import com.vexa.konumgonder.ui.theme.GreenAccent
import com.vexa.konumgonder.ui.theme.GreenDark
import com.vexa.konumgonder.ui.theme.GreenPrimary
import com.vexa.konumgonder.ui.theme.GreenSecondary
import com.vexa.konumgonder.ui.theme.KonumGonderTheme
import com.vexa.konumgonder.ui.theme.TextMuted
import com.vexa.konumgonder.ui.theme.TextSubtle
import com.vexa.konumgonder.ui.theme.TextWhite
import com.vexa.konumgonder.utils.LocationHelper
import com.vexa.konumgonder.utils.WhatsAppHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KonumGonderTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager(context) }
    val locationHelper = remember { LocationHelper(context) }

    var favoriteContacts by remember { mutableStateOf(prefsManager.getFavoriteContacts()) }
    var selectedContactId by remember { mutableStateOf(prefsManager.getSelectedContactId()) }
    var messageTemplate by remember { mutableStateOf(prefsManager.getMessageTemplate()) }

    var isLoadingLocation by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showContactsSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    fun refreshContacts() {
        favoriteContacts = prefsManager.getFavoriteContacts()
        selectedContactId = prefsManager.getSelectedContactId()
    }

    fun sendCurrentLocation() {
        coroutineScope.launch {
            isLoadingLocation = true
            val location = locationHelper.getCurrentLocation()
            isLoadingLocation = false

            if (location != null) {
                val mapsUrl = LocationHelper.createGoogleMapsUrl(location.latitude, location.longitude)
                val selectedContact = favoriteContacts.find { it.id == selectedContactId }

                WhatsAppHelper.sendLocationMessage(
                    context = context,
                    locationUrl = mapsUrl,
                    messageTemplate = messageTemplate,
                    phoneNumber = selectedContact?.phoneNumber
                )
            } else {
                Toast.makeText(
                    context,
                    "Konum alınamadı. Lütfen GPS ve Konum servisinizin açık olduğundan emin olun.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            sendCurrentLocation()
        } else {
            Toast.makeText(
                context,
                "Konum paylaşmak için konum izni gereklidir.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun onSendButtonClick() {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
            sendCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val selectedContact = favoriteContacts.find { it.id == selectedContactId }

    Scaffold(
        containerColor = AppBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KonumGönder",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                IconButton(
                    onClick = { showSettingsDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ayarlar",
                        tint = TextWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // MIDDLE HERO SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Konumunuzu paylaşmak için\nbüyük butona basın",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // BIG GREEN BUTTON
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            ambientColor = GreenPrimary,
                            spotColor = GreenPrimary
                        )
                        .border(
                            width = 10.dp,
                            color = GlowColor,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(GreenPrimary, GreenSecondary, GreenDark)
                            )
                        )
                        .clickable(enabled = !isLoadingLocation) {
                            onSendButtonClick()
                        }
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            color = TextWhite,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(52.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "KONUMUMU\nGÖNDER",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                if (selectedContact != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardBackground)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Gönderilecek: ",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${selectedContact.name} (${selectedContact.phoneNumber})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                    }
                }
            }

            // BOTTOM CARD - FAVORITE CONTACTS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favori Kişiler",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showContactsSheet = true }
                        ) {
                            Text(
                                text = "Tümünü Gör",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenAccent
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ADD FAVORITE CONTACT BUTTON CARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable { showAddContactDialog = true }
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Favori kişi ekleyin  ->",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenAccent
                            )
                        }
                    }
                }
            }
        }
    }

    // DIALOGS & BOTTOM SHEETS
    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onAddContact = { name, phone ->
                prefsManager.addContact(name, phone)
                refreshContacts()
                showAddContactDialog = false
                Toast.makeText(context, "$name eklendi.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showContactsSheet) {
        FavoriteContactsSheet(
            contacts = favoriteContacts,
            selectedContactId = selectedContactId,
            onDismiss = { showContactsSheet = false },
            onSelectContact = { id ->
                prefsManager.setSelectedContactId(id)
                refreshContacts()
            },
            onDeleteContact = { id ->
                prefsManager.removeContact(id)
                refreshContacts()
            },
            onAddNewContactClick = {
                showAddContactDialog = true
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            initialTemplate = messageTemplate,
            onDismiss = { showSettingsDialog = false },
            onSaveTemplate = { newTemplate ->
                prefsManager.setMessageTemplate(newTemplate)
                messageTemplate = newTemplate
                showSettingsDialog = false
                Toast.makeText(context, "Ayarlar kaydedildi.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
