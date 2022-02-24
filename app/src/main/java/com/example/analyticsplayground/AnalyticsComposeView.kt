package com.example.analyticsplayground

interface AnalyticsComposeView {
    val analyticKey: Any
    var shown: Boolean
    var fullyShown: Boolean
}

interface ItemShownActions<T> {
    fun resetVisibility(visibleItems: List<Int>) {}
    fun setViewToShown(itemUiState: T) {}
    fun setViewToFullyShown(itemUiState: T) {}
    fun resetViewVisibility(itemUiState: T) {}
    fun resetViewFullVisibility(itemUiState: T) {}
    fun fireShownEvent(itemUiState: T) {}
    fun fireFullyShownEvent(itemUiState: T) {}
}