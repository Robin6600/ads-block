package com.example

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import org.junit.Before

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class NavigationCrashTest {
    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        ShadowLog.stream = System.out
    }

    @Test
    fun testNavigationCrash() {
        // Toggle VPN
        composeTestRule.onNodeWithContentDescription("Toggle Protection").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Stats", ignoreCase = true, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Back", ignoreCase = true, useUnmergedTree = true).performClick()
        
        composeTestRule.onNodeWithText("Whitelist", ignoreCase = true, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Back", ignoreCase = true, useUnmergedTree = true).performClick()
        
        composeTestRule.onNodeWithText("Settings", ignoreCase = true, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        // Toggle a setting
        composeTestRule.onNodeWithText("Auto-start on Boot").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithContentDescription("Back", ignoreCase = true, useUnmergedTree = true).performClick()
    }
}
