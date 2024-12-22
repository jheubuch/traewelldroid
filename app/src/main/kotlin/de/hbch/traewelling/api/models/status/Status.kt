package de.hbch.traewelling.api.models.status

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.mastodon.CustomEmoji
import de.hbch.traewelling.api.models.user.LightUser
import de.hbch.traewelling.shared.MastodonEmojis
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.util.extractUsernames
import de.hbch.traewelling.util.extractCustomEmojis
import kotlinx.coroutines.launch
import java.net.URL
import java.time.ZonedDateTime

data class Status(
    val id: Int,
    val body: String?,
    val createdAt: ZonedDateTime,
    val visibility: StatusVisibility,
    val business: StatusBusiness,
    var likes: Int?,
    var liked: Boolean?,
    @SerializedName("isLikable") val likeable: Boolean?,
    @SerializedName("train") val journey: Journey,
    val event: Event?,
    val client: ApiClient?,
    @SerializedName("bodyMentions") val mentions: List<UserMention>,
    @SerializedName("userDetails") val user: LightUser,
    val tags: List<Tag>
) {
    fun getStatusText(): String {
        var statusBody = body ?: ""

        if (user.username == "ErikUden") {
            statusBody += "\n🍑"
        }

        return statusBody
    }

    @Composable
    fun getStatusBody(): Pair<AnnotatedString, Map<String, InlineTextContent>> {
        val context = LocalContext.current
        val mentionColor = LocalColorScheme.current.primary
        val statusBody = getStatusText()
        val mastodonEmoji = remember { mutableStateListOf<CustomEmoji>() }
        val coroutineScope = rememberCoroutineScope()

        val usernames = statusBody.extractUsernames()
        val usernameStyle = SpanStyle(fontWeight = FontWeight.ExtraBold, color = mentionColor)
        val extractedEmojis = statusBody.extractCustomEmojis()
        val matches = listOf(usernames, extractedEmojis).flatten().sortedBy { it.range.first }
        val builder = AnnotatedString.Builder()

        val inlineTextContent = mutableMapOf<String, InlineTextContent>()

        var lastRangeEnd = 0
        matches.forEach { match ->
            builder.append(statusBody.substring(lastRangeEnd, match.range.first))

            if (usernames.contains(match)) {
                val username = match.groupValues.getOrElse(1) { "" }
                builder.append(statusBody.substring(match.range))
                if (mentions.any { it.user.username == username }) {
                    builder.addStyle(usernameStyle, match.range.first, match.range.last + 1)
                    builder.addStringAnnotation(
                        "userMention",
                        username,
                        match.range.first,
                        match.range.last + 1
                    )
                }
            } else if (extractedEmojis.contains(match)) {
                if (user.mastodonUrl != null) {
                    val mastodonEmojis = MastodonEmojis.getInstance(context)
                    val instance = URL(user.mastodonUrl).host
                    val instanceEmoji = mastodonEmojis.emojis[instance]

                    if (instanceEmoji.isNullOrEmpty()) {
                        coroutineScope.launch {
                            mastodonEmoji.addAll(MastodonEmojis.getEmojis(instance, context))
                        }
                    } else {
                        mastodonEmoji.addAll(instanceEmoji)
                    }

                    val emoji = match.groupValues.getOrElse(1) { "" }
                    val customEmoji = mastodonEmoji.firstOrNull { it.shortcode == emoji }
                    if (customEmoji != null) {
                        builder.appendInlineContent(customEmoji.shortcode, ":${customEmoji.shortcode}:")
                        inlineTextContent[customEmoji.shortcode] = InlineTextContent(
                            Placeholder(24.sp, 24.sp, PlaceholderVerticalAlign.TextCenter)
                        ) {
                            AsyncImage(
                                model = customEmoji.url,
                                contentDescription = customEmoji.shortcode,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = painterResource(id = R.drawable.ic_hourglass)
                            )
                        }
                    } else {
                        builder.append(":$emoji:")
                    }
                }
            }

            lastRangeEnd = match.range.last + 1
        }

        builder.append(statusBody.substring(lastRangeEnd, statusBody.length))
        return Pair(builder.toAnnotatedString(), inlineTextContent)
    }

    val isTraewelldroidCheckIn get() = client?.id == 43
}

enum class StatusVisibility() {
    @SerializedName("0")
    PUBLIC {
        override val icon = R.drawable.ic_public
        override val title = R.string.visibility_public
    },
    @SerializedName("1")
    UNLISTED {
        override val icon = R.drawable.ic_lock_open
        override val title = R.string.visibility_unlisted
    },
    @SerializedName("2")
    FOLLOWERS {
        override val icon = R.drawable.ic_people
        override val title = R.string.visibility_followers
    },
    @SerializedName("3")
    PRIVATE {
        override val icon = R.drawable.ic_lock
        override val title = R.string.visibility_private
    },
    @SerializedName("4")
    ONLY_AUTHENTICATED {
        override val icon = R.drawable.ic_authorized
        override val title = R.string.visibility_only_authenticated
        override val isMastodonVisibility = false
    };

    abstract val icon: Int
    abstract val title: Int
    open val isMastodonVisibility: Boolean = true
}

enum class StatusBusiness(val business: Int) {
    @SerializedName("0")
    PRIVATE(0) {
        override val icon = R.drawable.ic_person
        override val title = R.string.business_private
    },
    @SerializedName("1")
    BUSINESS(1) {
        override val icon = R.drawable.ic_business
        override val title = R.string.business
    },
    @SerializedName("2")
    COMMUTE(2) {
        override val icon = R.drawable.ic_commute
        override val title = R.string.business_commute
    };

    abstract val icon: Int
    abstract val title: Int
}
