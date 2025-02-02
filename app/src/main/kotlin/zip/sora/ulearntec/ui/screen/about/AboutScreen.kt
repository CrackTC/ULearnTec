package zip.sora.ulearntec.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zip.sora.ulearntec.BuildConfig
import zip.sora.ulearntec.R
import zip.sora.ulearntec.ui.theme.ULearnTecTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackButtonClicked: () -> Unit,
    onLicenseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val urlHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onBackButtonClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_monochrome),
                contentDescription = null
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Tag,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = "Version", style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    Text(text = BuildConfig.VERSION_NAME)
                }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.github_mark),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                headlineContent = {
                    Text(text = stringResource(R.string.project_url))
                },
                supportingContent = {
                    Text(text = BuildConfig.REPO_URL)
                },
                modifier = Modifier.clickable { urlHandler.openUri(BuildConfig.REPO_URL) }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Balance,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = stringResource(R.string.license))
                },
                modifier = Modifier.clickable(onClick = onLicenseClicked)
            )
        }
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    ULearnTecTheme {
        AboutScreen(
            onBackButtonClicked = {},
            onLicenseClicked = {}
        )
    }
}