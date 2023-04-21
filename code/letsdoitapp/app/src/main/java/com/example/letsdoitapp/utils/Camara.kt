package com.example.letsdoitapp.utils

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.LoginActivity.Companion.useremail
import com.example.letsdoitapp.activity.MainActivity.Companion.countPhotos
import com.example.letsdoitapp.activity.MainActivity.Companion.lastimage
import com.example.letsdoitapp.databinding.ActivityCamaraBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Camara : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private var fileName: String = ""
    private var preview: Preview? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var binding: ActivityCamaraBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var dateRun: String
    private lateinit var startTimeRun: String
    private lateinit var metadata: StorageMetadata


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamaraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras //Recibe los datos enviados desde el Main
        dateRun = bundle?.getString("dateRun").toString()
        startTimeRun = bundle?.getString("startTimeRun").toString()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor() //Nuevo hilo para la camara
        binding.cameraCaptureButton.setOnClickListener { takePhoto() } //Acciona tomar foto al hacer click
        managingLens()//Gestionamos la Lente
        //permissionsGranted()//Permisos aprobados -> Inicia la camara , si no pide Permisos
        requestCameraPermissions()
    }

    private fun hasCameraPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermissions() {
        if (hasCameraPermissions()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (hasCameraPermissions()) {
                startCamera()
            } else {
                Utility.showCustomSnackbar(binding.clMain,"Debes proporcionar el permiso para utilizar la camara",R.color.orange_strong,3000)
                finish()
            }
        }
    }

    private fun managingLens() {
        binding.cameraSwitchButton.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCamera()//vincular camara para conectarse con el hardware de la camara
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "letsdoit").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun bindCamera() {
        val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = binding.viewFinder.display.rotation
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Fallo al iniciar la camara")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll() //Si hubiera alguna vinculacion Desvinculamiento de to do

        //Vinculamos
        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider) // Linea principal
        } catch (exc: Exception) {
            Log.e("Camara", "Fallo al vincular la camara", exc)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = kotlin.math.max(width, height).toDouble() / Integer.min(width, height)
        if (kotlin.math.abs(previewRatio - RATIO_4_3_VALUE) <= kotlin.math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun startCamera() {
        val cameraProviderFinnaly = ProcessCameraProvider.getInstance(this)
        cameraProviderFinnaly.addListener({
            cameraProvider = cameraProviderFinnaly.get()
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("No tenemos camara")
            }

            manageSwitchButton()
            bindCamera()

        }, ContextCompat.getMainExecutor(this))
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun manageSwitchButton() {
        val switchButton = binding.cameraSwitchButton
        try {
            switchButton.isEnabled = hasBackCamera() && hasFrontCamera() //Tengo las dos camaras

        } catch (exc: CameraInfoUnavailableException) {
            switchButton.isEnabled = false
        }
    }

    private fun takePhoto() {
        fileName = getString(R.string.app_name) + useremail + dateRun + startTimeRun
        fileName = fileName.replace(":", "").replace("/", "")

        //Guardamos los Metadata /vertical u horizontal
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            metadata = storageMetadata {
                contentType = "image/jpg"
                setCustomMetadata("orientation", "horizontal")
            }
        else
            metadata = storageMetadata {
                contentType = "image/jpg"
                setCustomMetadata("orientation", "vertical")
            }

        val photoFile = File(outputDirectory, "$fileName.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile) //Numero de Imagen
                    setGalleryThumbnail(savedUri) //Actualizamos la Galeria

                    //Añadimos la imagen a la galeria
                    val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(savedUri.toFile().extension)
                    MediaScannerConnection.scanFile(
                        baseContext,
                        arrayOf(savedUri.toFile().absolutePath),
                        arrayOf(mimeType)
                    ) { _, _ -> }
                    upLoadFile(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Utility.showCustomSnackbar(binding.clMain,"Error al guardar la imagen",R.color.orange_strong,3000)
                }
            })
    }

    private fun setGalleryThumbnail(uri: Uri) {
        val thumbnail = binding.photoViewButton
        thumbnail.post {
            Glide.with(thumbnail) //Glide permite crear la miniatura
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }

    private fun upLoadFile(image: File) {
        var dirName = dateRun + startTimeRun // Nombre del directorio
        dirName = dirName.replace(":", "").replace("/", "")
        val fileName = "$dirName-$countPhotos"
        val storageReference =
            FirebaseStorage.getInstance().getReference("images/$useremail/$dirName/$fileName")

        storageReference.putFile(Uri.fromFile(image))//subir el archivo
            .addOnSuccessListener {// Si se ha subido ...
                lastimage = "images/$useremail/$dirName/$fileName" //obtenemos la ultima imagen
                countPhotos++ // aumentamos el contador de fotos
                val myFile = File(image.absolutePath) //obtenemos la ruta absoluta
                myFile.delete() //Borramos el fichero del telefono

                //una vez que se ha subido el archivo le asignamos los metadatos
                val metaRef = FirebaseStorage.getInstance()
                    .getReference("images/$useremail/$dirName/$fileName")
                metaRef.updateMetadata(metadata)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }
                Utility.showCustomSnackbar(binding.clMain,"Imagen guardada en la nube correctamente",R.color.orange_strong,3000)
            }
            .addOnFailureListener {
                Utility.showCustomSnackbar(binding.clMain,"La imagen se guardó en el movil, pero no se guardó en la nube",R.color.orange_strong,3000)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}