package de.hbch.traewelling.shared

import android.content.Context
import de.hbch.traewelling.api.models.mastodon.CustomEmoji
import getCustomEmojiFromJson
import java.io.File

class MastodonEmojis {

    val emojis: MutableMap<String, List<CustomEmoji>> = mutableMapOf()

    companion object {
        private var instance: MastodonEmojis? = null

        fun getInstance(context: Context) = instance ?: MastodonEmojis().also {
            instance = it

            context.fileList().filter { it.endsWith(":custom-emoji.json") }.forEach { name ->
                val instance = name.split(':').firstOrNull()
                if (instance != null) {
                    val json = File(name).readText()
                    val emoji = getCustomEmojiFromJson(json)

                    it.emojis[instance] = emoji
                }
            }
        }
    }
}
