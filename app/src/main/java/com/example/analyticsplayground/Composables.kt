package com.example.analyticsplayground

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect

@Composable
fun LazyListAnalyticsFlow(scrollState: LazyListState, resetVisibility: (List<Int>) -> Unit) {
    LaunchedEffect(Unit) {
        snapshotFlow {
            scrollState.layoutInfo.visibleItemsInfo.map { it.index }
        }.collect {
            if (it.isNotEmpty()) {
                resetVisibility(it)
            }
        }
    }
}