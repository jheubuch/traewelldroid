package de.hbch.traewelling.ui.wrapped

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.BTModern
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading

@Composable
fun WrappedTeaser(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.year_is_over, 2024),
                fontFamily = BTModern,
                style = LocalFont.current.headlineSmall,
                color = LocalColorScheme.current.primary
            )
            Text(
                text = stringResource(R.string.discover_recap, 2024),
                style = LocalFont.current.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonWithIconAndText(
                    stringId = R.string.take_me_back,
                    drawableId = R.drawable.ic_arrow_right,
                    onClick = {
                        context.startActivity(Intent(context, WrappedActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun WrappedIsBeingPrepared(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DataLoading()
        Text(
            text = stringResource(R.string.wrapped_is_being_prepared),
            textAlign = TextAlign.Center
        )
    }
}
