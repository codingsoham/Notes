package com.example.notes.pages



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.AuthState
import com.example.notes.AuthViewModel
import com.example.notes.R


@Composable
fun GetStarted(authViewModel: AuthViewModel=viewModel(),onGetStartedClick:()->Unit={},modifier: Modifier=Modifier) {
    Column(
        modifier=Modifier.fillMaxSize().padding(16.dp).navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Image(
            painter = painterResource(R.drawable.notes),
            contentDescription = null,
            modifier=Modifier.size(150.dp).weight(0.7f)
        )
        Column(
            modifier = Modifier.weight(0.2f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = stringResource(R.string.capture_anything),
                modifier = Modifier.padding(bottom = 20.dp),
                style = typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.get_started_text),
                textAlign = TextAlign.Center,
                style = typography.bodyMedium
            )
            Button(
                onClick = {onGetStartedClick()},
                modifier = Modifier.padding(top= 20.dp,bottom = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.get_started)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedPreview() {
    GetStarted()
}