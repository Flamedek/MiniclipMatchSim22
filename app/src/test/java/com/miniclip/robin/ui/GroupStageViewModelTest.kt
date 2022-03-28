package com.miniclip.robin.ui

import android.content.res.Resources
import com.miniclip.robin.InstantExecutorExtension
import com.miniclip.robin.R
import com.miniclip.robin.SimApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(InstantExecutorExtension::class)
internal class GroupStageViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    companion object DispatchSetup {
        private val mainThreadSurrogate = newSingleThreadContext("UI thread")

        @BeforeAll
        @JvmStatic
        fun setUp() {
            // setup our test executor as main thread
            Dispatchers.setMain(mainThreadSurrogate)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            // reset the main dispatcher to the original Main dispatcher
            Dispatchers.resetMain()
            mainThreadSurrogate.close()
        }
    }

    @Test
    fun getIconResourceDefault() {
        val testIcon = "<<this-icon-doest-not-exists>>"
        val application = mockk<SimApplication>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        every { application.resources } returns resources
        every { application.packageName } returns "com.unit.test"
        every { resources.getIdentifier(testIcon, "drawable", application.packageName) } returns 0

        val viewModel = GroupStageViewModel(application)
        val iconResId = viewModel.getIconResource(testIcon)
        assertEquals(R.drawable.icon_team_default, iconResId)
    }

}