package de.hbch.traewelling.shared

import android.content.Context
import de.hbch.traewelling.api.models.mastodon.CustomEmoji
import de.hbch.traewelling.util.getCustomEmojiFromJson
import de.hbch.traewelling.util.readOrDownloadCustomEmoji
import java.io.File

class MastodonEmojis {

    val emojis: MutableMap<String, List<CustomEmoji>> = mutableMapOf()

    companion object {
        private var instance: MastodonEmojis? = null

        fun getInstance(context: Context) = instance ?: MastodonEmojis().also {
            instance = it

            context.fileList().filter { file -> file.endsWith(":custom-emoji.json") }.forEach { name ->
                val instance = name.split(':').firstOrNull()
                if (instance != null) {
                    try {
                        val json = File(name).readText()
                        val emoji = getCustomEmojiFromJson(json)

                        it.emojis[instance] = emoji
                    } catch (_: Exception) {
                        File(name).delete()
                    }
                }
            }
        }

        suspend fun getEmojis(instance: String, context: Context): List<CustomEmoji> {
            val emoji = context.readOrDownloadCustomEmoji(instance)
            getInstance(context).emojis[instance] = emoji
            return emoji
        }
    }
}
