package com.miniclip.robin.ui

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miniclip.robin.R
import com.miniclip.robin.data.model.GroupStage
import com.miniclip.robin.util.extensions.application
import kotlinx.coroutines.launch

class GroupStageViewModel(activity: Application) : AndroidViewModel(activity) {

    /**
     * Observable value for the current state to display in the view.
     */
    val viewState = MutableLiveData<ViewState>(ViewState.Initial)

    private lateinit var groupMode: GroupMode
    private lateinit var groupStage: GroupStage

    private val tournamentRepository get() = application.tournamentRepository

    //
    // Getters

    /**
     * Get the resource icon to use for a teams icon
     */
    @DrawableRes
    fun getIconResource(icon: String): Int {
        val resId = application.resources.getIdentifier(icon, "drawable", application.packageName)
        return resId.takeIf { it != 0 } ?: R.drawable.icon_team_default
    }

    /**
     * @return whether to take out the confetti cannon
     */
    fun shouldCelebrate(): Boolean {
        return viewState.value is ViewState.Results && groupStage.scores.firstOrNull()?.team?.name == "Ajax"
    }

    //
    // Interaction handlers

    fun onStartTeams(mode: GroupMode) {
        viewState.value = ViewState.Loading
        viewModelScope.launch {
            groupMode = mode
            groupStage = when (mode) {
                GroupMode.FIXED -> tournamentRepository.getFixedGroupStage()
                GroupMode.RANDOM -> tournamentRepository.getRandomGroupStage()
            }
            viewState.value = ViewState.Teams(groupStage)
        }
    }

    fun onStartMatches() {
        if (viewState.value != ViewState.Loading) {
            viewState.value = ViewState.Matches(groupStage)
        }
    }

    fun onPlayNextRound() {
        if (viewState.value is ViewState.Matches) {
            viewModelScope.launch {
                groupStage = tournamentRepository.simulateNextRound(groupStage)
                viewState.value = ViewState.Matches(groupStage)
            }
        }
    }

    fun onStartResults() {
        if (viewState.value != ViewState.Loading) {
            viewState.value = ViewState.Results(groupStage)
        }
    }

    fun onRestartClick() {
        viewModelScope.launch {
            groupStage = tournamentRepository.resetGroupStage(groupStage)
            viewState.value = ViewState.Matches(groupStage)
        }
    }

    fun onToStartClick() {
        viewState.value = ViewState.Initial
    }

    /**
     * The various states our View can be in
     */
    sealed class ViewState {

        object Loading : ViewState()

        object Initial : ViewState()

        class Teams(val group: GroupStage) : ViewState()

        class Matches(val group: GroupStage) : ViewState()

        class Results(val group: GroupStage) : ViewState()
    }

    enum class GroupMode {
        /** Use fixed predefined teams */
        FIXED,
        /** Use random teams */
        RANDOM;
    }

}