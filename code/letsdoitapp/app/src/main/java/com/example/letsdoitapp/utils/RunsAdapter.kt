package com.example.letsdoitapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.letsdoitapp.activity.LoginActivity.Companion.useremail
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.RunActivity
import com.example.letsdoitapp.utils.Utility.animateViewofFloat
import com.example.letsdoitapp.utils.Utility.deleteRunAndLinkedData
import com.example.letsdoitapp.utils.Utility.setHeightLinearLayout
import com.example.letsdoitapp.data.Runs
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File

class RunsAdapter(private val runsList: ArrayList<Runs>) :
    RecyclerView.Adapter<RunsAdapter.MyViewHolder>() {

    private var minimized = true
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.card_run, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val run: Runs = runsList[position]

        setHeightLinearLayout(holder.lyDataRunBody, 0)

        holder.lyDataRunBodyContainer.translationY = -200f

        //Abre y Cierra el menu de la cabecera del recycler
        holder.ivHeaderOpenClose.setOnClickListener {
            if (minimized) {
                var h = 600
                if (run.countPhotos!! > 0) h = 900 // Si hay foto el image view será más alto
                setHeightLinearLayout(holder.lyDataRunBody, h)
                animateViewofFloat(holder.lyDataRunBodyContainer, "translationY", 0f, 300L)
                holder.ivHeaderOpenClose.rotation = 180f //rotación de la flecha
                minimized = false
            } else {
                holder.lyDataRunBodyContainer.translationY = -200f
                setHeightLinearLayout(holder.lyDataRunBody, 0)
                holder.ivHeaderOpenClose.rotation = 0f
                minimized = true
            }
        }

        //Controla el formato de la fecha
        var day = run.date?.subSequence(8, 10)
        var n_month = run.date?.subSequence(5, 7)
        var month: String? = null
        var year = run.date?.subSequence(0, 4)

        when (n_month) {
            "01" -> month = "ENE"
            "02" -> month = "FEB"
            "03" -> month = "MAR"
            "04" -> month = "ABR"
            "05" -> month = "MAY"
            "06" -> month = "JUN"
            "07" -> month = "JUL"
            "08" -> month = "AGO"
            "09" -> month = "SEP"
            "10" -> month = "OCT"
            "11" -> month = "NOV"
            "12" -> month = "DIC"
        }

        var date: String = "$day-$month-$year"
        holder.tvDate.text = date
        holder.tvHeaderDate.text = date

        //Formato de la Hora
        holder.tvStartTime.text = run.startTime?.subSequence(0, 5)
        holder.tvDurationRun.text = run.duration
        holder.tvHeaderDuration.text = run.duration!!.subSequence(0, 5).toString() + "HH"

        //En el caso de que tuviera reto de duracion / distancia
        if (!run.challengeDuration.isNullOrEmpty())
            holder.tvChallengeDurationRun.text = run.challengeDuration
        else
            setHeightLinearLayout(holder.lyChallengeDurationRun, 0)

        //En el caso de que tuviera reto de distancia
        if (run.challengeDistance != null)
            holder.tvChallengeDistanceRun.text = run.challengeDistance.toString()
        else
            setHeightLinearLayout(holder.lyChallengeDistance, 0)

        //En el caso que tuviera intervalos
        if (run.intervalMode != null) {
            var details: String = "${run.intervalDuration}mins. ("
            details += "${run.runningTime}/${run.walkingTime})"
            holder.tvIntervalRun.text = details
        } else
            setHeightLinearLayout(holder.lyIntervalRun, 0)

        //Si hay medallas
        holder.tvDistanceRun.setText(run.distance.toString())
        holder.tvHeaderDistance.setText(run.distance.toString() + "KM")
        holder.tvMaxUnevennessRun.setText(run.maxAltitude.toString())
        holder.tvMinUnevennessRun.setText(run.minAltitude.toString())
        holder.tvAvgSpeedRun.setText(run.avgSpeed.toString())
        holder.tvHeaderAvgSpeed.setText(run.avgSpeed.toString() + "KM/H")
        holder.tvMaxSpeedRun.setText(run.maxSpeed.toString())

        when (run.medalDistance) {
            "gold" -> {
                holder.ivMedalDistance.setImageResource(R.drawable.medalgold)
                holder.ivHeaderMedalDistance.setImageResource(R.drawable.medalgold)
                holder.tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
            }
            "silver" -> {
                holder.ivMedalDistance.setImageResource(R.drawable.medalsilver)
                holder.ivHeaderMedalDistance.setImageResource(R.drawable.medalsilver)
                holder.tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
            }
            "bronze" -> {
                holder.ivMedalDistance.setImageResource(R.drawable.medalbronze)
                holder.ivHeaderMedalDistance.setImageResource(R.drawable.medalbronze)
                holder.tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
            }
        }
        when (run.medalAvgSpeed) {
            "gold" -> {
                holder.ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                holder.ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                holder.tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
            }
            "silver" -> {
                holder.ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                holder.ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                holder.tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
            }
            "bronze" -> {
                holder.ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                holder.ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                holder.tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
            }
        }
        when (run.medalMaxSpeed) {
            "gold" -> {
                holder.ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                holder.ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                holder.tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
            }
            "silver" -> {
                holder.ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                holder.ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                holder.tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
            }
            "bronze" -> {
                holder.ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                holder.ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                holder.tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
            }
        }
        //Codigo para cargar la imagen en el Historial y gestionar la foto
        //Si hay foto cargamos la Imagen de Storage
        if (run.lastimage != "") {
            val path = run.lastimage //Guardamos la ruta
            val storageRef =
                FirebaseStorage.getInstance().reference.child(path!!) //Referencia de Almacenamiento
            val localFile = File.createTempFile("tempImage", "jpg") //Creamos un archivo temporal
            storageRef.getFile(localFile)//Capturamos el archivo de la referencia de almacenamiento
                .addOnSuccessListener {//si lo hemos descargado
                    val bitmap =
                        BitmapFactory.decodeFile(localFile.absolutePath) //lo trasnformamos a bitmap y lo cargamos(fichero con ruta absoluta)
                    val metaRef = FirebaseStorage.getInstance()
                        .getReference(run.lastimage!!) //REferencia del archivo
                    val metadata: Task<StorageMetadata> = metaRef.metadata

                    metadata.addOnSuccessListener {
                        val orientation = it.getCustomMetadata("orientation")
                        if (orientation == "horizontal") {
                            val porcent = 80 / bitmap.width.toFloat()
                            setHeightLinearLayout(
                                holder.lyPicture,
                                (bitmap.width * porcent).toInt()
                            )
                            holder.ivPicture.setImageBitmap(bitmap)
                        } else {
                            val porcent = 80 / bitmap.height.toFloat()
                            setHeightLinearLayout(
                                holder.lyPicture,
                                (bitmap.width * porcent).toInt()
                            )
                            holder.ivPicture.setImageBitmap(bitmap)
                            holder.ivPicture.rotation = 90f //Rotamos la imagen
                        }
                    }
                    metadata.addOnFailureListener {
                        Toast.makeText(context, "fallo al cargar los metadatos", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "fallo al cargar la imagen", Toast.LENGTH_SHORT).show()
                }

        }

        //Onclick Reproducir con el objeto Run donde guardamos cada carrera

        holder.tvPlay.setOnClickListener {
            var idRun = run.date + run.startTime
            idRun = idRun.replace(":", "")
            idRun = idRun.replace("/", "")

            //ENVIO DE PARAMETROS
            val intent = Intent(context, RunActivity::class.java)
            val inParameter = intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            inParameter.putExtra("user", run.user)
            inParameter.putExtra("idRun", idRun)
            inParameter.putExtra("countPhotos", run.countPhotos)
            inParameter.putExtra("lastimage", run.lastimage)
            inParameter.putExtra("centerLatitude", run.centerLatitude)
            inParameter.putExtra("centerLongitude", run.centerLongitude)
            inParameter.putExtra("date", run.date)
            inParameter.putExtra("startTime", run.startTime)
            inParameter.putExtra("duration", run.duration)
            inParameter.putExtra("distance", run.distance)
            inParameter.putExtra("maxSpeed", run.maxSpeed)
            inParameter.putExtra("avgSpeed", run.avgSpeed)
            inParameter.putExtra("minAltitude", run.minAltitude)
            inParameter.putExtra("maxAltitude", run.maxAltitude)
            inParameter.putExtra("medalDistance", run.medalDistance)
            inParameter.putExtra("medalAvgSpeed", run.medalAvgSpeed)
            inParameter.putExtra("medalMaxSpeed", run.medalMaxSpeed)
            inParameter.putExtra("activatedGPS", run.activatedGPS)
            inParameter.putExtra("sport", run.sport)
            inParameter.putExtra("intervalMode", run.intervalMode)
            inParameter.putExtra("intervalDuration", run.intervalDuration)
            inParameter.putExtra("runningTime", run.runningTime)
            inParameter.putExtra("walkingTime", run.walkingTime)
            inParameter.putExtra("challengeDistance", run.challengeDistance)
            inParameter.putExtra("challengeDuration", run.challengeDuration)

            // Para llenar el Header del Nivel de cuando se realizó la carrera

            inParameter.putExtra("image_level", run.image_level)
            inParameter.putExtra("distanceTotal", run.distanceTotal)
            inParameter.putExtra("distanceTarget", run.distanceTarget)
            inParameter.putExtra("runsTotal", run.runsTotal)
            inParameter.putExtra("runsTarget", run.runsTarget)

            context.startActivity(intent)

        }

        //Onclick Borrar
        holder.tvDelete.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Borrar elemento")
            builder.setMessage("¿Estás seguro de querer borrar el elemento seleccionado?")
            builder.setPositiveButton("Sí") { _, _ ->

                // Código para borrar el elemento
                var id: String = useremail + run.date + run.startTime
                id = id.replace(":", "")
                id = id.replace("/", "")

                var currentRun = Runs()
                currentRun.distance = run.distance
                currentRun.avgSpeed = run.avgSpeed
                currentRun.maxSpeed = run.maxSpeed
                currentRun.duration = run.duration
                currentRun.activatedGPS = run.activatedGPS
                currentRun.date = run.date
                currentRun.startTime = run.startTime
                currentRun.user = run.user
                currentRun.sport = run.sport

                deleteRunAndLinkedData(id, currentRun.sport!!, holder.lyDataRunHeader, currentRun)
                runsList.removeAt(position)//quitamos un elemento del array
                notifyItemRemoved(position) //notificamos al adaptador el borrado
                //Utility.showCustomSnackbar(holder.rlRecyclers, "Registro borrado correctamente", R.color.orange_strong,4000)
                //Toast.makeText(context,"Registro borrado correctamente",Toast.LENGTH_LONG).show()
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()

        }
    }

    override fun getItemCount(): Int {
        return runsList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val lyDataRunHeader: LinearLayout = itemView.findViewById(R.id.lyDataRunHeader)
        val tvHeaderDate: TextView = itemView.findViewById(R.id.tvHeaderDate)
        val tvHeaderDuration: TextView = itemView.findViewById(R.id.tvHeaderDuration)
        val tvHeaderDistance: TextView = itemView.findViewById(R.id.tvHeaderDistance)
        val tvHeaderAvgSpeed: TextView = itemView.findViewById(R.id.tvHeaderAvgSpeed)
        val ivHeaderMedalDistance: ImageView = itemView.findViewById(R.id.ivHeaderMedalDistance)
        val ivHeaderMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalAvgSpeed)
        val ivHeaderMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalMaxSpeed)
        val ivHeaderOpenClose: ImageView = itemView.findViewById(R.id.ivHeaderOpenClose)

        val lyDataRunBody: LinearLayout = itemView.findViewById(R.id.lyDataRunBody)
        val lyDataRunBodyContainer: LinearLayout =
            itemView.findViewById(R.id.lyDataRunBodyContainer)

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)


        val tvDurationRun: TextView = itemView.findViewById(R.id.tvDurationRun)
        val lyChallengeDurationRun: LinearLayout =
            itemView.findViewById(R.id.lyChallengeDurationRun)
        val tvChallengeDurationRun: TextView = itemView.findViewById(R.id.tvChallengeDurationRun)
        val lyIntervalRun: LinearLayout = itemView.findViewById(R.id.lyIntervalRun)
        val tvIntervalRun: TextView = itemView.findViewById(R.id.tvIntervalRun)


        val tvDistanceRun: TextView = itemView.findViewById(R.id.tvDistanceRun)
        val lyChallengeDistance: LinearLayout = itemView.findViewById(R.id.lyChallengeDistance)
        val tvChallengeDistanceRun: TextView = itemView.findViewById(R.id.tvChallengeDistanceRun)
        val lyUnevennessRun: LinearLayout = itemView.findViewById(R.id.lyUnevennessRun)
        val tvMaxUnevennessRun: TextView = itemView.findViewById(R.id.tvMaxUnevennessRun)
        val tvMinUnevennessRun: TextView = itemView.findViewById(R.id.tvMinUnevennessRun)


        val tvAvgSpeedRun: TextView = itemView.findViewById(R.id.tvAvgSpeedRun)
        val tvMaxSpeedRun: TextView = itemView.findViewById(R.id.tvMaxSpeedRun)

        val ivMedalDistance: ImageView = itemView.findViewById(R.id.ivMedalDistance)
        val tvMedalDistanceTitle: TextView = itemView.findViewById(R.id.tvMedalDistanceTitle)
        val ivMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivMedalAvgSpeed)
        val tvMedalAvgSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalAvgSpeedTitle)
        val ivMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivMedalMaxSpeed)
        val tvMedalMaxSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalMaxSpeedTitle)

        val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        val lyPicture: LinearLayout = itemView.findViewById(R.id.lyPicture)
        val tvPlay: TextView = itemView.findViewById(R.id.tvPlay)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
    }


}
