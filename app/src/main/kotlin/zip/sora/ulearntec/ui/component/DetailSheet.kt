package zip.sora.ulearntec.ui.component

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import zip.sora.ulearntec.R

data class DetailEntry(
    @StringRes val name: Int,
    val value: String,
    val icon: ImageVector,
    val isLink: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(
    @StringRes title: Int,
    entries: List<DetailEntry>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        tonalElevation = 3.dp,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                entries.forEach { entry ->
                    InfoEntry(
                        icon = entry.icon,
                        overlineText = stringResource(entry.name),
                        headlineText = entry.value,
                        isLink = entry.isLink
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoEntry(
    icon: ImageVector,
    overlineText: String,
    headlineText: String,
    isLink: Boolean = false,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        overlineContent = {
            Text(
                text = overlineText,
                color = MaterialTheme.colorScheme.outline
            )
        },
        headlineContent = {
            if (isLink) {
                Text(text = buildAnnotatedString {
                    withLink(
                        LinkAnnotation.Url(
                            url = headlineText,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        )
                    ) {
                        append(headlineText)
                    }
                })
            } else {
                Text(text = headlineText)
            }
        },
        trailingContent = {
            IconButton(
                onClick = {
                    clipboardManager.setText(buildAnnotatedString { append(headlineText) })
                    Toast.makeText(context, R.string.text_copied, Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null
                )
            }
        },
        modifier = modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}