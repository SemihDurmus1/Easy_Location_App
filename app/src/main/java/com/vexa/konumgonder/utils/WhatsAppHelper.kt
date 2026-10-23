package com.vexa.konumgonder.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    fun formatPhoneNumber(rawPhone: String): String {
        var cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.startsWith("+")) {
            cleanPhone = cleanPhone.substring(1)
        }
        return cleanPhone
    }

    fun sendLocationMessage(
        context: Context,
        locationUrl: String,
        messageTemplate: String,
        phoneNumber: String? = null
    ) {
        val fullMessage = "$messageTemplate\n$locationUrl"

        if (!phoneNumber.isNullOrBlank()) {
            val formattedPhone = formatPhoneNumber(phoneNumber)
            val encodedMessage = URLEncoder.encode(fullMessage, "UTF-8")
            val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")

            val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                // WhatsApp Business check
                try {
                    val businessIntent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                        setPackage("com.whatsapp.w4b")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(businessIntent)
                } catch (_: Exception) {
                    // Browser fallback
                    val browserIntent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(browserIntent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "WhatsApp açılamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // General WhatsApp share or System Share
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, fullMessage)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(sendIntent)
            } catch (_: Exception) {
                try {
                    val businessIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, fullMessage)
                        setPackage("com.whatsapp.w4b")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(businessIntent)
                } catch (_: Exception) {
                    val shareChooser = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, fullMessage)
                        },
                        "Konum Paylaş"
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(shareChooser)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Paylaşım seçeneği bulunamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
