package de.hbch.traewelling.util

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.sp
import com.auth0.android.jwt.JWT
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.trip.HafasTrip
import java.lang.Exception
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun getLocalTimeString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

fun getLocalDateTimeString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

fun getLocalDateString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

fun getLongLocalDateString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

@Composable
fun getDelayColor(planned: ZonedDateTime, real: ZonedDateTime?): Color {
    val duration = Duration.between(planned, real ?: planned)
    val difference = duration.toMinutes()

    val color = when {
        difference <= 0 -> R.color.train_on_time
        difference in 1..5 -> R.color.warning
        else -> R.color.train_delayed
    }

    return colorResource(id = color)
}

@Composable
fun getDateRangeString(range: Pair<LocalDate, LocalDate>?): String {
    if (range == null)
        return ""
    return stringResource(
        id = R.string.date_range,
        getLocalDateString(date = range.first.atStartOfDay(ZoneId.systemDefault())),
        getLocalDateString(date = range.second.atStartOfDay(ZoneId.systemDefault()))
    )
}

fun isSameDay(date1: LocalDate, date2: LocalDate): Boolean {
    return date1.isEqual(date2)
}

fun getLastDestination(trip: HafasTrip): String {
    val lastDestination = clarifyRingbahnBerlin(trip)

    return lastDestination.ifBlank {
        trip.direction ?: (trip.destination?.name ?: "")
    }
}

fun getSwitzerlandLineName(productName: String, lineId: String): Pair<AnnotatedString?, Map<String, InlineTextContent>> {
    // Switzerland lines start with 85 in the second block of line id
    val match = "\\w+-85\\w*-(\\w+)\$".toRegex().find(lineId)
    if (match != null) {
        val inlineTextContent = mutableMapOf<String, InlineTextContent>()
        val builder = AnnotatedString.Builder()

        val icon = when(productName) {
            "IR" -> R.drawable.ic_ch_ir
            else -> null
        }

        if (icon == null) {
            builder.append(productName)
        } else {
            builder.appendInlineContent("product", productName)
            inlineTextContent["product"] = InlineTextContent(
                Placeholder(28.sp, 12.sp, PlaceholderVerticalAlign.Center)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
                //Box(modifier = Modifier.fillMaxSize().background(color = Color.Green))
            }
        }
        builder.append(" ")
        builder.append(match.groupValues.getOrNull(1)?.uppercase() ?: "")
        return Pair(builder.toAnnotatedString(), inlineTextContent)
    }
    return Pair(null, mapOf())
}

private fun clarifyRingbahnBerlin(trip: HafasTrip): String {
    if (trip.line == null || trip.direction == null || trip.line.operator == null)
        return ""

    if (trip.line.operator.id == "s-bahn-berlin" && trip.direction.contains("Ring")) {
        return trip.direction.replace("S41", "↻")
            .replace("S42", "↺")
    }

    return ""
}

@Composable
fun getJwtExpiration(jwt: String): String {
    var expiresAt = ZonedDateTime.now()
    if (jwt != "") {
        try {
            expiresAt = ZonedDateTime
                .ofInstant(
                    JWT(jwt).expiresAt?.toInstant() ?: Instant.now(),
                    ZoneId.systemDefault()
                )
        } catch (_: Exception) {
        }
    }
    return getLocalDateTimeString(expiresAt)
}

fun getStationNameWithRL100(station: Station): String =
    station.name.let {
        var stationName = it
        if (station.ds100 != null) {
            stationName = stationName.plus(" [${station.ds100}]")
        }
        stationName
    }

@Composable
fun getGreeting(): String {
    val time = LocalDateTime.now()

    return when (time.hour) {
        in 5..11 -> stringResource(id = R.string.greeting_morning)
        in 12..17 -> stringResource(id = R.string.greeting_day)
        in 18..22 -> stringResource(id = R.string.greeting_evening)
        23, in 0..4 -> stringResource(id = R.string.greeting_night)
        else -> "WTF"
    }
}
