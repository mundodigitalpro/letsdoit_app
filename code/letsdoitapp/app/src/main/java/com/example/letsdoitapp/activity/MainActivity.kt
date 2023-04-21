package com.example.letsdoitapp.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.LoginActivity.Companion.providerSession
import com.example.letsdoitapp.activity.LoginActivity.Companion.useremail
import com.example.letsdoitapp.data.Level
import com.example.letsdoitapp.data.Runs
import com.example.letsdoitapp.data.Totals
import com.example.letsdoitapp.databinding.ActivityMainBinding
import com.example.letsdoitapp.di.FirestoreService
import com.example.letsdoitapp.utils.Camara
import com.example.letsdoitapp.utils.Constants.APP
import com.example.letsdoitapp.utils.Constants.AUTHOR
import com.example.letsdoitapp.utils.Constants.CENTRE
import com.example.letsdoitapp.utils.Constants.EMAIL
import com.example.letsdoitapp.utils.Constants.INTERVAL_LOCATION
import com.example.letsdoitapp.utils.Constants.LIMIT_DISTANCE_ACCEPTED_BIKE
import com.example.letsdoitapp.utils.Constants.LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE
import com.example.letsdoitapp.utils.Constants.LIMIT_DISTANCE_ACCEPTED_RUNNING
import com.example.letsdoitapp.utils.Constants.PAYPAL_CLIENT_ID
import com.example.letsdoitapp.utils.Constants.key_challengeAutofinish
import com.example.letsdoitapp.utils.Constants.key_challengeDistance
import com.example.letsdoitapp.utils.Constants.key_challengeDurationHH
import com.example.letsdoitapp.utils.Constants.key_challengeDurationMM
import com.example.letsdoitapp.utils.Constants.key_challengeDurationSS
import com.example.letsdoitapp.utils.Constants.key_challengeNofify
import com.example.letsdoitapp.utils.Constants.key_hardVol
import com.example.letsdoitapp.utils.Constants.key_intervalDuration
import com.example.letsdoitapp.utils.Constants.key_maxCircularSeekBar
import com.example.letsdoitapp.utils.Constants.key_modeChallenge
import com.example.letsdoitapp.utils.Constants.key_modeChallengeDistance
import com.example.letsdoitapp.utils.Constants.key_modeChallengeDuration
import com.example.letsdoitapp.utils.Constants.key_modeInterval
import com.example.letsdoitapp.utils.Constants.key_notifyVol
import com.example.letsdoitapp.utils.Constants.key_premium
import com.example.letsdoitapp.utils.Constants.key_progressCircularSeekBar
import com.example.letsdoitapp.utils.Constants.key_provider
import com.example.letsdoitapp.utils.Constants.key_runningTime
import com.example.letsdoitapp.utils.Constants.key_selectedSport
import com.example.letsdoitapp.utils.Constants.key_softVol
import com.example.letsdoitapp.utils.Constants.key_userApp
import com.example.letsdoitapp.utils.Constants.key_walkingTime
import com.example.letsdoitapp.utils.Utility.animateViewofFloat
import com.example.letsdoitapp.utils.Utility.animateViewofInt
import com.example.letsdoitapp.utils.Utility.getFormattedStopWatch
import com.example.letsdoitapp.utils.Utility.getFormattedTotalTime
import com.example.letsdoitapp.utils.Utility.getSecFromWatch
import com.example.letsdoitapp.utils.Utility.roundNumber
import com.example.letsdoitapp.utils.Utility.setHeightLinearLayout
import com.example.letsdoitapp.utils.Utility.showCustomSnackbar
import com.example.letsdoitapp.utils.Widget
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.UserAction
import dagger.hilt.android.AndroidEntryPoint
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {//END MAIN

    companion object {


        @SuppressLint("StaticFieldLeak")
        lateinit var mainContext: Context
        var activatedGPS: Boolean = true
        var isPremium: Boolean = false
        lateinit var totalsSelectedSport: Totals
        lateinit var totalsBike: Totals
        lateinit var totalsRollerSkate: Totals
        lateinit var totalsRunning: Totals

        val REQUIRED_PERMISSIONS_GPS =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        var countPhotos: Int = 0 // SE cuentan las imagenes que se pueden hacer
        var lastimage: String = ""

        lateinit var chronoWidget: String
        lateinit var distanceWidget: String

        lateinit var editor: SharedPreferences.Editor

        fun becamePremium() {//GUARDA EL USUARIO PREMIUM
            editor.apply {
                putBoolean(key_premium, true)
            }.apply()
        }


    }

    //RECORDS
    private var record_distancia_conseguido: Boolean = true
    private var record_avgSpeed_conseguido: Boolean = true
    private var record_speed_conseguido: Boolean = true

    private var mHandler: Handler? = null
    private var mInterval = 1000
    private var timeInSeconds = 0L
    private var rounds: Int = 1
    private var startButtonClicked = false
    private var isRunning = true

    private lateinit var drawer: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    private lateinit var rlMain: RelativeLayout

    private var challengeDistance: Float = 0f
    private var challengeDuration: Int = 0
    private var timeRunning = 0
    private var roundInterval = 300
    private var hardTime = true

    private lateinit var lyMap: LinearLayout
    private lateinit var lyIntervalModeSpace: LinearLayout
    private lateinit var lyChallengesSpace: LinearLayout
    private lateinit var lySettingsVolumesSpace: LinearLayout
    private lateinit var lySoftTrack: LinearLayout
    private lateinit var lySoftVolume: LinearLayout
    private lateinit var lyFragmentMap: LinearLayout
    private lateinit var lyIntervalMode: LinearLayout
    private lateinit var lyChallenges: LinearLayout
    private lateinit var lySettingsVolumes: LinearLayout
    private lateinit var lyChallengeDuration: LinearLayout
    private lateinit var lyChallengeDistance: LinearLayout
    private lateinit var btStart: LinearLayout
    private lateinit var lyPopupRun: LinearLayout
    private lateinit var lyWindow: LinearLayout
    private lateinit var lyChronoProgressBg: LinearLayout
    private lateinit var lyRoundProgressBg: LinearLayout
    private lateinit var lySportBike: LinearLayout
    private lateinit var lySportRollerSkate: LinearLayout
    private lateinit var lySportRunning: LinearLayout
    private lateinit var lyOpenerMap: LinearLayout


    private lateinit var npChallengeDistance: NumberPicker
    private lateinit var npChallengeDurationHH: NumberPicker
    private lateinit var npChallengeDurationMM: NumberPicker
    private lateinit var npChallengeDurationSS: NumberPicker
    private lateinit var npDurationInterval: NumberPicker

    private lateinit var tvRunningTime: TextView
    private lateinit var tvWalkingTime: TextView
    private lateinit var tvReset: TextView
    private lateinit var tvChrono: TextView
    private lateinit var tvRounds: TextView
    private lateinit var tvDistanceRecord: TextView
    private lateinit var tvAvgSpeedRecord: TextView
    private lateinit var tvMaxSpeedRecord: TextView
    private lateinit var tvChallengeDuration: TextView
    private lateinit var tvChallengeDistance: TextView
    private lateinit var btStartLabel: TextView
    private lateinit var tvHardPosition: TextView
    private lateinit var tvHardRemaining: TextView
    private lateinit var tvSoftPosition: TextView
    private lateinit var tvSoftRemaining: TextView
    private lateinit var tvCurrentDistance: TextView
    private lateinit var tvCurrentAvgSpeed: TextView
    private lateinit var tvCurrentSpeed: TextView
    private lateinit var tvTotalRunsLevel: TextView
    private lateinit var tvTotalDistanceLevel: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalTime: TextView

    private lateinit var dateRun: String
    private lateinit var startTimeRun: String


    private lateinit var swIntervalMode: Switch
    private lateinit var swVolumes: Switch
    private lateinit var swChallenges: Switch

    private lateinit var csbRunWalk: CircularSeekBar
    private lateinit var csbChallengeDistance: CircularSeekBar
    private lateinit var csbCurrentDistance: CircularSeekBar
    private lateinit var csbRecordDistance: CircularSeekBar
    private lateinit var csbCurrentAvgSpeed: CircularSeekBar
    private lateinit var csbRecordAvgSpeed: CircularSeekBar
    private lateinit var csbCurrentSpeed: CircularSeekBar
    private lateinit var csbCurrentMaxSpeed: CircularSeekBar
    private lateinit var csbRecordSpeed: CircularSeekBar


    private lateinit var cbNotify: CheckBox
    private lateinit var cbAutoFinish: CheckBox

    private var widthScreenPixels: Int = 0
    private var heightScreenPixels: Int = 0
    private var widthAnimations: Int = 0

    private lateinit var fbCamara: FloatingActionButton

    private var mpNotify: MediaPlayer? = null
    private var mpHard: MediaPlayer? = null
    private var mpSoft: MediaPlayer? = null
    private lateinit var sbHardVolume: SeekBar
    private lateinit var sbSoftVolume: SeekBar
    private lateinit var sbNotifyVolume: SeekBar
    private lateinit var sbHardTrack: SeekBar
    private lateinit var sbSoftTrack: SeekBar

    //private var activatedGPS = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_ID = 42

    private var flagSavedLocation = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var initialLatitude: Double = 0.0
    private var initialLongitude: Double = 0.0

    private var distance: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var speed: Double = 0.0

    private var minAltitude: Double? = null
    private var maxAltitude: Double? = null
    private var minLatitude: Double? = null
    private var maxLatitude: Double? = null
    private var minLongitude: Double? = null
    private var maxLongitude: Double? = null

    private lateinit var map: GoogleMap
    private var mapCentered = true

    private lateinit var ivOpenClose: ImageView
    private lateinit var ivTypeMap: ImageView

    private lateinit var lyOpenerButton: LinearLayout
    private val LOCATION_PERMISSION_REQ_CODE = 1000
    private var LIMIT_DISTANCE_ACCEPTED: Double = 0.0
    private lateinit var sportSelected: String
    private var sportsLoaded: Int = 0
    private lateinit var listPoints: Iterable<LatLng>

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var levelBike: Level
    private lateinit var levelRollerSkate: Level
    private lateinit var levelRunning: Level
    private lateinit var levelSelectedSport: Level
    private lateinit var levelsListBike: ArrayList<Level>
    private lateinit var levelsListRollerSkate: ArrayList<Level>
    private lateinit var levelsListRunning: ArrayList<Level>

    private lateinit var medalsListBikeDistance: ArrayList<Double>
    private lateinit var medalsListBikeAvgSpeed: ArrayList<Double>
    private lateinit var medalsListBikeMaxSpeed: ArrayList<Double>

    private lateinit var medalsListRollerSkateDistance: ArrayList<Double>
    private lateinit var medalsListRollerSkateAvgSpeed: ArrayList<Double>
    private lateinit var medalsListRollerSkateMaxSpeed: ArrayList<Double>

    private lateinit var medalsListRunningDistance: ArrayList<Double>
    private lateinit var medalsListRunningAvgSpeed: ArrayList<Double>
    private lateinit var medalsListRunningMaxSpeed: ArrayList<Double>

    private lateinit var medalsListSportSelectedDistance: ArrayList<Double>
    private lateinit var medalsListSportSelectedAvgSpeed: ArrayList<Double>
    private lateinit var medalsListSportSelectedMaxSpeed: ArrayList<Double>

    private var recDistanceGold: Boolean = false
    private var recDistanceSilver: Boolean = false
    private var recDistanceBronze: Boolean = false
    private var recAvgSpeedGold: Boolean = false
    private var recAvgSpeedSilver: Boolean = false
    private var recAvgSpeedBronze: Boolean = false
    private var recMaxSpeedGold: Boolean = false
    private var recMaxSpeedSilver: Boolean = false
    private var recMaxSpeedBronze: Boolean = false

    private lateinit var saveDuration: String
    private lateinit var saveDistance: String
    private lateinit var saveAvgSpeed: String
    private lateinit var saveMaxSpeed: String
    private var centerLatitude: Double? = null
    private var centerLongitude: Double? = null
    private lateinit var medalDistance: String
    private lateinit var medalAvgSpeed: String
    private lateinit var medalMaxSpeed: String

    private lateinit var widget: Widget
    private lateinit var mAppWidgetManager: AppWidgetManager

    //Banner Premium
    private lateinit var lyBanner: LinearLayout
    private lateinit var lyBannerSlogan: LinearLayout
    private lateinit var lySports: LinearLayout
    private lateinit var lyOptionsRun: LinearLayout
    private lateinit var lyCurrentData: LinearLayout


    @Inject
    lateinit var firestore: FirestoreService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mainContext = this
        initializeFusedLocationClient(mainContext)
        init()
        initWidget()
        loadFromDB()
        showBanner()
        initPayment()
    }

    private fun initializeFusedLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val helpItem = menu.findItem(R.id.action_help)

        helpItem.setOnMenuItemClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_help, null)

            val helpTitle = dialogView.findViewById<TextView>(R.id.help_title)
            helpTitle.text = getString(R.string.manual)

            val helpText = dialogView.findViewById<TextView>(R.id.help_text)
            helpText.text = getString(R.string.helpText)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ -> }
                .show()

            true
        }

        return true
    }


    private fun initPayment() {
        val config = CheckoutConfig(
            application = application,
            clientId = PAYPAL_CLIENT_ID,
            environment = Environment.SANDBOX,
            currencyCode = CurrencyCode.USD,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true
            )
        )
        PayPalCheckout.setConfig(config)
    }


    // Función para mostrar el banner
    fun showBanner() {
        // si es premium devuelve un true si no es devuelve un false
        isPremium = sharedPreferences.getBoolean(key_premium, false)
        if (isPremium) {
            lyBanner.visibility = GONE
            lyBannerSlogan.visibility = GONE
            lyCurrentData.visibility = VISIBLE
            lySports.visibility = VISIBLE
            lyOptionsRun.visibility = VISIBLE
            lyOpenerMap.visibility = VISIBLE
        } else {
            lyBanner.visibility = VISIBLE
            lyBannerSlogan.visibility = VISIBLE
            lyCurrentData.visibility = GONE
            lySports.visibility = GONE
            lyOptionsRun.visibility = GONE
            lyOpenerMap.visibility = GONE
        }

    }


    private fun initWidget() {
        chronoWidget = ""
        distanceWidget = ""
        widget = Widget()
        mAppWidgetManager = AppWidgetManager.getInstance(mainContext)!!
        updateWidgets()
    }

    private fun updateWidgets() {

        chronoWidget = tvChrono.text.toString()
        distanceWidget = roundNumber(distance.toString(), 1)//distancia redondeada a 1 decimal

        val intent = Intent(application, Widget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = mAppWidgetManager.getAppWidgetIds(ComponentName(application, Widget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }


    private fun loadFromDB() {
        loadTotalUser()
        loadMedalsUser()
    }

    private fun loadTotalUser() {
        loadTotalSport("Bike")
        loadTotalSport("RollerSkate")
        loadTotalSport("Running")
    }

    //FIREBASE UPDATE
    private fun loadTotalSport(sport: String) {
        var collection = "totals$sport"
        //var dbTotalsUser = FirebaseFirestore.getInstance()
        firestore.db.collection(collection).document(useremail)
            .get() // Recibimos los datos
            .addOnSuccessListener { document ->
                if (document.data?.size != null) {
                    var total = document.toObject(Totals::class.java)
                    when (sport) {
                        "Bike" -> totalsBike = total!!
                        "RollerSkate" -> totalsRollerSkate = total!!
                        "Running" -> totalsRunning = total!!
                    }

                } else {
                    //val dbTotal: FirebaseFirestore = FirebaseFirestore.getInstance()
                    firestore.db.collection(collection).document(useremail).set(
                        hashMapOf( // guardamos los datos
                            "recordAvgSpeed" to 0.0,
                            "recordDistance" to 0.0,
                            "recordSpeed" to 0.0,
                            "totalDistance" to 0.0,
                            "totalRuns" to 0,
                            "totalTime" to 0
                        )
                    )
                }
                sportsLoaded++
                setLevelSport(sport) //Comprobamos en que nivel se encuentra
                //cuando sean los deportes 3 cargamos el deporte seleccionado
                if (sportsLoaded == 3) selectSport(sportSelected)

            }
            .addOnFailureListener { exception ->
                Log.d("ERROR loadTotalsUser", "get failed with ", exception)
            }

    }

    //FIREBASE SET LEVEL
    private fun setLevelSport(sport: String) {
        //val dbLevels: FirebaseFirestore = FirebaseFirestore.getInstance()
        firestore.db.collection("levels$sport")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    when (sport) {
                        "Bike" -> levelsListBike.add(document.toObject(Level::class.java))
                        "RollerSkate" -> levelsListRollerSkate.add(document.toObject(Level::class.java))
                        "Running" -> levelsListRunning.add(document.toObject(Level::class.java))
                    }
                }
                when (sport) {
                    "Bike" -> setLevelBike()
                    "RollerSkate" -> setLevelRollerSkate()
                    "Running" -> setLevelRunning()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun setLevelBike() {
        var lyNavLevelBike = findViewById<LinearLayout>(R.id.lyNavLevelBike)
        if (totalsBike.totalTime!! == 0) setHeightLinearLayout(lyNavLevelBike, 0)
        else {
            setHeightLinearLayout(lyNavLevelBike, 300)
            for (level in levelsListBike) {
                //Si el total de carreras/distancia está por debajo del requisito, ese es mi nivel
                if (totalsBike.totalRuns!! < level.RunsTarget!!
                    || totalsBike.totalDistance!! < level.DistanceTarget!!
                ) {
                    levelBike.name = level.name!!
                    levelBike.image = level.image!!
                    levelBike.RunsTarget = level.RunsTarget!!
                    levelBike.DistanceTarget = level.DistanceTarget!!
                    break
                }
            }
            var ivLevelBike = findViewById<ImageView>(R.id.ivLevelBike)
            var tvTotalTimeBike = findViewById<TextView>(R.id.tvTotalTimeBike)
            var tvTotalRunsBike = findViewById<TextView>(R.id.tvTotalRunsBike)
            var tvTotalDistanceBike = findViewById<TextView>(R.id.tvTotalDistanceBike)
            var tvNumberLevelBike = findViewById<TextView>(R.id.tvNumberLevelBike)
            var levelText = "${getString(R.string.level)} ${levelBike.image!!.subSequence(6, 7)}"
            tvNumberLevelBike.text = levelText
            var tt = getFormattedTotalTime(totalsBike.totalTime!!.toLong())
            tvTotalTimeBike.text = tt

            when (levelBike.image) {
                "level_1" -> ivLevelBike.setImageResource(R.drawable.level_1)
                "level_2" -> ivLevelBike.setImageResource(R.drawable.level_2)
                "level_3" -> ivLevelBike.setImageResource(R.drawable.level_3)
//                "level_4" -> ivLevelBike.setImageResource(R.drawable.level_4)
//                "level_5" -> ivLevelBike.setImageResource(R.drawable.level_5)
//                "level_6" -> ivLevelBike.setImageResource(R.drawable.level_6)
//                "level_7" -> ivLevelBike.setImageResource(R.drawable.level_7)
            }
            tvTotalRunsBike.text = "${totalsBike.totalRuns}/${levelBike.RunsTarget}"
            var porcent =
                totalsBike.totalDistance!!.toInt() * 100 / levelBike.DistanceTarget!!.toInt()
            tvTotalDistanceBike.text = "${porcent}%"

            var csbDistanceBike = findViewById<CircularSeekBar>(R.id.csbDistanceBike)
            csbDistanceBike.max = levelBike.DistanceTarget!!.toFloat()
            if (totalsBike.totalDistance!! >= levelBike.DistanceTarget!!.toDouble())
                csbDistanceBike.progress = csbDistanceBike.max
            else
                csbDistanceBike.progress = totalsBike.totalDistance!!.toFloat()

            var csbRunsBike = findViewById<CircularSeekBar>(R.id.csbRunsBike)
            csbRunsBike.max = levelBike.RunsTarget!!.toFloat()
            if (totalsBike.totalRuns!! >= levelBike.RunsTarget!!.toInt())
                csbRunsBike.progress = csbRunsBike.max
            else
                csbRunsBike.progress = totalsBike.totalRuns!!.toFloat()

        }
    }

    private fun setLevelRollerSkate() {

        var lyNavLevelRollerSkate = findViewById<LinearLayout>(R.id.lyNavLevelRollerSkate)
        if (totalsRollerSkate.totalTime!! == 0) setHeightLinearLayout(lyNavLevelRollerSkate, 0)
        else {

            setHeightLinearLayout(lyNavLevelRollerSkate, 300)
            for (level in levelsListRollerSkate) {
                if (totalsRollerSkate.totalRuns!! < level.RunsTarget!!.toInt()
                    || totalsRollerSkate.totalDistance!! < level.DistanceTarget!!.toDouble()
                ) {

                    levelRollerSkate.name = level.name!!
                    levelRollerSkate.image = level.image!!
                    levelRollerSkate.RunsTarget = level.RunsTarget!!
                    levelRollerSkate.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelRollerSkate = findViewById<ImageView>(R.id.ivLevelRollerSkate)
            var tvTotalTimeRollerSkate = findViewById<TextView>(R.id.tvTotalTimeRollerSkate)
            var tvTotalRunsRollerSkate = findViewById<TextView>(R.id.tvTotalRunsRollerSkate)
            var tvTotalDistanceRollerSkate = findViewById<TextView>(R.id.tvTotalDistanceRollerSkate)

            var tvNumberLevelRollerSkate = findViewById<TextView>(R.id.tvNumberLevelRollerSkate)
            var levelText = "${getString(R.string.level)} ${
                levelRollerSkate.image!!.subSequence(6, 7).toString()
            }"
            tvNumberLevelRollerSkate.text = levelText

            var tt = getFormattedTotalTime(totalsRollerSkate.totalTime!!.toLong())
            tvTotalTimeRollerSkate.text = tt

            when (levelRollerSkate.image) {
                "level_1" -> ivLevelRollerSkate.setImageResource(R.drawable.level_1)
                "level_2" -> ivLevelRollerSkate.setImageResource(R.drawable.level_2)
                "level_3" -> ivLevelRollerSkate.setImageResource(R.drawable.level_3)
            }


            tvTotalRunsRollerSkate.text =
                "${totalsRollerSkate.totalRuns}/${levelRollerSkate.RunsTarget}"

            var porcent =
                totalsRollerSkate.totalDistance!!.toInt() * 100 / levelRollerSkate.DistanceTarget!!.toInt()
            tvTotalDistanceRollerSkate.text = "${porcent.toInt()}%"

            var csbDistanceRollerSkate = findViewById<CircularSeekBar>(R.id.csbDistanceRollerSkate)
            csbDistanceRollerSkate.max = levelRollerSkate.DistanceTarget!!.toFloat()
            if (totalsRollerSkate.totalDistance!! >= levelRollerSkate.DistanceTarget!!.toDouble())
                csbDistanceRollerSkate.progress = csbDistanceRollerSkate.max
            else
                csbDistanceRollerSkate.progress = totalsRollerSkate.totalDistance!!.toFloat()

            var csbRunsRollerSkate = findViewById<CircularSeekBar>(R.id.csbRunsRollerSkate)
            csbRunsRollerSkate.max = levelRollerSkate.RunsTarget!!.toFloat()
            if (totalsRollerSkate.totalRuns!! >= levelRollerSkate.RunsTarget!!.toInt())
                csbRunsRollerSkate.progress = csbRunsRollerSkate.max
            else
                csbRunsRollerSkate.progress = totalsRollerSkate.totalRuns!!.toFloat()
        }
    }

    private fun setLevelRunning() {
        var lyNavLevelRunning = findViewById<LinearLayout>(R.id.lyNavLevelRunning)
        if (totalsRunning.totalTime!! == 0) setHeightLinearLayout(lyNavLevelRunning, 0)
        else {

            setHeightLinearLayout(lyNavLevelRunning, 300)
            for (level in levelsListRunning) {
                if (totalsRunning.totalRuns!! < level.RunsTarget!!.toInt()
                    || totalsRunning.totalDistance!! < level.DistanceTarget!!.toDouble()
                ) {

                    levelRunning.name = level.name!!
                    levelRunning.image = level.image!!
                    levelRunning.RunsTarget = level.RunsTarget!!
                    levelRunning.DistanceTarget = level.DistanceTarget!!

                    break
                }
            }

            var ivLevelRunning = findViewById<ImageView>(R.id.ivLevelRunning)
            var tvTotalTimeRunning = findViewById<TextView>(R.id.tvTotalTimeRunning)
            var tvTotalRunsRunning = findViewById<TextView>(R.id.tvTotalRunsRunning)
            var tvTotalDistanceRunning = findViewById<TextView>(R.id.tvTotalDistanceRunning)


            var tvNumberLevelRunning = findViewById<TextView>(R.id.tvNumberLevelRunning)
            var levelText =
                "${getString(R.string.level)} ${levelRunning.image!!.subSequence(6, 7).toString()}"
            tvNumberLevelRunning.text = levelText

            var tt = getFormattedTotalTime(totalsRunning.totalTime!!.toLong())
            tvTotalTimeRunning.text = tt

            when (levelRunning.image) {
                "level_1" -> ivLevelRunning.setImageResource(R.drawable.level_1)
                "level_2" -> ivLevelRunning.setImageResource(R.drawable.level_2)
                "level_3" -> ivLevelRunning.setImageResource(R.drawable.level_3)
            }

            tvTotalRunsRunning.text = "${totalsRunning.totalRuns}/${levelRunning.RunsTarget}"
            var porcent =
                totalsRunning.totalDistance!!.toInt() * 100 / levelRunning.DistanceTarget!!.toInt()
            tvTotalDistanceRunning.text = "${porcent.toInt()}%"

            var csbDistanceRunning = findViewById<CircularSeekBar>(R.id.csbDistanceRunning)
            csbDistanceRunning.max = levelRunning.DistanceTarget!!.toFloat()
            if (totalsRunning.totalDistance!! >= levelRunning.DistanceTarget!!.toDouble())
                csbDistanceRunning.progress = csbDistanceRunning.max
            else
                csbDistanceRunning.progress = totalsRunning.totalDistance!!.toFloat()

            var csbRunsRunning = findViewById<CircularSeekBar>(R.id.csbRunsRunning)
            csbRunsRunning.max = levelRunning.RunsTarget!!.toFloat()
            if (totalsRunning.totalRuns!! >= levelRunning.RunsTarget!!.toInt())
                csbRunsRunning.progress = csbRunsRunning.max
            else
                csbRunsRunning.progress = totalsRunning.totalRuns!!.toFloat()

        }
    }

    private fun init() {

        initObjects()
        initChrono()
        initToolBar()
        initNavigationView()
        initPermissionsGPS()
        initMetrics()
        hideLayouts()
        initIntervalMode()
        initChallengeMode()
        hidePopUpRun()
        initChrono()
        initMusic()
        initTotals()
        initMap()
        initLevels()
        initMedals()
        initPreferences()
        recoveryPreferences()

    }

    private fun initMedals() {
        medalsListSportSelectedDistance = arrayListOf()
        medalsListSportSelectedAvgSpeed = arrayListOf()
        medalsListSportSelectedMaxSpeed = arrayListOf()
        medalsListSportSelectedDistance.clear()
        medalsListSportSelectedAvgSpeed.clear()
        medalsListSportSelectedMaxSpeed.clear()

        medalsListBikeDistance = arrayListOf()
        medalsListBikeAvgSpeed = arrayListOf()
        medalsListBikeMaxSpeed = arrayListOf()
        medalsListBikeDistance.clear()
        medalsListBikeAvgSpeed.clear()
        medalsListBikeMaxSpeed.clear()

        medalsListRollerSkateDistance = arrayListOf()
        medalsListRollerSkateAvgSpeed = arrayListOf()
        medalsListRollerSkateMaxSpeed = arrayListOf()
        medalsListRollerSkateDistance.clear()
        medalsListRollerSkateAvgSpeed.clear()
        medalsListRollerSkateMaxSpeed.clear()

        medalsListRunningDistance = arrayListOf()
        medalsListRunningAvgSpeed = arrayListOf()
        medalsListRunningMaxSpeed = arrayListOf()
        medalsListRunningDistance.clear()
        medalsListRunningAvgSpeed.clear()
        medalsListRunningMaxSpeed.clear()
    }

    private fun resetMedals() {
        recDistanceGold = false
        recDistanceSilver = false
        recDistanceBronze = false
        recAvgSpeedGold = false
        recAvgSpeedSilver = false
        recAvgSpeedBronze = false
        recMaxSpeedGold = false
        recMaxSpeedSilver = false
        recMaxSpeedBronze = false

    }

    private fun initTotals() {
        totalsBike = Totals()
        totalsRollerSkate = Totals()
        totalsRunning = Totals()
        totalsSelectedSport = Totals() //esta variable no la tiene inicializada

        totalsBike.totalRuns = 0
        totalsBike.totalDistance = 0.0
        totalsBike.totalTime = 0
        totalsBike.recordDistance = 0.0
        totalsBike.recordSpeed = 0.0
        totalsBike.recordAvgSpeed = 0.0

        totalsRollerSkate.totalRuns = 0
        totalsRollerSkate.totalDistance = 0.0
        totalsRollerSkate.totalTime = 0
        totalsRollerSkate.recordDistance = 0.0
        totalsRollerSkate.recordSpeed = 0.0
        totalsRollerSkate.recordAvgSpeed = 0.0

        totalsRunning.totalRuns = 0
        totalsRunning.totalDistance = 0.0
        totalsRunning.totalTime = 0
        totalsRunning.recordDistance = 0.0
        totalsRunning.recordSpeed = 0.0
        totalsRunning.recordAvgSpeed = 0.0

    }

    private fun initObjects() {
        drawer = binding.drawerLayout

        //LinealLayouts
        lyMap = binding.lyMap
        lyIntervalModeSpace = binding.lyIntervalModeSpace
        lyChallengesSpace = binding.lyChallengesSpace
        lySettingsVolumesSpace = binding.lySettingsVolumesSpace
        lySettingsVolumesSpace = binding.lySettingsVolumesSpace
        lySoftTrack = binding.lySoftTrack
        lySoftVolume = binding.lySoftVolume
        lyFragmentMap = binding.lyFragmentMap
        lyIntervalMode = binding.lyIntervalMode
        lyChallenges = binding.lyChallenges
        lySettingsVolumes = binding.lySettingsVolumes
        lyChallengeDuration = binding.lyChallengeDuration
        lyChallengeDistance = binding.lyChallengeDistance
        btStart = binding.btStart
        lyRoundProgressBg = binding.lyRoundProgressBg
        lyChronoProgressBg = binding.lyChronoProgressBg
        lyWindow = binding.lyWindow
        lyPopupRun = binding.lyPopupRun
        lyOpenerButton = binding.lyOpenerButton
        lySportBike = binding.lySportBike
        lySportRollerSkate = binding.lySportRollerSkate
        lySportRunning = binding.lySportRunning
        rlMain = binding.rlMain
        lyBanner = binding.lyBanner
        lyBannerSlogan = binding.lyBannerSlogan
        lySports = binding.lySports
        lyOptionsRun = binding.lyOptionsRun
        lyCurrentData = binding.lyCurrentData

        lyOpenerMap = binding.lyOpenerMap


        //CircularSeekbar
        csbRunWalk = binding.csbRunWalk
        csbCurrentDistance = binding.csbCurrentDistance
        csbChallengeDistance = binding.csbChallengeDistance
        csbRecordDistance = binding.csbRecordDistance
        csbCurrentAvgSpeed = binding.csbCurrentAvgSpeed
        csbRecordAvgSpeed = binding.csbRecordAvgSpeed
        csbCurrentSpeed = binding.csbCurrentSpeed
        csbCurrentMaxSpeed = binding.csbCurrentMaxSpeed
        csbRecordSpeed = binding.csbRecordSpeed


        //Numberpickers
        npChallengeDistance = binding.npChallengeDistance
        npChallengeDurationHH = binding.npChallengeDurationHH
        npChallengeDurationMM = binding.npChallengeDurationMM
        npChallengeDurationSS = binding.npChallengeDurationSS
        npDurationInterval = binding.npDurationInterval

        //Textviews
        tvDistanceRecord = binding.tvDistanceRecord
        tvAvgSpeedRecord = binding.tvAvgSpeedRecord
        tvMaxSpeedRecord = binding.tvMaxSpeedRecord
        tvRunningTime = binding.tvRunningTime
        tvWalkingTime = binding.tvWalkingTime
        tvReset = binding.tvReset
        tvChrono = binding.tvChrono
        tvRounds = binding.tvRounds
        tvChallengeDuration = binding.tvChallengeDuration
        tvChallengeDistance = binding.tvChallengeDistance
        btStartLabel = binding.btStartLabel
        tvHardPosition = binding.tvHardPosition
        tvHardRemaining = binding.tvHardRemaining
        tvSoftPosition = binding.tvSoftPosition
        tvSoftRemaining = binding.tvSoftRemaining
        tvCurrentDistance = binding.tvCurrentDistance
        tvCurrentAvgSpeed = binding.tvCurrentAvgSpeed
        tvCurrentSpeed = binding.tvCurrentSpeed
        tvTotalRunsLevel = binding.tvTotalRunsLevel
        tvTotalDistanceLevel = binding.tvTotalDistanceLevel
        tvTotalDistance = binding.tvTotalDistance
        tvTotalTime = binding.tvTotalTime


        //Switchs
        swIntervalMode = binding.swIntervalMode
        swVolumes = binding.swVolumes
        swChallenges = binding.swChallenges

        //Checboxes
        cbNotify = binding.cbNotify
        cbAutoFinish = binding.cbAutoFinish

        //Floating Action Button
        fbCamara = binding.fbCamera

        //Seekbar
        sbHardVolume = binding.sbHardVolume
        sbSoftVolume = binding.sbSoftVolume
        sbHardTrack = binding.sbHardTrack
        sbSoftTrack = binding.sbSoftTrack
        sbNotifyVolume = binding.sbNotifyVolume

        //ImageViews
        ivOpenClose = binding.ivOpenClose
        ivTypeMap = binding.ivTypeMap


    }

    private fun initMetrics() {

        //RECORDS
        record_distancia_conseguido = true
        record_avgSpeed_conseguido = true
        record_speed_conseguido = true


        csbCurrentDistance.progress = 0f
        csbChallengeDistance.progress = 0f
        csbCurrentAvgSpeed.progress = 0f
        csbCurrentSpeed.progress = 0f
        csbCurrentMaxSpeed.progress = 0f
        tvDistanceRecord.text = ""
        tvAvgSpeedRecord.text = ""
        tvMaxSpeedRecord.text = ""
    }

    private fun hideLayouts() {
        setHeightLinearLayout(lyMap, 0)
        setHeightLinearLayout(lyIntervalModeSpace, 0)
        setHeightLinearLayout(lyChallengesSpace, 0)
        setHeightLinearLayout(lySettingsVolumesSpace, 0)
        setHeightLinearLayout(lySoftTrack, 0)
        setHeightLinearLayout(lySoftVolume, 0)
        lyFragmentMap.translationY = -300f
        lyIntervalMode.translationY = -300f
        lyChallenges.translationY = -300f
        lySettingsVolumes.translationY = -300f
    }

    private fun initToolBar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.bar_title,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun initNavigationView() {
        binding.navView.setNavigationItemSelectedListener(this)
        val headerView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.nav_header_main, binding.navView, false)
        binding.navView.removeHeaderView(headerView)
        binding.navView.addHeaderView(headerView)
        val tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = useremail
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_record -> callRecordActivity()
            R.id.nav_item_clearpreferences -> alertClearPreferences()
            R.id.nav_item_signout -> alertSignOut()
            R.id.nav_item_premium -> alertPremium()
            R.id.nav_item_author -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                showAuthorInfoAndApps(this)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showAuthorInfoAndApps(context: Context) {
        val appName = APP
        val emailAddress = EMAIL
        val authorName = AUTHOR
        val centre = CENTRE
        val contact = "<a href='mailto:$emailAddress'>$emailAddress</a>"
        val message = "Hola soy $authorName!\n\n" +
                "He desarrollado $appName.\n\n" +
                "Espero que encuentres útil esta app.\n\n" +
                "He intentado que sea fácil de usar.\n\n" +
                "Si tienes algún problema o sugerencia, por favor házmelo saber.\n\n" +
                "Email: $contact\n\n" +
                "¡Gracias por utilizar $appName!"


        // Crea el diálogo con la información del autor
        val dialogBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AlertDialog.Builder(context)
                .setTitle(appName)
                .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(R.string.acept, null)
        } else {
            TODO("VERSION.SDK_INT < N")
        }

        // Agrega el enlace a tus otras aplicaciones en la PlayStore
        //val playStoreLink = "https://play.google.com/store/apps/developer?id=TU_NOMBRE_DE_DESARROLLADOR"
        val playStoreLink = "https://play.google.com/store/apps/"
        dialogBuilder.setNeutralButton("Mis apps") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreLink))
            context.startActivity(intent)
        }

        // Agrega un listener al mensaje para abrir la actividad de redacción de correo electrónico de Gmail
        val messageView = dialogBuilder.show().findViewById<TextView>(android.R.id.message)
        messageView?.movementMethod = LinkMovementMethod.getInstance()
        messageView?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$emailAddress")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Sugerencias o problemas con la aplicación")
            context.startActivity(Intent.createChooser(intent, "Enviar correo electrónico"))
        }
    }


    private fun alertPremium() {
        if (!isPremium) {
            AlertDialog.Builder(this)
                .setTitle(" ¿ Quieres ser usuario Premium ? ")
                .setMessage(" Al ser Premium no verás publicidad y tendrás opciones extra ")
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    //botón OK pulsado
                    launchPaymentCard()
                    //becamePremium()
                    showBanner()

                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { _, _ ->
                    //botón cancel pulsado
                }
                .setCancelable(true)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle(R.string.premium)
                .setMessage(R.string.ya_premium)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setCancelable(true)
                .show()
        }
    }

    private fun launchPaymentCard() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
    }


    private fun alertSignOut() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alertSignOutTitle))
            .setMessage(R.string.alertSignOutTDescription)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                //botón OK pulsado
                signOut()
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { dialog, which ->
                //botón cancel pulsado
            }
            .setCancelable(true)
            .show()
    }

    private fun alertClearPreferences() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alertClearPreferencesTitle))
            .setMessage(getString(R.string.alertClearPreferencesDescription))
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialgo, which ->
                    callClearPreferences()
                })
            .setNegativeButton(android.R.string.cancel,
                DialogInterface.OnClickListener { dialgo, which ->

                })
            .setCancelable(true)
            .show()
    }

    private fun callClearPreferences() {
        editor.clear().apply()
        showCustomSnackbar(
            rlMain, "Los ajustes han sido reestablecidos", R.color.orange_strong, 2000
        )
    }

    private fun callRecordActivity() {
        if (startButtonClicked) manageStartStop()//Si pulsamos el boton que detenga la carrera
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }

    private fun signOut() {
        useremail = ""
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onBackPressed() {
        if (lyPopupRun.isVisible) closePopUpRun() //Si esta el popup abierto lo cerramos
        else {
            if (drawer.isDrawerOpen(GravityCompat.START))//comprobamos si el menu está desplegado
                drawer.closeDrawer(GravityCompat.START)
            else
                if (timeInSeconds > 0L) resetClicked() //Esta iniciada la carrera ?? finalizamos
            alertSignOut() // ¿ quieres cerrar sesion ?
        }
    }

    fun inflateIntervalMode(v: View) {
        if (swIntervalMode.isChecked) {
            animateViewofInt(
                swIntervalMode, "textColor", ContextCompat.getColor(this, R.color.orange),
                500
            )
            setHeightLinearLayout(lyIntervalModeSpace, 600)
            animateViewofFloat(lyIntervalMode, "translationY", 0f, 500)
            animateViewofFloat(tvChrono, "translationX", -110f, 500)
            tvRounds.setText(R.string.rounds)
            animateViewofInt(
                tvRounds,
                "textColor",
                ContextCompat.getColor(this, R.color.gray_dark),
                500
            )
            setHeightLinearLayout(lySoftTrack, 120)
            setHeightLinearLayout(lySoftVolume, 200)
            if (swVolumes.isChecked) {
                setHeightLinearLayout(lySettingsVolumesSpace, 600)
            }
            timeRunning = getSecFromWatch(tvRunningTime.text.toString())
        } else {
            swIntervalMode.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            setHeightLinearLayout(lyIntervalModeSpace, 0)
            lyIntervalMode.translationY = -200f
            animateViewofFloat(tvChrono, "translationX", 0f, 500)
            tvRounds.text = ""
            setHeightLinearLayout(lySoftTrack, 0)
            setHeightLinearLayout(lySoftVolume, 0)
            if (swVolumes.isChecked) {
                setHeightLinearLayout(lySettingsVolumesSpace, 400)
            }
        }
    }

    private fun checkStopRun(Secs: Long) {
        var secAux: Long = Secs
        //Mientras la duracion de los segundos sea mayor que el intervalo vamos a restar
        while (secAux.toInt() > roundInterval) secAux -= roundInterval

        //Si los segundos que nos quedan son iguales al time running  hay que dejar de correr
        if (secAux.toInt() == timeRunning) {

            //El texto Cambia a color azul
            tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_walking))

            //El progress bar cambia de color a color caminar
            lyRoundProgressBg.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.chrono_walking
                )
            )
            //Movemos el progress
            lyRoundProgressBg.translationX = -widthAnimations.toFloat()

            //Si dejamos de correr para comenzar a caminar tambien Cambiamos la música
            mpHard?.pause()
            notifySound()
            mpSoft?.start()

            // En el caso contrario continuamos corriendo actualiza el progress bar de la ronda
        } else updateProgressBarRound(Secs)
    }

    private fun notifySound() {
        mpNotify?.start()
    }


    private fun updateProgressBarRound(secs: Long) {
        var s = secs.toInt()
        while (s >= roundInterval) s -= roundInterval
        s++
        if (tvChrono.currentTextColor == ContextCompat.getColor(this, R.color.chrono_running)) {
            val movement =
                -1 * (widthAnimations - (s * widthAnimations / timeRunning)).toFloat()
            animateViewofFloat(lyRoundProgressBg, "translationX", movement, 1000L)
            isRunning = true
        }
        if (tvChrono.currentTextColor == ContextCompat.getColor(this, R.color.chrono_walking)) {
            s -= timeRunning
            val movement =
                -1 * (widthAnimations - (s * widthAnimations / (roundInterval - timeRunning))).toFloat()
            animateViewofFloat(lyRoundProgressBg, "translationX", movement, 1000L)
            isRunning = false
        }
    }

    private fun checkNewRound(Secs: Long) {
        //Tenemos una ronda nueva si el tiempo que llevamos es multiplo de la ronda / que sea mayor que cero
        if (Secs.toInt() % roundInterval == 0 && Secs.toInt() > 0) {

            //Hay que actualizar el texto de la ronda y se aumenta el numero de rondas
            rounds++
            tvRounds.text = "Round $rounds"

            //Cuando hay una nueva ronda cambia a color rojo por que lo siguiente es correr
            //Cambia a color rojo el progress bar y el cronometro

            tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_running))
            lyRoundProgressBg.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.chrono_running
                )
            )
            //Lo reseteamos y lo llevamos al punto inicial
            lyRoundProgressBg.translationX = -widthAnimations.toFloat()

            //Si dejamos de andar para comenzar a correr tambien Cambiamos la música
            mpSoft?.pause()
            notifySound()
            mpHard?.start()


            //Cuando no sea asi hay que aumentar el progress
        } else updateProgressBarRound(Secs)
    }

    fun inflateVolumes(v: View) {
        if (swVolumes.isChecked) {
            animateViewofInt(
                swVolumes,
                "textColor",
                ContextCompat.getColor(this, R.color.orange),
                500
            )
            var value = 400
            if (swIntervalMode.isChecked) value = 600
            setHeightLinearLayout(lySettingsVolumesSpace, value)
            animateViewofFloat(lySettingsVolumes, "translationY", 0f, 500)
        } else {
            swVolumes.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            setHeightLinearLayout(lySettingsVolumesSpace, 0)
            lySettingsVolumes.translationY = -300f
        }
    }

    fun inflateChallenges(v: View) {
        if (swChallenges.isChecked) {
            animateViewofInt(
                swChallenges,
                "textColor",
                ContextCompat.getColor(this, R.color.orange),
                500
            )
            setHeightLinearLayout(lyChallengesSpace, 750)
            animateViewofFloat(lyChallenges, "translationY", 0f, 500)
        } else {
            swChallenges.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            setHeightLinearLayout(lyChallengesSpace, 0)
            lyChallenges.translationY = -300f
            challengeDistance = 0f
            challengeDuration = 0
        }
    }

    fun showDuration(v: View) {
        if (timeInSeconds == 0L) showChallenge("duration")
    }

    fun showDistance(v: View) {
        if (timeInSeconds == 0L) showChallenge("distance")
    }

    private fun showChallenge(option: String) {
        when (option) {
            "duration" -> {
                lyChallengeDuration.translationZ = 5f
                lyChallengeDistance.translationZ = 0f
                tvChallengeDuration.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange
                    )
                )
                tvChallengeDuration.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_dark
                    )
                )
                tvChallengeDistance.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
                tvChallengeDistance.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_medium
                    )
                )
                challengeDistance = 0f
                getChallengeDuration(
                    npChallengeDurationHH.value,
                    npChallengeDurationMM.value,
                    npChallengeDurationSS.value
                )
            }
            "distance" -> {
                lyChallengeDuration.translationZ = 0f
                lyChallengeDistance.translationZ = 5f
                tvChallengeDuration.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
                tvChallengeDuration.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_medium
                    )
                )
                tvChallengeDistance.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange
                    )
                )
                tvChallengeDistance.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray_dark
                    )
                )
                challengeDuration = 0
                challengeDistance = npChallengeDistance.value.toFloat()
            }
        }
    }

    private fun getChallengeDuration(hh: Int, mm: Int, ss: Int) {
        var hours: String = hh.toString()
        if (hh < 10) hours = "0" + hours
        var minutes: String = mm.toString()
        if (mm < 10) minutes = "0" + minutes
        var seconds: String = ss.toString()
        if (ss < 10) seconds = "0" + seconds
        challengeDuration = getSecFromWatch("${hours}:${minutes}:${seconds}")
    }

    private fun initIntervalMode() {
        npDurationInterval.minValue = 1
        npDurationInterval.maxValue = 60
        npDurationInterval.value = 5
        npDurationInterval.wrapSelectorWheel = true
        npDurationInterval.setFormatter { i -> String.format("%02d", i) }
        npDurationInterval.setOnValueChangedListener { _, _, newVal ->
            csbRunWalk.max = (newVal * 60).toFloat()
            csbRunWalk.progress = csbRunWalk.max / 2
            tvRunningTime.text =
                getFormattedStopWatch(((newVal * 60 / 2) * 1000).toLong()).subSequence(3, 8)
            tvWalkingTime.text = tvRunningTime.text
            roundInterval = newVal * 60
            timeRunning = roundInterval / 2
        }
        csbRunWalk.max = 300f
        csbRunWalk.progress = 150f
        csbRunWalk.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    var steps = 15
                    if (roundInterval > 600) steps = 60
                    if (roundInterval > 1800) steps = 300
                    var set = 0
                    var p = progress.toInt()
                    var limit = 60
                    if (roundInterval > 1800) limit = 300
                    if (p % steps != 0 && progress != csbRunWalk.max) {
                        while (p >= limit) p -= limit
                        while (p >= steps) p -= steps
                        //if (steps - p > steps / 2) set = -1 * p
                        set = if (steps - p > steps / 2) -1 * p
                        else steps - p
                        if (csbRunWalk.progress + set > csbRunWalk.max)
                            csbRunWalk.progress = csbRunWalk.max
                        else
                            csbRunWalk.progress = csbRunWalk.progress + set
                    }
                }
                //Si el progress está a cero se pone a false y si no se habilita correr
                if (csbRunWalk.progress == 0f) manageEnableButtonsRun(false, false)
                else manageEnableButtonsRun(false, true)

                tvRunningTime.text =
                    getFormattedStopWatch((csbRunWalk.progress.toInt() * 1000).toLong()).subSequence(
                        3,
                        8
                    )
                tvWalkingTime.text =
                    getFormattedStopWatch(((roundInterval - csbRunWalk.progress.toInt()) * 1000).toLong()).subSequence(
                        3,
                        8
                    )
                timeRunning = getSecFromWatch(tvRunningTime.text.toString())
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
            }
        })
    }

    private fun initChallengeMode() {

        npChallengeDistance.minValue = 1
        npChallengeDistance.maxValue = 300
        npChallengeDistance.value = 10
        npChallengeDistance.wrapSelectorWheel = true

        npChallengeDistance.setOnValueChangedListener { picker, oldVal, newVal ->
            challengeDistance = newVal.toFloat()
            csbChallengeDistance.max = newVal.toFloat()
            csbChallengeDistance.progress = newVal.toFloat()
            challengeDuration = 0

            if (csbChallengeDistance.max > csbRecordDistance.max)
                csbCurrentDistance.max = csbChallengeDistance.max
        }

        npChallengeDurationHH.minValue = 0
        npChallengeDurationHH.maxValue = 23
        npChallengeDurationHH.value = 1
        npChallengeDurationHH.wrapSelectorWheel = true
        npChallengeDurationHH.setFormatter(NumberPicker.Formatter { i ->
            String.format(
                "%02d",
                i
            )
        })

        npChallengeDurationMM.minValue = 0
        npChallengeDurationMM.maxValue = 59
        npChallengeDurationMM.value = 0
        npChallengeDurationMM.wrapSelectorWheel = true
        npChallengeDurationMM.setFormatter(NumberPicker.Formatter { i ->
            String.format(
                "%02d",
                i
            )
        })

        npChallengeDurationSS.minValue = 0
        npChallengeDurationSS.maxValue = 59
        npChallengeDurationSS.value = 0
        npChallengeDurationSS.wrapSelectorWheel = true
        npChallengeDurationSS.setFormatter(NumberPicker.Formatter { i ->
            String.format(
                "%02d",
                i
            )
        })

        npChallengeDurationHH.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(
                newVal,
                npChallengeDurationMM.value,
                npChallengeDurationSS.value
            )
        }
        npChallengeDurationMM.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(
                npChallengeDurationHH.value,
                newVal,
                npChallengeDurationSS.value
            )
        }
        npChallengeDurationSS.setOnValueChangedListener { picker, oldVal, newVal ->
            getChallengeDuration(
                npChallengeDurationHH.value,
                npChallengeDurationMM.value,
                newVal
            )
        }
    }

    private fun hidePopUpRun() {
        lyWindow.translationX =
            400f // translacion 400f oculta el layout padre ya que lo desplaza hacia la derecha y lo deja fuera
        lyPopupRun.isVisible = false // El layout hijo lo oculta
    }

    private fun initChrono() {
        tvChrono.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        initStopWatch()
        widthScreenPixels = resources.displayMetrics.widthPixels
        heightScreenPixels = resources.displayMetrics.heightPixels
        widthAnimations = widthScreenPixels
        lyChronoProgressBg.translationX = -widthAnimations.toFloat()
        lyRoundProgressBg.translationX = -widthAnimations.toFloat()
        tvReset.setOnClickListener { resetClicked() }
        fbCamara.isVisible = false
    }

    private fun initStopWatch() {
        tvChrono.text = getString(R.string.init_stop_watch_value)
    }

    //FIREBASE UPDATE TOTALS USER
    private fun updateTotalsUser() {
/*
        totalsSelectedSport.totalRuns = totalsSelectedSport.totalRuns!! + 1
        totalsSelectedSport.totalDistance = totalsSelectedSport.totalDistance!! + distance
        totalsSelectedSport.totalTime = totalsSelectedSport.totalTime!! + timeInSeconds.toInt()
*/
        if (totalsSelectedSport.totalRuns == null) {
            totalsSelectedSport.totalRuns = 1
        } else {
            totalsSelectedSport.totalRuns = totalsSelectedSport.totalRuns!! + 1
        }
        totalsSelectedSport.totalDistance = totalsSelectedSport.totalDistance!! + distance
        totalsSelectedSport.totalTime = totalsSelectedSport.totalTime!! + timeInSeconds.toInt()


        if (distance > totalsSelectedSport.recordDistance!!) {
            totalsSelectedSport.recordDistance = distance
        }
        if (maxSpeed > totalsSelectedSport.recordSpeed!!) {
            totalsSelectedSport.recordSpeed = maxSpeed
        }
        if (avgSpeed > totalsSelectedSport.recordAvgSpeed!!) {
            totalsSelectedSport.recordAvgSpeed = avgSpeed
        }
        totalsSelectedSport.totalDistance =
            roundNumber(totalsSelectedSport.totalDistance.toString(), 1).toDouble()
        totalsSelectedSport.recordDistance =
            roundNumber(totalsSelectedSport.recordDistance.toString(), 1).toDouble()
        totalsSelectedSport.recordSpeed =
            roundNumber(totalsSelectedSport.recordSpeed.toString(), 1).toDouble()
        totalsSelectedSport.recordAvgSpeed =
            roundNumber(totalsSelectedSport.recordAvgSpeed.toString(), 1).toDouble()

        var collection = "totals$sportSelected"
        // dbUpdateTotals = FirebaseFirestore.getInstance()
        firestore.db.collection(collection).document(useremail)
            .update("recordAvgSpeed", totalsSelectedSport.recordAvgSpeed)
        firestore.db.collection(collection).document(useremail)
            .update("recordDistance", totalsSelectedSport.recordDistance)
        firestore.db.collection(collection).document(useremail)
            .update("recordSpeed", totalsSelectedSport.recordSpeed)
        firestore.db.collection(collection).document(useremail)
            .update("totalDistance", totalsSelectedSport.totalDistance)
        firestore.db.collection(collection).document(useremail)
            .update("totalRuns", totalsSelectedSport.totalRuns)
        firestore.db.collection(collection).document(useremail)
            .update("totalTime", totalsSelectedSport.totalTime)

        when (sportSelected) {
            "Bike" -> {
                totalsBike = totalsSelectedSport
                medalsListBikeDistance = medalsListSportSelectedDistance
                medalsListBikeAvgSpeed = medalsListSportSelectedAvgSpeed
                medalsListBikeMaxSpeed = medalsListSportSelectedMaxSpeed
            }
            "RollerSkate" -> {
                totalsRollerSkate = totalsSelectedSport
                medalsListRollerSkateDistance = medalsListSportSelectedDistance
                medalsListRollerSkateAvgSpeed = medalsListSportSelectedAvgSpeed
                medalsListRollerSkateMaxSpeed = medalsListSportSelectedMaxSpeed
            }
            "Running" -> {
                totalsRunning = totalsSelectedSport
                medalsListRunningDistance = medalsListSportSelectedDistance
                medalsListRunningAvgSpeed = medalsListSportSelectedAvgSpeed
                medalsListRunningMaxSpeed = medalsListSportSelectedMaxSpeed
            }
        }
    }

    private fun resetClicked() {

        savePreferences()
        saveDataRun()
        updateTotalsUser()
        setLevelSport(sportSelected)
        showPopUp() //Mostramos los datos antes de resetar variables
        checkMedals(distance, avgSpeed, maxSpeed) //Chekea Medallas
        resetTimeView()
        resetInterface()
    }

    //FIREBASE SAVE DATA RUN
    private fun saveDataRun() {
        var id: String = useremail + dateRun + startTimeRun
        id = id.replace(":", "")//Eliminamos los puntos:
        id = id.replace("/", "")//Eliminamos la barra/

        //var saveDuration = tvChrono.text.toString() //Guardamos Duración de la Carrera
        saveDuration = tvChrono.text.toString() //Guardamos Duración de la Carrera
        saveDistance = roundNumber(distance.toString(), 1) //Guardamos Distancia
        saveAvgSpeed = roundNumber(avgSpeed.toString(), 1) //Guardamos Velocidad media
        saveMaxSpeed = roundNumber(maxSpeed.toString(), 1) //Guardamos Velocidad maxima

        // Calculos para saber el centro detodo el recorrido
        centerLatitude = (minLatitude!! + maxLatitude!!) / 2
        centerLongitude = (minLongitude!! + maxLongitude!!) / 2
        medalDistance = "none"
        medalAvgSpeed = "none"
        medalMaxSpeed = "none"

        if (recDistanceGold) medalDistance = "gold"
        if (recDistanceSilver) medalDistance = "silver"
        if (recDistanceBronze) medalDistance = "bronze"

        if (recAvgSpeedGold) medalAvgSpeed = "gold"
        if (recAvgSpeedSilver) medalAvgSpeed = "silver"
        if (recAvgSpeedBronze) medalAvgSpeed = "bronze"

        if (recMaxSpeedGold) medalMaxSpeed = "gold"
        if (recMaxSpeedSilver) medalMaxSpeed = "silver"
        if (recMaxSpeedBronze) medalMaxSpeed = "bronze"


        var collection = "runs$sportSelected"
        //var dbRun = FirebaseFirestore.getInstance()

        firestore.db.collection(collection).document(id).set(
            hashMapOf(
                "user" to useremail,
                "date" to dateRun,
                "startTime" to startTimeRun,
                "sport" to sportSelected,
                "activatedGPS" to activatedGPS,
                "duration" to saveDuration,
                "distance" to saveDistance.toDouble(),
                "avgSpeed" to saveAvgSpeed.toDouble(),
                "maxSpeed" to saveMaxSpeed.toDouble(),
                "minAltitude" to minAltitude,
                "maxAltitude" to maxAltitude,
                "minLatitude" to minLatitude,
                "maxLatitude" to maxLatitude,
                "minLongitude" to minLongitude,
                "maxLongitude" to maxLongitude,
                "centerLatitude" to centerLatitude,
                "centerLongitude" to centerLongitude,
                "medalDistance" to medalDistance,
                "medalAvgSpeed" to medalAvgSpeed,
                "medalMaxSpeed" to medalMaxSpeed,
                "lastimage" to lastimage, //ruta de la última imagen
                "countPhotos" to countPhotos //contador de fotos de la carrera
            )
        )

        //Si el modo intervalos está activado mandamos a actualizar campos , si no existen los crea
        //Hacemos un update para añadir sin borrar lo anterior

        if (swIntervalMode.isChecked) {
            firestore.db.collection(collection).document(id).update("intervalMode", true)
            firestore.db.collection(collection).document(id)
                .update("intervalDuration", npDurationInterval.value)
            firestore.db.collection(collection).document(id)
                .update("runningTime", tvRunningTime.text.toString())
            firestore.db.collection(collection).document(id)
                .update("walkingTime", tvWalkingTime.text.toString())
        }

        if (swChallenges.isChecked) {
            if (challengeDistance > 0f)
                firestore.db.collection(collection).document(id).update(
                    "challengeDistance",
                    roundNumber(challengeDistance.toString(), 1).toDouble()
                )
            if (challengeDuration > 0)
                firestore.db.collection(collection).document(id)
                    .update(
                        "challengeDuration",
                        getFormattedStopWatch(challengeDuration.toLong())
                    )
        }

    }

    private fun resetInterface() {
        fbCamara.isVisible = false
        tvCurrentDistance.text = "0.0"
        tvCurrentAvgSpeed.text = "0.0"
        tvCurrentSpeed.text = "0.0"
        tvDistanceRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvAvgSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvMaxSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        csbCurrentDistance.progress = 0f
        csbCurrentAvgSpeed.progress = 0f
        csbCurrentSpeed.progress = 0f
        csbCurrentMaxSpeed.progress = 0f
        tvRounds.text = getString(R.string.rounds)
        lyChronoProgressBg.translationX = -widthAnimations.toFloat()
        lyRoundProgressBg.translationX = -widthAnimations.toFloat()
        swIntervalMode.isClickable = true
        npDurationInterval.isEnabled = true
        csbRunWalk.isEnabled = true
        swChallenges.isClickable = true
        npChallengeDistance.isEnabled = true
        npChallengeDurationHH.isEnabled = true
        npChallengeDurationMM.isEnabled = true
        npChallengeDurationSS.isEnabled = true
        sbHardTrack.isEnabled = false
        sbSoftTrack.isEnabled = false

        record_distancia_conseguido = true
        record_avgSpeed_conseguido = true
        record_speed_conseguido = true
    }

    private fun resetTimeView() {
        initStopWatch()
        manageEnableButtonsRun(e_reset = false, e_run = true)
        tvChrono.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
    }

    private fun updateTimesTrack(
        timesH: Boolean,
        timesS: Boolean
    ) {    // Funcion para controlar el inicio y el restante
        if (timesH) {
            tvHardPosition.text = getFormattedStopWatch(sbHardTrack.progress.toLong())
            val hardRemaining = mpHard!!.duration.toLong() - sbHardTrack.progress.toLong()
            tvHardRemaining.text =
                resources.getString(R.string.remaining, getFormattedStopWatch(hardRemaining))
        }
        if (timesS) {
            tvSoftPosition.text = getFormattedStopWatch(sbSoftTrack.progress.toLong())
            val softRemaining = mpSoft!!.duration.toLong() - sbSoftTrack.progress.toLong()
            tvSoftRemaining.text =
                resources.getString(R.string.remaining, getFormattedStopWatch(softRemaining))
        }
    }

    private var chronometer: Runnable = object : Runnable {
        override fun run() {
            try {
                //Aumentamos el Progress Si se ejecuta un track u otro

                if (mpHard!!.isPlaying) {
                    sbHardTrack.progress = mpHard!!.currentPosition
                }
                if (mpSoft!!.isPlaying) {
                    sbSoftTrack.progress = mpSoft!!.currentPosition
                }
                updateTimesTrack(true, true)

                //Controlamos la administracion localizacion en el intervalo indicado
                //En este caso por motivos de no sobrecargar el sistema , se comprueba cada 4 segundos
                // (los segundos se indican en la constante INTERVAL_LOCATION)
                if (activatedGPS && timeInSeconds.toInt() % INTERVAL_LOCATION == 0) manageLocation() //Administramos la localizacion

                if (swIntervalMode.isChecked) { //Si el modo intervalo está activado
                    checkStopRun(timeInSeconds) //Enviamos los segundos Comprueba si hay que dejar de correr
                    checkNewRound(timeInSeconds) // Comprueba si hay que empezar una nueva ronda
                }

                timeInSeconds += 1
                updateStopWatchView()
                updateWidgets() // se actualiza  el widget
            } finally {
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun updateStopWatchView() {
        tvChrono.text = getFormattedStopWatch(timeInSeconds * 1000)
    }

    fun startOrStopButtonClicked(v: View) {
        manageStartStop()
    }

    private fun startTime() {
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }

    private fun stopTime() {
        mHandler?.removeCallbacks(chronometer)
    }

    private fun activationLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //Devuelve un true si alguno de los dos servicios está habilitado
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    private fun manageStartStop() {
        //Si TimeInSecond no es cero es que la carrera está iniciada
        // Y Tambien Comprobamos si tiene habilitada la localización
        if (timeInSeconds == 0L && !isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.alertActivationGPSTitle))
                .setMessage(getString(R.string.alertActivationGPSDescription))
                .setPositiveButton(R.string.aceptActivationGPS, //En caso de que pulse positivo
                    DialogInterface.OnClickListener { dialog, which ->
                        activationLocation() //Activamos la ubicación
                    })
                .setNegativeButton(R.string.ignoreActivationGPS, //En el caso de que pulse negativo
                    DialogInterface.OnClickListener { dialog, which ->
                        activatedGPS = false // No hay GPS
                        manageRun() // Empieza la carrera
                    })
                .setCancelable(true) //Que se pueda cancelar el mensaje
                .show() // Que lo muestre

        } else manageRun() //Se ejecuta si ya la he ha dado al boton empezar
    }


    private fun manageRun() {


        if (timeInSeconds.toInt() == 0) { //Empieza la carrera

            dateRun = SimpleDateFormat("yyyy/MM/dd").format(Date()) //Guarda fecha carrera
            startTimeRun = SimpleDateFormat("HH:mm:ss").format(Date()) //Guarda hora carrera

            //Si la carrera está en marcha activamos/desactivamos opciones
            fbCamara.isVisible = true
            swIntervalMode.isClickable = false
            npDurationInterval.isEnabled = false
            csbRunWalk.isEnabled = false
            swChallenges.isClickable = false
            npChallengeDistance.isEnabled = false
            npChallengeDurationHH.isEnabled = false
            npChallengeDurationMM.isEnabled = false
            npChallengeDurationSS.isEnabled = false
            tvChrono.setTextColor(ContextCompat.getColor(this, R.color.chrono_running))
            mpHard?.start() //Lanzamos la música al empezar a correr
            isRunning = true

            //Al iniciar la carrera Vamos a preguntar si el GPS está activado
            if (activatedGPS) {
                flagSavedLocation = false
                manageLocation()
                flagSavedLocation = true
                manageLocation()
            }
        }

        if (!startButtonClicked) {
            startButtonClicked = true
            startTime()
            manageEnableButtonsRun(
                e_reset = false,
                e_run = true
            )//Desactiva Finalizar/Reset y Activa Carrera
            (if (isRunning) mpHard else mpSoft)?.start() //si esta en carrera reproduce una musica u otra
        } else {
            startButtonClicked = false
            stopTime()
            manageEnableButtonsRun(
                e_reset = true,
                e_run = true
            )//Activa Finalizar/reset y Activa Carrera
            (if (isRunning) mpHard else mpSoft)?.pause() //si esta en carrera pausa una musica u otra
            //checkMedals(distance, avgSpeed, maxSpeed) //Chekea Med
        }
    }

    private fun manageEnableButtonsRun(e_reset: Boolean, e_run: Boolean) {
        tvReset.isEnabled = e_reset
        btStart.isEnabled = e_run

        if (e_reset) { // Si el reset está habilitado se pone en color verde y se eleva
            tvReset.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            animateViewofFloat(tvReset, "translationY", 0f, 500)
        } else { // En caso contrario se pone en gris y se oculta
            tvReset.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            animateViewofFloat(tvReset, "translationY", 150f, 500)
        }

        if (e_run) { // El boton de la carrera está activo cambiamos el color del circulo
            if (startButtonClicked) {
                btStart.background =
                    AppCompatResources.getDrawable(this, R.drawable.circle_background_topause)
                btStartLabel.setText(R.string.stop)
            } else {
                btStart.background =
                    AppCompatResources.getDrawable(this, R.drawable.circle_background_toplay)
                btStartLabel.setText(R.string.start)
            }
        } else btStart.background =
            AppCompatResources.getDrawable(this, R.drawable.circle_background_todisable)
    }

    private fun resetVariablesRun() {
        timeInSeconds = 0
        rounds = 1
        distance = 0.0
        maxSpeed = 0.0
        avgSpeed = 0.0
        hardTime = true

        minAltitude = null
        maxAltitude = null
        minLatitude = null
        maxLatitude = null
        minLongitude = null
        maxLongitude = null
        (listPoints as ArrayList<LatLng>).clear()

        challengeDistance = 0f
        challengeDuration = 0
        activatedGPS = true
        flagSavedLocation = false
    }

    private fun initMusic() {
        mpNotify = MediaPlayer.create(this, R.raw.ding)
        mpHard = MediaPlayer.create(this, R.raw.running)
        mpSoft = MediaPlayer.create(this, R.raw.walking)
        mpHard?.isLooping = true
        mpSoft?.isLooping = true
        setVolumes()
        setProgressTracks()
    }

    private fun setVolumes() {
        sbHardVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpHard?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        sbSoftVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpSoft?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        sbNotifyVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, p2: Boolean) {
                mpNotify?.setVolume(i / 100.0f, i / 100.0f)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun setProgressTracks() {
        sbHardTrack.max = mpHard!!.duration
        sbSoftTrack.max = mpSoft!!.duration
        updateTimesTrack(timesH = true, timesS = true)
        sbHardTrack.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, fromUser: Boolean) {
                if (fromUser) {
                    mpHard?.pause() // Pausamos la cancion
                    mpHard?.seekTo(i) // Nos desplazamos al punto que ha indicado el usuario
                    mpHard?.start() // Reanudamos la cancion de nuevo
                    updateTimesTrack(timesH = true, timesS = false) // Actualizamos los tracks
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        sbSoftTrack.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, i: Int, fromUser: Boolean) {
                if (fromUser) {
                    mpSoft?.pause()
                    mpSoft?.seekTo(i)
                    mpSoft?.start()
                    updateTimesTrack(timesH = false, timesS = true)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

    }

    private fun initPermissionsGPS() {
        //Comprobamos si estan aprobados los permisos
        if (allPermissionsGrantedGPS())
        //Nos da todos los datos atraves del servicio de localizacion
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        else
            requestPermissionLocation() //en caso contrario vamos a solicitar al usuario permisos
    }

    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        //Si todos los componentes del array REQUIRED..
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionLocation() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun manageLocation() {
        if (checkPermission()) { //comprobamos permisos
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener {   //Solicitamos la ultima localizacion
                        requestNewLocationData()
                    }
                }
            } else activationLocation()
        } else requestPermissionLocation()
    }

    @SuppressLint(
        "MissingPermission",
        "VisibleForTests"
    ) //Olvida los permisos la comprobacion esta hecha en manageLocation()
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            //Variable donde guardamos todos los datos (Ubicación del Usuario)
            // Solo funciona con la version 'com.google.android.gms:play-services-location:17.0.0'
            val mLastLocation: Location = locationResult.lastLocation
            initialLatitude = mLastLocation.latitude
            initialLongitude = mLastLocation.longitude
            //Si nos encontramos en una posicion de la carrera mayor de cero registramos la posicion
            if (timeInSeconds > 0L) registerNewLocation(mLastLocation)
        }
    }

    private fun registerNewLocation(location: Location) {
        //Controlamos cada nuevo punto de geolocalizacion que se recibe

        var new_latitude: Double = location.latitude
        var new_longitude: Double = location.longitude

        if (flagSavedLocation) {
            if (timeInSeconds >= INTERVAL_LOCATION) {
                var distanceInterval = calculateDistance(new_latitude, new_longitude)

                if (distanceInterval <= LIMIT_DISTANCE_ACCEPTED) {
                    updateSpeeds(distanceInterval)
                    refreshInterfaceData()

                    saveLocation(location)//Guardamos las localizaciones

                    var newPos = LatLng(new_latitude, new_longitude)
                    (listPoints as ArrayList<LatLng>).add(newPos) //casting a arraylist
                    createPolylines(listPoints) //pintamos los polylines

                    //Comprobamos si hay un nuevo record
                    //checkMedals(distance, avgSpeed, maxSpeed)

                }

            }
        }
        latitude = new_latitude
        longitude = new_longitude

        if (mapCentered == true) centerMap(latitude, longitude)

        if (minLatitude == null) { // Si es nulo le ponemos todos los valores
            minLatitude = latitude
            maxLatitude = latitude
            minLongitude = longitude
            maxLongitude = longitude
        }
        //Controlamos si es un valor minimo/maximo de todos los valores
        if (latitude < minLatitude!!) minLatitude = latitude
        if (latitude > maxLatitude!!) maxLatitude = latitude
        if (longitude < minLongitude!!) minLongitude = longitude
        if (longitude > maxLongitude!!) maxLongitude = longitude

        //Si la altitud es por encima de la maxima o minima
        if (location.hasAltitude()) {
            if (maxAltitude == null) {
                maxAltitude = location.altitude
                minAltitude = location.altitude
            }
            if (location.latitude > maxAltitude!!) maxAltitude = location.altitude
            if (location.latitude < minAltitude!!) minAltitude = location.altitude
        }
    }

    private fun checkMedals(distance: Double, averageSpeed: Double, maxSpeed: Double) {

        //if (distance > 0) {
        // Preguntamos si Distancia es mayor que cero
        //if (distance > totalsSelectedSport.recordDistance!! && record_distancia_conseguido){
        if (distance > medalsListSportSelectedDistance[0]) { //Es mayor que el primer elemento ?
            recDistanceGold = true; recDistanceSilver = false; recDistanceBronze =
                false //Ha conseguido Oro
            //notifyMedal("distance", "gold", "PERSONAL") // Enviamos la notificacion

            if (record_distancia_conseguido) {
                //notifyMedalSnack("Distancia", "Gold")
                //notifyMedalSnack("Has alcanzado un nuevo record de DISTANCIA", "Medalla de ORO")
                notifyMedal("distance", "gold", "PERSONAL") // Enviamos la notificacion
                record_distancia_conseguido = false
            }
        } else { // Si no es mayor que el siguiente
            if (distance > medalsListSportSelectedDistance[1]) { //Es mayor que el segundo elemento ?
                recDistanceGold = false; recDistanceSilver = true; recDistanceBronze =
                    false // Ha conseguido Plata
                //notifyMedal("distance", "silver", "PERSONAL")


                if (record_distancia_conseguido) {
                    //notifyMedalSnack("Has alcanzado un nuevo record de DISTANCIA", "Medalla de PLATA")
                    notifyMedal("distance", "silver", "PERSONAL")
                    record_distancia_conseguido = false
                }

            } else {
                if (distance > medalsListSportSelectedDistance[2]) { //Es mayor que el tercer elemento ?
                    recDistanceGold = false; recDistanceSilver = false; recDistanceBronze =
                        true // Medalla Bronce
                    //notifyMedal("distance", "bronze", "PERSONAL")

                    if (record_distancia_conseguido) {
                        //notifyMedalSnack("Has alcanzado un nuevo record de DISTANCIA", "Medalla de BRONCE")
                        notifyMedal("distance", "bronze", "PERSONAL")
                        record_distancia_conseguido = false
                    }
                }
            }
        }
        //}

        if (averageSpeed > 0) { //Preguntamos si Velocidad media es mayor que cero
            if (averageSpeed > medalsListSportSelectedAvgSpeed[0]) {
                recAvgSpeedGold = true; recAvgSpeedSilver = false; recAvgSpeedBronze = false
                //notifyMedal("avgSpeed", "gold", "PERSONAL")

                if (record_avgSpeed_conseguido) {
                    //notifyMedalSnack("Has alcanzado un nuevo record de VELOCIDAD MEDIA", "Medalla de ORO")
                    notifyMedal("avgSpeed", "gold", "PERSONAL")
                    record_avgSpeed_conseguido = false
                }

            } else {
                if (averageSpeed > medalsListSportSelectedAvgSpeed[1]) {
                    recAvgSpeedGold = false; recAvgSpeedSilver = true; recAvgSpeedBronze = false
                    //notifyMedal("avgSpeed", "silver", "PERSONAL")

                    if (record_avgSpeed_conseguido) {
                        //notifyMedalSnack("Has alcanzado un nuevo record de  VELOCIDAD MEDIA", "Medalla de PLATA")
                        notifyMedal("avgSpeed", "silver", "PERSONAL")
                        record_avgSpeed_conseguido = false
                    }
                } else {
                    if (averageSpeed > medalsListSportSelectedAvgSpeed[2]) {
                        recAvgSpeedGold = false; recAvgSpeedSilver = false; recAvgSpeedBronze = true
                        //notifyMedal("avgSpeed", "bronze", "PERSONAL")

                        if (record_avgSpeed_conseguido) {
                            //notifyMedalSnack("Has alcanzado un nuevo record de  VELOCIDAD MEDIA", "Medalla de BRONCE")
                            notifyMedal("avgSpeed", "bronze", "PERSONAL")
                            record_avgSpeed_conseguido = false
                        }

                    }
                }
            }
        }

        if (maxSpeed > 0) { //Preguntamos si velocidad maxima es mayor que cero
            if (maxSpeed > medalsListSportSelectedMaxSpeed[0]) {
                recMaxSpeedGold = true; recMaxSpeedSilver = false; recMaxSpeedBronze = false
                //notifyMedal("maxSpeed", "gold", "PERSONAL")

                if (record_speed_conseguido) {
                    //notifyMedalSnack("Has alcanzado un nuevo record de  VELOCIDAD MÁXIMA", "Medalla de ORO")
                    notifyMedal("maxSpeed", "gold", "PERSONAL")
                    record_speed_conseguido = false
                }

            } else {
                if (maxSpeed > medalsListSportSelectedMaxSpeed[1]) {
                    recMaxSpeedGold = false; recMaxSpeedSilver = true; recMaxSpeedBronze = false
                    //notifyMedal("maxSpeed", "silver", "PERSONAL")

                    if (record_speed_conseguido) {
                        //notifyMedalSnack("Has alcanzado un nuevo record de  VELOCIDAD MÁXIMA", "Medalla de PLATA")
                        notifyMedal("maxSpeed", "silver", "PERSONAL")
                        record_speed_conseguido = false
                    }

                } else {
                    if (maxSpeed > medalsListSportSelectedMaxSpeed[2]) {
                        recMaxSpeedGold = false; recMaxSpeedSilver = false; recMaxSpeedBronze = true
                        // notifyMedal("maxSpeed", "bronze", "PERSONAL")

                        if (record_speed_conseguido) {
                            //notifyMedalSnack("Has alcanzado un nuevo record de  VELOCIDAD MÁXIMA", " Medalla de BRONCE")
                            notifyMedal("maxSpeed", "bronze", "PERSONAL")
                            record_speed_conseguido = false
                        }

                    }
                }
            }
        }
    }

    private fun notifyMedalSnack(category: String, metal: String) {
        showCustomSnackbar(rlMain, "$category $metal", R.color.orange_strong, 5000)
    }

    @SuppressLint("MissingPermission")
    private fun notifyMedal(category: String, metal: String, scope: String) {

        val CHANNEL_NAME = "notifyMedal"
        val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
        var CHANNEL_ID = "NUEVO $scope RECORD - $sportSelected"

        var textNotification = ""
        when (metal) {
            "gold" -> textNotification = "1ª "
            "silver" -> textNotification = "2ª "
            "bronze" -> textNotification = "3ª "
        }
        textNotification += "mejor marca personal en "
        when (category) {
            "distance" -> textNotification += "distancia recorrida"
            "avgSpeed" -> textNotification += " velocidad promedio"
            "maxSpeed" -> textNotification += " velocidad máxima alcanzada"
        }

        //Guardamos las medallas en una variable
        var iconNotificacion: Int = 0
        when (metal) {
            "gold" -> iconNotificacion = R.drawable.medalgold
            "silver" -> iconNotificacion = R.drawable.medalsilver
            "bronze" -> iconNotificacion = R.drawable.medalbronze
        }

        //Constructor del Canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            //Constructor de la Notificacion
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(iconNotificacion)
                .setContentTitle(CHANNEL_ID)
                .setContentText(textNotification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            var notificationId: Int = 0
            when (category) {
                "distance" ->
                    when (metal) {
                        "gold" -> notificationId = 11
                        "silver" -> notificationId = 12
                        "bronze" -> notificationId = 13
                    }
                "avgSpeed" ->
                    when (metal) {
                        "gold" -> notificationId = 21
                        "silver" -> notificationId = 22
                        "bronze" -> notificationId = 23
                    }
                "maxSpeed" ->
                    when (metal) {
                        "gold" -> notificationId = 31
                        "silver" -> notificationId = 32
                        "bronze" -> notificationId = 33
                    }
            }
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }
    }

    //FIREBASE SAVE LOCATION
    private fun saveLocation(location: Location) {
        var dirName = dateRun + startTimeRun // Identificador de carrera Fecha y Hora de Inicio
        dirName = dirName.replace("/", "")
        dirName = dirName.replace(":", "")
        var docName = timeInSeconds.toString()
        while (docName.length < 4) docName = "0" + docName //Añadimos el formato de 00001

        /**La variable "speed" representa la velocidad actual y la variable "maxSpeed" es una
         * constante que representa la velocidad máxima permitida.
         * La condición verifica si la velocidad actual es igual a la velocidad máxima permitida
         * y si la velocidad actual es mayor que cero.
         * Si ambas condiciones son verdaderas, entonces "maxSpeed" se establece en verdadero.
         * De lo contrario, "maxSpeed" se establece en falso.
         * **/
        var maxSpeed: Boolean = speed == maxSpeed && speed > 0

        //var dbLocation = FirebaseFirestore.getInstance()
        //Dentro de location estaran todos los nombres de los usuarios y dentro de cada carpeta
        //Tendremos los identificadores de carrera que seran su fecha y hora de inicio
        //Dentro tendremos las localizaciones que estamos añadiendo
        firestore.db.collection("locations/$useremail/$dirName").document(docName).set(
            hashMapOf(
                "time" to SimpleDateFormat("HH:mm:ss").format(Date()),
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "altitude" to location.altitude,
                "hasAltitude" to location.hasAltitude(),
                "speedFromGoogle" to location.speed,
                "speedFromMe" to speed,
                "maxSpeed" to maxSpeed,
                "color" to tvChrono.currentTextColor // Almacena el Color. Si es Azul ha sido caminando o Rojo ha sido corriendo
            )
        )

    }

    private fun updateLocationLimits(location: Location) {
        // Si es nulo le ponemos todos los valores
        if (minLatitude == null) {
            minLatitude = latitude
            maxLatitude = latitude
            minLongitude = longitude
            maxLongitude = longitude
        }
        //Controlamos si es un valor minimo/maximo de todos los valores
        if (latitude < minLatitude!!) minLatitude = latitude
        if (latitude > maxLatitude!!) maxLatitude = latitude
        if (longitude < minLongitude!!) minLongitude = longitude
        if (longitude > maxLongitude!!) maxLongitude = longitude

        //Si la altitud es por encima de la maxima o minima
        if (location.hasAltitude()) {
            if (maxAltitude == null) {
                maxAltitude = location.altitude
                minAltitude = location.altitude
            }
            if (location.latitude > maxAltitude!!) maxAltitude = location.altitude
            if (location.latitude < minAltitude!!) minAltitude = location.altitude
        }
    }

    private fun calculateDistance(newLatitude: Double, newLongitude: Double): Double {
        //Formula Calcula distancia entre dos coordenadas
        val radioTierra = 6371.0 //en kilómetros
        val dLat = Math.toRadians(newLatitude - latitude)
        val dLng = Math.toRadians(newLongitude - longitude)
        val sindLat = sin(dLat / 2)
        val sindLng = sin(dLng / 2)
        val va1 =
            sindLat.pow(2.0) + (sindLng.pow(2.0)
                    * cos(Math.toRadians(latitude)) * cos(
                Math.toRadians(newLatitude)
            ))
        val va2 = 2 * atan2(sqrt(va1), sqrt(1 - va1))
        val newDistance = radioTierra * va2 //Distancia que hemos recorrido en el intervalo
        //if (n_distance < LIMIT_DISTANCE_ACCEPTED) distance += n_distance

        //La distancia acumulada la  voy registrando . Distancia total + la distancia que en cada
        //Intervalo hemos registrado
        distance += newDistance
        return newDistance
    }

    private fun updateSpeeds(d: Double) {
        //Funcion de calculo de distancias Velocidad = Distancia / Tiempo
        //Paso la distancia como parámetro , km se pasan a metros
        //la distancia se calcula en km, asi que la pasamos a metros para el calculo de velocidad
        //convertirmos m/s a km/h multiplicando por 3.6
        speed = ((d * 1000) / INTERVAL_LOCATION) * 3.6
        if (speed > maxSpeed) maxSpeed = speed
        avgSpeed = ((distance * 1000) / timeInSeconds) * 3.6
    }

    private fun refreshInterfaceData() {
        //Para refrescar los datos asignamos a todos los circular seekbar los datos
        // Tambien se actualizan los textview Si hemos hecho algún record
        tvCurrentDistance.text = roundNumber(distance.toString(), 2)
        tvCurrentAvgSpeed.text = roundNumber(avgSpeed.toString(), 1)
        tvCurrentSpeed.text = roundNumber(speed.toString(), 1)

        //Actualizamos el color del TexView para mostrar que hemos superado un record
        csbCurrentDistance.progress = distance.toFloat()//Actualizamos Distancia
        if (distance > totalsSelectedSport.recordDistance!!) { //La distancia total ha superado ya el record ?
            tvDistanceRecord.text = roundNumber(distance.toString(), 2)
            tvDistanceRecord.setTextColor(ContextCompat.getColor(this, R.color.salmon_dark))
            csbCurrentDistance.max =
                distance.toFloat() //Indicamos el maximo actual para que no siga creciendo el Seekbar
            csbCurrentDistance.progress = distance.toFloat()
            totalsSelectedSport.recordDistance = distance

            // si conseguimos un record se notifica con sonido y snackbar
            if (record_distancia_conseguido) {
                mpNotify?.start()
                showCustomSnackbar(rlMain,"Has alcanzado un nuevo record de DISTANCIA",R.color.orange_strong, 3000)
                record_distancia_conseguido = false
            }
        }

        csbCurrentAvgSpeed.progress = avgSpeed.toFloat()

        if (avgSpeed > totalsSelectedSport.recordAvgSpeed!!) {//Actualizamos velocidad Media
            tvAvgSpeedRecord.text = roundNumber(avgSpeed.toString(), 1)
            tvAvgSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.salmon_dark))
            csbRecordAvgSpeed.max = avgSpeed.toFloat()
            csbRecordAvgSpeed.progress = avgSpeed.toFloat()
            csbCurrentAvgSpeed.max = avgSpeed.toFloat()
            totalsSelectedSport.recordAvgSpeed = avgSpeed

            if (record_avgSpeed_conseguido) {
                mpNotify?.start()
                showCustomSnackbar(rlMain,"Has alcanzado un nuevo record de velocidad MEDIA!",R.color.orange_strong, 3000)

                record_avgSpeed_conseguido = false
            }

        }

        if (speed > totalsSelectedSport.recordSpeed!!) {
            tvMaxSpeedRecord.text = roundNumber(speed.toString(), 1)
            tvMaxSpeedRecord.setTextColor(ContextCompat.getColor(this, R.color.salmon_dark))
            csbRecordSpeed.max = speed.toFloat()
            csbRecordSpeed.progress = speed.toFloat()
            csbCurrentMaxSpeed.max = speed.toFloat()
            csbCurrentMaxSpeed.progress = speed.toFloat()
            csbCurrentSpeed.max = speed.toFloat()
            totalsSelectedSport.recordSpeed = speed

            if (record_speed_conseguido) {
                mpNotify?.start()
                showCustomSnackbar(rlMain,"Has alcanzado un nuevo record de velocidad MÁXIMA!",R.color.orange_strong, 3000)
                record_speed_conseguido = false
            }
        } else {
            //En el caso de que la velocidad que hemos registrado sea la maxima cambia el limite del seekbar
            //actualiza el dato correspondiente . Punto maximo se pinta de rosa y el actual de amarillo
            if (speed == maxSpeed) {
                csbCurrentMaxSpeed.max = csbRecordSpeed.max
                csbCurrentMaxSpeed.progress = speed.toFloat()
                csbCurrentSpeed.max = csbRecordSpeed.max
            }
        }
        csbCurrentMaxSpeed.max = csbRecordSpeed.max
        csbCurrentMaxSpeed.progress = speed.toFloat()
        csbCurrentSpeed.max = csbRecordSpeed.max
        csbCurrentSpeed.progress = speed.toFloat() //Actualizamos velocidad
    }// FIN refreshInterfaceData

    private fun initMap() {
        listPoints = arrayListOf()
        (listPoints as ArrayList<LatLng>).clear() //hacemos un casting de iterable a arraylist para limpiar datos
        createMapFragment()
        if (allPermissionsGrantedGPS()) lyOpenerButton.isEnabled = true
        else lyOpenerButton.isEnabled = false
    }

    private fun createMapFragment() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }

    private fun enableMyLocation() {
        if (!::map.isInitialized) return//si el mapa no está inicializado se sale
        //Si no estan aprobados los permisos se sale ( Comprobacion de Google obligatoria )
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED

            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLocation()
            return
        } else map.isMyLocationEnabled = true
    }

    private fun centerMap(lt: Double, ln: Double) {
        val posMap = LatLng(lt, ln)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(posMap, 16f), 1000, null)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        enableMyLocation()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnMapLongClickListener { mapCentered = false }
        map.setOnMapClickListener { mapCentered = false }
        manageLocation() //Capturamos los datos
        centerMap(
            initialLatitude,
            initialLongitude
        )// Desplaza la camara al punto que le hayamos indicado
    }

    fun changeTypeMap(v: View) {// Cambia los modos y las imageview de modos hibrido/normal
        if (map.mapType == GoogleMap.MAP_TYPE_HYBRID) {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            ivTypeMap.setImageResource(R.drawable.map_type_hybrid)
        } else {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            ivTypeMap.setImageResource(R.drawable.map_type_normal)
        }
    }

    fun callCenterMap(v: View) {
        mapCentered = true
        if (latitude == 0.0) centerMap(
            initialLatitude,
            initialLongitude
        ) //si hace al inicio esta en cero lo centramos con datos iniciacles
        else centerMap(latitude, longitude) // Si tenemos datos recuperamos datos
    }

    fun callShowHideMap(v: View) {
        if (allPermissionsGrantedGPS()) { // Estan los permisos ?
            if (lyMap.height == 0) { //Si el layout tiene cero es que está oculto
                setHeightLinearLayout(lyMap, 1250)//medida que hay que darle para ver el mapa
                animateViewofFloat(
                    lyFragmentMap,
                    "translationY",
                    0f,
                    0
                )//Animamos la vista del hijo
                ivOpenClose.rotation = 180f// le damos la vuelta al imageview
            } else { //en el caso contrario ( que este desplegado ) lo encogemos
                setHeightLinearLayout(lyMap, 0)
                lyFragmentMap.translationY = -300f
                ivOpenClose.rotation = 0f
            }
        } else requestPermissionLocation()//Si no estan los permisos hay que pedirlos
    }

    //cuando se piden permisos sale la ventana y regresa con nuevos permisos tenemos que saber que ha cambiado
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            //Analizamos el permiso de la ubicación
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) // si ha sido aprobado
                    lyOpenerButton.isEnabled = true //habilitamos el layout
                else {
                    if (lyMap.height > 0) { //layout padre// Si es mayor de 0 está desplegado
                        setHeightLinearLayout(
                            lyMap,
                            0
                        ) //encogemos y el hijo lo desplazamos hacia arriba
                        lyFragmentMap.translationY = -300f
                        ivOpenClose.rotation = 0f
                    }
                    lyOpenerButton.isEnabled = false// En caso contrario lo desabilitamos
                }
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onMyLocationClick(p0: Location) {
        TODO("Not yet implemented")
    }

    fun selectBike(v: View) {
        if (timeInSeconds.toInt() == 0) selectSport("Bike")
    }

    fun selectRollerSkate(v: View) {
        if (timeInSeconds.toInt() == 0) selectSport("RollerSkate")
    }

    fun selectRunning(v: View) {
        if (timeInSeconds.toInt() == 0) selectSport("Running")
    }

    private fun selectSport(sport: String) {
        sportSelected = sport
        when (sport) {
            "Bike" -> {
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_BIKE
                lySportBike.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.orange
                    )
                )
                lySportRollerSkate.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.gray_medium
                    )
                )
                lySportRunning.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.gray_medium
                    )
                )
                levelSelectedSport = levelBike
                totalsSelectedSport = totalsBike

                medalsListSportSelectedDistance = medalsListBikeDistance
                medalsListSportSelectedAvgSpeed = medalsListBikeAvgSpeed
                medalsListSportSelectedMaxSpeed = medalsListBikeMaxSpeed
            }

            "RollerSkate" -> {
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE

                lySportBike.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.gray_medium
                    )
                )
                lySportRollerSkate.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.orange
                    )
                )
                lySportRunning.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.gray_medium
                    )
                )
                levelSelectedSport = levelRollerSkate
                totalsSelectedSport = totalsRollerSkate

                medalsListSportSelectedDistance = medalsListRollerSkateDistance
                medalsListSportSelectedAvgSpeed = medalsListRollerSkateAvgSpeed
                medalsListSportSelectedMaxSpeed = medalsListRollerSkateMaxSpeed
            }
            "Running" -> {
                LIMIT_DISTANCE_ACCEPTED = LIMIT_DISTANCE_ACCEPTED_RUNNING

                lySportBike.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.gray_medium
                    )
                )
                lySportRollerSkate.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.gray_medium
                    )
                )
                lySportRunning.setBackgroundColor(
                    ContextCompat.getColor(
                        mainContext,
                        R.color.orange
                    )
                )
                levelSelectedSport = levelRunning
                totalsSelectedSport = totalsRunning

                medalsListSportSelectedDistance = medalsListRunningDistance
                medalsListSportSelectedAvgSpeed = medalsListRunningAvgSpeed
                medalsListSportSelectedMaxSpeed = medalsListRunningMaxSpeed
            }
        }
        refreshCBSsSport()
        refreshRecords()
    }

    private fun refreshCBSsSport() {
        csbRecordDistance.max = totalsSelectedSport.recordDistance?.toFloat()!!
        csbRecordDistance.progress = totalsSelectedSport.recordDistance?.toFloat()!!
        csbRecordAvgSpeed.max = totalsSelectedSport.recordAvgSpeed?.toFloat()!!
        csbRecordAvgSpeed.progress = totalsSelectedSport.recordAvgSpeed?.toFloat()!!
        csbRecordSpeed.max = totalsSelectedSport.recordSpeed?.toFloat()!!
        csbRecordSpeed.progress = totalsSelectedSport.recordSpeed?.toFloat()!!
        csbCurrentDistance.max = csbRecordDistance.max
        csbCurrentAvgSpeed.max = csbRecordAvgSpeed.max
        csbCurrentSpeed.max = csbRecordSpeed.max
        csbCurrentMaxSpeed.max = csbRecordSpeed.max
        csbCurrentMaxSpeed.progress = 0f
    }

    private fun refreshRecords() {
        if (totalsSelectedSport.recordDistance!! > 0)
            tvDistanceRecord.text = totalsSelectedSport.recordDistance.toString()
        else
            tvDistanceRecord.text = ""
        if (totalsSelectedSport.recordAvgSpeed!! > 0)
            tvAvgSpeedRecord.text = totalsSelectedSport.recordAvgSpeed.toString()
        else
            tvAvgSpeedRecord.text = ""
        if (totalsSelectedSport.recordSpeed!! > 0)
            tvMaxSpeedRecord.text = totalsSelectedSport.recordSpeed.toString()
        else
            tvMaxSpeedRecord.text = ""
    }

    private fun initLevels() {
        levelSelectedSport = Level()
        levelBike = Level()
        levelRollerSkate = Level()
        levelRunning = Level()

        levelsListBike = arrayListOf()
        levelsListBike.clear()

        levelsListRollerSkate = arrayListOf()
        levelsListRollerSkate.clear()

        levelsListRunning = arrayListOf()
        levelsListRunning.clear()

        levelBike.name = "level_1"
        levelBike.image = "level_1"
        levelBike.RunsTarget = 1
        levelBike.DistanceTarget = 1

        levelRollerSkate.name = "level_1"
        levelRollerSkate.image = "level_1"
        levelRollerSkate.RunsTarget = 1
        levelRollerSkate.DistanceTarget = 1

        levelRunning.name = "level_1"
        levelRunning.image = "level_1"
        levelRunning.RunsTarget = 1
        levelRunning.DistanceTarget = 1
    }

    private fun createPolylines(listPosition: Iterable<LatLng>) {
        //creamos la polylinea
        val polylineOptions = PolylineOptions()
            .width(25f)
            .color(ContextCompat.getColor(this, R.color.salmon_dark))
            .addAll(listPosition)
        //la pintamos en el mapa // devuelve un valor y lo guardamos y redondeamos
        val polyline = map.addPolyline(polylineOptions)
        polyline.startCap = RoundCap()
    }

    private fun initPreferences() {
        sharedPreferences = getSharedPreferences("sharedPrefs_$useremail", MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun savePreferences() {
        editor.clear()
        editor.apply {
            if(isPremium) putBoolean(key_premium, true)
            putString(key_userApp, useremail)
            putString(key_provider, providerSession)
            putString(key_selectedSport, sportSelected)
            putBoolean(key_modeInterval, swIntervalMode.isChecked)
            putInt(key_intervalDuration, npDurationInterval.value)
            putFloat(key_progressCircularSeekBar, csbRunWalk.progress)
            putFloat(key_maxCircularSeekBar, csbRunWalk.max)
            putString(key_runningTime, tvRunningTime.text.toString())
            putString(key_walkingTime, tvWalkingTime.text.toString())
            putBoolean(key_modeChallenge, swChallenges.isChecked)
            putBoolean(key_modeChallengeDuration, !(challengeDuration == 0))
            putInt(key_challengeDurationHH, npChallengeDurationHH.value)
            putInt(key_challengeDurationMM, npChallengeDurationMM.value)
            putInt(key_challengeDurationSS, npChallengeDurationSS.value)
            putBoolean(key_modeChallengeDistance, !(challengeDistance == 0f))
            putInt(key_challengeDistance, npChallengeDistance.value)
            putBoolean(key_challengeNofify, cbNotify.isChecked)
            putBoolean(key_challengeAutofinish, cbAutoFinish.isChecked)
            putInt(key_hardVol, sbHardVolume.progress)
            putInt(key_softVol, sbSoftVolume.progress)
            putInt(key_notifyVol, sbNotifyVolume.progress)

        }.apply()
    }


    private fun recoveryPreferences() {
        if (sharedPreferences.getString(key_userApp, "null") == useremail) {
            sportSelected = sharedPreferences.getString(key_selectedSport, "Running").toString()

            swIntervalMode.isChecked = sharedPreferences.getBoolean(key_modeInterval, false)
            if (swIntervalMode.isChecked) {
                npDurationInterval.value = sharedPreferences.getInt(key_intervalDuration, 5)
                roundInterval = npDurationInterval.value * 60
                csbRunWalk.progress =
                    sharedPreferences.getFloat(key_progressCircularSeekBar, 150.0f)
                csbRunWalk.max = sharedPreferences.getFloat(key_maxCircularSeekBar, 300.0f)
                tvRunningTime.text = sharedPreferences.getString(key_runningTime, "2:30")
                tvWalkingTime.text = sharedPreferences.getString(key_walkingTime, "2:30")
                swIntervalMode.callOnClick()
            }

            swChallenges.isChecked = sharedPreferences.getBoolean(key_modeChallenge, false)
            if (swChallenges.isChecked) {
                swChallenges.callOnClick()
                if (sharedPreferences.getBoolean(key_modeChallengeDuration, false)) {
                    npChallengeDurationHH.value =
                        sharedPreferences.getInt(key_challengeDurationHH, 1)
                    npChallengeDurationMM.value =
                        sharedPreferences.getInt(key_challengeDurationMM, 0)
                    npChallengeDurationSS.value =
                        sharedPreferences.getInt(key_challengeDurationSS, 0)
                    getChallengeDuration(
                        npChallengeDurationHH.value,
                        npChallengeDurationMM.value,
                        npChallengeDurationSS.value
                    )
                    challengeDistance = 0f

                    showChallenge("duration")
                }
                if (sharedPreferences.getBoolean(key_modeChallengeDistance, false)) {
                    npChallengeDistance.value =
                        sharedPreferences.getInt(key_challengeDistance, 10)
                    challengeDistance = npChallengeDistance.value.toFloat()
                    challengeDuration = 0

                    showChallenge("distance")
                }
            }
            cbNotify.isChecked = sharedPreferences.getBoolean(key_challengeNofify, true)
            cbAutoFinish.isChecked =
                sharedPreferences.getBoolean(key_challengeAutofinish, false)
            sbHardVolume.progress = sharedPreferences.getInt(key_hardVol, 100)
            sbSoftVolume.progress = sharedPreferences.getInt(key_softVol, 100)
            sbNotifyVolume.progress = sharedPreferences.getInt(key_notifyVol, 100)
        } else sportSelected = "Running" // Si es nulo seleccionamos Runnin por defecto
    }

    private fun showPopUp() {
        rlMain.isEnabled = false //deshabilitamos el menu principal
        lyPopupRun.isVisible = true //Habilitamos el PopUp
        //Desplazamos el Layout hacia el centro (0f) de la pantalla para que se visualice
        //var lyWindow = findViewById<LinearLayout>(R.id.lyWindow)
        ObjectAnimator.ofFloat(lyWindow, "translationX", 0f).apply {
            duration = 200L // Esto es el tiempo de la animacion para que lo muestre rapido
            start()
        }
        loadDataPopUp() //Funcion con la que Cargamos los datos
    }

    private fun loadDataPopUp() {
        showHeaderPopUp()
        showMedals()
        showDataRun()
    }

    private fun showHeaderPopUp() {
        var csbRunsLevel = findViewById<CircularSeekBar>(R.id.csbRunsLevel)
        var csbDistanceLevel = findViewById<CircularSeekBar>(R.id.csbDistanceLevel)
        var tvTotalRunsLevel = findViewById<TextView>(R.id.tvTotalRunsLevel)
        var tvTotalDistanceLevel = findViewById<TextView>(R.id.tvTotalDistanceLevel)


        var ivSportSelected = findViewById<ImageView>(R.id.ivSportSelected)
        var ivCurrentLevel = findViewById<ImageView>(R.id.ivCurrentLevel)
        var tvTotalDistance = findViewById<TextView>(R.id.tvTotalDistance)
        var tvTotalTime = findViewById<TextView>(R.id.tvTotalTime)

        when (sportSelected) {
            "Bike" -> {
                levelSelectedSport = levelBike
                setLevelBike()
                ivSportSelected.setImageResource(R.drawable.bike)
            }
            "RollerSkate" -> {
                levelSelectedSport = levelRollerSkate
                setLevelRollerSkate()
                ivSportSelected.setImageResource(R.drawable.rollerskate)
            }
            "Running" -> {
                levelSelectedSport = levelRunning
                setLevelRunning()
                ivSportSelected.setImageResource(R.drawable.running)
            }
        }
        var tvNumberLevel = findViewById<TextView>(R.id.tvNumberLevel)
        var levelText = "${getString(R.string.level)} ${
            levelSelectedSport.image!!.subSequence(6, 7).toString()
        }"
        tvNumberLevel.text = levelText

        csbRunsLevel.max = levelSelectedSport.RunsTarget!!.toFloat()
        csbRunsLevel.progress = totalsSelectedSport.totalRuns!!.toFloat()
        if (totalsSelectedSport.totalRuns!! > levelSelectedSport.RunsTarget!!.toInt()) {
            csbRunsLevel.max = levelSelectedSport.RunsTarget!!.toFloat()
            csbRunsLevel.progress = csbRunsLevel.max
        }

        csbDistanceLevel.max = levelSelectedSport.DistanceTarget!!.toFloat()
        csbDistanceLevel.progress = totalsSelectedSport.totalDistance!!.toFloat()
        if (totalsSelectedSport.totalDistance!! > levelSelectedSport.DistanceTarget!!.toInt()) {
            csbDistanceLevel.max = levelSelectedSport.DistanceTarget!!.toFloat()
            csbDistanceLevel.progress = csbDistanceLevel.max
        }

        tvTotalRunsLevel.text =
            "${totalsSelectedSport.totalRuns!!}/${levelSelectedSport.RunsTarget!!}"

        var td = totalsSelectedSport.totalDistance!!
        var td_k: String = td.toString()
        if (td > 1000) td_k = (td / 1000).toInt().toString() + "K"
        var ld = levelSelectedSport.DistanceTarget!!.toDouble()
        var ld_k: String = ld.toInt().toString()
        if (ld > 1000) ld_k = (ld / 1000).toInt().toString() + "K"
        tvTotalDistance.text = "${td_k}/${ld_k} kms"

        var porcent =
            (totalsSelectedSport.totalDistance!!.toDouble() * 100 / levelSelectedSport.DistanceTarget!!.toDouble()).toInt()
        tvTotalDistanceLevel.text = "$porcent%"

        when (levelSelectedSport.image) {
            "level_1" -> ivCurrentLevel.setImageResource(R.drawable.level_1)
            "level_2" -> ivCurrentLevel.setImageResource(R.drawable.level_2)
            "level_3" -> ivCurrentLevel.setImageResource(R.drawable.level_3)
        }

        var formatedTime = getFormattedTotalTime(totalsSelectedSport.totalTime!!.toLong())
        tvTotalTime.text = getString(R.string.PopUpTotalTime) + formatedTime

    }

    private fun showMedals() {

        val ivMedalDistance = findViewById<ImageView>(R.id.ivMedalDistance)
        val ivMedalAvgSpeed = findViewById<ImageView>(R.id.ivMedalAvgSpeed)
        val ivMedalMaxSpeed = findViewById<ImageView>(R.id.ivMedalMaxSpeed)

        val tvMedalDistanceTitle = findViewById<TextView>(R.id.tvMedalDistanceTitle)
        val tvMedalAvgSpeedTitle = findViewById<TextView>(R.id.tvMedalAvgSpeedTitle)
        val tvMedalMaxSpeedTitle = findViewById<TextView>(R.id.tvMedalMaxSpeedTitle)

        //Consultamos las medallas para poner la imagen
        if (recDistanceGold) ivMedalDistance.setImageResource(R.drawable.medalgold)
        if (recDistanceSilver) ivMedalDistance.setImageResource(R.drawable.medalsilver)
        if (recDistanceBronze) ivMedalDistance.setImageResource(R.drawable.medalbronze)

        //Si alguna medalla está en true ponemos el mensaje "Medalla de "
        if (recDistanceGold || recDistanceSilver || recDistanceBronze)
            tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)

        if (recAvgSpeedGold) ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
        if (recAvgSpeedSilver) ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
        if (recAvgSpeedBronze) ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
        if (recAvgSpeedGold || recAvgSpeedSilver || recAvgSpeedBronze)
            tvMedalAvgSpeedTitle.setText(R.string.medalAvgSpeedDescription)

        if (recMaxSpeedGold) ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
        if (recMaxSpeedSilver) ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
        if (recMaxSpeedBronze) ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
        if (recMaxSpeedGold || recMaxSpeedSilver || recMaxSpeedBronze)
            tvMedalMaxSpeedTitle.setText(R.string.medalMaxSpeedDescription)

    }

    private fun showDataRun() {
        var tvDurationRun = findViewById<TextView>(R.id.tvDurationRun)
        var lyChallengeDurationRun = findViewById<LinearLayout>(R.id.lyChallengeDurationRun)
        var tvChallengeDurationRun = findViewById<TextView>(R.id.tvChallengeDurationRun)
        var lyIntervalRun = findViewById<LinearLayout>(R.id.lyIntervalRun)
        var tvIntervalRun = findViewById<TextView>(R.id.tvIntervalRun)
        var tvDistanceRun = findViewById<TextView>(R.id.tvDistanceRun)
        var lyChallengeDistancePopUp = findViewById<LinearLayout>(R.id.lyChallengeDistancePopUp)
        var tvChallengeDistanceRun = findViewById<TextView>(R.id.tvChallengeDistanceRun)
        var lyUnevennessRun = findViewById<LinearLayout>(R.id.lyUnevennessRun)
        var tvMaxUnevennessRun = findViewById<TextView>(R.id.tvMaxUnevennessRun)
        var tvMinUnevennessRun = findViewById<TextView>(R.id.tvMinUnevennessRun)
        var tvAvgSpeedRun = findViewById<TextView>(R.id.tvAvgSpeedRun)
        var tvMaxSpeedRun = findViewById<TextView>(R.id.tvMaxSpeedRun)

        tvDurationRun.text = tvChrono.text //Duracion de la carrera lo que diga el Chrono

        if (challengeDuration > 0) { // Si tiene un reto de duracion
            setHeightLinearLayout(lyChallengeDurationRun, 120) //le damos altura al layout
            // Ponemos dentro cual fue la duracion que se marco como reto
            tvChallengeDurationRun.text =
                getFormattedStopWatch((challengeDuration * 1000).toLong())
            //en formato de cronometro 00:00:00 con getFormattedStopWatch
        } else setHeightLinearLayout(lyChallengeDurationRun, 0)//en el caso de no, altura cero

        if (swIntervalMode.isChecked) { //Si tiene intervalos
            setHeightLinearLayout(lyIntervalRun, 120)//le damos altura al layout
            var details =
                "${npDurationInterval.value}mins. (" //Indicamos que tipo de intervalo duracion del intervalo
            details += "${tvRunningTime.text} / ${tvWalkingTime.text})" //Cuanto tiempo para caminar / correr
            tvIntervalRun.text =
                details //El details se lo pasamos al textview del popup que muestra el dato
        } else setHeightLinearLayout(lyIntervalRun, 0)//en el caso de que no, altura cero

        //distancia de la carrera la convertimos a texto y redondeamos 2 decimales
        tvDistanceRun.text = roundNumber(distance.toString(), 2)

        if (challengeDistance > 0f) { //Si tenia reto de distancia
            setHeightLinearLayout(lyChallengeDistancePopUp, 120)//le damos altura al layout
            tvChallengeDistanceRun.text =
                challengeDistance.toString()//Indicamos el tipo de distancia
        } else setHeightLinearLayout(
            lyChallengeDistancePopUp,
            0
        )//en el caso de que no altura cero

        if (maxAltitude == null) setHeightLinearLayout(
            lyUnevennessRun,
            0
        )//Desnivel de la carrera
        //Si es null no se han capturado datos entonces lo ponemos en cero

        else { //En caso contario si se han capturado datos
            setHeightLinearLayout(lyUnevennessRun, 120)//le damos altura al layout
            tvMaxUnevennessRun.text =
                maxAltitude!!.toInt().toString() //pasamos los datos a int y string
            tvMinUnevennessRun.text = minAltitude!!.toInt().toString()
        }

        tvAvgSpeedRun.text = roundNumber(avgSpeed.toString(), 1)//Velocidad media
        tvMaxSpeedRun.text = roundNumber(maxSpeed.toString(), 1) //Velocida maxima

    }

    fun deleteRun(view: View) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alertDeleteRunTitle))
            .setMessage(getString(R.string.alertDeleteRunDescription))
            .setPositiveButton(R.string.acept,
                DialogInterface.OnClickListener { dialog, which ->

                    //Codigo del ejercicio
                    var id: String = useremail + dateRun + startTimeRun
                    id = id.replace("/", "")
                    id = id.replace(":", "")

                    var lyPopUpRun = findViewById<LinearLayout>(R.id.lyPopupRun)
                    var currentRun = Runs()
                    currentRun.distance = roundNumber(distance.toString(), 1).toDouble()
                    currentRun.avgSpeed = roundNumber(avgSpeed.toString(), 1).toDouble()
                    currentRun.maxSpeed = roundNumber(maxSpeed.toString(), 1).toDouble()
                    currentRun.duration = tvChrono.text.toString()
                    firestore.deleteRunAndLinkedData(id, sportSelected, lyPopUpRun, currentRun)
                    loadMedalsUser()
                    setLevelSport(sportSelected)
                    closePopUpRun()
                })
            .setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { dialog, which ->
                    //no hacer nada
                })
            .setCancelable(true)
            .show()
    }

    fun closePopUp(v: View) {
        closePopUpRun()
    }

    private fun closePopUpRun() {
        hidePopUpRun()
        rlMain.isEnabled = true //habilitamos menu principal
        //Cuando pulsamos en el boton finalizar  se muestran los datos con showPopUp
        // Al cerrar es cuando hay que limpiar los datos de las variables solo cuando se han mostrado la ventana
        // ya que los dato podrian ser necesarios
        resetVariablesRun() //reseteamos variables
        resetMedals() // resetea medallas
        selectSport(sportSelected)
        updateWidgets()
    }

    private fun loadMedalsUser() {
        loadMedalsBike()
        loadMedalsRollerSkate()
        loadMedalsRunning()
    }

    //FIREBASE LOAD MEDALS BIKE
    private fun loadMedalsBike() {
        //var dbRecords = FirebaseFirestore.getInstance()

        //Calculamos el Top 3 de Distancia
        firestore.db.collection("runsBike")
            .orderBy("distance", Query.Direction.DESCENDING) //Ordenamos de mayor a menor
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail) // Compruebo el usuario
                        medalsListBikeDistance.add(
                            document["distance"].toString().toDouble()
                        ) // Añado el usuario al array
                    if (medalsListBikeDistance.size == 3) break
                }
                while (medalsListBikeDistance.size < 3) medalsListBikeDistance.add(0.0) //Cuando sea menor de 3 añadimos 0.0

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        firestore.db.collection("runsBike")
            .orderBy("avgSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListBikeAvgSpeed.add(document["avgSpeed"].toString().toDouble())
                    if (medalsListBikeAvgSpeed.size == 3) break
                }
                while (medalsListBikeAvgSpeed.size < 3) medalsListBikeAvgSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        firestore.db.collection("runsBike")
            .orderBy("maxSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListBikeMaxSpeed.add(document["maxSpeed"].toString().toDouble())
                    if (medalsListBikeMaxSpeed.size == 3) break
                }
                while (medalsListBikeMaxSpeed.size < 3) medalsListBikeMaxSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    //FIREBASE LOAD MEDALS ROLLER
    private fun loadMedalsRollerSkate() {
        //var dbRecords = FirebaseFirestore.getInstance()
        firestore.db.collection("runsRollerSkate")
            .orderBy("distance", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListRollerSkateDistance.add(
                            document["distance"].toString().toDouble()
                        )
                    if (medalsListRollerSkateDistance.size == 3) break
                }
                while (medalsListRollerSkateDistance.size < 3) medalsListRollerSkateDistance.add(
                    0.0
                )

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        firestore.db.collection("runsRollerSkate")
            .orderBy("avgSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListRollerSkateAvgSpeed.add(
                            document["avgSpeed"].toString().toDouble()
                        )
                    if (medalsListRollerSkateAvgSpeed.size == 3) break
                }
                while (medalsListRollerSkateAvgSpeed.size < 3) medalsListRollerSkateAvgSpeed.add(
                    0.0
                )

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        firestore.db.collection("runsRollerSkate")
            .orderBy("maxSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListRollerSkateMaxSpeed.add(
                            document["maxSpeed"].toString().toDouble()
                        )
                    if (medalsListRollerSkateMaxSpeed.size == 3) break
                }
                while (medalsListRollerSkateMaxSpeed.size < 3) medalsListRollerSkateMaxSpeed.add(
                    0.0
                )

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    //FIREBASE LOAD MEDALS RUNNING
    private fun loadMedalsRunning() {
        //var dbRecords = FirebaseFirestore.getInstance()
        firestore.db.collection("runsRunning")
            .orderBy("distance", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListRunningDistance.add(
                            document["distance"].toString().toDouble()
                        )
                    if (medalsListRunningDistance.size == 3) break
                }
                while (medalsListRunningDistance.size < 3) medalsListRunningDistance.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        firestore.db.collection("runsRunning")
            .orderBy("avgSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListRunningAvgSpeed.add(
                            document["avgSpeed"].toString().toDouble()
                        )
                    if (medalsListRunningAvgSpeed.size == 3) break
                }
                while (medalsListRunningAvgSpeed.size < 3) medalsListRunningAvgSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        firestore.db.collection("runsRunning")
            .orderBy("maxSpeed", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document["user"] == useremail)
                        medalsListRunningMaxSpeed.add(
                            document["maxSpeed"].toString().toDouble()
                        )
                    if (medalsListRunningMaxSpeed.size == 3) break
                }
                while (medalsListRunningMaxSpeed.size < 3) medalsListRunningMaxSpeed.add(0.0)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    fun takePicture(v: View) { //Envia los datos a Camara
        val intent = Intent(this, Camara::class.java)
        val inParameter = intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        inParameter.putExtra("dateRun", dateRun)
        inParameter.putExtra("startTimeRun", startTimeRun)
        startActivity(intent)
    }

    fun shareRun(v: View) {
        callShareRun()
    }

    private fun callShareRun() {
        var tvDurationRun = findViewById<TextView>(R.id.tvDurationRun)
        var idRun = dateRun + startTimeRun
        idRun = idRun.replace(":", "")
        idRun = idRun.replace("/", "")

        var centerLatitude: Double = 0.0
        var centerLongitude: Double = 0.0

        if (activatedGPS == true) {
            centerLatitude = ((minLatitude!! + maxLatitude!!) / 2)
            centerLongitude = ((minLongitude!! + maxLongitude!!) / 2)
        }

        //var saveDuration = tvChrono.text.toString()
        var saveDuration = tvDurationRun.text.toString()
        var saveDistance = roundNumber(distance.toString(), 1)
        var saveMaxSpeed = roundNumber(maxSpeed.toString(), 1)
        var saveAvgSpeed = roundNumber(avgSpeed.toString(), 1)

        var medalDistance = "none"
        var medalAvgSpeed = "none"
        var medalMaxSpeed = "none"

        if (recDistanceGold) medalDistance = "gold"
        if (recDistanceSilver) medalDistance = "silver"
        if (recDistanceBronze) medalDistance = "bronze"

        if (recAvgSpeedGold) medalAvgSpeed = "gold"
        if (recAvgSpeedSilver) medalAvgSpeed = "silver"
        if (recAvgSpeedBronze) medalAvgSpeed = "bronze"

        if (recMaxSpeedGold) medalMaxSpeed = "gold"
        if (recMaxSpeedSilver) medalMaxSpeed = "silver"
        if (recMaxSpeedBronze) medalMaxSpeed = "bronze"

        //ENVIO DE PARAMETROS
        val intent = Intent(this, RunActivity::class.java)

        val inParameter = intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        inParameter.putExtra("user", useremail)
        inParameter.putExtra("idRun", idRun)
        inParameter.putExtra("centerLatitude", centerLatitude)
        inParameter.putExtra("centerLongitude", centerLongitude)

        inParameter.putExtra("countPhotos", countPhotos)
        inParameter.putExtra("lastimage", lastimage)

        inParameter.putExtra("date", dateRun)
        inParameter.putExtra("startTime", startTimeRun)
        inParameter.putExtra("duration", saveDuration)
        inParameter.putExtra("distance", saveDistance.toDouble())
        inParameter.putExtra("maxSpeed", saveMaxSpeed.toDouble())
        inParameter.putExtra("avgSpeed", saveAvgSpeed.toDouble())
        inParameter.putExtra("minAltitude", minAltitude)
        inParameter.putExtra("maxAltitude", maxAltitude)
        inParameter.putExtra("medalDistance", medalDistance)
        inParameter.putExtra("medalAvgSpeed", medalAvgSpeed)
        inParameter.putExtra("medalMaxSpeed", medalMaxSpeed)
        inParameter.putExtra("activatedGPS", activatedGPS)
        inParameter.putExtra("sport", sportSelected)
        inParameter.putExtra("intervalMode", swIntervalMode.isChecked)
        if (swIntervalMode.isChecked) {
            inParameter.putExtra("intervalDuration", npDurationInterval.value)
            inParameter.putExtra("runningTime", tvRunningTime.text.toString())
            inParameter.putExtra("walkingTime", tvWalkingTime.text.toString())
        }
        if (swChallenges.isChecked) {
            if (challengeDistance > 0f)
                inParameter.putExtra(
                    "challengeDistance",
                    roundNumber(challengeDistance.toString(), 1).toDouble()
                )
            if (challengeDuration > 0)
                inParameter.putExtra(
                    "challengeDuration",
                    getFormattedStopWatch(challengeDuration.toLong())
                )
        }

        inParameter.putExtra("level_n", levelSelectedSport.name)
        inParameter.putExtra("image_level", levelSelectedSport.image)
        inParameter.putExtra("distanceTarget", levelSelectedSport.DistanceTarget!!.toDouble())
        inParameter.putExtra("distanceTotal", totalsSelectedSport.totalDistance)
        inParameter.putExtra("runsTarget", levelSelectedSport.RunsTarget!!.toInt())
        inParameter.putExtra("runsTotal", totalsSelectedSport.totalRuns)

        startActivity(intent)

    }

}
