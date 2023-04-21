package com.example.letsdoitapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.letsdoitapp.activity.LoginActivity
import com.example.letsdoitapp.activity.MainActivity
import org.hamcrest.Matchers.instanceOf
import org.junit.Test

class LoginActivityTest {

    @Test
    fun loginSuccess_startsMainActivity() {
        // Iniciamos la Activity
        val scenario = ActivityScenario.launch(LoginActivity::class.java)

        // Esperamos a que aparezca la vista de email y password
        onView(withId(R.id.etEmail)).perform(click())

        // Introducimos un email y contraseña válidos
        onView(withId(R.id.etEmail)).perform(typeText("josejordan@outlook.com"))
        onView(withId(R.id.etPassword)).perform(typeText("Mun1234taL?#"))

        // Hacemos clic en el botón de Login
        onView(withId(R.id.tvLogin)).perform(click())

        // Esperamos a que se inicie la MainActivity
        scenario.onActivity { activity ->
            assertThat(activity, instanceOf(MainActivity::class.java))
        }
    }
}
