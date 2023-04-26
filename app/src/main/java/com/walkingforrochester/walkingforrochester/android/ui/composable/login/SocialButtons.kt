package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRButton
import com.walkingforrochester.walkingforrochester.android.ui.theme.FacebookBlue

@Composable
fun SocialLoginButtons(
    modifier: Modifier = Modifier,
    onContinueWithGoogle: () -> Unit,
    onContinueWithFacebook: () -> Unit
) {
    Column(modifier = modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GoogleLoginButton(modifier = Modifier.fillMaxWidth(), onClick = onContinueWithGoogle)
        FacebookLoginButton(modifier = Modifier.fillMaxWidth(), onClick = onContinueWithFacebook)
    }
}

@Composable
fun GoogleLoginButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    WFRButton(
        icon = R.drawable.ic_google,
        label = R.string.continue_with_google,
        buttonColor = Color.White,
        labelColor = Color.Black,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun FacebookLoginButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    WFRButton(
        icon = R.drawable.ic_facebook,
        label = R.string.continue_with_facebook,
        buttonColor = FacebookBlue,
        labelColor = Color.White,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewFacebook() {
    FacebookLoginButton(onClick = { /*TODO*/ })
}

@Preview
@Composable
fun PreviewGoogle() {
    GoogleLoginButton(onClick = { /*TODO*/ })
}

@Preview
@Composable
fun PreviewSocialLoginButtons() {
    SocialLoginButtons(onContinueWithGoogle = { /*TODO*/ }) {

    }
}