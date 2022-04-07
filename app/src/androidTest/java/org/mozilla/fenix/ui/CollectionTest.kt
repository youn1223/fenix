/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.customannotations.SmokeTest
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.FeatureSettingsHelper
import org.mozilla.fenix.helpers.HomeActivityIntentTestRule
import org.mozilla.fenix.helpers.TestAssetHelper.getGenericAsset
import org.mozilla.fenix.ui.robots.browserScreen
import org.mozilla.fenix.ui.robots.collectionRobot
import org.mozilla.fenix.ui.robots.homeScreen
import org.mozilla.fenix.ui.robots.navigationToolbar
import org.mozilla.fenix.ui.robots.tabDrawer

/**
 *  Tests for verifying basic functionality of tab collections
 *
 */

class CollectionTest {
    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer
    private val firstCollectionName = "testcollection_1"
    private val secondCollectionName = "testcollection_2"
    private val collectionName = "First Collection"
    private val featureSettingsHelper = FeatureSettingsHelper()

    @get:Rule
    val activityTestRule = HomeActivityIntentTestRule()

    @Before
    fun setUp() {
        // disabling these features to have better visibility of Collections
        featureSettingsHelper.setRecentTabsFeatureEnabled(false)
        featureSettingsHelper.setPocketEnabled(false)
        featureSettingsHelper.setJumpBackCFREnabled(false)
        featureSettingsHelper.setRecentlyVisitedFeatureEnabled(false)

        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()

        // resetting modified features enabled setting to default
        featureSettingsHelper.resetAllFeatureFlags()
    }

    @SmokeTest
    @Test
    fun createFirstCollectionTest() {
        val firstWebPage = getGenericAsset(mockWebServer, 1)
        val secondWebPage = getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebPage.url) {
            mDevice.waitForIdle()
        }.openTabDrawer {
        }.openNewTab {
        }.submitQuery(secondWebPage.url.toString()) {
            mDevice.waitForIdle()
        }.goToHomescreen {
            swipeToBottom()
        }.clickSaveTabsToCollectionButton {
            longClickTab(firstWebPage.title)
            selectTab(secondWebPage.title)
        }.clickSaveCollection {
            typeCollectionNameAndSave(collectionName)
        }

        tabDrawer {
            verifySnackBarText("Collection saved!")
            snackBarButtonClick("VIEW")
        }

        homeScreen {
            verifyCollectionIsDisplayed(collectionName)
        }
    }

    @SmokeTest
    @Test
    fun verifyExpandedCollectionItemsTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, collectionName)
            snackBarButtonClick("VIEW")
        }

        homeScreen {
            verifyCollectionIsDisplayed(collectionName)
        }.expandCollection(collectionName) {
            verifyTabSavedInCollection(webPage.title)
            verifyCollectionTabLogo(true)
            verifyCollectionTabUrl(true)
            verifyShareCollectionButtonIsVisible(true)
            verifyCollectionMenuIsVisible(true)
            verifyCollectionItemRemoveButtonIsVisible(webPage.title, true)
        }.collapseCollection(collectionName) {}

        collectionRobot {
            verifyTabSavedInCollection(webPage.title, false)
            verifyShareCollectionButtonIsVisible(false)
            verifyCollectionMenuIsVisible(false)
            verifyCollectionTabLogo(false)
            verifyCollectionTabUrl(false)
            verifyCollectionItemRemoveButtonIsVisible(webPage.title, false)
        }

        homeScreen {
        }.expandCollection(collectionName) {
            verifyTabSavedInCollection(webPage.title)
            verifyCollectionTabLogo(true)
            verifyCollectionTabUrl(true)
            verifyShareCollectionButtonIsVisible(true)
            verifyCollectionMenuIsVisible(true)
            verifyCollectionItemRemoveButtonIsVisible(webPage.title, true)
        }.collapseCollection(collectionName) {}

        collectionRobot {
            verifyTabSavedInCollection(webPage.title, false)
            verifyShareCollectionButtonIsVisible(false)
            verifyCollectionMenuIsVisible(false)
            verifyCollectionTabLogo(false)
            verifyCollectionTabUrl(false)
            verifyCollectionItemRemoveButtonIsVisible(webPage.title, false)
        }
    }

    @SmokeTest
    @Test
    fun openAllTabsInCollectionTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, collectionName)
            verifySnackBarText("Collection saved!")
            closeTab()
        }

        homeScreen {
        }.expandCollection(collectionName) {
            clickCollectionThreeDotButton()
            selectOpenTabs()
        }
        tabDrawer {
            verifyExistingOpenTabs(webPage.title)
        }
    }

    @SmokeTest
    @Test
    fun shareCollectionTest() {
        val firstWebsite = getGenericAsset(mockWebServer, 1)
        val secondWebsite = getGenericAsset(mockWebServer, 2)
        val sharingApp = "Gmail"
        val urlString = "${secondWebsite.url}\n\n${firstWebsite.url}"

        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebsite.url) {
            verifyPageContent(firstWebsite.content)
        }.openTabDrawer {
            createCollection(firstWebsite.title, collectionName)
        }.openNewTab {
        }.submitQuery(secondWebsite.url.toString()) {
            verifyPageContent(secondWebsite.content)
        }.openThreeDotMenu {
        }.openSaveToCollection {
        }.selectExistingCollection(collectionName) {
        }.goToHomescreen {
        }.expandCollection(collectionName) {
        }.clickShareCollectionButton {
            verifyShareTabsOverlay(firstWebsite.title, secondWebsite.title)
            verifySharingWithSelectedApp(sharingApp, urlString, collectionName)
        }
    }

    // @Ignore("Failing, see: https://github.com/mozilla-mobile/fenix/issues/23296")
    @SmokeTest
    @Test
    // Test running on beta/release builds in CI:
    // caution when making changes to it, so they don't block the builds
    fun deleteCollectionTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, collectionName)
            snackBarButtonClick("VIEW")
        }

        homeScreen {
        }.expandCollection(collectionName) {
            clickCollectionThreeDotButton()
            selectDeleteCollection()
        }

        homeScreen {
            verifySnackBarText("Collection deleted")
            verifyNoCollectionsText()
        }
    }

    @Test
    // open a webpage, and add currently opened tab to existing collection
    fun mainMenuSaveToExistingCollection() {
        val firstWebPage = getGenericAsset(mockWebServer, 1)
        val secondWebPage = getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebPage.url) {
        }.openTabDrawer {
            createCollection(firstWebPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
        }.closeTabDrawer {}

        navigationToolbar {
        }.enterURLAndEnterToBrowser(secondWebPage.url) {
            verifyPageContent(secondWebPage.content)
        }.openThreeDotMenu {
        }.openSaveToCollection {
        }.selectExistingCollection(firstCollectionName) {
            verifySnackBarText("Tab saved!")
        }.goToHomescreen {
        }.expandCollection(firstCollectionName) {
            verifyTabSavedInCollection(firstWebPage.title)
            verifyTabSavedInCollection(secondWebPage.title)
        }
    }

    @Test
    fun verifyAddTabButtonOfCollectionMenu() {
        val firstWebPage = getGenericAsset(mockWebServer, 1)
        val secondWebPage = getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebPage.url) {
        }.openTabDrawer {
            createCollection(firstWebPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
            closeTab()
        }

        navigationToolbar {
        }.enterURLAndEnterToBrowser(secondWebPage.url) {
        }.goToHomescreen {
        }.expandCollection(firstCollectionName) {
            clickCollectionThreeDotButton()
            selectAddTabToCollection()
            verifyTabsSelectedCounterText(1)
            saveTabsSelectedForCollection()
            verifySnackBarText("Tab saved!")
            verifyTabSavedInCollection(secondWebPage.title)
        }
    }

    @Test
    fun renameCollectionTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
        }.closeTabDrawer {
        }.goToHomescreen {
        }.expandCollection(firstCollectionName) {
            clickCollectionThreeDotButton()
            selectRenameCollection()
        }.typeCollectionNameAndSave("renamed_collection") {}

        homeScreen {
            verifyCollectionIsDisplayed("renamed_collection")
        }
    }

    @Test
    fun createSecondCollectionTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
            createCollection(webPage.title, secondCollectionName, false)
            verifySnackBarText("Collection saved!")
        }.closeTabDrawer {
        }.goToHomescreen {}

        homeScreen {
            verifyCollectionIsDisplayed(firstCollectionName)
            verifyCollectionIsDisplayed(secondCollectionName)
        }
    }

    @Test
    fun removeTabFromCollectionTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
            closeTab()
        }

        homeScreen {
        }.expandCollection(firstCollectionName) {
            verifyTabSavedInCollection(webPage.title, true)
            removeTabFromCollection(webPage.title)
            verifyTabSavedInCollection(webPage.title, false)
        }
        // To add this step when https://github.com/mozilla-mobile/fenix/issues/13177 is fixed
//        homeScreen {
//            verifyCollectionIsDisplayed(firstCollectionName, false)
//        }
    }

    @Test
    fun swipeToRemoveTabFromCollectionTest() {
        val firstWebPage = getGenericAsset(mockWebServer, 1)
        val secondWebPage = getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebPage.url) {
        }.openTabDrawer {
            createCollection(firstWebPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
            closeTab()
        }

        navigationToolbar {
        }.enterURLAndEnterToBrowser(secondWebPage.url) {
        }.openThreeDotMenu {
        }.openSaveToCollection {
        }.selectExistingCollection(firstCollectionName) {
        }.openTabDrawer {
            closeTab()
        }

        homeScreen {
        }.expandCollection(firstCollectionName) {
            swipeToBottom()
            swipeCollectionItem(firstWebPage.title, Direction.RIGHT)
            verifyTabSavedInCollection(firstWebPage.title, false)
            swipeCollectionItem(secondWebPage.title, Direction.LEFT)
            verifyTabSavedInCollection(secondWebPage.title, false)
        }
        // To add this step when https://github.com/mozilla-mobile/fenix/issues/13177 is fixed
//        homeScreen {
//            verifyCollectionIsDisplayed(firstCollectionName, false)
//        }
    }

    @Test
    fun selectTabOnLongTapTest() {
        val firstWebPage = getGenericAsset(mockWebServer, 1)
        val secondWebPage = getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebPage.url) {
        }.openTabDrawer {
        }.openNewTab {
        }.submitQuery(secondWebPage.url.toString()) {
            mDevice.waitForIdle()
        }.openTabDrawer {
            longClickTab(firstWebPage.title)
            verifyTabsMultiSelectionCounter(1)
            selectTab(secondWebPage.title)
            verifyTabsMultiSelectionCounter(2)
        }.clickSaveCollection {
            typeCollectionNameAndSave(firstCollectionName)
            verifySnackBarText("Tabs saved!")
        }

        tabDrawer {
        }.closeTabDrawer {
        }.goToHomescreen {
        }.expandCollection(firstCollectionName) {
            verifyTabSavedInCollection(firstWebPage.title)
            verifyTabSavedInCollection(secondWebPage.title)
        }
    }

    @Test
    fun navigateBackInCollectionFlowTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, firstCollectionName)
            verifySnackBarText("Collection saved!")
        }.closeTabDrawer {
        }.openThreeDotMenu {
        }.openSaveToCollection {
            verifySelectCollectionScreen()
            goBackInCollectionFlow()
        }

        browserScreen {
        }.openThreeDotMenu {
        }.openSaveToCollection {
            verifySelectCollectionScreen()
            clickAddNewCollection()
            verifyCollectionNameTextField()
            goBackInCollectionFlow()
            verifySelectCollectionScreen()
            goBackInCollectionFlow()
        }
        // verify the browser layout is visible
        browserScreen {
            verifyMenuButton()
        }
    }

    @SmokeTest
    @Test
    fun undoDeleteCollectionTest() {
        val webPage = getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(webPage.url) {
        }.openTabDrawer {
            createCollection(webPage.title, firstCollectionName)
            snackBarButtonClick("VIEW")
        }

        homeScreen {
        }.expandCollection(firstCollectionName) {
            clickCollectionThreeDotButton()
            selectDeleteCollection()
        }

        homeScreen {
            verifySnackBarText("Collection deleted")
            clickUndoCollectionDeletion("UNDO")
            verifyCollectionIsDisplayed(firstCollectionName, true)
        }
    }
}
