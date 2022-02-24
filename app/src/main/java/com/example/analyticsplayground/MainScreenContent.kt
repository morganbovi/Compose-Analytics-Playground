package com.example.analyticsplayground

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import trackVisibility

data class ItemState(val index: Int) {
    val text = "This is item: $index"
    val backgroundIsDark = index.mod(2) == 0
}

@Composable
fun MainScreenContent() {
    val scrollState = rememberLazyListState()

    val firstItem =
        remember(key1 = scrollState.layoutInfo) { scrollState.layoutInfo.visibleItemsInfo.firstOrNull() }
    val lastItem =
        remember(key1 = scrollState.layoutInfo) { scrollState.layoutInfo.visibleItemsInfo.lastOrNull() }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Magenta.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "First Viz Item ${scrollState.firstVisibleItemIndex}")
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "First Viz Item Scroll Off ${scrollState.firstVisibleItemScrollOffset}")

            Spacer(modifier = Modifier.height(8.dp))

            with(scrollState.layoutInfo) {
                Text(text = "LayoutInfo")
                Text(text = "visibleItemsInfo.size ${visibleItemsInfo.size}")
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "viewportSize $viewportSize")
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "viewportStartOffset $viewportStartOffset")
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "viewportEndOffset $viewportEndOffset")
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (firstItem != null && lastItem != null) {
                Spacer(modifier = Modifier.height(8.dp))
                VisibleInfoLayout(visibleInfo = firstItem)
                Spacer(modifier = Modifier.height(8.dp))
                VisibleInfoLayout(visibleInfo = lastItem)
                Spacer(modifier = Modifier.height(8.dp))
            }


        }

        Divider(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth(),
            color = Color.Black
        )

        LazyColumn(state = scrollState) {
            val list = mutableListOf<ItemState>().apply {
                repeat(100) {
                    add(ItemState(it))
                }
            }

            itemsIndexed(list) { index, item ->
                val visibleItemsRange = remember(scrollState.layoutInfo) {
                    (firstItem?.index ?: 0)..(lastItem?.index ?: 0)
                }
                val isItemVisible = remember(visibleItemsRange) { index !in visibleItemsRange }

                val sendIf = index == 0 || true

                Box(
                    modifier = Modifier
                        .background(if (item.backgroundIsDark) Color.Black.copy(alpha = 0.06f) else Color.White)
                        .padding(16.dp)
                        .trackVisibility(
                            onViewShown = {
                                if (sendIf)
                                    log("Item Container shown: ${index}")
                            },
                            onViewShownFully = {
                                if (sendIf)
                                    log("Item Container shownFully: ${index}")
                            },
                        )
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        var itemWindowInfo by remember { mutableStateOf("") }

                        Text(
                            text = item.text + "     Realtime Index: $index",
                            Modifier
                                .fillMaxWidth()
                                .trackVisibility(
                                    key = isItemVisible,
                                    index = index,
                                    onWindowInfoUpdated = { itemWindowInfo = it },
                                    onViewShown = {
                                        if (sendIf)
                                            log("text view shown: ${index}")
                                    },
                                    onViewShownFully = {
                                        if (sendIf)
                                            log("text view shownFully: ${index}")
                                    },
                                )
                        )
                        if (itemWindowInfo.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = itemWindowInfo,
                                modifier = Modifier.trackVisibility(
                                    onViewShown = {
                                        if (sendIf)
                                            log("Analytics Data Text shown: ${index}")
                                    },
                                    onViewShownFully = {
                                        if (sendIf)
                                            log("Analytics Data Text shownFully: ${index}")
                                    },
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisibleInfoLayout(visibleInfo: LazyListItemInfo) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        with(visibleInfo) {
            Text(text = "Visible Item $index")
            Text(text = "key $key")
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "offset $offset")
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "size $size")
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

fun Any.log(message: String) {
    Log.e(this::class.java.simpleName, message)
}