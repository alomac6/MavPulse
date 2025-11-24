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

class LoginPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        RetrofitInstance.setBaseUrl(server.url("/").toString())

        // You might need to navigate to the login page first if it's not the start destination
        // For now, let's assume it is or we navigate via the drawer
        try {
            composeTestRule.onNodeWithText("Login").performClick()
        } catch (e: Exception) {
            // Ignore if already on the login page
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun displaysErrorMessage_onInvalidCredentials() {
        server.enqueue(
            MockResponse()
                .setBody("{\"error\":\"Invalid credentials\"}")
                .setResponseCode(401)
        )

        composeTestRule.onNodeWithText("Email").performTextInput("wrong@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpass")
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }

    @Test
    fun navigatesToHome_onSuccessfulLogin() {
        server.enqueue(
            MockResponse()
                .setBody("{\"accessToken\":\"fake-token\", \"username\":\"testuser\"}")
                .setResponseCode(200)
        )

        composeTestRule.onNodeWithText("Email").performTextInput("correct@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("correctpass")
        composeTestRule.onNodeWithText("Login").performClick()

        // Check that we've navigated away, for example, by checking for a known element on the home page
        composeTestRule.onNodeWithText("Pick your department").assertDoesNotExist()
    }
}
