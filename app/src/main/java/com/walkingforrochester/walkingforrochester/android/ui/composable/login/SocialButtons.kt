package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.FacebookBlue

@Composable
fun SocialLoginButtons(
    onContinueWithGoogle: () -> Unit,
    onContinueWithFacebook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GoogleLoginButton(onClick = onContinueWithGoogle)
        FacebookLoginButton(onClick = onContinueWithFacebook)
    }
}

@Composable
fun GoogleLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SocialButton(
        onClick = onClick,
        label = R.string.continue_with_google,
        icon = R.drawable.ic_google,
        buttonColor = Color.White,
        labelColor = Color.Black,
        modifier = modifier
    )
}

@Composable
fun FacebookLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SocialButton(
        onClick = onClick,
        label = R.string.continue_with_facebook,
        icon = R.drawable.ic_facebook,
        buttonColor = FacebookBlue,
        labelColor = Color.White,
        modifier = modifier
    )
}

@Composable
fun SocialButton(
    onClick: () -> Unit,
    @StringRes label: Int,
    @DrawableRes icon: Int,
    buttonColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,

    ) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = labelColor
        ),
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth(),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding

    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

        Text(text = stringResource(label))
    }
}

@Preview
@Composable
fun PreviewFacebook() {
    FacebookLoginButton(onClick = {})
}

@Preview
@Composable
fun PreviewGoogle() {
    GoogleLoginButton(onClick = {})
}

@Preview
@Composable
fun PreviewSocialLoginButtons() {
    SocialLoginButtons(
        onContinueWithGoogle = {},
        onContinueWithFacebook = {}
    )
}