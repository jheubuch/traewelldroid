package de.hbch.traewelling.api.models.mastodon

import com.google.gson.annotations.SerializedName

data class CustomEmoji(
    val shortcode: String,
    @SerializedName("static_url") val url: String,
    @SerializedName("visible_in_picker") val visibleInPicker: Boolean
)
