package com.walkingforrochester.walkingforrochester.android.ui.composable.contact

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.BuildConfig
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ktx.safeStartActivity
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun ContactUsScreen(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.padding(horizontal = 20.dp),
    ) {
        val context = LocalContext.current
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.contact_us_text),
            style = MaterialTheme.typography.bodyLarge
        )
        Row(modifier.padding(vertical = 16.dp)) {
            Column(Modifier.padding(end = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.phone_label),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.email_label),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Column {
                Text(
                    text = "585-358-6888",
                    modifier = Modifier
                        .clickable {
                            callOffice(context)
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "info@walkingforrochester.org",
                    modifier = Modifier
                        .clickable {
                            emailUs(context)
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(
                id = R.string.app_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            ),
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

fun callOffice(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:+15853586888")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    context.safeStartActivity(intent)
}

fun emailUs(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = buildUri(context)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    context.safeStartActivity(intent)
}

private fun buildUri(context: Context): Uri {
    val uriString = MailTo.MAILTO_SCHEME +
        Uri.encode("info@walkingforrochester.org") +
        "?subject=" +
        Uri.encode(context.getString(R.string.email_subject))

    return Uri.parse(uriString)
}

@Preview
@Composable
fun ContactUsPreview() {
    WalkingForRochesterTheme {
        Surface {
            ContactUsScreen()
        }
    }
}