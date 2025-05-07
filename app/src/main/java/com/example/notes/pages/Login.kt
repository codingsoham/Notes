package com.example.notes.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.AuthState
import com.example.notes.AuthViewModel
import com.example.notes.R

@Composable
fun LoginScreen(onForgotClick: ()->Unit={},onSignUpClick: () ->Unit={},onLoginSuccess:()->Unit={},authViewModel: AuthViewModel=viewModel(),modifier: Modifier=Modifier){
    val authUiState by authViewModel.uiState.collectAsState()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> onLoginSuccess()
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT)
                .show()
            else -> Unit
        }
    }
    Box(modifier= Modifier.fillMaxSize(),contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 500.dp)
                .fillMaxSize()
                .padding(26.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.sign_in_your_account),
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )


            OutlinedTextField(
                value = authUiState.email,
                onValueChange = { authViewModel.setEmail(it) },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = authUiState.password,
                onValueChange = { authViewModel.setPassword(it) },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(50),
                visualTransformation = if (authUiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (!authUiState.passwordVisible)
                        painterResource(R.drawable.visibility)
                    else
                        painterResource(R.drawable.visibility_off)
                    val description = if (!authUiState.passwordVisible)
                        stringResource(R.string.hide_password)
                    else
                        stringResource(R.string.show_password)

                    Icon(
                        painter = image,
                        contentDescription = description,
                        modifier = Modifier.clickable(onClick = { authViewModel.togglePasswordVisibility() })
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { authViewModel.login(authUiState.email, authUiState.password) }
                )
            )
            Text(
                text = stringResource(R.string.forgot_password),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable(onClick = {
                        onForgotClick()
                    })
            )
            Button(
                onClick = { authViewModel.login(authUiState.email, authUiState.password) },
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 20.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = authState.value != AuthState.Loading,
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    style = typography.titleMedium,
                )
            }
            Text(
                text = stringResource(R.string.or_sign_in_with)
            )
            Row(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 20.dp, start = 75.dp, end = 75.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(60.dp)) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier
                            .size(50.dp)
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(60.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.apple),
                        contentDescription = "Apple",
                        modifier = Modifier
                            .size(40.dp)
                    )
                }

                IconButton(onClick = {}, modifier = Modifier.size(60.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.x),
                        contentDescription = "X",
                        modifier = Modifier
                            .size(35.dp)
                    )
                }

            }
            Text(
                text = stringResource(R.string.don_t_have_an_account_sign_up),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable(onClick = { onSignUpClick() })
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
