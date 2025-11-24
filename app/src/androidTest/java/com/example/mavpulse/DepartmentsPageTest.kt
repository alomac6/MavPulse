package com.example.mavpulse

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.mavpulse.network.RetrofitInstance
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DepartmentsPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        RetrofitInstance.setBaseUrl(server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun navigateToDepartmentsPage() {
        // A more robust solution would use test tags.
        composeTestRule.onNodeWithText("Class Notes").performClick()
    }

    @Test
    fun displaysDepartments_onSuccessfulResponse() {
        server.enqueue(
            MockResponse()
                .setBody("[{\"department\":\"Computer Science\"},{\"department\":\"Mathematics\"}]")
                .setResponseCode(200)
        )

        navigateToDepartmentsPage()

        composeTestRule.onNodeWithText("Computer Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mathematics").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyMessage_onEmptyResponse() {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        navigateToDepartmentsPage()

        composeTestRule.onNodeWithText("No courses found for this department.", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysErrorAndRetryButton_onApiFailure() {
        server.enqueue(MockResponse().setResponseCode(500))

        navigateToDepartmentsPage()

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        // Mock a successful response for the retry attempt
        server.enqueue(
            MockResponse()
                .setBody("[{\"department\":\"Retried Department\"}]")
                .setResponseCode(200)
        )
        composeTestRule.onNodeWithText("Retry").performClick()

        composeTestRule.onNodeWithText("Retried Department").assertIsDisplayed()
    }
}
