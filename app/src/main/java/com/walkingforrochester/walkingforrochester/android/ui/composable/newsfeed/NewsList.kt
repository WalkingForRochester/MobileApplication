package com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import java.time.LocalDate

@Composable
fun NewsList(
    modifier: Modifier = Modifier, news: List<News> = List(3) { index ->
        News(
            index.toLong(),
            text = stringResource(R.string.lorem_ipsum),
            headline = "Update №$index",
            author = "John Smith",
            date = LocalDate.now()
        )
    }
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LocalDate.now()
        items(news) { item ->
            NewsCard(newsItem = item)
        }
    }
}

data class News(
    val id: Long,
    val headline: String,
    val text: String,
    val date: LocalDate,
    val author: String
)
