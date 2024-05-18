import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hbch.traewelling.api.models.mastodon.CustomEmoji
import de.hbch.traewelling.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate

suspend fun Context.readOrDownloadCustomEmoji(
    mastodonInstance: String
): List<CustomEmoji> {
    val customEmojisUrl = URL("https://$mastodonInstance/api/v1/custom_emojis")
    val epochDayToday = LocalDate.now().toEpochDay()
    val file = File(filesDir, "$mastodonInstance:$epochDayToday:custom-emoji.json")
    val emoji = try {
        var download = false

        val existing = fileList()
            .filter { it.startsWith(mastodonInstance) }
            .minOfOrNull { it.split(':').getOrNull(1)?.toLongOrNull() ?: 0L } ?: 0L

        if (existing < epochDayToday)
            download = true

        withContext(Dispatchers.IO) {
            if (download) {
                val inputStream: InputStream = customEmojisUrl.openStream()
                Files.copy(
                    inputStream,
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }

            return@withContext getCustomEmojiFromJson(file.readText())
        }
    } catch (ex: Exception) {
        Logger.captureException(ex)
        listOf()
    }
    return emoji
}

fun getCustomEmojiFromJson(json: String): List<CustomEmoji> {
    return Gson().fromJson(
        json,
        object : TypeToken<List<CustomEmoji>>() {}.type
    )
}

fun String.extractCustomEmojis() = ":(\\w+):".toRegex().findAll(this).toList()
