package com.example.playground.testing.util

/**
 * Extension functions and Binding Adapters
 */
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.playground.R
import com.example.playground.testing.ScrollChildSwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar

/**
 * Transforms static java Snackbar.make() to an extension function on View
 */
fun View.showSnackbar(text: String, timeLength: Int) {
    Snackbar.make(this, text, timeLength).run {
        show()
    }
}

/**
 * Triggers a snackbar message when the value contained by
 * snackbarTaskMessageLiveEvent is modified
 */
fun View.setupSnackbar(
    lifecycleOwner: LifecycleOwner,
    snackbarEvent: LiveData<TodoEvent<Int>>,
    timeLenght: Int
) {
    snackbarEvent.observe(lifecycleOwner, Observer { event ->
        event.getContentIfNotHandled()?.let {
            showSnackbar(context.getString(it), timeLenght)
        }
    })
}

fun Fragment.setupRefreshLayout(
    refreshLayout: ScrollChildSwipeRefreshLayout,
    scrollUpChild: View? = null
) {
    refreshLayout.setColorSchemeColors(
        ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
        ContextCompat.getColor(requireActivity(), R.color.colorAccent),
        ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark)
    )
    // Set the scrolling view in the custom SwipeRefreshLayout
    scrollUpChild?.let {
        refreshLayout.scrollUpChild = it
    }
}