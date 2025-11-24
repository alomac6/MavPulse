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

class CoursesPageTest {

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

    private fun navigateToCoursesPage() {
        // First navigate to departments page
        composeTestRule.onNodeWithText("Class Notes").performClick()
        // Mock a response for departments and click on one
        server.enqueue(
            MockResponse()
                .setBody("[{\"department\":\"Computer Science\"}]")
                .setResponseCode(200)
        )
        composeTestRule.onNodeWithText("Computer Science").performClick()
    }

    @Test
    fun displaysCourses_onSuccessfulResponse() {
        server.enqueue(
            MockResponse()
                .setBody("[{\"course_code\":\"CSE 1310\", \"course_name\":\"Intro to Programming\"}]")
                .setResponseCode(200)
        )

        navigateToCoursesPage()

        composeTestRule.onNodeWithText("CSE 1310").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyMessage_onEmptyResponse() {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        navigateToCoursesPage()

        composeTestRule.onNodeWithText("No courses found for this department.", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysErrorAndRetryButton_onApiFailure() {
        server.enqueue(MockResponse().setResponseCode(500))

        navigateToCoursesPage()

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        server.enqueue(
            MockResponse()
                .setBody("[{\"course_code\":\"RETRY 101\", \"course_name\":\"Retried Course\"}]")
                .setResponseCode(200)
        )
        composeTestRule.onNodeWithText("Retry").performClick()

        composeTestRule.onNodeWithText("RETRY 101").assertIsDisplayed()
    }
}
