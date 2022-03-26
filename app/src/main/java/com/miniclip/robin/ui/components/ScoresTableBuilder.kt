package com.miniclip.robin.ui.components

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.miniclip.robin.R
import com.miniclip.robin.data.model.GroupStageStats
import com.miniclip.robin.databinding.ScoresTableBinding
import com.miniclip.robin.databinding.ScoresTableRowBinding
import com.miniclip.robin.ui.GroupStageViewModel
import com.miniclip.robin.util.extensions.BindingHolder

class ScoresTableBuilder(private val viewModel: GroupStageViewModel) {

    private lateinit var bindings: ScoresTableBinding
    private lateinit var adapter: RowAdapter

    fun inflate(inflater: LayoutInflater): View {
        return inflate(inflater, null, false)
    }

    fun inflate(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): View {
        adapter = RowAdapter()
        bindings = ScoresTableBinding.inflate(inflater, parent, attachToParent)
        bindings.scoreTableItems.adapter = adapter
        bindings.scoreTableItems.layoutManager = LinearLayoutManager(inflater.context)
        return bindings.root
    }

    fun updateScores(items: List<GroupStageStats>) {
        if (adapter.itemCount == 0) {
            adapter.submitList(items)
        } else {
            /*
            * We use a RecyclerView based on [ListAdapter] to get automatic position change animations.
            * Delayed to time animation with match list view.
            */
            bindings.root.postDelayed(300L) {
                adapter.submitList(items)
            }
        }
    }

    private inner class RowAdapter : ListAdapter<GroupStageStats, BindingHolder<ScoresTableRowBinding>>(StatsItemDiff()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ScoresTableRowBinding> {
            val view = ScoresTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BindingHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: BindingHolder<ScoresTableRowBinding>, position: Int) {
            val item = getItem(position)
            with (holder.views) {
                root.setBackgroundResource(if (position < 2) R.color.highlight_group_stage else 0)
                scoreTableItemIndex.text = (position + 1).toString()

                scoreTableItemName.text = item.team.name
                scoreTableItemIcon.setImageResource(viewModel.getIconResource(item.team.icon))

                scoreTableItemGames.text = item.gamesPlayed.toString()
                scoreTableItemWins.text = item.wins.toString()
                scoreTableItemDraws.text = item.draws.toString()
                scoreTableItemLosses.text = item.losses.toString()
                scoreTableItemGoalsScored.text = item.goalsScored.toString()
                scoreTableItemGoalsAgainst.text = item.goalsAgainst.toString()
                scoreTableItemGoalsDifference.text = item.goalsDifference.toString()
                scoreTableItemPoints.text = item.points.toString()
            }
        }
    }

    private class StatsItemDiff : DiffUtil.ItemCallback<GroupStageStats>() {
        override fun areItemsTheSame(oldItem: GroupStageStats, newItem: GroupStageStats): Boolean {
            return oldItem.team.id == newItem.team.id
        }

        override fun areContentsTheSame(oldItem: GroupStageStats, newItem: GroupStageStats): Boolean {
            return oldItem == newItem
        }
    }

}