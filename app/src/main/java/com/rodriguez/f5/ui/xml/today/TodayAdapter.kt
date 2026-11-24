package com.rodriguez.f5.ui.xml.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rodriguez.f5.R
import com.rodriguez.f5.databinding.ItemTodayHeaderBinding
import com.rodriguez.f5.databinding.ItemTodayHeroBinding
import com.rodriguez.f5.databinding.ItemTodayMatchBinding
import com.rodriguez.f5.domain.model.Match
import com.rodriguez.f5.domain.model.MatchStatus
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * One entry of the list. The screen groups matches by competition, so the
 * adapter has to render two different shapes of row.
 */
sealed interface TodayItem {
    /** The live match, lifted out of its group to the top of the screen. */
    data class Hero(val match: Match) : TodayItem
    data class Header(val competitionSlug: String, val name: String, val matchCount: Int) : TodayItem
    data class MatchRow(val match: Match) : TodayItem
}

/** Loads a crest with Picasso, which both view holders need. */
private fun loadCrest(url: String?, view: android.widget.ImageView) {
    Picasso.get()
        .load(url?.takeIf { it.isNotBlank() })
        .placeholder(R.drawable.ic_crest_placeholder)
        .error(R.drawable.ic_crest_placeholder)
        .fit()
        .centerInside()
        .into(view)
}

/**
 * Adapter of the only screen built with traditional views.
 *
 * It uses [ListAdapter] with a [DiffUtil] callback instead of calling
 * notifyDataSetChanged: with live scores refreshing every couple of minutes,
 * redrawing the whole list would lose the scroll position and flicker.
 */
class TodayAdapter(
    private val onMatchClick: (Match) -> Unit
) : ListAdapter<TodayItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is TodayItem.Hero -> TYPE_HERO
        is TodayItem.Header -> TYPE_HEADER
        is TodayItem.MatchRow -> TYPE_MATCH
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HERO -> HeroViewHolder(ItemTodayHeroBinding.inflate(inflater, parent, false), onMatchClick)
            TYPE_HEADER -> HeaderViewHolder(ItemTodayHeaderBinding.inflate(inflater, parent, false))
            else -> MatchViewHolder(ItemTodayMatchBinding.inflate(inflater, parent, false), onMatchClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TodayItem.Hero -> (holder as HeroViewHolder).bind(item.match)
            is TodayItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TodayItem.MatchRow -> (holder as MatchViewHolder).bind(item.match)
        }
    }

    class HeroViewHolder(
        private val binding: ItemTodayHeroBinding,
        private val onMatchClick: (Match) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            binding.heroStatus.text = match.minute?.let { "EN VIVO · $it’" } ?: "EN VIVO"
            binding.heroHomeName.text = match.home.shortName
            binding.heroAwayName.text = match.away.shortName
            binding.heroScore.text = "${match.homeScore ?: 0} – ${match.awayScore ?: 0}"
            loadCrest(match.home.crestUrl, binding.heroHomeCrest)
            loadCrest(match.away.crestUrl, binding.heroAwayCrest)
            binding.root.setOnClickListener { onMatchClick(match) }
        }
    }

    class HeaderViewHolder(
        private val binding: ItemTodayHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodayItem.Header) {
            binding.competitionName.text = item.name
            binding.badge.text = item.name.take(1).uppercase()
            binding.matchCount.text = item.matchCount.toString()
        }
    }

    class MatchViewHolder(
        private val binding: ItemTodayMatchBinding,
        private val onMatchClick: (Match) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val kickoffFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(match: Match) {
            val context = binding.root.context

            binding.homeName.text = match.home.shortName
            binding.awayName.text = match.away.shortName
            binding.homeScore.text = match.homeScore?.toString() ?: "–"
            binding.awayScore.text = match.awayScore?.toString() ?: "–"

            binding.statusLabel.text = when (match.status) {
                MatchStatus.LIVE -> match.minute?.let { "$it’" } ?: "En vivo"
                MatchStatus.FINISHED -> "Final"
                MatchStatus.POSTPONED -> "Susp."
                MatchStatus.SCHEDULED -> kickoffFormat.format(Date(match.kickoffEpochMillis))
            }

            val statusColor = when (match.status) {
                MatchStatus.LIVE -> R.color.brand_coral
                MatchStatus.SCHEDULED -> R.color.brand_lime
                MatchStatus.POSTPONED -> R.color.brand_amber
                MatchStatus.FINISHED -> R.color.text_tertiary
            }
            binding.statusDot.backgroundTintList =
                ContextCompat.getColorStateList(context, statusColor)

            // A live match is the one thing a person opens the app to find,
            // so it gets both a tinted row and a bar down its left edge.
            binding.liveBar.isVisible = match.isLive
            binding.row.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (match.isLive) R.color.surface_alternate else R.color.surface
                )
            )

            loadCrest(match.home.crestUrl, binding.homeCrest)
            loadCrest(match.away.crestUrl, binding.awayCrest)

            binding.root.setOnClickListener { onMatchClick(match) }
        }

    }

    private companion object {
        const val TYPE_HERO = 0
        const val TYPE_HEADER = 1
        const val TYPE_MATCH = 2

        val DIFF = object : DiffUtil.ItemCallback<TodayItem>() {

            override fun areItemsTheSame(old: TodayItem, new: TodayItem): Boolean = when {
                old is TodayItem.Hero && new is TodayItem.Hero -> old.match.id == new.match.id
                old is TodayItem.Header && new is TodayItem.Header ->
                    old.competitionSlug == new.competitionSlug
                old is TodayItem.MatchRow && new is TodayItem.MatchRow ->
                    old.match.id == new.match.id
                else -> false
            }

            // Data classes compare by value, so a changed score redraws only
            // that row and a refresh that changed nothing redraws nothing.
            override fun areContentsTheSame(old: TodayItem, new: TodayItem): Boolean = old == new
        }
    }
}
