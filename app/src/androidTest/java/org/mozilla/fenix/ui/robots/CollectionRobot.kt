/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.fenix.R
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTime
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.fenix.helpers.TestHelper.packageName
import org.mozilla.fenix.helpers.TestHelper.scrollToElementByText
import org.mozilla.fenix.helpers.ext.waitNotNull

class CollectionRobot {

    fun verifySelectCollectionScreen() {
        assertTrue(
            mDevice.findObject(UiSelector().text("Select collection"))
                .exists()
        )
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("$packageName:id/collections_list"))
                .exists()
        )
        assertTrue(
            mDevice.findObject(UiSelector().text("Add new collection"))
                .exists()
        )
    }

    fun clickAddNewCollection() = addNewCollectionButton().click()

    fun verifyCollectionNameTextField() {
        assertTrue(
            mainMenuEditCollectionNameField().waitForExists(waitingTime)
        )
    }

    // names a collection saved from tab drawer
    fun typeCollectionNameAndSave(collectionName: String) {
        collectionNameTextField().text = collectionName
        mDevice.findObject(UiSelector().textContains("OK")).click()
    }

    fun verifyTabsSelectedCounterText(numOfTabs: Int) {
        mDevice.findObject(UiSelector().text("Select tabs to save"))
            .waitUntilGone(waitingTime)

        val tabsCounter = mDevice.findObject(UiSelector().resourceId("$packageName:id/bottom_bar_text"))
        when (numOfTabs) {
            1 -> assertTrue(tabsCounter.text.equals("$numOfTabs tab selected"))
            2 -> assertTrue(tabsCounter.text.equals("$numOfTabs tabs selected"))
        }
    }

    fun saveTabsSelectedForCollection() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/save_button")).click()
    }

    fun verifyTabSavedInCollection(title: String, visible: Boolean = true) {
        if (visible) {
            scrollToElementByText(title)
            assertTrue(
                collectionListItem(title).waitForExists(waitingTime)
            )
        } else
            assertTrue(
                collectionListItem(title).waitUntilGone(waitingTime)
            )
    }

    fun verifyCollectionTabUrl(visible: Boolean) {
        val tabUrl = mDevice.findObject(UiSelector().resourceId("$packageName:id/caption"))

        if (visible) {
            assertTrue(tabUrl.exists())
        } else {
            assertFalse(tabUrl.exists())
        }
    }

    fun verifyCollectionTabLogo(visible: Boolean) {
        val tabLogo = mDevice.findObject(UiSelector().resourceId("$packageName:id/favicon"))

        if (visible) {
            assertTrue(tabLogo.exists())
        } else {
            assertFalse(tabLogo.exists())
        }
    }

    fun verifyShareCollectionButtonIsVisible(visible: Boolean) {
        if (visible) {
            assertTrue(shareCollectionButton().exists())
        } else {
            assertFalse(shareCollectionButton().exists())
        }
    }

    fun verifyCollectionMenuIsVisible(visible: Boolean) {
        if (visible) {
            assertTrue(collectionThreeDotButton().waitForExists(waitingTime))
        } else {
            assertFalse(collectionThreeDotButton().waitForExists(waitingTime))
        }
    }

    fun clickCollectionThreeDotButton() {
        collectionThreeDotButton().waitForExists(waitingTime)
        collectionThreeDotButton().click()
    }

    fun selectOpenTabs() {
        mDevice.findObject(UiSelector().text("Open tabs")).click()
    }

    fun selectRenameCollection() {
        mDevice.findObject(UiSelector().text("Rename collection")).click()
        mainMenuEditCollectionNameField().waitForExists(waitingTime)
    }

    fun selectAddTabToCollection() {
        mDevice.findObject(UiSelector().text("Add tab")).click()
        mDevice.waitNotNull(Until.findObject(By.text("Select Tabs")))
    }

    fun selectDeleteCollection() {
        mDevice.findObject(
        //     UiSelector().resourceId("android.widget.ScrollView")
        // ).getChild(
            By.text("Delete collection")
        ).click()

    }

    fun confirmDeleteCollection() {
        mDevice.findObject(UiSelector().text("DELETE")).click()
        mDevice.waitNotNull(
            Until.findObject(By.res("$packageName:id/no_collections_header")),
            waitingTime
        )
    }

    fun verifyCollectionItemRemoveButtonIsVisible(title: String, visible: Boolean) {
        if (visible) {
            assertTrue(
                removeTabFromCollectionButton(title).exists()
            )
        } else {
            assertFalse(
                removeTabFromCollectionButton(title).exists()
            )
        }
    }

    fun removeTabFromCollection(title: String) = removeTabFromCollectionButton(title).click()

    fun swipeCollectionItem(title: String, direction: Direction) {
        /* The By selector has a better working swipe method than the UiSelector used in another
            definition of collectionItem.
            We need both definitions for different purposes.
        */
        val swipeableItem =
            mDevice.findObject(
                By
                    .res("$packageName:id/label")
                    .text(title)
            )
        // Swiping can sometimes fail to remove the tab, so if the tab still exists, we need to repeat it
        var retries = 0 // number of retries before failing, will stop at 2
        while (collectionListItem(title).waitForExists(waitingTimeShort) && retries < 2) {
            swipeableItem.swipe(direction, 1.0f)
            retries++
        }
    }

    fun verifySnackBarText(expectedText: String) {
        mDevice.findObject(UiSelector().text(expectedText)).waitForExists(waitingTime)
    }

    fun goBackInCollectionFlow() = backButton().click()

    fun swipeToBottom() =
        UiScrollable(
            UiSelector().resourceId("$packageName:id/sessionControlRecyclerView")
        ).scrollToEnd(3)

    class Transition {
        fun collapseCollection(
            title: String,
            interact: HomeScreenRobot.() -> Unit
        ): HomeScreenRobot.Transition {
            try {
                collectionTitle(title).waitForExists(waitingTime)
                collectionTitle(title).click()
            } catch (e: NoMatchingViewException) {
                scrollToElementByText(title)
                collectionTitle(title).click()
            }

            HomeScreenRobot().interact()
            return HomeScreenRobot.Transition()
        }

        // names a collection saved from the 3dot menu
        fun typeCollectionNameAndSave(
            name: String,
            interact: BrowserRobot.() -> Unit
        ): BrowserRobot.Transition {
            mainMenuEditCollectionNameField().waitForExists(waitingTime)
            mainMenuEditCollectionNameField().text = name
            onView(withId(R.id.name_collection_edittext)).perform(pressImeActionButton())

            // wait for the collection creation wrapper to be dismissed
            mDevice.waitNotNull(Until.gone(By.res("$packageName:id/createCollectionWrapper")))

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun selectExistingCollection(
            title: String,
            interact: BrowserRobot.() -> Unit
        ): BrowserRobot.Transition {
            collectionTitle(title).waitForExists(waitingTime)
            collectionTitle(title).click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun clickShareCollectionButton(interact: ShareOverlayRobot.() -> Unit): ShareOverlayRobot.Transition {
            shareCollectionButton().waitForExists(waitingTime)
            shareCollectionButton().click()

            ShareOverlayRobot().interact()
            return ShareOverlayRobot.Transition()
        }
    }
}

fun collectionRobot(interact: CollectionRobot.() -> Unit): CollectionRobot.Transition {
    CollectionRobot().interact()
    return CollectionRobot.Transition()
}

private fun collectionTitle(title: String) =
    mDevice.findObject(
        UiSelector()
            .text(title)
    )


private fun collectionThreeDotButton() =
    mDevice.findObject(UiSelector().description("Collection menu"))

private fun collectionListItem(title: String) =
    mDevice.findObject(
        UiSelector()
            .resourceId("$packageName:id/label")
            .text(title)
    )

private fun shareCollectionButton() =
    mDevice.findObject(
        UiSelector().description("Share")
    )

private fun removeTabFromCollectionButton(title: String) =
    mDevice.findObject(
        UiSelector().text(title)
    ).getFromParent(
        UiSelector()
            .description("Remove tab from collection")
    )

// collection name text field, opened from tab drawer
private fun collectionNameTextField() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/collection_name")
    )

// collection name text field, when saving from the main menu option
private fun mainMenuEditCollectionNameField() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/name_collection_edittext")
    )

private fun addNewCollectionButton() =
    mDevice.findObject(UiSelector().text("Add new collection"))

private fun backButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/back_button")
    )
