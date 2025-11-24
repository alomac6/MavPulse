package com.example.mavpulse

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.mavpulse.network.RetrofitInstance
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        RetrofitInstance.setBaseUrl(server.url("/").toString())

        // Navigate to Register page
        try {
            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.onNodeWithText("Register").performClick()
        } catch (e: Exception) {
            // Ignore if starting on a different page
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun displaysValidationError_whenPasswordIsTooShort() {
        composeTestRule.onNodeWithText("Password*").performTextInput("short")
        composeTestRule.onNodeWithText("Register").performClick()

        composeTestRule.onNodeWithText("Password must be at least 8 characters.").assertIsDisplayed()
    }

    @Test
    fun navigatesToLogin_onSuccessfulRegistration() {
        server.enqueue(
            MockResponse()
                .setBody("{\"message\":\"User created successfully\"}")
                .setResponseCode(201)
        )

        composeTestRule.onNodeWithText("Username*").performTextInput("testuser")
        composeTestRule.onNodeWithText("UTA Email*").performTextInput("test@mavs.uta.edu")
        composeTestRule.onNodeWithText("Password*").performTextInput("Password123!")
        composeTestRule.onNodeWithText("Confirm Password*").performTextInput("Password123!")
        composeTestRule.onNodeWithText("Register").performClick()

        // After successful registration, we expect to be on the Login page.
        // A good check is to see if the "Login" button is now displayed.
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }
}
