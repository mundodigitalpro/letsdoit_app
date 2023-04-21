package com.example.letsdoitapp.utils

import android.animation.ObjectAnimator
import android.content.ContentValues
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.LoginActivity.Companion.useremail
import com.example.letsdoitapp.activity.MainActivity.Companion.activatedGPS
import com.example.letsdoitapp.activity.MainActivity.Companion.countPhotos
import com.example.letsdoitapp.activity.MainActivity.Companion.totalsBike
import com.example.letsdoitapp.activity.MainActivity.Companion.totalsRollerSkate
import com.example.letsdoitapp.activity.MainActivity.Companion.totalsRunning
import com.example.letsdoitapp.activity.MainActivity.Companion.totalsSelectedSport
import com.example.letsdoitapp.data.Runs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.concurrent.TimeUnit

object Utility {

    private var totalsChecked: Int = 0

    /* FUNCIONES DE ANIMACION Y CAMBIOS DE ATRIBUTOS */
    fun setHeightLinearLayout(ly: LinearLayout, value: Int) {
        val params: LinearLayout.LayoutParams = ly.layoutParams as LinearLayout.LayoutParams
        params.height = value
        ly.layoutParams = params
    }

    fun animateViewofInt(v: View, attr: String, value: Int, time: Long) {
        ObjectAnimator.ofInt(v, attr, value).apply {
            duration = time
            start()
        }
    }

    fun animateViewofFloat(v: View, attr: String, value: Float, time: Long) {
        ObjectAnimator.ofFloat(v, attr, value).apply {
            duration = time
            start()
        }
    }

    fun getSecFromWatch(watch: String): Int {
        var secs = 0
        var w: String = watch
        if (w.length == 5) w = "00:" + w
        // 00:00:00
        secs += w.subSequence(0, 2).toString().toInt() * 3600
        secs += w.subSequence(3, 5).toString().toInt() * 60
        secs += w.subSequence(6, 8).toString().toInt()
        return secs
    }

    fun getFormattedStopWatch(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }

    //Función para redondear a 2 decimales
    fun roundNumber(data: String, decimals: Int): String {
        var d: String = data
        var p = d.indexOf(".", 0)

        if (p != null) {
            var limit: Int = p + decimals + 1
            if (d.length <= p + decimals + 1) limit = d.length //-1
            d = d.subSequence(0, limit).toString()
        }
        return d
    }

    fun getFormattedTotalTime(secs: Long): String {
        var seconds: Long = secs
        var total: String = ""

        //1 dia = 86400s
        //1 mes (30 dias) = 2592000s
        //365 dias = 31536000s

        var years: Int = 0
        while (seconds >= 31536000) {
            years++; seconds -= 31536000; }

        var months: Int = 0
        while (seconds >= 2592000) {
            months++; seconds -= 2592000; }

        var days: Int = 0
        while (seconds >= 86400) {
            days++; seconds -= 86400; }

        if (years > 0) total += "${years}y "
        if (months > 0) total += "${months}m "
        if (days > 0) total += "${days}d "

        total += getFormattedStopWatch(seconds * 1000)

        return total
    }

    /* FUNCIONES DE BORRADO DE CARRERA */

    //ORDEN DE BORRADO
    //Si teniamos el GPS, borramos las ubicaciones
    //Si habia fotos, borramos todas las fotos
    //Revisamos los totales y los records


    fun deleteRunAndLinkedData(idRun: String, sport: String, ly: LinearLayout, cr: Runs) {

        if (activatedGPS) deleteLocations(idRun, useremail)
        if (countPhotos > 0) deletePicturesRun(idRun)//Si tenemos fotos las borramos

        updateTotals(cr)//pasamos como parametro el registro de tipo Runs
        checkRecords(cr, sport, useremail)
        deleteRun(idRun, sport, ly)
    }

    //Borramos la carrera
    private fun deleteRun(idRun: String, sport: String, ly: LinearLayout) {
        var dbRun = FirebaseFirestore.getInstance()
        dbRun.collection("runs$sport").document(idRun).delete()
            .addOnSuccessListener {
                  showCustomSnackbar(ly, "Registro Borrado Correctamente", R.color.orange_strong, 2000)
            }
            .addOnFailureListener {
                showCustomSnackbar(ly, "Error al Borrar Registro", R.color.orange_strong, 2000)
            }
    }

    private fun deleteLocations(idRun: String, user: String) {
        var idLocations = idRun.subSequence(user.length, idRun.length).toString()

        var dbLocations = FirebaseFirestore.getInstance()
        dbLocations.collection("locations/$user/$idLocations")
            .get()
            .addOnSuccessListener { documents ->
                for (docLocation in documents) {
                    var dbLoc = FirebaseFirestore.getInstance()
                    dbLoc.collection("locations/$user/$idLocations").document(docLocation.id)
                        .delete()
                }

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)

            }
    }

    private fun deletePicturesRun(idRun: String) {
        val idFolder = idRun.subSequence(useremail.length, idRun.length).toString()
        // val delRef = FirebaseStorage.getInstance().getReference("images/$useremail/$idFolder")
        var delRef: StorageReference
        val storage = Firebase.storage
        val listRef = storage.reference.child("images/$useremail/$idFolder")
        listRef.listAll()
            // .addOnSuccessListener { (items, prefixes) ->
            .addOnSuccessListener { listResult ->
                //items.forEach { item ->
                listResult.items.forEach { item ->
                    val storageRef = storage.reference
                    //val deleteRef = storageRef.child((item.path))
                    delRef = storageRef.child((item.path))
                    delRef.delete()
                }
            }
            .addOnFailureListener {
            }
    }

    private fun updateTotals(cr: Runs) {
        totalsSelectedSport.totalDistance = totalsSelectedSport.totalDistance!! - cr.distance!!
        totalsSelectedSport.totalRuns = totalsSelectedSport.totalRuns!! - 1
        totalsSelectedSport.totalTime =
            totalsSelectedSport.totalTime!! - getSecFromWatch(cr.duration!!)

    }

    private fun checkRecords(cr: Runs, sport: String, user: String) {

        totalsChecked = 0
        checkDistanceRecord(cr, sport, user)
        checkAvgSpeedRecord(cr, sport, user)
        checkMaxSpeedRecord(cr, sport, user)
    }

    private fun checkDistanceRecord(cr: Runs, sport: String, user: String) {
        if (cr.distance!! == totalsSelectedSport.recordDistance) {
            var dbRecords = FirebaseFirestore.getInstance()
            dbRecords.collection("runs$sport")
                .orderBy("distance", Query.Direction.DESCENDING)
                .whereEqualTo("user", user) // Los registros del usuario particular
                .get()
                .addOnSuccessListener { documents ->

/*                    if (documents.size() == 1) totalsSelectedSport.recordDistance = 0.0
                    else totalsSelectedSport.recordDistance =
                        documents.documents[1].get("distance").toString().toDouble()*/

                    if (documents.size() < 2) {
                        totalsSelectedSport.recordDistance = 0.0
                    } else {
                        totalsSelectedSport.recordDistance =
                            documents.documents[1].get("distance").toString().toDouble()
                    }

                    var collection = "totals$sport"
                    var dbUpdateTotals = FirebaseFirestore.getInstance()
                    dbUpdateTotals.collection(collection).document(user)
                        .update("recordDistance", totalsSelectedSport.recordDistance)

                    totalsChecked++
                    if (totalsChecked == 3) refreshTotalsSport(sport)

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
                }
        }
    }

    private fun checkAvgSpeedRecord(cr: Runs, sport: String, user: String) {
        if (cr.avgSpeed!! == totalsSelectedSport.recordAvgSpeed) {
            var dbRecords = FirebaseFirestore.getInstance()
            dbRecords.collection("runs$sport")
                .orderBy("avgSpeed", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->

/*                    if (documents.size() == 1) totalsSelectedSport.recordAvgSpeed = 0.0
                    else totalsSelectedSport.recordAvgSpeed = documents.documents[1].get("avgSpeed").toString().toDouble()*/

                    if (documents.size() < 2) {
                        totalsSelectedSport.recordAvgSpeed = 0.0
                    } else {
                        totalsSelectedSport.recordAvgSpeed =
                            documents.documents[1].get("avgSpeed").toString().toDouble()
                    }

                    var collection = "totals$sport"
                    var dbUpdateTotals = FirebaseFirestore.getInstance()
                    dbUpdateTotals.collection(collection).document(user)
                        .update("recordAvgSpeed", totalsSelectedSport.recordAvgSpeed)

                    totalsChecked++
                    if (totalsChecked == 3) refreshTotalsSport(sport)

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
                }
        }
    }

    private fun checkMaxSpeedRecord(cr: Runs, sport: String, user: String) {
        if (cr.maxSpeed!! == totalsSelectedSport.recordSpeed) {
            var dbRecords = FirebaseFirestore.getInstance()
            dbRecords.collection("runs$sport")
                .orderBy("maxSpeed", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->

/*                    if (documents.size() == 1) totalsSelectedSport.recordSpeed = 0.0
                    else totalsSelectedSport.recordSpeed =
                        documents.documents[1].get("maxSpeed").toString().toDouble()*/

                    if (documents.size() < 2) {
                        totalsSelectedSport.recordSpeed = 0.0
                    } else {
                        totalsSelectedSport.recordSpeed =
                            documents.documents[1].get("maxSpeed").toString().toDouble()
                    }

                    var collection = "totals$sport"
                    var dbUpdateTotals = FirebaseFirestore.getInstance()
                    dbUpdateTotals.collection(collection).document(user)
                        .update("recordSpeed", totalsSelectedSport.recordSpeed)

                    totalsChecked++
                    if (totalsChecked == 3) refreshTotalsSport(sport)

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
                }
        }
    }

    private fun refreshTotalsSport(sport: String) {
        when (sport) {
            "Bike" -> totalsBike = totalsSelectedSport
            "RollerSkate" -> totalsRollerSkate = totalsSelectedSport
            "Running" -> totalsRunning = totalsSelectedSport
        }
    }

    fun showCustomSnackbar(
        view: View,
        message: String,
        backgroundColor: Int,
        duration: Int
    ) {
        val snack = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.custom_snackbar, view.rootView as ViewGroup, false)

        // Cambiar el color de fondo
        snack.view.setBackgroundColor(ContextCompat.getColor(view.context, backgroundColor))

        // Cambiar el texto del Snackbar
        val tvMessage = customView.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message

        // Cambiar la fuente del Snackbar
        tvMessage.typeface = Typeface.create("sans-serif-light", Typeface.BOLD)

        // Agregar la vista personalizada a la Snackbar
        val snackbarLayout = snack.view as Snackbar.SnackbarLayout
        snackbarLayout.addView(customView, 0)

        // Agregar un efecto de animación
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_up)
        snack.view.startAnimation(animation)
        snack.duration = duration
        snack.show()
    }

}