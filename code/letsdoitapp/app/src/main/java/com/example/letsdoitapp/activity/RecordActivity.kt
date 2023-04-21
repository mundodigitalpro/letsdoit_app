package com.example.letsdoitapp.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.LoginActivity.Companion.useremail
import com.example.letsdoitapp.data.Runs
import com.example.letsdoitapp.utils.RunsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecordActivity : AppCompatActivity() {

    private var sportSelected: String = "Running"
    private lateinit var ivBike: ImageView
    private lateinit var ivRollerSkate: ImageView
    private lateinit var ivRunning: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var runsArrayList: ArrayList<Runs>
    private lateinit var myAdapter: RunsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_record)
        setSupportActionBar(toolbar)

        toolbar.title = getString(R.string.bar_title_record)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        ivBike = findViewById(R.id.ivBike)
        ivRollerSkate = findViewById(R.id.ivRollerSkate)
        ivRunning = findViewById(R.id.ivRunning)

        recyclerView = findViewById(R.id.rvRecords)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        runsArrayList = arrayListOf()
        myAdapter = RunsAdapter(runsArrayList)
        recyclerView.adapter = myAdapter
    }

    override fun onResume() {
        super.onResume()
        loadRecyclerView("date", Query.Direction.DESCENDING)
    }

    override fun onPause() {
        super.onPause()
        runsArrayList.clear()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.order_records_by, menu)
        return true //super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var order: Query.Direction
        val orderby_dateZA = getString(R.string.orderby_dateZA)
        val orderby_dateAZ = getString(R.string.orderby_dateAZ)
        val orderby_durationZA = getString(R.string.orderby_durationZA)
        val orderby_durationAZ = getString(R.string.orderby_durationAZ)
        val orderby_distanceZA = getString(R.string.orderby_distanceZA)
        val orderby_distanceAZ = getString(R.string.orderby_distanceAZ)
        val orderby_avgspeedZA = getString(R.string.orderby_avgspeedZA)
        val orderby_avgspeedAZ = getString(R.string.orderby_avgspeedAZ)
        val orderby_maxspeedZA = getString(R.string.orderby_maxspeedZA)
        val orderby_maxspeedAZ = getString(R.string.orderby_maxspeedAZ)

        fun sortBy(field: String, ascendingTitle: String, descendingTitle: String) {
            if (item.title == ascendingTitle) {
                item.title = descendingTitle
                order = Query.Direction.DESCENDING
            } else {
                item.title = ascendingTitle
                order = Query.Direction.ASCENDING
            }
            loadRecyclerView(field, order)
        }

        when (item.itemId) {
            R.id.orderby_date -> {
                sortBy("date", orderby_dateAZ, orderby_dateZA)
                return true
            }
            R.id.orderby_duration -> {
                sortBy("duration", orderby_durationAZ, orderby_durationZA)
                return true
            }
            R.id.orderby_distance -> {
                sortBy("distance", orderby_distanceAZ, orderby_distanceZA)
                return true
            }
            R.id.orderby_avgspeed -> {
                sortBy("avgSpeed", orderby_avgspeedAZ, orderby_avgspeedZA)
                return true
            }
            R.id.orderby_maxspeed -> {
                sortBy("maxSpeed", orderby_maxspeedAZ, orderby_maxspeedZA)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun loadRecyclerView(field: String, order: Query.Direction) {
        runsArrayList.clear() //Limpiamos el Array
        val dbRuns = FirebaseFirestore.getInstance()
        dbRuns.collection("runs$sportSelected").orderBy(field, order)
            .whereEqualTo("user", useremail)
            .get()
            .addOnSuccessListener { documents ->
                for (run in documents)
                    runsArrayList.add(run.toObject(Runs::class.java))
                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
            }
    }

    fun loadSport(v: View) {

        sportSelected = v.tag.toString()

        val colorGray = ContextCompat.getColor(this, R.color.gray_medium)
        val colorOrange = ContextCompat.getColor(this, R.color.orange)

        ivBike.setBackgroundColor(colorGray)
        ivRollerSkate.setBackgroundColor(colorGray)
        ivRunning.setBackgroundColor(colorGray)

        when (sportSelected) {
            "Bike" -> {
                ivBike.setBackgroundColor(colorOrange)
                loadRecyclerView("date", Query.Direction.DESCENDING)
            }
            "RollerSkate" -> {
                ivRollerSkate.setBackgroundColor(colorOrange)
                loadRecyclerView("date", Query.Direction.DESCENDING)
            }
            "Running" -> {
                ivRunning.setBackgroundColor(colorOrange)
                loadRecyclerView("date", Query.Direction.DESCENDING)
            }
        }
    }

    fun callHome(v: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}