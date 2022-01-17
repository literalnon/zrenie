/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.zrenie20.augmentedimage


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.media.CamcorderProfile
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.OrientationEventListener
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.zrenie20.*
import com.example.zrenie20.data.RealmDataPackageObject
import com.example.zrenie20.data.toDataPackageObject
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.space.VideoRecorder
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.ux.ArFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class AugmentedImageActivity : AppCompatActivity() {

    companion object {
        val CODE = 278
    }

    private var arFragment: ArFragment? = null
    private val augmentedImageMap: MutableMap<AugmentedImage, AugmentedImageNode?> = HashMap()

    var videoRecorder: VideoRecorder? = null

    val VIDEO = R.string.video
    val PHOTO = R.string.photo

    var choice = PHOTO

    lateinit var orientationListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.augmented_image_activity)

        AugmentedImageFragment()?.let { mArFragment ->
            arFragment = mArFragment
            supportFragmentManager.let {
                it.beginTransaction()
                    .replace(R.id.fragmentContainer, mArFragment)
                    .commit()
            }

            arFragment?.setOnSessionInitializationListener {
                photoVideoRecorderInit()
            }
        }

        ivFlash?.setOnClickListener {
            Toast.makeText(this, R.string.flush, Toast.LENGTH_LONG).show()
        }

        //arFragment?.arSceneView?.planeRenderer?.isEnabled = false

        ivChangeVisibility?.setOnClickListener {
            if (llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE

                //arFragment?.arSceneView?.planeRenderer?.isEnabled = true
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE

                //arFragment?.arSceneView?.planeRenderer?.isEnabled = false
            }
        }

        ivChangeVisibility?.performClick()

        ivSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                val activePackage = packages.firstOrNull {
                    it.id == BaseArActivity.checkedPackageId
                }
                /*if (BaseArActivity.checkedPackageId == null) {
                    val ap = packages.firstOrNull()
                    BaseArActivity.checkedPackageId = ap?.id
                    ap
                } else {
                    packages.firstOrNull {
                        it.id == BaseArActivity.checkedPackageId
                    }
                }*/

                activePackage?.thumbnailPath?.let {
                    Glide.with(this)
                        .load(it)
                        .apply(
                            RequestOptions()
                                .transform(RoundedCorners(16))
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivStack)
                }
            }

        orientationListener =
            object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
                override fun onOrientationChanged(orientation: Int) {
                    Log.e("listener", "onOrientationChanged : ${orientation}")

                    val newOrientation = when (orientation) {
                        in 0..45 -> {
                            360
                        }
                        in 45..135 -> {
                            270
                        }
                        in 135..225 -> {
                            180
                        }
                        in 225..315 -> {
                            90
                        }
                        in 315..360 -> {
                            360
                        }
                        else -> {
                            360
                        }
                    }.toFloat()


                    ivChangeVisibility?.rotation = newOrientation
                }
            }

        ivArrowBack?.setOnClickListener {

            ivArrowBack?.visibility = View.GONE

            llFocus.visibility = View.GONE
            llMainActivities.visibility = View.VISIBLE

            val height = ViewGroup.LayoutParams.MATCH_PARENT

            flMirror?.visibility = View.GONE

            val mArFragmentLayoutParams = flInArFragment.layoutParams
            mArFragmentLayoutParams.height = height
            flInArFragment.layoutParams = mArFragmentLayoutParams

            ivChangeVisibility.visibility = View.VISIBLE

            stopBinakular(false)
        }

        ivVirtualReality?.setOnClickListener {
            ivArrowBack?.visibility = View.VISIBLE

            llFocus.visibility = View.GONE
            llMainActivities.visibility = View.GONE

            val height = pxFromDp(350)

            val mArFragmentLayoutParams = flInArFragment.layoutParams
            mArFragmentLayoutParams.height = height
            flInArFragment.layoutParams = mArFragmentLayoutParams

            ivChangeVisibility.visibility = View.GONE

            flMirror?.visibility = View.VISIBLE

            startBinacular(false)
        }

        flMirror?.post {
            flMirror?.visibility = View.GONE
        }
    }

    fun pxFromDp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun startBinacular(isLifecycle: Boolean) {
        if (isLifecycle) {
            return
        }

        //svMirror?.visibility = View.VISIBLE

        svMirror?.post {
            svMirror?.holder?.surface?.let { surface ->

                arFragment?.arSceneView?.renderer?.startMirroring(
                    surface,
                    0,
                    0,
                    svMirror.width,
                    svMirror.height
                )
            }
        }
    }

    fun stopBinakular(isLifecycle: Boolean) {
        /*if (!isLifecycle) {
            svMirror?.visibility = View.GONE
        }*/

        svMirror?.post {
            svMirror?.holder?.surface?.let { surface ->
                arFragment?.arSceneView?.renderer?.stopMirroring(surface)
            }
        }
    }

    fun photoVideoRecorderInit() {
        videoRecorder = VideoRecorder(this)
        val orientation = resources.configuration.orientation
        videoRecorder?.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)
        videoRecorder?.setSceneView(arFragment!!.arSceneView!!)

        choice = PHOTO

        tvPhoto?.setOnClickListener {
            btnPhoto.setImageResource(com.example.zrenie20.R.drawable.ic_photo_button)

            tvPhoto?.setTextColor(getColor(R.color.white))
            tvVideo?.setTextColor(getColor(R.color.grayTextColor))

            choice = PHOTO
        }

        tvVideo?.setOnClickListener {
            //toggleRecording()
            btnPhoto.setImageResource(com.example.zrenie20.R.drawable.ic_video_button)

            tvPhoto?.setTextColor(getColor(R.color.grayTextColor))
            tvVideo?.setTextColor(getColor(R.color.white))

            choice = VIDEO
        }

        btnPhoto.setOnClickListener {
            if (choice == VIDEO) {
                toggleRecording()
            } else {
                takePhoto()
            }
        }
    }

    fun toggleRecording() {
        val recording: Boolean = videoRecorder?.onToggleRecord() == true
        if (recording) {
            //recordButton.setImageResource(R.drawable.round_stop)
            btnPhoto.setImageResource(R.drawable.ic_video_recording_button)
            tvVideo.setText(R.string.stop)
        } else {
            tvVideo.setText(VIDEO)
            btnPhoto.setImageResource(R.drawable.ic_video_button)
            //recordButton.setImageResource(R.drawable.round_videocam)
            val videoPath = videoRecorder?.videoPath?.absolutePath
            //Toast.makeText(this, "Video saved: $videoPath", Toast.LENGTH_SHORT).show()
            Log.d("AugmentedImageActivity", "Video saved: $videoPath")

            // Send  notification of updated content.
            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATA, videoPath)
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

            shareFile(File(videoPath))
        }
    }

    fun shareFile(file: File) {
        val intentShareFile = Intent(Intent.ACTION_SEND)

        Log.e(
            "SHARE_FILE",
            "URLConnection.guessContentTypeFromName(file.name) : ${
                URLConnection.guessContentTypeFromName(file.name)
            }"
        )
        Log.e("SHARE_FILE", "file.name : ${file.name}")

        intentShareFile.apply {
            type = URLConnection.guessContentTypeFromName(file.name)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.app_name)
            )

            putExtra(
                Intent.EXTRA_TEXT,
                "Sharing file from ${getString(R.string.app_name)}"
            )

            val fileURI = FileProvider.getUriForFile(
                Objects.requireNonNull(getApplicationContext()),
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            )

            putExtra(
                Intent.EXTRA_STREAM,
                fileURI
            )
        }

        startActivity(Intent.createChooser(intentShareFile, "Share File"))
    }

    fun takePhoto() {
        val view = arFragment!!.arSceneView

        // Create a bitmap the size of the scene view.
        val bitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )

        // Create a handler thread to offload the processing of the image.
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        // Make the request to copy.
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult === PixelCopy.SUCCESS) {
                try {
                    if (
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            CODE
                        )
                    } else {
                        val file = saveBitmapToDisk(bitmap)

                        shareFile(file)
                    }
                    /* val toast: Toast = Toast.makeText(
                         this, "Screenshot saved in : ${file.canonicalPath}",
                         Toast.LENGTH_LONG
                     )
                     toast.show()*/
                } catch (e: IOException) {
                    val toast: Toast = Toast.makeText(
                        this, e.toString(),
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    return@request
                }



                Log.e("AugmentedImageActivity", "Screenshot saved in /Pictures/Screenshots")
            } else {
                Log.e("AugmentedImageActivity", "Failed to take screenshot")
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    override fun onResume() {
        super.onResume()

        orientationListener.enable()
    }

    override fun onPause() {
        super.onPause()

        orientationListener.disable()
    }

    open fun saveBitmapToDisk(bitmap: Bitmap): File {

        val androidDirectory = File(
            Environment.getExternalStorageDirectory(), "Android"
        )

        if (!androidDirectory.exists()) {
            androidDirectory.mkdirs()
        }

        Log.e("AugmentedImageActivity", "saveBitmapToDisk 1.1 : ${androidDirectory.exists()}")

        val dataDirectory = File(
            androidDirectory, "data"
        )

        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }

        Log.e("AugmentedImageActivity", "saveBitmapToDisk 1.2 : ${dataDirectory.exists()}")

        /* val videoDirectory = File(
             Environment.getExternalStorageDirectory().toString() + "/Android/data/" + packageName
         )*/

        /*val videoDirectory = File(
            dataDirectory, "zrenie"//packageName
        )*/

        val videoDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )

        Log.e("AugmentedImageActivity", "saveBitmapToDisk 1 : ${videoDirectory.exists()}")
        /*if (!videoDirectory.exists()) {
            videoDirectory.mkdirs()
            Log.e("AugmentedImageActivity", "saveBitmapToDisk 1.5 : ${videoDirectory.exists()}")
            videoDirectory.mkdir()
        }*/

        Log.e("AugmentedImageActivity", "saveBitmapToDisk 2 : ${videoDirectory.exists()}")

        /*val videoDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Screenshots"
        )*/

        //videoDirectory.mkdir()

        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH.mm.ss")
        val formattedDate = df.format(c.time)

        val mediaFile: File = File(
            videoDirectory,
            "FieldVisualizer$formattedDate.jpeg"
        )

        try {
            if (!mediaFile.parentFile.exists())
                mediaFile.parentFile.mkdirs();

            if (!mediaFile.exists())
                mediaFile.createNewFile();
        } catch (e: Exception) {
            e.printStackTrace()
        }
        /*try {
            mediaFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }*/

        Log.e("BaseArActivity", "mediaFile: ${mediaFile.canonicalPath}")

        val fileOutputStream = FileOutputStream(mediaFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        return mediaFile
    }
}