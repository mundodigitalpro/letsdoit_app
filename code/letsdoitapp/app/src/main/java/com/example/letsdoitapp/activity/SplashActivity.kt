package com.example.letsdoitapp.activity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.letsdoitapp.R
import com.example.letsdoitapp.databinding.ActivitySplashBinding


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Referencias a las vistas de imagen en el diseño de actividad_splash.xml
        val imageView1 = binding.imageView1
        val imageView2 = binding.imageView2
        val imageView3 = binding.imageView3
        val imageView4 = binding.imageView4

        // Carga de animaciones desde los archivos XML en la carpeta res/anim
        val animation1 = AnimationUtils.loadAnimation(applicationContext, R.anim.rotation)
        val animation2 = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left)
        val animation3 = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_right)
        val animation4 = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)

        // Extensión para agregar una acción que se ejecutará al final de una animación
        fun Animation.doOnEnd(action: () -> Unit) {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    action()
                }
            })
        }

        // Asignación de acciones a ejecutar al final de cada animación
        animation1.doOnEnd {
            imageView1.visibility = View.GONE
            imageView2.visibility = View.VISIBLE
            imageView2.startAnimation(animation2)
        }

        animation2.doOnEnd {
            imageView2.visibility = View.GONE
            imageView3.visibility = View.VISIBLE
            imageView3.startAnimation(animation3)
        }

        animation3.doOnEnd {
            imageView3.visibility = View.GONE
            imageView4.visibility = View.VISIBLE
            imageView4.startAnimation(animation4)
        }

        // Inicio de la primera animación
        imageView1.startAnimation(animation1)

        // Tiempo de espera antes de lanzar la siguiente actividad
        val SPLASH_TIME_OUT = 4000L // Tiempo de espera en milisegundos

        // Creación de un objeto Handler con el looper principal y ejecución de la tarea después del tiempo de espera especificado
        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_TIME_OUT)
    }
}


