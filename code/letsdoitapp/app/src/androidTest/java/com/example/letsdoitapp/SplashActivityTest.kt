package com.example.letsdoitapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.letsdoitapp.activity.SplashActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SplashActivity::class.java)

    @Test
    fun testActivityStart() {
        // Espera a que la animación termine y la actividad LoginActivity se inicie
        Thread.sleep(5000)

        // Verifica que la actividad LoginActivity se ha iniciado correctamente
        onView(withId(R.id.activity_login)).check(matches(isDisplayed()))
    }

}
