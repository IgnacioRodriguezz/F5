package com.rodriguez.f5.ui.xml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.rodriguez.f5.R
import com.rodriguez.f5.databinding.ActivityMainBinding

/**
 * Single activity of the app.
 *
 * Hosts the four bottom navigation tabs as fragments: one built with XML
 * views and three hosting Compose content.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.inflateMenu(R.menu.bottom_nav)
        binding.bottomNav.setOnItemSelectedListener { item ->
            show(fragmentFor(item.itemId))
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.tab_competitions
        }
    }

    private fun fragmentFor(itemId: Int): Fragment = when (itemId) {
        R.id.tab_today -> com.rodriguez.f5.ui.xml.today.TodayFragment()
        R.id.tab_favorites -> FavoritesFragment()
        R.id.tab_search -> SearchFragment()
        else -> CompetitionsFragment()
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.navHost, fragment)
        }
    }
}
