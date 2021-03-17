package org.jetbrains.research.ml.codingAssistant.models

import kotlinx.serialization.Serializable

@Serializable
data class StoredInfo(
    var loggedUIData: Map<String, String> = mapOf(),
    var userId: String? = null
)
