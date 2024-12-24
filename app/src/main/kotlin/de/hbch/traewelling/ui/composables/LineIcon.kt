package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.api.models.lineIcons.LineIconShape
import de.hbch.traewelling.shared.HexagonShape
import de.hbch.traewelling.shared.LineIcons
import de.hbch.traewelling.shared.SettingsViewModel
import de.hbch.traewelling.shared.TrapezoidShape
import de.hbch.traewelling.theme.LineIconStyle
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.util.getSwitzerlandLineName

@Composable
fun LineIcon(
    lineName: String,
    journeyNumber: Int?,
    modifier: Modifier = Modifier,
    operatorCode: String? = null,
    lineId: String? = null,
    defaultTextStyle: TextStyle = LocalFont.current.bodyMedium,
    displayJourneyNumber: Boolean = true
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = context as ViewModelStoreOwner
    )
    val displayJourneyNumberSetting by settingsViewModel.displayJourneyNumber.observeAsState(true)

    val opCode = operatorCode?.replace("nahreisezug", "") ?: ""

    val lineIcon = LineIcons.getInstance().icons.firstOrNull {
        it.lineId == lineId
                && it.operatorCode == opCode
    }

    val shape: Shape = when (lineIcon?.shape) {
        LineIconShape.hexagon -> HexagonShape()
        LineIconShape.pill -> RoundedCornerShape(percent = 50)
        LineIconShape.rectangle_rounded_corner -> RoundedCornerShape(percent = 20)
        LineIconShape.trapezoid -> TrapezoidShape()
        else -> RectangleShape
    }
    val borderColor: Color = lineIcon?.getBorderColor() ?: Color.Transparent

    val switzerlandString = getSwitzerlandLineName(
        lineId = lineId ?: "",
        productName = lineName.split(" ").getOrElse(0) { "" }
    )

    val displayedName =
        lineIcon?.displayedName ?: switzerlandString?.first?.text ?: lineName

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lineIcon != null) {
            Box(
                modifier = Modifier
                    .widthIn(48.dp, 144.dp)
                    .background(
                        color = lineIcon.getBackgroundColor(),
                        shape = shape
                    )
                    .border(2.dp, borderColor, shape)
                    .padding(2.dp)
            ) {
                Text(
                    text = displayedName,
                    modifier = Modifier.align(Alignment.Center),
                    color = lineIcon.getTextColor(),
                    style = LineIconStyle,
                    fontWeight = FontWeight.Bold
                )
            }
        }  else if (switzerlandString?.first != null) {
            Box(
                modifier = Modifier
                    .widthIn(48.dp, 144.dp)
                    .background(
                        color = Color.Red
                    )
                    .padding(2.dp)
            ) {
                Text(
                    text = switzerlandString.first!!,
                    color = Color.White,
                    style = LineIconStyle,
                    fontWeight = FontWeight.Bold,
                    inlineContent = switzerlandString.second,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            Text(
                text = displayedName,
                modifier = modifier,
                style = defaultTextStyle,
                fontWeight = FontWeight.ExtraBold
            )
        }
        if (displayJourneyNumber
            && displayJourneyNumberSetting
            && !displayedName.contains(journeyNumber.toString())
            && journeyNumber != null && journeyNumber != 0) {
            Text(
                text = "($journeyNumber)",
                style = LocalFont.current.bodySmall
            )
        }
    }
}

@Composable
fun LineIconView(
    lineName: String,
    modifier: Modifier = Modifier,
    operatorCode: String? = null,
    lineId: String? = null,
    defaultTextStyle: TextStyle = LocalFont.current.bodyMedium
) {
    val opCode = operatorCode?.replace("nahreisezug", "") ?: ""

    val lineIcon = LineIcons.getInstance().icons.firstOrNull {
        it.lineId == lineId
                && it.operatorCode == opCode
    }

    val shape: Shape = when (lineIcon?.shape) {
        LineIconShape.pill -> RoundedCornerShape(percent = 50)
        LineIconShape.rectangle_rounded_corner -> RoundedCornerShape(percent = 20)
        else -> RectangleShape
    }
    val borderColor: Color = lineIcon?.getBorderColor() ?: Color.Transparent

    val switzerlandString = getSwitzerlandLineName(
        lineId = lineId ?: "",
        productName = lineName.split(" ").getOrElse(0) { "" }
    )

    val displayedName =
        lineIcon?.displayedName ?: switzerlandString?.first?.text ?: lineName

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lineIcon != null) {
            Box(
                modifier = Modifier
                    .widthIn(48.dp, 144.dp)
                    .background(
                        color = lineIcon.getBackgroundColor(),
                        shape = shape
                    )
                    .border(2.dp, borderColor, shape)
                    .padding(2.dp)
            ) {
                Text(
                    text = displayedName,
                    modifier = Modifier.align(Alignment.Center),
                    color = lineIcon.getTextColor(),
                    style = LineIconStyle,
                    fontWeight = FontWeight.Bold
                )
            }
        }  else if (switzerlandString?.first != null) {
            Box(
                modifier = Modifier
                    .widthIn(48.dp, 144.dp)
                    .background(
                        color = Color.Red
                    )
                    .padding(2.dp)
            ) {
                Text(
                    text = switzerlandString.first!!,
                    color = Color.White,
                    style = LineIconStyle,
                    fontWeight = FontWeight.Bold,
                    inlineContent = switzerlandString.second,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            Text(
                text = displayedName,
                modifier = modifier,
                style = defaultTextStyle,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
