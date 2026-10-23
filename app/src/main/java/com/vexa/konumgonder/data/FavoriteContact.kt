package com.vexa.konumgonder.data

data class FavoriteContact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val isSelected: Boolean = false
)
