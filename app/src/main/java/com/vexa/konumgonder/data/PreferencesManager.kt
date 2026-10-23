package com.vexa.konumgonder.data

import android.content.Context
import android.content.SharedPreferences
import com.vexa.konumgonder.R
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("konum_gonder_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CONTACTS = "key_favorite_contacts"
        private const val KEY_SELECTED_CONTACT_ID = "key_selected_contact_id"
        private const val KEY_MESSAGE_TEMPLATE = "key_message_template"

        private val KNOWN_DEFAULTS = listOf(
            "Merhaba, anlık konumum:",
            "Hello, my live location:",
            "Hola, mi ubicación actual:",
            "Olá, minha localização atual:",
            "Hallo, mein aktueller Standort:",
            "Bonjour, ma position actuelle :",
            "Ciao, la mia posizione attuale:",
            "Привет, мое текущее местоположение:",
            "مرحباً، موقعي الحالي:",
            "नमस्ते, मेरी वर्तमान लोकेशन:",
            "Halo, lokasi saya saat ini:"
        )
    }

    fun getFavoriteContacts(): List<FavoriteContact> {
        val jsonString = prefs.getString(KEY_CONTACTS, null) ?: return emptyList()
        val selectedId = getSelectedContactId()
        val list = mutableListOf<FavoriteContact>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val name = obj.getString("name")
                val phone = obj.getString("phoneNumber")
                list.add(
                    FavoriteContact(
                        id = id,
                        name = name,
                        phoneNumber = phone,
                        isSelected = (id == selectedId)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveFavoriteContacts(contacts: List<FavoriteContact>) {
        val jsonArray = JSONArray()
        for (contact in contacts) {
            val obj = JSONObject().apply {
                put("id", contact.id)
                put("name", contact.name)
                put("phoneNumber", contact.phoneNumber)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, jsonArray.toString()).apply()
    }

    fun addContact(name: String, phoneNumber: String): FavoriteContact {
        val currentList = getFavoriteContacts().toMutableList()
        val newContact = FavoriteContact(name = name, phoneNumber = phoneNumber)
        currentList.add(newContact)
        saveFavoriteContacts(currentList)
        return newContact
    }

    fun removeContact(id: String) {
        val currentList = getFavoriteContacts().filter { it.id != id }
        saveFavoriteContacts(currentList)
        if (getSelectedContactId() == id) {
            setSelectedContactId(null)
        }
    }

    fun getSelectedContactId(): String? {
        return prefs.getString(KEY_SELECTED_CONTACT_ID, null)
    }

    fun setSelectedContactId(id: String?) {
        if (id == null) {
            prefs.edit().remove(KEY_SELECTED_CONTACT_ID).apply()
        } else {
            prefs.edit().putString(KEY_SELECTED_CONTACT_ID, id).apply()
        }
    }

    fun getMessageTemplate(): String {
        val currentDefault = try {
            context.getString(R.string.default_message_template)
        } catch (_: Exception) {
            "Merhaba, anlık konumum:"
        }
        val saved = prefs.getString(KEY_MESSAGE_TEMPLATE, null)
        if (saved.isNullOrBlank() || KNOWN_DEFAULTS.contains(saved)) {
            return currentDefault
        }
        return saved
    }

    fun setMessageTemplate(template: String) {
        prefs.edit().putString(KEY_MESSAGE_TEMPLATE, template).apply()
    }
}
