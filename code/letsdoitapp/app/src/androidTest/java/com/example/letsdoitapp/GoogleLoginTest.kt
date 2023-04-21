package com.example.letsdoitapp
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.letsdoitapp.activity.LoginActivity
import com.example.letsdoitapp.activity.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleLoginTest {

    @Test
    fun loginWithEmail() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = ActivityScenario.launch(LoginActivity::class.java)
        val expectedEmail = "test@example.com"
        val expectedProvider = "email"

        onView(withId(R.id.etEmail)).perform(typeText(expectedEmail), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("Test123!"), closeSoftKeyboard())
        onView(withId(R.id.tvLogin)).perform(click())

        scenario.onActivity { activity ->
            assertEquals(expectedEmail, LoginActivity.useremail)
            assertEquals(expectedProvider, LoginActivity.providerSession)
            val intent = Intent(context, MainActivity::class.java)
            val expectedIntent = activity.packageManager.resolveActivity(intent, 0)?.activityInfo?.name
            val actualIntent = activity.componentName.className
            assertEquals(expectedIntent, actualIntent)
        }
    }

    @Test
    fun loginWithGoogle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = ActivityScenario.launch(LoginActivity::class.java)
        val expectedProvider = "Google"

        onView(withId(R.id.btSignGoogle)).perform(click())
        onView(withId(R.id.cbAcept)).perform(click())
        //onView(withId(android.R.id.button1)).perform(click())

        scenario.onActivity { activity ->
            assertEquals(expectedProvider, LoginActivity.providerSession)
            val intent = Intent(context, MainActivity::class.java)
            val expectedIntent = activity.packageManager.resolveActivity(intent, 0)?.activityInfo?.name
            val actualIntent = activity.componentName.className
            assertEquals(expectedIntent, actualIntent)
        }
    }
}
