package com.hiendao.presentation.home.autoSwipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.hiendao.domain.model.Book
import com.hiendao.presentation.appRoute.Routes

@Composable
fun AutoSwipeSection(
    modifier: Modifier = Modifier,
    sectionType: String,
    listMedia: List<Book>,
    onBookClick: (Book) -> Unit = {}
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = sectionType,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        AutoSwipePager(
            books = listMedia.take(7),
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth(),
            navigate = { book ->
                onBookClick.invoke(book)
            }
        )
    }
}