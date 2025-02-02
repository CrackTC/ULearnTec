package zip.sora.ulearntec.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.model.User
import zip.sora.ulearntec.ui.component.ClickableListEntry
import zip.sora.ulearntec.ui.component.DetailEntry
import zip.sora.ulearntec.ui.component.DetailSheet
import zip.sora.ulearntec.ui.mockUser
import zip.sora.ulearntec.ui.theme.ULearnTecTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    uiState: MoreUiState,
    onRefresh: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAboutClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAccountInfoSheet by rememberSaveable { mutableStateOf(false) }
    val user = uiState.user

    if (showAccountInfoSheet && user != null) {
        DetailSheet(
            title = R.string.account_information,
            entries = listOf(
                DetailEntry(
                    name = R.string.student_id,
                    value = user.studentId.toString(),
                    icon = Icons.Filled.Fingerprint
                ),
                DetailEntry(
                    name = R.string.student_no,
                    value = user.studentNo,
                    icon = Icons.Filled.Tag
                ),
                DetailEntry(
                    name = R.string.student_name,
                    value = user.studentName,
                    icon = Icons.Outlined.Badge
                ),
                DetailEntry(
                    name = R.string.school_name,
                    value = user.schoolName,
                    icon = Icons.Outlined.School
                ),
                DetailEntry(
                    name = R.string.username,
                    value = user.userName,
                    icon = Icons.Outlined.Person
                ),
                DetailEntry(
                    name = R.string.avatar,
                    value = user.avatar,
                    icon = Icons.Outlined.Camera,
                    isLink = true
                ),
                DetailEntry(
                    name = R.string.member_id,
                    value = user.memberId,
                    icon = Icons.Outlined.Groups
                )
            ),
            onDismissRequest = { showAccountInfoSheet = false }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.more)) }) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState is MoreUiState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (user != null) {
                        AccountCard(
                            user = user,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ClickableListEntry(
                        onClick = { showAccountInfoSheet = true },
                        icon = Icons.Filled.AccountBox
                    ) {
                        Text(text = stringResource(R.string.account_information))
                    }
                    ClickableListEntry(
                        onClick = onSettingsClicked,
                        icon = Icons.Filled.Settings
                    ) {
                        Text(text = stringResource(R.string.settings))
                    }
                    ClickableListEntry(
                        onClick = onAboutClicked,
                        icon = Icons.Filled.Info
                    ) {
                        Text(text = stringResource(R.string.about))
                    }
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Button(
                    onClick = onLogoutClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

}

@Composable
private fun AccountCard(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.avatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Column {
                Text(text = user.studentName, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = user.msg, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
private fun MoreScreenPreview() {
    ULearnTecTheme {
        MoreScreen(
            uiState = MoreUiState.Success(user = mockUser),
            onRefresh = {},
            onSettingsClicked = {},
            onAboutClicked = {},
            onLogoutClicked = {}
        )
    }
}