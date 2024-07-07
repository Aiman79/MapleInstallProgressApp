package com.rktechnohub.sugarbashprogressapp.task.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
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
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.launch

class AddTaskActivity : AppCompatActivity() {
    private lateinit var etName: AppCompatEditText
    private lateinit var etEmoji: AppCompatEditText
    private lateinit var btnNext: AppCompatButton
    private lateinit var ivEdit: AppCompatImageView
    private lateinit var ivBack: AppCompatImageView
    private lateinit var ivAddImage: AppCompatImageView

    private var previousText: String = ""
    private var projectModel: Project? = null
    private var taskModel: TaskModel? = null

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

        getBundle()
        registerViews()
        init()
        addListeners()
    }

    private fun getBundle() {
        if (intent != null && intent.extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                projectModel = intent.extras!!.getParcelable("data", Project::class.java)
                taskModel = intent.extras!!.getParcelable("data_task", TaskModel::class.java)
            } else {
                projectModel = intent.extras!!.getParcelable("data")
                taskModel = intent.extras!!.getParcelable("data_task")
            }
        }
    }

    private fun registerViews(){
        btnNext = findViewById(R.id.btn_next)
        etEmoji = findViewById(R.id.et_emoji)
        etName = findViewById(R.id.et_name)
        ivEdit = findViewById(R.id.iv_edit)
        ivBack = findViewById(R.id.iv_back)
        ivAddImage = findViewById(R.id.iv_image)

        etName.hint = getString(R.string.type_task_name_here)
        etEmoji.hint = getString(R.string.please_add_emoji_for_your_task_icon)
    }

    private fun init(){
        etEmoji.filters = arrayOf(EmojiFilter())
    }

    private fun addListeners(){
        etEmoji.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                previousText = p0.toString()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.e("Emoji", p0!!.toString())
                if (previousText == "" && p0 != ""){
                    previousText = p0.toString()
                    etEmoji.isEnabled = false
                    isEmojiMain = true
                    ivAddImage.setImageDrawable(
                        AppCompatResources
                            .getDrawable(this@AddTaskActivity, R.drawable.ic_vector_add)
                    )
                } else {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        btnNext.setOnClickListener {
           addNewTask()
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

    private fun addNewTask(){
        var icon = ""
        var success = true
        if (isEmojiMain){
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
                // The entered text is not an emoji
                Snackbar.make(this, btnNext, "Please enter an emoji or an image\"", Snackbar.LENGTH_SHORT).show()
            }
            addTaskFinish(icon)
        } else {
            if (selectedImage != null) {
                val fb = FirebaseDatabaseOperations()
                lifecycleScope.launch {
                    val url = fb.uploadBitmapToFirebaseStorage(selectedImage!!)
                    icon = url.toString()
                    addTaskFinish(icon)
                }
            } else {
                Snackbar.make(
                    this,
                    btnNext,
                    "Please enter an emoji or an image",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addTaskFinish(icon: String){
        val name = etName.text.toString()
        val db = FirebaseDatabaseOperations()
        var task :TaskModel? = null
        val session = SessionManager(this)
        if (taskModel != null){
            task = TaskModel("", name, icon,
                "", "", taskModel?.id!!, "0", session.getUId(),
                projectModel?.id!!, "0", "", "")

            db.addTask(task)
            if(taskModel?.taskId.isNullOrEmpty()){
                taskModel?.taskId = task.id
            } else {
                taskModel?.taskId = taskModel?.taskId + "," + task.id
            }
            db.updateTask(taskModel!!)
        } else{
            task = TaskModel("", name, icon,
                "", "", "", "0", session.getUId(),
                projectModel?.id!!, "0", "", "")

            db.addTask(task)
            if(projectModel?.taskId.isNullOrEmpty()){
                projectModel?.taskId = task.id
            } else {
                projectModel?.taskId = projectModel?.taskId + "," + task.id
            }
            db.updateProject(projectModel!!)
        }




        val intent = Intent()
        intent.putExtra("data", projectModel)
        intent.putExtra("data_task", taskModel)
        setResult(2000, intent)
        finish()
    }
}