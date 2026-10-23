package com.vexa.konumgonder

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vexa.konumgonder.data.PreferencesManager
import com.vexa.konumgonder.ui.components.AboutDialog
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
    var messageTemplate by remember { mutableStateOf(prefsManager.getMessageTemplate()) }

    var isLoadingLocation by remember { mutableStateOf(false) }
    var pendingPhoneToSend by remember { mutableStateOf<String?>(null) }

    var showContactsSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val toastContactAddedTemplate = stringResource(R.string.toast_contact_added)
    val toastLocationFailed = stringResource(R.string.toast_location_failed)
    val toastPermissionRequired = stringResource(R.string.toast_permission_required)

    fun refreshContacts() {
        favoriteContacts = prefsManager.getFavoriteContacts()
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            if (contactUri != null) {
                try {
                    val cursor = context.contentResolver.query(
                        contactUri,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val name = it.getString(0) ?: ""
                            val phone = it.getString(1) ?: ""
                            if (phone.isNotBlank()) {
                                val displayName = if (name.isNotBlank()) name else phone
                                prefsManager.addContact(displayName, phone)
                                refreshContacts()
                                Toast.makeText(context, String.format(toastContactAddedTemplate, displayName), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Kişi bilgisi alınamadı.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun launchContactPicker() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )
        contactPickerLauncher.launch(pickIntent)
    }

    fun sendLocationTo(targetPhone: String? = null) {
        coroutineScope.launch {
            isLoadingLocation = true
            val location = locationHelper.getCurrentLocation()
            isLoadingLocation = false

            if (location != null) {
                val mapsUrl = LocationHelper.createGoogleMapsUrl(location.latitude, location.longitude)
                WhatsAppHelper.sendLocationMessage(
                    context = context,
                    locationUrl = mapsUrl,
                    messageTemplate = messageTemplate,
                    phoneNumber = targetPhone
                )
            } else {
                Toast.makeText(
                    context,
                    toastLocationFailed,
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
            sendLocationTo(pendingPhoneToSend)
            pendingPhoneToSend = null
        } else {
            Toast.makeText(
                context,
                toastPermissionRequired,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun triggerSendLocation(targetPhone: String? = null) {
        pendingPhoneToSend = targetPhone
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
            sendLocationTo(targetPhone)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = AppBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    text = stringResource(R.string.app_name),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.about_app),
                            tint = GreenAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = TextWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
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
                    text = stringResource(R.string.subtitle_press_button),
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
                            triggerSendLocation(targetPhone = null)
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
                                text = stringResource(R.string.btn_send_location),
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
            }

            // BOTTOM CARD - FAVORITE CONTACTS (HORIZONTAL SCROLLING LIST)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(vertical = 18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.favorite_contacts),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showContactsSheet = true }
                        ) {
                            Text(
                                text = stringResource(R.string.see_all),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // HORIZONTAL SCROLLING ROW (LAZYROW)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // ITEM 0: ADD CONTACT BUTTON (DIRECTLY OPENS CONTACT PICKER)
                        item {
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AppBackground)
                                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                                    .clickable { launchContactPicker() }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = stringResource(R.string.add_contact),
                                        tint = GreenAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.add_contact),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAccent,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // FAVORITE CONTACTS ITEMS
                        items(favoriteContacts, key = { it.id }) { contact ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AppBackground)
                                    .border(1.dp, GreenAccent, RoundedCornerShape(16.dp))
                                    .clickable { triggerSendLocation(targetPhone = contact.phoneNumber) }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GreenPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = TextWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = contact.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = contact.phoneNumber,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS & BOTTOM SHEETS
    val toastSettingsSaved = stringResource(R.string.toast_settings_saved)

    if (showContactsSheet) {
        FavoriteContactsSheet(
            contacts = favoriteContacts,
            onDismiss = { showContactsSheet = false },
            onSendToContact = { contact ->
                showContactsSheet = false
                triggerSendLocation(targetPhone = contact.phoneNumber)
            },
            onDeleteContact = { id ->
                prefsManager.removeContact(id)
                refreshContacts()
            },
            onAddNewContactClick = {
                showContactsSheet = false
                launchContactPicker()
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
                Toast.makeText(context, toastSettingsSaved, Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}
