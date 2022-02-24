import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.analyticsplayground.AnalyticsComposeView

fun Modifier.trackScrollableContent(
    uiModel: AnalyticsComposeView,
    shown: (Any) -> Unit,
    fullyShown: (Any) -> Unit = {},
    resetShown: (Any) -> Unit = {},
    resetFullyShown: (Any) -> Unit = {}
): Modifier {
    return then(onGloballyPositioned {
        val bounds = it.boundsInWindow()
        val relativePosition = it.positionInWindow()
        val top = bounds.top - relativePosition.y
        val bottom = bounds.bottom + relativePosition.y
        val left = bounds.left - relativePosition.x
        val right = bounds.right + relativePosition.x

        // top == 0 means that the item is visible. when the item is not visible anymore, top is different from zero
        if (!uiModel.shown && top == 0f && left == 0f) {
            shown(uiModel.analyticKey)
        }

        // reset shown here if the view is partially visible and was previously shown
        if (uiModel.shown && top != 0f) {
            resetShown(uiModel.analyticKey)
        }

        if (!uiModel.fullyShown && bottom == it.size.height.toFloat() && right == it.size.width.toFloat()) {
            fullyShown(uiModel.analyticKey)
        }

        if (uiModel.fullyShown && bottom < it.size.height.toFloat()) {
            resetFullyShown(uiModel.analyticKey)
        }
    })
}

fun Modifier.trackHorizontallyScrollableContent(
    uiModel: AnalyticsComposeView,
    shown: (Any) -> Unit,
    resetShown: (Any) -> Unit = {},
): Modifier {
    return then(onGloballyPositioned {
        val bounds = it.boundsInWindow()
        val relativePosition = it.positionInWindow()

        val top = bounds.top - relativePosition.y
        val left = bounds.left - relativePosition.x

        if (!uiModel.shown && top == 0f && left == 0f) {
            shown(uiModel.analyticKey)
        }
        if (uiModel.shown && left != 0f) {
            resetShown(uiModel.analyticKey)
        }
    })
}

fun Modifier.trackVisibility(
    key: Any = Unit,
    index: Int,
    onWindowInfoUpdated: (String) -> Unit = {},
    onViewShownFully: () -> Unit = {},
    onViewShown: () -> Unit,
): Modifier {
    return then(
        composed {
            var shown by rememberSaveable { mutableStateOf(false) }
            var shownFully by rememberSaveable { mutableStateOf(false) }

            var layoutCoordinates: LayoutCoordinates? = remember { null }

            val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

            fun checkViewShown() {
                layoutCoordinates?.let {
                    val bounds: Rect = it.boundsInWindow()
                    val relativePosition: Offset = it.positionInWindow()

                    val left = bounds.left - relativePosition.x
                    val top = bounds.top - relativePosition.y

                    val isBottomVisible = (relativePosition.y + it.size.height) <= bounds.bottom
                    val isRightVisible = (relativePosition.x + it.size.width) <= bounds.right

                    val isShown = top == 0f && left == 0f
                    val isShownFully = isBottomVisible && isRightVisible

                    onWindowInfoUpdated(
                        "bounds: $bounds \n relativePosition: $relativePosition" +
                                "\nleft: $left | top: $top" +
                                "\nisBottomVisible: $isBottomVisible" +
                                "\n(relativePosition.y + it.size.height): ${(relativePosition.y + it.size.height)}" +
                                "\nbounds.bottom: ${bounds.bottom}\n" +
                                "\nisRightVisible: $isRightVisible" +
                                "\n(relativePosition.x + it.size.width): ${(relativePosition.x + it.size.width)}" +
                                "\nbounds.bottom: ${bounds.right}\n" +
                                "\nisShown: $isShown | isShownFully: $isShownFully"
                    )

                    // top == 0 means that the item is visible. when the item is not visible anymore, top is different from zero
                    if (!shown && isShown) {
                        shown = true
                        onViewShown()
                    }

                    // reset shown here if the view is partially visible and was previously shown
                    if (shown && (top != 0f || left != 0f)) {
                        shown = false
                    }

                    // shownFully means the bottom right corner and top left were both shown
                    if (!shownFully && isShownFully) {
                        shownFully = true
                        onViewShownFully()
                    }

                    // reset shownFully here if the view is partially visible and was previously shown
                    if (shownFully && (!isBottomVisible || !isRightVisible)) {
                        shownFully = false
                    }
                }
            }

            DisposableEffect(key1 = lifecycleOwner.value) {
                val lifecycle = lifecycleOwner.value.lifecycle
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        shown = false
                        shownFully = false

                        checkViewShown()
                    }
                }

                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            onGloballyPositioned {
                layoutCoordinates = it

                checkViewShown()
            }
        }
    )
}