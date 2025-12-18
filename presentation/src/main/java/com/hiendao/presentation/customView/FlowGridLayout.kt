package com.hiendao.presentation.customView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FlowGridLayout(
    modifier: Modifier = Modifier,
    items: List<T>,
    itemsInARow: Int,
    itemContent: @Composable (T) -> Unit
) {
    FlowRow(
        modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.SpaceBetween,
        maxItemsInEachRow = itemsInARow
    ) {
        items.forEach {
            Box(Modifier.weight(1f)){
                itemContent(it)
            }
        }
        // add empty feature to fill last row if row is not filled
        val totalItems = items.size
        val remainder = totalItems % itemsInARow
        if (remainder != 0) {
            val itemsToAdd = itemsInARow - remainder
            repeat(itemsToAdd) {
                Spacer(Modifier.weight(1f).height(0.dp)) // Add a Spacer as a placeholder
            }
        }
    }
}