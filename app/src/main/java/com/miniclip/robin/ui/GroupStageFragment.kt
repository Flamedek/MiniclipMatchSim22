package com.miniclip.robin.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.plusAssign
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.miniclip.robin.R
import com.miniclip.robin.data.model.GroupStage
import com.miniclip.robin.databinding.*
import com.miniclip.robin.ui.GroupStageViewModel.ViewState.*
import com.miniclip.robin.ui.components.MatchListBuilder
import com.miniclip.robin.ui.components.ScoresTableBuilder
import com.miniclip.robin.util.EaseInOutQuartInterpolator
import com.miniclip.robin.util.extensions.dpToPx
import com.miniclip.robin.util.extensions.fragmentViewModel
import com.miniclip.robin.util.extensions.viewBinding

private const val DEFAULT_TRANSITION_DURATION = 650L
private const val TOTAL_TRANSITION_DURATION = DEFAULT_TRANSITION_DURATION * 3

class GroupStageFragment : Fragment(R.layout.stage_fragment) {

    private val viewModel: GroupStageViewModel by fragmentViewModel()

    private val views: StageFragmentBinding by viewBinding()

    private var displayedState: GroupStageViewModel.ViewState? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.viewState.observe(viewLifecycleOwner, ::moveToState)
    }

    /**
     * Called when the ViewModel moves to a new state
     */
    private fun moveToState(state: GroupStageViewModel.ViewState) {
        if (!state::class.isInstance(displayedState) && state !is Results) {
            val transition = getDefaultTransition()
            TransitionManager.beginDelayedTransition(views.root, transition)
            views.mainCard.removeAllViews()
        }

        when (state) {
            Loading -> initLoadingState()
            Initial -> initInitialState()
            is Teams -> initTeamsState(state.group)
            is Matches -> initMatchesState(state.group)
            is Results -> initResultsState(state.group)
        }

        displayedState = state
    }

    private fun initLoadingState() {
        val view = ProgressBar(requireContext(), null, 0, android.R.style.Widget_Material_ProgressBar_Small)
        views.mainCard += view
    }

    private fun initInitialState() {
        val view = StageFragmentButtonBinding.inflate(LayoutInflater.from(requireContext()), views.mainCard, true)

        view.mainActionButton.setText(R.string.btn_start_teams)
        view.mainActionButton.setOnClickListener {
            viewModel.onStartTeams()
        }
    }

    private fun initTeamsState(group: GroupStage) {
        val view = StageFragmentTeamsBinding.inflate(LayoutInflater.from(requireContext()), views.mainCard, true)

        view.mainCardTitle.text = getString(R.string.group_title, group.name)
        view.mainActionButton.root.visibility = View.GONE
        view.mainActionButton.root.setText(R.string.btn_start_matches)
        view.mainActionButton.root.setOnClickListener {
            viewModel.onStartMatches()
        }

        // For each team add a row and animate in delayed
        for ((i, team) in group.teams.withIndex()) {
            val item = StageFragmentTeamsItemBinding.inflate(LayoutInflater.from(requireContext()), view.mainFragmentTeams, true)
            val iconResource = resources.getIdentifier(team.icon, "drawable", requireContext().packageName)
            if (iconResource != 0) {
                item.mainFragmentTeamIcon.setImageResource(iconResource)
            }
            item.mainFragmentTeamText.text = team.name

            item.root.alpha = 0f
            item.root.translationY = dpToPx(-8f)
            item.root.postDelayed(TOTAL_TRANSITION_DURATION) {
                item.root.animate()
                    .setDuration(300L)
                    .setInterpolator(EaseInOutQuartInterpolator())
                    .alpha(1f)
                    .translationY(0f).startDelay = i * 300L
            }
        }

        // add action button after delay
        view.root.postDelayed(TOTAL_TRANSITION_DURATION + (group.teams.size * 300L) + 200L) {
            TransitionManager.beginDelayedTransition(views.root, getDefaultTransition())
            view.mainActionButton.root.visibility = View.VISIBLE
        }
    }

    private lateinit var matchViews: StageFragmentMatchesBinding
    private lateinit var scoreTableBuilder: ScoresTableBuilder
    private lateinit var matchListBuilder: MatchListBuilder

    private fun initMatchesState(group: GroupStage) {
        if (displayedState !is Matches) {
            matchViews = StageFragmentMatchesBinding.inflate(layoutInflater, views.mainCard, true)

            scoreTableBuilder = ScoresTableBuilder(viewModel)
            scoreTableBuilder.inflate(layoutInflater, matchViews.mainCardTableLayout, true)

            matchListBuilder = MatchListBuilder(viewModel)
            matchListBuilder.inflate(layoutInflater, matchViews.mainCardMatchesLayout, true)
        }

        val isFinished = group.matches.all { match -> match.result != null }

        matchViews.mainCardTitle.text = getString(R.string.group_title, group.name)
        matchViews.button.mainActionButton.text = if (isFinished) {
            getString(R.string.btn_start_results)
        } else {
            getString(R.string.btn_play_matches)
        }

        matchViews.button.mainActionButton.setOnClickListener {
            if (isFinished) {
                viewModel.onStartResults()
            } else {
                viewModel.onPlayNextRound()
            }
        }

        scoreTableBuilder.updateScores(group.scores)
        matchListBuilder.updateMatches(group.matches)
    }

    private fun initResultsState(group: GroupStage) {
        TransitionManager.beginDelayedTransition(views.root, getDefaultTransition())

        matchViews.button.mainActionButton.visibility = View.GONE
        matchViews.mainCardMatchesLayout.removeAllViews()

        val view = StageFragmentResultsBinding.inflate(layoutInflater, matchViews.mainCardMatchesLayout, true)

        view.result1Name.text = group.scores[0].team.name
        view.result1Logo.setImageResource(viewModel.getIconResource(group.scores[0].team.icon))

        view.result2Name.text = group.scores[1].team.name
        view.result2Logo.setImageResource(viewModel.getIconResource(group.scores[1].team.icon))

        view.mainButtonRestart.setOnClickListener {
            viewModel.onRestartClick()
        }
    }

    private fun getDefaultTransition(): Transition {
        return AutoTransition().apply {
            duration = DEFAULT_TRANSITION_DURATION
            interpolator = EaseInOutQuartInterpolator()
        }
    }

}