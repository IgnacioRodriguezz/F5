package com.rodriguez.f5.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.rodriguez.f5.F5Application
import com.rodriguez.f5.di.AppContainer
import com.rodriguez.f5.ui.theme.F5Theme

/**
 * Base for the tabs whose content is written in Compose.
 *
 * The assignment asks for Fragments as the main structure and for Compose as
 * the UI toolkit, so each tab is a Fragment that hosts a [ComposeView]. This
 * class holds that bridge once instead of repeating it in every tab.
 */
abstract class ComposeFragment : Fragment() {

    val container: AppContainer
        get() = (requireActivity().application as F5Application).container

    @Composable
    protected abstract fun Content()

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        // Ties the composition to the fragment's view lifecycle, which avoids
        // leaking it when the fragment's view is destroyed but the fragment is not.
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            // this@ComposeFragment matters: inside apply the receiver is the
            // ComposeView, which has its own abstract Content(), and calling
            // that one makes the view render itself forever.
            F5Theme { this@ComposeFragment.Content() }
        }
    }
}
