package com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import java.time.LocalDate

@Composable
fun NewsCard(modifier: Modifier = Modifier, newsItem: News) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${newsItem.date.dayOfMonth}",
                    style = MaterialTheme.typography.displayMedium.copy(color = Color.Red)
                )
                Text(text = newsItem.date.month.name, style = MaterialTheme.typography.bodyMedium)
            }
            Column(
                modifier = Modifier.weight(3f)
            ) {
                Text(text = newsItem.headline, style = MaterialTheme.typography.headlineLarge)
                Text(newsItem.author, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    painter = painterResource(id = if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more),
                    contentDescription = if (expanded) {
                        "Show less"
                    } else {
                        "Show more"
                    }
                )
            }
        }
        if (expanded) {
            Text(
                text = newsItem.text,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewNewsCard() {
    WalkingForRochesterTheme {
        NewsCard(
            newsItem = News(
                id = 1,
                headline = "Welcome!",
                text = stringResource(id = R.string.lorem_ipsum),
                author = "John Smith",
                date = LocalDate.parse("2022-02-24")
            )
        )
    }
}