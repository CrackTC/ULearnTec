package zip.sora.ulearntec.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zip.sora.ulearntec.R
import zip.sora.ulearntec.ui.component.LoadingButton
import zip.sora.ulearntec.ui.theme.ULearnTecTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onSubmit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.login)) }) }
    ) { innerPadding ->
        CredentialForm(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            loginState = uiState,
            onSubmit = onSubmit
        )
    }
}

@Composable
private fun CredentialForm(
    modifier: Modifier = Modifier,
    loginState: LoginUiState,
    onSubmit: (String, String) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showUsernameHint by rememberSaveable { mutableStateOf(false) }
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_visibility)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically
        ),
        modifier = modifier
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        OutlinedTextField(
            value = username,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = null)
            },
            onValueChange = { username = it },
            label = { Text(text = stringResource(R.string.username)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.None
            ),
            supportingText = {
                AnimatedVisibility(visible = showUsernameHint) {
                    Text(text = stringResource(R.string.username_hint))
                }
            },
            isError = loginState is LoginUiState.Error,
            enabled = loginState !is LoginUiState.Loading,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .onFocusChanged { showUsernameHint = it.hasFocus }
                .semantics { contentType = ContentType.Username }
        )

        OutlinedTextField(
            value = password,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Image(
                        painter = rememberAnimatedVectorPainter(image, showPassword),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                }
            },
            onValueChange = { password = it },
            label = { Text(text = stringResource(R.string.password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = { onSubmit(username, password) }
            ),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = loginState is LoginUiState.Error,
            enabled = loginState !is LoginUiState.Loading,
            supportingText = {
                if (loginState is LoginUiState.Error) {
                    Text(
                        text = loginState.message(LocalContext.current),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .semantics { contentType = ContentType.Password }
        )

        LoadingButton(
            text = stringResource(R.string.login),
            onClick = { onSubmit(username, password) },
            isLoading = loginState is LoginUiState.Loading,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    ULearnTecTheme {
        LoginScreen(
            uiState = LoginUiState.Normal,
            onSubmit = { _, _ -> }
        )
    }
}