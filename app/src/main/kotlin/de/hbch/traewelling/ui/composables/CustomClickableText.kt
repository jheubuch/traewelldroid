package de.hbch.traewelling.ui.composables

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

@Composable
fun CustomClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onClick: (Int) -> Unit = { },
    inlineContent: Map<String, InlineTextContent> = mapOf()
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                layoutResult?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(offset))
                }
            }
        },
        text = text,
        onTextLayout = { layoutResult = it },
        style = style,
        inlineContent = inlineContent
    )
}
