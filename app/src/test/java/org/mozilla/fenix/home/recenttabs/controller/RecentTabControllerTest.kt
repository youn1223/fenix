/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.recenttabs.controller

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.test.ext.joinBlocking
import mozilla.components.support.test.rule.MainCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController

@OptIn(ExperimentalCoroutinesApi::class)
class RecentTabControllerTest {

    @get:Rule
    val coroutinesTestRule = MainCoroutineRule()

    private val navController: NavController = mockk(relaxed = true)
    private val selectTabUseCase: TabsUseCases = mockk(relaxed = true)
    private val metrics: MetricController = mockk(relaxed = true)
    private val appStore: AppStore = mockk()

    private lateinit var store: BrowserStore

    private lateinit var controller: DefaultRecentTabsController

    @Before
    fun setup() {
        store = BrowserStore(
            BrowserState()
        )
        controller = spyk(
            DefaultRecentTabsController(
                selectTabUseCase = selectTabUseCase.selectTab,
                navController = navController,
                metrics = metrics,
                store = store,
                appStore = appStore,
            )
        )
        every { navController.navigateUp() } returns true
    }

    @Test
    fun handleRecentTabClicked() {
        every { navController.currentDestination } returns mockk {
            every { id } returns R.id.homeFragment
        }

        val tab = createTab(
            url = "https://mozilla.org",
            title = "Mozilla"
        )
        store.dispatch(TabListAction.AddTabAction(tab)).joinBlocking()
        store.dispatch(TabListAction.SelectTabAction(tab.id)).joinBlocking()

        controller.handleRecentTabClicked(tab.id)

        verify {
            selectTabUseCase.selectTab.invoke(tab.id)
            navController.navigate(R.id.browserFragment)
            metrics.track(Event.OpenRecentTab)
        }
    }

    @Test
    fun handleRecentTabShowAllClickedFromHome() {
        every { navController.currentDestination } returns mockk {
            every { id } returns R.id.homeFragment
        }

        controller.handleRecentTabShowAllClicked()

        verify {
            controller.dismissSearchDialogIfDisplayed()
            navController.navigate(
                match<NavDirections> { it.actionId == R.id.action_global_tabsTrayFragment }
            )
            metrics.track(Event.ShowAllRecentTabs)
        }
        verify(exactly = 0) {
            navController.navigateUp()
        }
    }

    @Test
    fun handleRecentTabShowAllClickedFromSearchDialog() {
        every { navController.currentDestination } returns mockk {
            every { id } returns R.id.searchDialogFragment
        }

        controller.handleRecentTabShowAllClicked()

        verify {
            controller.dismissSearchDialogIfDisplayed()
            navController.navigateUp()
            navController.navigate(
                match<NavDirections> { it.actionId == R.id.action_global_tabsTrayFragment }
            )
            metrics.track(Event.ShowAllRecentTabs)
        }
    }
}
