package com.rodriguez.f5.ui.compose.components

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import com.rodriguez.f5.ui.theme.F5Theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rodriguez.f5.R
import com.squareup.picasso.Picasso

/** The four sizes a crest is drawn at across the app. */
enum class CrestSize(val dp: Dp) {
    TINY(16.dp),
    SMALL(24.dp),
    MEDIUM(40.dp),
    LARGE(72.dp)
}

/**
 * Team badge.
 *
 * The assignment requires Picasso, which is a View based library with no
 * Compose API, so the image is loaded into a plain [ImageView] wrapped in
 * [AndroidView]. That is the interoperability bridge Compose provides for
 * reusing existing views, and it is why this component exists instead of a
 * direct call to an image composable.
 */
@Composable
fun TeamCrest(
    url: String?,
    size: CrestSize,
    modifier: Modifier = Modifier
) {
    // Crests carry their own colours, so they sit on a neutral white plate:
    // a dark outlined badge would disappear against the dark background.
    Box(
        modifier = modifier
            .size(size.dp)
            .background(F5Theme.colors.crestPlate, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .size(size.dp)
                .padding(size.dp * 0.14f),
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                }
            },
            update = { imageView ->
                Picasso.get()
                    .load(url?.takeIf { it.isNotBlank() })
                    .placeholder(R.drawable.ic_crest_placeholder)
                    .error(R.drawable.ic_crest_placeholder)
                    .fit()
                    .centerInside()
                    .into(imageView)
            }
        )
    }
}
