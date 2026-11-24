package com.rodriguez.f5.ui.xml.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rodriguez.f5.F5Application
import com.rodriguez.f5.R
import com.rodriguez.f5.databinding.FragmentTodayBinding
import com.rodriguez.f5.databinding.ItemDayBinding
import com.rodriguez.f5.ui.common.UiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Matches of the day, grouped by competition.
 *
 * This is the screen built with traditional XML views and a RecyclerView, as
 * the assignment requires. Everything else in the app is Compose.
 */
class TodayFragment : Fragment() {

    private val SPANISH = Locale("es", "AR")

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: TodayViewModel by viewModels {
        TodayViewModel.Factory(
            (requireActivity().application as F5Application).container.footballRepository
        )
    }

    private lateinit var adapter: TodayAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TodayAdapter(onMatchClick = { /* match detail comes next */ })
        binding.matchList.layoutManager = LinearLayoutManager(requireContext())
        binding.matchList.adapter = adapter

        buildDayStrip()

        binding.retryButton.setOnClickListener { viewModel.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedDay.collect(::renderSelectedDay)
            }
        }

        // repeatOnLifecycle stops collecting while the tab is not visible,
        // which is the View equivalent of collectAsStateWithLifecycle.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
    }

    private fun render(state: UiState<List<TodayItem>>) {
        binding.retryButton.isVisible = state is UiState.Error

        when (state) {
            is UiState.Loading -> showMessage("Buscando partidos…")
            is UiState.Empty -> showMessage(state.message)
            is UiState.Error -> showMessage(state.message)
            is UiState.Success -> {
                binding.stateMessage.isVisible = false
                binding.matchList.isVisible = true
                adapter.submitList(state.data)
            }
        }
    }

    private fun showMessage(message: String) {
        binding.stateMessage.text = message
        binding.stateMessage.isVisible = true
        binding.matchList.isVisible = false
    }

    /** Builds the five day chips once; only their selected state changes later. */
    private fun buildDayStrip() {
        val inflater = LayoutInflater.from(requireContext())
        val weekday = SimpleDateFormat("EEE", SPANISH)
        val number = SimpleDateFormat("d", SPANISH)

        viewModel.days.forEach { day ->
            val chip = ItemDayBinding.inflate(inflater, binding.dayStrip, false)
            chip.weekday.text = weekday.format(Date(day)).lowercase().take(3)
            chip.dayNumber.text = number.format(Date(day))
            chip.root.tag = day
            chip.root.setOnClickListener { viewModel.selectDay(day) }
            (chip.root.layoutParams as ViewGroup.MarginLayoutParams).marginEnd =
                resources.getDimensionPixelSize(R.dimen.day_chip_gap)
            binding.dayStrip.addView(chip.root)
        }
    }

    private fun renderSelectedDay(day: Long) {
        binding.subtitle.text = SimpleDateFormat("EEEE d 'de' MMMM", SPANISH)
            .format(Date(day))
            .replaceFirstChar { it.uppercase() }

        // The chip colours come from a selector, so flipping isSelected is
        // enough: no colours are set from code.
        for (index in 0 until binding.dayStrip.childCount) {
            val chip = binding.dayStrip.getChildAt(index)
            val isSelected = chip.tag == day
            chip.isSelected = isSelected
            val textColor = if (isSelected) R.color.brand_ink else R.color.text_primary
            val secondary = if (isSelected) R.color.brand_ink else R.color.text_secondary
            ItemDayBinding.bind(chip).apply {
                dayNumber.setTextColor(requireContext().getColor(textColor))
                weekday.setTextColor(requireContext().getColor(secondary))
            }
        }
    }

    override fun onDestroyView() {
        // The binding holds the view tree; keeping it past onDestroyView leaks it.
        binding.matchList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
