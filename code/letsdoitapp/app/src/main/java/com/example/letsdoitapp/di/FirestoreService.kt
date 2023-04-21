package com.example.letsdoitapp.di

import android.content.ContentValues
import android.util.Log
import android.widget.LinearLayout
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.LoginActivity.Companion.useremail
import com.example.letsdoitapp.activity.MainActivity
import com.example.letsdoitapp.activity.MainActivity.Companion.totalsSelectedSport
import com.example.letsdoitapp.data.Runs
import com.example.letsdoitapp.utils.Utility
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import javax.inject.Inject


class FirestoreService @Inject constructor() {
    val db = FirebaseFirestore.getInstance()
    private var totalsChecked: Int = 0

    fun deleteRunAndLinkedData(idRun: String, sport: String, ly: LinearLayout, cr: Runs) {

        if (MainActivity.activatedGPS) deleteLocations(idRun, useremail)
        if (MainActivity.countPhotos > 0) deletePicturesRun(idRun)//Si tenemos fotos las borramos

        updateTotals(cr)//pasamos como parametro el registro de tipo Runs
        checkRecords(cr, sport, useremail)
        deleteRun(idRun, sport, ly)
    }

    private fun deleteRun(idRun: String, sport: String, ly: LinearLayout) {
        //var dbRun = FirebaseFirestore.getInstance()
        db.collection("runs$sport").document(idRun).delete()
            .addOnSuccessListener {
                Utility.showCustomSnackbar(ly, " Registro borrado correctamente", R.color.orange_strong, 2000)
            }
            .addOnFailureListener {
                Utility.showCustomSnackbar(ly, " Error al Borrar Registro", R.color.orange_strong, 2000)
            }
    }

    private fun deleteLocations(idRun: String, user: String) {
        val idLocations = idRun.subSequence(user.length, idRun.length).toString()
        //var dbLocations = FirebaseFirestore.getInstance()
        db.collection("locations/$user/$idLocations")
            .get()
            .addOnSuccessListener { documents ->
                for (docLocation in documents) {
                    //var dbLoc = FirebaseFirestore.getInstance()
                    db.collection("locations/$user/$idLocations").document(docLocation.id)
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
            totalsSelectedSport.totalTime!! - Utility.getSecFromWatch(cr.duration!!)

    }

    private fun checkRecords(cr: Runs, sport: String, user: String) {

        totalsChecked = 0
        checkDistanceRecord(cr, sport, user)
        checkAvgSpeedRecord(cr, sport, user)
        checkMaxSpeedRecord(cr, sport, user)
    }

    private fun checkDistanceRecord(cr: Runs, sport: String, user: String) {
        if (cr.distance!! == totalsSelectedSport.recordDistance) {
            //var dbRecords = FirebaseFirestore.getInstance()
            db.collection("runs$sport")
                .orderBy("distance", Query.Direction.DESCENDING)
                .whereEqualTo("user", user) // Los registros del usuario particular
                .get()
                .addOnSuccessListener { documents ->

/*                    if (documents.size() == 1) MainActivity.totalsSelectedSport.recordDistance = 0.0
                    else totalsSelectedSport.recordDistance =
                        documents.documents[1].get("distance").toString().toDouble()*/

                    if (documents.isEmpty) {
                        // La lista de documentos está vacía, no hay documentos que satisfagan la consulta
                        totalsSelectedSport.recordDistance = 0.0
                    } else if (documents.size() == 1) {
                        // Solo hay un documento que satisfaga la consulta, el registro actual es el récord de velocidad
                        totalsSelectedSport.recordDistance = cr.distance
                    } else {
                        // Hay más de un documento que satisfaga la consulta, el récord de velocidad es el segundo más alto
                        totalsSelectedSport.recordDistance =
                            documents.documents[1].get("distance").toString().toDouble()
                    }

                    val collection = "totals$sport"
                    //var dbUpdateTotals = FirebaseFirestore.getInstance()
                    db.collection(collection).document(user)
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
            //var dbRecords = FirebaseFirestore.getInstance()
            db.collection("runs$sport")
                .orderBy("avgSpeed", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->

/*                    if (documents.size() == 1) MainActivity.totalsSelectedSport.recordAvgSpeed = 0.0
                    else MainActivity.totalsSelectedSport.recordAvgSpeed =
                        documents.documents[1].get("avgSpeed").toString().toDouble()*/


                    if (documents.isEmpty) {
                        // La lista de documentos está vacía, no hay documentos que satisfagan la consulta
                        totalsSelectedSport.recordAvgSpeed = 0.0
                    } else if (documents.size() == 1) {
                        // Solo hay un documento que satisfaga la consulta, el registro actual es el récord de velocidad
                        totalsSelectedSport.recordAvgSpeed = cr.avgSpeed
                    } else {
                        // Hay más de un documento que satisfaga la consulta, el récord de velocidad es el segundo más alto
                        totalsSelectedSport.recordAvgSpeed =
                            documents.documents[1].get("avgSpeed").toString().toDouble()
                    }

                    val collection = "totals$sport"
                    //var dbUpdateTotals = FirebaseFirestore.getInstance()
                    db.collection(collection).document(user)
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
            //var dbRecords = FirebaseFirestore.getInstance()
            db.collection("runs$sport")
                .orderBy("maxSpeed", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->
/*

                    if (documents.size() == 1) MainActivity.totalsSelectedSport.recordSpeed = 0.0
                    else MainActivity.totalsSelectedSport.recordSpeed =
                        documents.documents[1].get("maxSpeed").toString().toDouble()
*/

                    if (documents.isEmpty) {
                        // La lista de documentos está vacía, no hay documentos que satisfagan la consulta
                        totalsSelectedSport.recordSpeed = 0.0
                    } else if (documents.size() == 1) {
                        // Solo hay un documento que satisfaga la consulta, el registro actual es el récord de velocidad
                        totalsSelectedSport.recordSpeed = cr.maxSpeed
                    } else {
                        // Hay más de un documento que satisfaga la consulta, el récord de velocidad es el segundo más alto
                        totalsSelectedSport.recordSpeed =
                            documents.documents[1].get("maxSpeed").toString().toDouble()
                    }

                    val collection = "totals$sport"
                    //var dbUpdateTotals = FirebaseFirestore.getInstance()
                    db.collection(collection).document(user)
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
            "Bike" -> MainActivity.totalsBike = totalsSelectedSport
            "RollerSkate" -> MainActivity.totalsRollerSkate = totalsSelectedSport
            "Running" -> MainActivity.totalsRunning = totalsSelectedSport
        }

    }




}