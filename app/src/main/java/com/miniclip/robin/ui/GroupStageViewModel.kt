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
     * Observable value for the current state to display
     */
    val viewState = MutableLiveData<ViewState>()


    private lateinit var groupStage: GroupStage

    private val tournamentRepository get() = application.tournamentRepository

    init {
        viewState.value = ViewState.Loading
        reloadGroupStage()
    }

    @DrawableRes
    fun getIconResource(icon: String): Int {
        val resId = application.resources.getIdentifier(icon, "drawable", application.packageName)
        return resId.takeIf { it != 0 } ?: R.drawable.icon_team_default
    }

    //
    // Interaction handlers

    fun onStartTeams() {
        if (viewState.value != ViewState.Loading) {
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
        reloadGroupStage()
    }

    fun shouldCelebrate(): Boolean {
        return viewState.value is ViewState.Results && groupStage.scores.firstOrNull()?.team?.name == "Ajax"
    }

    private fun reloadGroupStage() {
        viewModelScope.launch {
            groupStage = tournamentRepository.getCurrentGroupStage()
            viewState.value = ViewState.Initial
        }
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

}