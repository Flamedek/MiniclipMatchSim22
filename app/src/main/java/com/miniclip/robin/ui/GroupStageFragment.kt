package com.miniclip.robin.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.plusAssign
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.transition.*
import com.miniclip.robin.R
import com.miniclip.robin.data.model.GroupStage
import com.miniclip.robin.databinding.*
import com.miniclip.robin.ui.GroupStageViewModel.ViewState.*
import com.miniclip.robin.ui.components.MatchListBuilder
import com.miniclip.robin.ui.components.ScoresTableBuilder
import com.miniclip.robin.ui.components.alphamovie.AlphaMovieView
import com.miniclip.robin.util.EaseInOutQuartInterpolator
import com.miniclip.robin.util.extensions.dpToPx
import com.miniclip.robin.util.extensions.fragmentViewModel
import com.miniclip.robin.util.extensions.getResourceUri
import com.miniclip.robin.util.extensions.viewBinding

private const val DEFAULT_TRANSITION_DURATION = 650L
private const val TOTAL_TRANSITION_DURATION = DEFAULT_TRANSITION_DURATION * 2

/**
 * Main Fragment for the app with multiple states.
 * Could be further split up to increase readability.
 * This view is 'dumb', only shows data from the ViewModel and reports back user events.
 */
class GroupStageFragment : Fragment(R.layout.stage_fragment) {

    /** Main view model */
    private val viewModel: GroupStageViewModel by fragmentViewModel()

    /** Base view bindings */
    private val views: StageFragmentBinding by viewBinding()

    /** The state that is currently shown */
    private var displayedState: GroupStageViewModel.ViewState? = null

    // some variables for the match/result stages
    private lateinit var matchViews: StageFragmentMatchesBinding
    private lateinit var scoreTableBuilder: ScoresTableBuilder
    private lateinit var matchListBuilder: MatchListBuilder

    private var celebrationOverlayView: AlphaMovieView? = null

    //
    // Lifecycle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.viewState.observe(viewLifecycleOwner, ::moveToState)
    }

    override fun onResume() {
        super.onResume()
        celebrationOverlayView?.onResume()
        celebrationOverlayView?.start()
    }

    override fun onPause() {
        super.onPause()
        celebrationOverlayView?.onPause()
    }

    /**
     * Called when the ViewModel moves to a new state
     */
    private fun moveToState(state: GroupStageViewModel.ViewState) {
        if (!state::class.isInstance(displayedState) && state !is Results) {
            // if state changed, kickoff an transition
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
        val view = StageFragmentInitialBinding.inflate(layoutInflater, views.mainCard, true)

        view.mainBtnGroupRandom.setOnClickListener {
            viewModel.onStartTeams(GroupStageViewModel.GroupMode.RANDOM)
        }
        view.mainBtnGroupF.setOnClickListener {
            viewModel.onStartTeams(GroupStageViewModel.GroupMode.FIXED)
        }
    }

    private fun initTeamsState(group: GroupStage) {
        val view = StageFragmentTeamsBinding.inflate(layoutInflater, views.mainCard, true)

        view.mainCardTitle.text = getString(R.string.group_title, group.name)
        view.mainBtnTeamsContinue.visibility = View.GONE
        view.mainBtnTeamsContinue.setOnClickListener {
            viewModel.onStartMatches()
        }

        // For each team add a row and animate in delayed
        for ((i, team) in group.teams.withIndex()) {
            val item = StageFragmentTeamsItemBinding.inflate(layoutInflater, view.mainFragmentTeams, true)

            item.mainFragmentTeamIcon.setImageResource(viewModel.getIconResource(team.icon))
            item.mainFragmentTeamText.text = team.name

            item.root.alpha = 0f
            item.root.translationY = dpToPx(-8f)
            item.root.postDelayed(TOTAL_TRANSITION_DURATION) {
                item.root.animate()
                    .setDuration(300L)
                    .setInterpolator(EaseInOutQuartInterpolator())
                    .alpha(1f)
                    .translationY(0f).startDelay = i * 250L
            }
        }

        // add action button after delay
        view.root.postDelayed(TOTAL_TRANSITION_DURATION + ((group.teams.size + 1) * 250L)) {
            TransitionManager.beginDelayedTransition(views.root, getDefaultTransition())
            view.mainBtnTeamsContinue.visibility = View.VISIBLE
        }
    }

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
        matchViews.mainBtnMatchesContinue.text = if (isFinished) {
            getString(R.string.btn_start_results)
        } else {
            getString(R.string.btn_play_matches)
        }

        matchViews.mainBtnMatchesContinue.setOnClickListener {
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

        matchViews.mainBtnMatchesContinue.visibility = View.GONE
        matchViews.mainCardMatchesLayout.removeAllViews()

        val view = StageFragmentResultsBinding.inflate(layoutInflater, matchViews.mainCardMatchesLayout, true)

        view.result1Name.text = group.scores[0].team.name
        view.result1Logo.setImageResource(viewModel.getIconResource(group.scores[0].team.icon))

        view.result2Name.text = group.scores[1].team.name
        view.result2Logo.setImageResource(viewModel.getIconResource(group.scores[1].team.icon))

        view.mainButtonToStart.setOnClickListener {
            removeCelebration()
            viewModel.onToStartClick()
        }
        view.mainButtonRestart.setOnClickListener {
            removeCelebration()
            viewModel.onRestartClick()
        }

        if (viewModel.shouldCelebrate()) {
            val overlay = AlphaMovieView(requireContext())
            overlay.setAlphaColor(Color.BLACK)
            overlay.setAccuracy(0.4f)
            overlay.setVideoFromUri(requireContext(), resources.getResourceUri(R.raw.video_confetti_hq_portrait_veed))
            overlay.elevation = 24f

            views.root.addView(overlay, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            celebrationOverlayView = overlay
        }
    }

    private fun removeCelebration() {
        celebrationOverlayView?.let { overlay ->
            overlay.stop()
            overlay.release()
            views.root.removeView(overlay)
        }
    }

    private fun getDefaultTransition(): Transition {
        /*
         * Mimic AutoTransition but with adjusted durations
         */
        return TransitionSet().apply {
            ordering = TransitionSet.ORDERING_SEQUENTIAL
            interpolator = EaseInOutQuartInterpolator()

            addTarget(ViewGroup::class.java)
            addTarget(TextView::class.java)

            addTransition(Fade(Fade.OUT).apply {
                duration = DEFAULT_TRANSITION_DURATION / 2
            })
            addTransition(ChangeBounds().apply {
                duration = DEFAULT_TRANSITION_DURATION
            })
            addTransition(Fade(Fade.IN).apply {
                duration = DEFAULT_TRANSITION_DURATION / 2
            })
        }
    }

}