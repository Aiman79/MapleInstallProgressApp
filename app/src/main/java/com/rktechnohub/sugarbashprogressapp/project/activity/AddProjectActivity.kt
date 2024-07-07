package com.rktechnohub.sugarbashprogressapp.project.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
//import androidx.emoji.text.
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.model.EmojiFilter
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddProjectActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private lateinit var etName: AppCompatEditText
    private lateinit var etEmoji: AppCompatEditText
    private lateinit var btnNext: AppCompatButton
    private lateinit var ivEdit: AppCompatImageView
    private lateinit var ivBack: AppCompatImageView
    private lateinit var ivAddImage: AppCompatImageView

    private var previousText: String = ""
    private var isEmojiMain = true
    private var selectedImage: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_add_project)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerViews()
        checkPermission()
        init()
        addListeners()
    }

    private fun registerViews() {
        btnNext = findViewById(R.id.btn_next)
        etEmoji = findViewById(R.id.et_emoji)
        etName = findViewById(R.id.et_name)
        ivEdit = findViewById(R.id.iv_edit)
        ivBack = findViewById(R.id.iv_back)
        ivAddImage = findViewById(R.id.iv_image)
    }

    private fun init() {
        etEmoji.filters = arrayOf(EmojiFilter())
    }

    private fun addListeners() {
        etEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                previousText = p0.toString()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.e("Emoji", p0!!.toString())
                if (previousText == "" && p0 != "") {
                    previousText = p0.toString()
                    etEmoji.isEnabled = false
                    isEmojiMain = true
                    ivAddImage.setImageDrawable(
                        AppCompatResources
                            .getDrawable(this@AddProjectActivity, R.drawable.ic_vector_add)
                    )
                } else {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        btnNext.setOnClickListener {
            addNewProject()
        }

        ivEdit.setOnClickListener {
            etEmoji.setText("")
            previousText = ""
            etEmoji.isEnabled = true
        }

        ivBack.setOnClickListener { finish() }

        ivAddImage.setOnClickListener {
            isEmojiMain = false
            etEmoji.setText("")
            previousText = ""
            etEmoji.isEnabled = true
            getImageFromGallery()
        }
    }

    private fun coroutineAddProject(project: Project) = CoroutineScope(Dispatchers.IO).launch {
        val db = FirebaseDatabaseOperations()
        db.addProject(project)
        withContext(Dispatchers.Main) {
            finish()
        }
    }


    private fun checkPermission() {
        ivAddImage.isEnabled = false
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES
                ), REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
            )
        } else {
            // We already have permission, so we can get the image
            ivAddImage.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We got permission, so we can get the image
                    ivAddImage.isEnabled = true
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

    private val activityGalleryImageListener = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            val uri: Uri? = it.data!!.data
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, uri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            selectedImage = bitmap
            ivAddImage.setImageBitmap(bitmap)
            isEmojiMain = false
        }
    }

    private fun addNewProject() {
        var icon = ""
        var success = true
        if (isEmojiMain) {
            //emoji is selected
            val emoji = etEmoji.text.toString()
            var isEmoji = true
            for (element in emoji) {
                val type = Character.getType(element).toByte()
                if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                    isEmoji = false
                    break
                }
            }
            if (isEmoji) {
                // The entered text is an emoji
                icon = emoji

            } else {
                success = false
                // The entered text is not an emoji
                Snackbar.make(
                    this,
                    btnNext,
                    "Please enter an emoji or an image",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            addProjectFinish(success, icon)
        } else {
            // image is selected
            if (selectedImage != null) {
                val fb = FirebaseDatabaseOperations()
                lifecycleScope.launch {
                    val url = fb.uploadBitmapToFirebaseStorage(selectedImage!!)
                    icon = url.toString()
                    addProjectFinish(true, icon)
                }
            } else {
                success = false
                Snackbar.make(
                    this,
                    btnNext,
                    "Please enter an emoji or an image",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addProjectFinish(success: Boolean, icon: String) {
        if (success) {
            val name = etName.text.toString()
            var project: Project? = null
            val session = SessionManager(this)
            when (session.getRole()) {
                AppUtils.roleAdmin.toString() -> {
                    project = Project(
                        "", name, icon, AppUtils.getCurrentDate(), "", "",
                        "", "", "", session.getUId(), "", "",
                        "0", "", "", ""
                    )
                }

                AppUtils.roleSuperAdmin.toString() -> {
                    project = Project(
                        "", name, icon, AppUtils.getCurrentDate(), "", "", "","",
                        "",  session.getUId(), "","", "0", "", "", ""
                    )
                }

                else -> {
                    project = Project(
                        "", name, icon, AppUtils.getCurrentDate(), "", "", "", "",
                        "", "", "", "", "0", "", "", ""
                    )
                }
            }
            coroutineAddProject(project)
        }
    }
}