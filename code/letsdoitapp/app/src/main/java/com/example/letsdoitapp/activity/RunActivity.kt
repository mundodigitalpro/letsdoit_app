package com.example.letsdoitapp.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.updateLayoutParams
import com.example.letsdoitapp.R
import com.example.letsdoitapp.utils.Utility.setHeightLinearLayout
import com.example.letsdoitapp.data.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.io.File
import java.io.FileOutputStream
import java.util.*

class RunActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var user: String? = null
    private var idRun: String? = null
    private var centerLat: Double? = null
    private var centerLong: Double? = null
    private lateinit var tvShare: TextView
    private lateinit var rlActivity: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)
        createMapFragment()
        loadData()
        rlActivity = findViewById(R.id.rlActivity)
        tvShare = findViewById(R.id.tvShare)
    }
    private fun createMapFragment(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(centerLat!!, centerLong!!),
                16f), 1000, null)
        loadLocations()
    }

    private fun loadLocations(){
        val collection = "locations/$user/$idRun"
        var point: LatLng
        val listPoints: Iterable<LatLng>
        listPoints = arrayListOf()
        listPoints.clear()
        val dbLocations: FirebaseFirestore = FirebaseFirestore.getInstance()
        dbLocations.collection(collection)
            .orderBy("time")//Ordenamos las ubicaciones por el orden que se produjeron
            .get()
            .addOnSuccessListener { documents ->//En caso de exito recibo en un array de documentos
                for (docLocation in documents) { //Revisamos cada uno de ellos
                    val position = docLocation.toObject(Location::class.java) //Transformamos cada uno de estos objetos y lo guardamos
                    //listPosition.add(position!!)
                    point = LatLng(position.latitude!!, position.longitude!!) //capturamos logitud y latitud de position y la guardamos
                    listPoints.add(point) //El punto lo añadimos al array
                }
                paintRun(listPoints) //pintamos la carrera
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting locations: ", exception)
            }
    }
    private fun paintRun(listPosition:  Iterable<LatLng>){ //Funcion para pintar la carrera
        val polylineOptions = PolylineOptions()
            .width(25f)
            .color(ContextCompat.getColor(this, R.color.salmon_dark))
            .addAll(listPosition)
        val polyline = map.addPolyline(polylineOptions)
        polyline.startCap = RoundCap()
    }

    fun changeTypeMap(v: View){
        val ivTypeMap = findViewById<ImageView>(R.id.ivTypeMap)
        if (map.mapType == GoogleMap.MAP_TYPE_HYBRID){
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            ivTypeMap.setImageResource(R.drawable.map_type_hybrid)
        }
        else{
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            ivTypeMap.setImageResource(R.drawable.map_type_normal)
        }
    }

    //INICIO LOADDATA
    private fun loadData(){
        val bundle = intent.extras //RECEPCION DE PARAMETROS
        user = bundle?.getString("user")
        idRun = bundle?.getString("idRun")
        centerLat = bundle?.getDouble("centerLatitude")
        centerLong = bundle?.getDouble("centerLongitude")

        //Si no tiene datos se cierra el Layout
        if (bundle?.getDouble("distanceTarget") == 0.0){
            val lyCurrentLevel = findViewById<LinearLayout>(R.id.lyCurrentLevel)
            setHeightLinearLayout(lyCurrentLevel, 0)

        } else{ // En caso contrario Tiene datos le damos valores

            //NIVEL
            val levelText = "${getString(R.string.level)} ${bundle?.getString("image_level")!!.subSequence(6,7).toString()}"
            val tvNumberLevel = findViewById<TextView>(R.id.tvNumberLevel)
            tvNumberLevel.text = levelText
            val ivCurrentLevel = findViewById<ImageView>(R.id.ivCurrentLevel)
            when (bundle.getString("image_level")){
                "level_1" -> ivCurrentLevel.setImageResource(R.drawable.level_1)
                "level_2" -> ivCurrentLevel.setImageResource(R.drawable.level_2)
                "level_3" -> ivCurrentLevel.setImageResource(R.drawable.level_3)
//                "level_4" -> ivCurrentLevel.setImageResource(R.drawable.level_4)
//                "level_5" -> ivCurrentLevel.setImageResource(R.drawable.level_5)
//                "level_6" -> ivCurrentLevel.setImageResource(R.drawable.level_6)
//                "level_7" -> ivCurrentLevel.setImageResource(R.drawable.level_7)
            }

            //CIRCULAR SEEK BAR
            val csbDistanceLevel = findViewById<CircularSeekBar>(R.id.csbDistanceLevel)
            csbDistanceLevel.max = bundle.getDouble("distanceTarget").toFloat() // mod a !!
            csbDistanceLevel.progress = bundle.getDouble("distanceTotal").toFloat()// mod a !!

            //DISTANCIA TOTAL
            val td = bundle.getDouble("distanceTotal")
            var td_k: String = td.toString()
            if (td > 1000) td_k = (td/1000).toInt().toString() + "K"
            //var ld = bundle?.getDouble("distanceTotal").toDouble()
            val ld = bundle.getDouble("distanceTotal").toDouble()
            var ld_k: String = ld.toInt().toString()
            if (ld > 1000) ld_k = (ld/1000).toInt().toString() + "K"

            var tvTotalDistance = findViewById<TextView>(R.id.tvTotalDistance)
            tvTotalDistance.text = "${td_k}/${ld_k} kms"
            //tvTotalDistance.text = "${totalsSelectedSport.totalDistance!!}/${levelSelectedSport.DistanceTarget!!} kms"

            var porcent = (bundle.getDouble("distanceTotal") *100 / bundle.getDouble("distanceTarget")).toInt()
            var tvTotalDistanceLevel = findViewById<TextView>(R.id.tvTotalDistanceLevel)
            tvTotalDistanceLevel.text = "$porcent%"

            var csbRunsLevel = findViewById<CircularSeekBar>(R.id.csbRunsLevel)
            csbRunsLevel.max = bundle.getDouble("runsTarget").toFloat()
            csbRunsLevel.max = bundle.getDouble("runsTotal").toFloat()

            var tvTotalRunsLevel = findViewById<TextView>(R.id.tvTotalRunsLevel)
            tvTotalRunsLevel.text = "${bundle.getInt("runsTotal")}/${bundle.getInt("runsTarget")}"
        }

        //¿ TIENE FOTOS ?
        //if (bundle?.getInt("countPhotos") > 0){
        if (bundle.getInt("countPhotos", 0) > 0) {
            // hacer algo si countPhotos es mayor que cero
            val ivPicture = findViewById<ImageView>(R.id.ivPicture)
            val path = bundle.getString("lastimage")

            val storageRef = FirebaseStorage.getInstance().reference.child(path!!) //.jpg")
            val localFile = File.createTempFile("tempImage", "jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                ivPicture.setImageBitmap(bitmap)

                //CARGA METADATOS
                val metaRef = FirebaseStorage.getInstance().reference.child(path)
                metaRef.metadata.addOnSuccessListener { metadata ->
                    if (metadata.getCustomMetadata("orientation") == "horizontal"){
                        ivPicture.updateLayoutParams {//Actualiza parametros del layout
                            height = bitmap.height
                            ivPicture.translationX = 20f
                            ivPicture.translationY = -200f
                        }
                    }
                    else{
                        ivPicture.rotation = 90f
                        ivPicture.translationY = -500f
                        ivPicture.translationX = -80f
                        ivPicture.updateLayoutParams {
                            height = bitmap.width
                        }
                    }
                }.addOnFailureListener {
                    // Uh-oh, an error occurred!
                }

            }.addOnFailureListener{
                Toast.makeText(this, "fallo al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

        //IMAGEN DEL DEPORTE
        val ivSportSelected = findViewById<ImageView>(R.id.ivSportSelected)
        when (bundle.getString("sport")){
            "Bike" ->  ivSportSelected.setImageResource(R.drawable.bike)
            "RollerSkate" -> ivSportSelected.setImageResource(R.drawable.rollerskate)
            "Running" -> ivSportSelected.setImageResource(R.drawable.running)

        }

        //GPS ACTIVADO/DESACTIVADO
        val activatedGPS = bundle.getBoolean("activatedGPS")
        if (!activatedGPS){ // SI NO está activado quitamos el mapa y los datos de mediciones
            val lyRun = findViewById<LinearLayout>(R.id.lyRun)
            setHeightLinearLayout(lyRun, 0)
            val lyDatas = findViewById<LinearLayout>(R.id.lyDatas)
            setHeightLinearLayout(lyDatas, 0)
        }else{ //SI ESTA ACTIVADO MOSTRAMOS MEDALLAS
            var medalDistance = bundle?.getString("medalDistance")
            var medalAvgSpeed = bundle?.getString("medalAvgSpeed")
            var medalMaxSpeed = bundle?.getString("medalMaxSpeed")

            if (medalDistance == "none"
                && medalAvgSpeed == "none"
                && medalMaxSpeed == "none"){

                var lyMedalsRun = findViewById<LinearLayout>(R.id.lyMedalsRun)
                setHeightLinearLayout(lyMedalsRun, 0)
            }
            else{
                var ivMedalDistance = findViewById<ImageView>(R.id.ivMedalDistance)
                var tvMedalDistanceTitle = findViewById<TextView>(R.id.tvMedalDistanceTitle)

                when (medalDistance){
                    "gold" -> {
                        ivMedalDistance.setImageResource(R.drawable.medalgold)
                        tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        ivMedalDistance.setImageResource(R.drawable.medalsilver)
                        tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        ivMedalDistance.setImageResource(R.drawable.medalbronze)
                        tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                }

                var ivMedalAvgSpeed = findViewById<ImageView>(R.id.ivMedalAvgSpeed)
                var tvMedalAvgSpeedTitle = findViewById<TextView>(R.id.tvMedalAvgSpeedTitle)

                when (medalAvgSpeed){
                    "gold" -> {
                        ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                        tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                        tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                        tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                }

                var ivMedalMaxSpeed = findViewById<ImageView>(R.id.ivMedalMaxSpeed)
                var tvMedalMaxSpeedTitle = findViewById<TextView>(R.id.tvMedalMaxSpeedTitle)

                when (medalMaxSpeed){
                    "gold" -> {
                        ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                        tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                        tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                        tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                }
            }


            var tvDurationRun = findViewById<TextView>(R.id.tvDurationRun)

            tvDurationRun.text = bundle?.getString("duration")
            if (bundle?.getInt("challengeDuration") == 0){
                var lyChallengeDurationRun = findViewById<LinearLayout>(R.id.lyChallengeDurationRun)
                setHeightLinearLayout(lyChallengeDurationRun, 0)
            }
            else{
                var tvChallengeDurationRun = findViewById<TextView>(R.id.tvChallengeDurationRun)
                tvChallengeDurationRun.text = bundle?.getString("challengeDuration")
            }
            if (bundle?.getBoolean("intervalMode") == false){
                var lyIntervalRun = findViewById<LinearLayout>(R.id.lyIntervalRun)
                setHeightLinearLayout(lyIntervalRun, 0)
            }
            else{
                var details: String = "${bundle?.getInt("intervalDuration")}mins. ("
                details += "${bundle?.getString("runningTime")} / ${bundle?.getString("walkingTime")})"

                var tvIntervalRun = findViewById<TextView>(R.id.tvIntervalRun)
                tvIntervalRun.setText(details)
            }


            var tvDistanceRun = findViewById<TextView>(R.id.tvDistanceRun)
            tvDistanceRun.text = bundle?.getDouble("distance").toString()
            if (bundle?.getDouble("challengeDistance") == 0.0){
                var lyChallengeDistancePopUp = findViewById<LinearLayout>(R.id.lyChallengeDistancePopUp)
                setHeightLinearLayout(lyChallengeDistancePopUp, 0)
            }
            else{
                var tvChallengeDistanceRun = findViewById<TextView>(R.id.tvChallengeDistanceRun)
                tvChallengeDistanceRun.text = bundle?.getDouble("challengeDistance").toString()
            }

            if (bundle?.getDouble("minAltitude") == 0.0){
                var lyUnevennessRun = findViewById<LinearLayout>(R.id.lyUnevennessRun)
                setHeightLinearLayout(lyUnevennessRun, 0)
            }
            else{

                var tvMaxUnevennessRun = findViewById<TextView>(R.id.tvMaxUnevennessRun)
                var tvMinUnevennessRun = findViewById<TextView>(R.id.tvMinUnevennessRun)
                //tvMaxUnevennessRun.text = bundle?.getDouble("maxAltitude").toInt().toString()
                tvMaxUnevennessRun.text = bundle?.getDouble("maxAltitude")!!.toInt().toString()
                //tvMinUnevennessRun.text = bundle?.getDouble("minAltitude").toInt().toString()
                tvMinUnevennessRun.text = bundle?.getDouble("minAltitude")!!.toInt().toString()
            }
            var tvAvgSpeedRun = findViewById<TextView>(R.id.tvAvgSpeedRun)
            var tvMaxSpeedRun = findViewById<TextView>(R.id.tvMaxSpeedRun)

            tvAvgSpeedRun.text = bundle?.getDouble("avgSpeed").toString()
            tvMaxSpeedRun.text = bundle?.getDouble("maxSpeed").toString()
        }
    }
    //FIN LOADDATA

    fun compartir (v: View){
        val now = Date()
        DateFormat.format("yyyy-mm-dd-hh:mm:ss", now)
        val path= getExternalFilesDir(null)!!.absolutePath + "/"+now+".jpg"
        var bitmap= Bitmap.createBitmap(rlActivity.width, rlActivity.height,Bitmap.Config.ARGB_8888)
        var canvas= Canvas(bitmap)
        rlActivity.draw(canvas)
        val imagefile = File(path)
        val outputStream= FileOutputStream(imagefile)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
        outputStream.flush()
        outputStream.close()

        val URI=FileProvider.getUriForFile(applicationContext,"com.example.letsdoitapp.fileprovider",imagefile)

        val intent= Intent()
        intent.action= Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT,"Last Run" +"\n"+"File: " + imagefile.toString())
        intent.putExtra(Intent.EXTRA_STREAM, URI)
        intent.type="text/plain"
        startActivity(intent)


    }

}