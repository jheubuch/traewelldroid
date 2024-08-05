package de.hbch.traewelling.ui.include.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.ui.composables.LineIcon
import de.hbch.traewelling.ui.user.getDurationString
import java.time.Duration
import java.time.ZonedDateTime

@Composable
fun ActiveStatusBar(
    status: Status?,
    modifier: Modifier = Modifier
) {
    if (status != null) {
        val progress = calculateProgress(
            from = status.journey.departureManual ?: status.journey.origin.departureReal
            ?: status.journey.origin.departurePlanned,
            to = status.journey.arrivalManual ?: status.journey.destination.arrivalReal
            ?: status.journey.destination.arrivalPlanned
        )
        val duration = Duration.between(
            status.journey.destination.arrivalReal ?: status.journey.destination.arrivalPlanned,
            ZonedDateTime.now()
        ).toMinutes() * -1
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LineIcon(
                        lineName = status.journey.line,
                        journeyNumber = null,
                        lineId = status.journey.lineId,
                        operatorCode = status.journey.operator?.id
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null
                    )
                    Text(
                        text = status.journey.destination.name
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "noch ${getDurationString(duration.toInt())}"
                    )
                    Box(
                        modifier = Modifier.background(Color.Blue)
                    ) {
                        Text(
                            text = "Gl. ${status.journey.destination.arrivalPlatformReal}",
                            modifier = Modifier.padding(2.dp),
                            color = Color.White
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
