package com.rktechnohub.sugarbashprogressapp.map.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.map.models.DrawingImageView
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.launch

class MapEditActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private lateinit var ivMap: DrawingImageView
    private lateinit var vRedLine: View
    private lateinit var vBlueLine: View
    private lateinit var vOrangeLine: View
    private lateinit var tvYellowLine: AppCompatTextView
    private lateinit var ivStar: AppCompatImageView
    private lateinit var ivCircle: AppCompatImageView
    private lateinit var ivRectangle: AppCompatImageView

    private lateinit var ivUndo: AppCompatImageView
    private lateinit var ivDone: AppCompatImageView

    private var projectModel: Project? = null
    private lateinit var btnUploadMap: AppCompatButton

    private var isImage = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//        ivMap.setStyle(AppUtils.styleHome)

        /*Handler().postDelayed({
            // Your action here
            // This code will be executed after 3 seconds
            // For example, you can start a new activity, show a dialog, etc.

            ivMap.setStyle(AppUtils.styleRed)
        }, 3000)*/

        getBundle()
        registerViews()
        checkPermission()
        init()
    }

    private fun getBundle() {
        if (intent != null && intent.extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                projectModel = intent.extras!!.getParcelable("data", Project::class.java)
            }
        }
    }

    private fun registerViews(){
        ivUndo = findViewById(R.id.iv_undo)
        vRedLine = findViewById(R.id.redLine)
        vOrangeLine = findViewById(R.id.orangeLine)
        vBlueLine = findViewById(R.id.blueLine)
        tvYellowLine = findViewById(R.id.tv_yellow_line)
        ivStar = findViewById(R.id.iv_star)
        ivCircle = findViewById(R.id.iv_circle)
        ivRectangle = findViewById(R.id.iv_rect)
        ivDone = findViewById(R.id.iv_done)
        btnUploadMap = findViewById(R.id.btn_upload)

        ivMap = findViewById(R.id.iv_draw)
    }

    private fun init(){
        if (projectModel?.mapLink.isNullOrEmpty()){

        } else {
            isImage = true
            btnUploadMap.visibility = View.GONE
            Glide.with(this)
                .load(projectModel?.mapLink)
                .skipMemoryCache(true)
                .into(ivMap)
                .onLoadFailed(AppCompatResources.getDrawable(this, R.drawable.ic_forest))
            /*val storageReference = FirebaseDatabaseOperations().database.getReferenceFromUrl(projectModel?.mapLink!!)

            storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val uri = Uri.parse(storageReference.toString())
                Glide.with(this)
                    .load(uri.toString())
                    .into(ivMap)
                    .onLoadFailed(AppCompatResources.getDrawable(this, R.drawable.ic_forest))
            }*/
        }


        vRedLine.setOnClickListener{
            unselectAllViews()
            vRedLine.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleRed)
        }
        vBlueLine.setOnClickListener{
            unselectAllViews()
            vBlueLine.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleBlue)
        }
        vOrangeLine.setOnClickListener{
            unselectAllViews()
            vOrangeLine.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleOrange)
        }
        tvYellowLine.setOnClickListener{
            unselectAllViews()
            tvYellowLine.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleYellowDotted)
        }
        ivStar.setOnClickListener{
            unselectAllViews()
            ivStar.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleStar)
        }
        ivCircle.setOnClickListener{
            unselectAllViews()
            ivCircle.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleCircle)
        }
        ivRectangle.setOnClickListener{
            unselectAllViews()
            ivRectangle.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.setStyle(AppUtils.styleRectangle)
        }

        ivUndo.setOnClickListener {
            unselectAllViews()
            ivUndo.background = AppCompatResources.getDrawable(this, R.drawable.rect_green_border)
            ivMap.undo()
        }

        btnUploadMap.setOnClickListener {
            getImageFromGallery()
        }

        ivDone.setOnClickListener {
            val bitmap = ivMap.saveAsBitmap()
            val fb = FirebaseDatabaseOperations()
            lifecycleScope.launch {
                val url = fb.uploadBitmapToFirebaseStorage(bitmap)
                projectModel?.mapLink = url.toString()
                fb.updateProject(projectModel!!)
                finish()
            }
        }
    }

    private fun unselectAllViews(){
       vRedLine.background = null
       vBlueLine.background = null
       vOrangeLine.background = null
       tvYellowLine.background = null
       ivStar.background = null
       ivCircle.background = null
       ivRectangle.background = null
       ivUndo.background = null
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            ) {
            // Request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES
                ), REQUEST_PERMISSION_READ_EXTERNAL_STORAGE)
        } else {
            // We already have permission, so we can get the image
            if (!isImage){
                btnUploadMap.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We got permission, so we can get the image
                    if (!isImage){
                        btnUploadMap.visibility = View.VISIBLE
                    }
                } else {
                    // We didn't get permission, so we can't get the image
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityGalleryImageListener.launch(intent)
    }

    private val activityGalleryImageListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            val uri: Uri? = it.data!!.data
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, uri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            ivMap.setImageBitmap(bitmap)
            isImage = false
        }
    }
}