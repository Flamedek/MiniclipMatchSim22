package com.miniclip.robin.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.core.view.plusAssign
import com.miniclip.robin.data.model.Match
import com.miniclip.robin.databinding.MatchListRowBinding
import com.miniclip.robin.ui.GroupStageViewModel
import com.miniclip.robin.util.extensions.TAG

@SuppressLint("SetTextI18n")
class MatchListBuilder(private val viewModel: GroupStageViewModel) {

    private lateinit var layoutInflater: LayoutInflater
    private lateinit var container: LinearLayout

    private val rows = mutableListOf<MatchListRowBinding>()
    private val context get() = layoutInflater.context

    fun inflate(layoutInflater: LayoutInflater): View {
        return inflate(layoutInflater, null, false)
    }

    fun inflate(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): View {
        layoutInflater = inflater
        container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        if (parent != null && attachToParent) {
            parent += container.apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        return container
    }

    fun updateMatches(items: List<Match>) {
        // 1. remove rows if matches are removed
        if (container.childCount > 0 && container.childCount != items.size) {
            Log.w(TAG, "Match List changed size! was ${container.childCount}, now ${items.size}")
            repeat(rows.size - items.size) {
                container.removeViewAt(container.childCount - 1)
                rows.removeAt(rows.lastIndex)
            }
        }

        // 2. inflate rows if necessary
        while (rows.size < items.size) {
            rows += MatchListRowBinding.inflate(layoutInflater, container, true)
        }

        // 3. bind data to each row
        var revealIndex = 0
        for ((i, item) in items.withIndex()) {
            val (home, away) = item.teams
            with(rows[i]) {
                matchIconHome.setImageResource(viewModel.getIconResource(home.icon))
                matchNameHome.text = home.name

                matchIconAway.setImageResource(viewModel.getIconResource(away.icon))
                matchNameAway.text = away.name

                val result = item.result
                if (result != null) {
                    if (matchScore.visibility != View.VISIBLE) {
                        // first time this row has a score! switch the date and score views with an animation
                        hideScoreAnimation(matchDate).withEndAction {
                            matchDate.visibility = View.GONE
                            matchScore.visibility = View.VISIBLE
                            revealScoreAnimation(matchScore).startDelay = revealIndex++ * 300L
                        }
                    }
                    matchScore.text = "${result.score.first} - ${result.score.second}"
                } else {
                    matchScore.visibility = View.GONE
                    matchDate.visibility = View.VISIBLE
                    matchDate.text = "Ronde ${(i / 2) + 1}"
                }
            }
        }
    }

    private fun hideScoreAnimation(view: View): ViewPropertyAnimator {
        return with(view) {
            animate()
                .alpha(0f)
                .scaleX(0.5f)
                .scaleY(0.5f)
        }
    }

    private fun revealScoreAnimation(view: View): ViewPropertyAnimator  {
        return with(view) {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            animate()
                .setInterpolator(OvershootInterpolator(1.5f))
                .setDuration(600L)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
        }
    }
}