package com.rktechnohub.sugarbashprogressapp.task.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.BuildCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.model.OrderClass
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.adapter.ProjectAdapter
import com.rktechnohub.sugarbashprogressapp.project.interfaces.ItemMoveCallback
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.task.adapter.TasksAdapter
import com.rktechnohub.sugarbashprogressapp.task.interfaces.ItemMoveCallbackTask
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModelFactory
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class TaskDetailActivity : AppCompatActivity() {
    private var projectModel: Project? = null
    private var taskModel: TaskModel? = null

    //seekbar no subtasks
    private lateinit var clSeekBar: ConstraintLayout
    private lateinit var tvSeekProgress: AppCompatTextView
    private lateinit var seekBar: SeekBar

    //progressbar have subtasks
    private lateinit var clProgressBar: ConstraintLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: AppCompatTextView
    private var taskViewModel: TaskViewModel? = null

    //common
    private lateinit var tvTitle: AppCompatTextView
    private lateinit var tvTaskName: AppCompatTextView
    private lateinit var tvTaskIcon: AppCompatTextView
    private lateinit var ivBack: AppCompatImageView
    private lateinit var tvIcon: AppCompatTextView
    private lateinit var tvProject: AppCompatTextView
    private lateinit var tvDate: AppCompatTextView
    private lateinit var tvTime: AppCompatTextView
    private lateinit var tvDescription: AppCompatTextView
    private lateinit var rvTasks: RecyclerView
    private lateinit var btnAddSubTask: AppCompatButton
    private lateinit var ivAssign: AppCompatImageView
    private lateinit var ivMinus: AppCompatImageView
    private lateinit var ivPlus: AppCompatImageView

    private var role = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getBundle()
        registerViews()
        init()
    }

    private fun getBundle() {
        if (intent != null && intent.extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                taskModel = intent.extras!!.getParcelable("data_task", TaskModel::class.java)
                projectModel = intent.extras!!.getParcelable("data", Project::class.java)
            } else {
                projectModel = intent.extras!!.getParcelable("data")!!
                taskModel = intent.extras!!.getParcelable("data_task")!!
            }
        }
    }

    private fun registerViews() {
        //common
        tvTitle = findViewById(R.id.tv_title)
        ivAssign = findViewById(R.id.iv_assign)
        ivPlus = findViewById(R.id.iv_plus)
        ivMinus = findViewById(R.id.iv_minus)
        tvTaskName = findViewById(R.id.tv_task_name)
        tvTaskIcon = findViewById(R.id.tv_task_icon)
        ivBack = findViewById(R.id.iv_back)
        tvIcon = findViewById(R.id.tv_icon)
        tvDate = findViewById(R.id.tv_date)
        tvTime = findViewById(R.id.tv_time)
        tvProject = findViewById(R.id.tv_project)
        tvDescription = findViewById(R.id.tv_description)
        rvTasks = findViewById(R.id.rv_tasks)
        btnAddSubTask = findViewById(R.id.btn_add_task)

        ivAssign.visibility = View.GONE

        //seekbar no subtasks
        clSeekBar = findViewById(R.id.cl_seek_progress)
        tvSeekProgress = findViewById(R.id.tv_progress)
        seekBar = findViewById(R.id.seek_bar)

        //progressbar have subtasks
        clProgressBar = findViewById(R.id.cl_circle_progress)
        progressBar = findViewById(R.id.circular_progress_bar)
        tvProgress = findViewById(R.id.progress_text)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    val intent = Intent()
                    intent.putExtra("progress", taskModel?.progress?.toInt())
                    setResult(200, intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                finish()
            }
        })


        // ...
    }

    private fun init() {
        val session = SessionManager(this)
       role = session.getRole()

        setProgressLayout(role)

        tvTitle.visibility = View.GONE
        tvTaskName.text = taskModel?.name
        tvTaskIcon.text = taskModel?.icon
        tvIcon.text = projectModel?.icon
        tvProject.text = projectModel?.name
        try {
            tvDescription.text = taskModel?.description!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!taskModel?.deadline_date.isNullOrEmpty()) {
            tvDate.text = taskModel?.deadline_date
        }

        if (!taskModel?.deadline_time.isNullOrEmpty()) {
            tvTime.text = taskModel?.deadline_time
        }

        addListeners(role)
    }

    private fun addListeners(role: String) {
        ivBack.setOnClickListener { finish() }

        if (role != AppUtils.roleClient.toString()){
            tvDescription.setOnClickListener {
                showDescriptionDialog()
            }

            tvDate.setOnClickListener { showCalendar() }

            tvTime.setOnClickListener { showTimePickerDialog() }

            btnAddSubTask.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable("data", projectModel)
                bundle.putParcelable("data_task", taskModel)
                val intent = Intent(this, AddTaskActivity::class.java)
                intent.putExtras(bundle)
//            startActivity(intent)
                activityAddTaskResult.launch(intent)
            }
        }

        ivPlus.setOnClickListener {
            if (seekBar.progress < 100){
                seekBar.progress++
                tvProgress.text = seekBar.progress.toString()
            }
        }

        ivMinus.setOnClickListener {
            if (seekBar.progress > 0){
                seekBar.progress--
                tvProgress.text = seekBar.progress.toString()
            }
        }
    }

    private fun setProgressLayout(role: String) {

        if (taskModel?.taskId.isNullOrEmpty() && role != AppUtils.roleClient.toString()) {
            //no subtasks
            clSeekBar.visibility = View.VISIBLE
            clProgressBar.visibility = View.GONE

            tvSeekProgress.text = taskModel?.progress

            seekBar.max = 100
            try {
                seekBar.progress = taskModel?.progress?.toInt()!!
            } catch (e: Exception) {
                e.printStackTrace()
            }

            seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    tvSeekProgress.text = p1.toString()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    taskModel?.progress = p0?.progress.toString()
                    val fbOp = FirebaseDatabaseOperations()
                    fbOp.updateTask(taskModel!!)
                }

            })

        } else {
            //subtasks
            clSeekBar.visibility = View.GONE
            clProgressBar.visibility = View.VISIBLE

            tvProgress.text = taskModel?.progress

            try {
                progressBar.setProgress(taskModel?.progress?.toInt()!!, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            lifecycleScope.launch {
                taskViewModel?.getDataPr(this@TaskDetailActivity, taskModel?.id!!, "t")
                withContext(Dispatchers.Main){
                    setUpRecyclerView()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
       setProgressLayout(role)
    }

    private fun setUpRecyclerView() {

        taskViewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(this, taskModel?.id!!, "t")
        )[TaskViewModel::class.java]


        rvTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TasksAdapter(mutableListOf())
        }

        val callback = ItemMoveCallbackTask(rvTasks.adapter as TasksAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvTasks)

        ((rvTasks.adapter) as TasksAdapter).setOnItemClickedListener(object :
            TasksAdapter.OnItemClickedListener {
            override fun onItemClicked(task: TaskModel) {
                val bundle = Bundle()
                bundle.putParcelable("data", projectModel)
                bundle.putParcelable("data_task", task)
                val intent = Intent(this@TaskDetailActivity, TaskDetailActivity::class.java)
                intent.putExtras(bundle)
                updateTaskResult.launch(intent)
            }

            override fun onItemDelete(task: TaskModel, pos: Int) {
                showDeleteItemDialog(task, pos)
            }

            override fun onDragged(list: MutableList<TaskModel>) {
                getOrderList(list, SessionManager(this@TaskDetailActivity))
            }

        })

        // Observe changes to the ViewModel's items LiveData
        taskViewModel?.items?.observe(this, Observer { items ->
            /* if(items.isEmpty()){
                 rvTasks.visibility = View.GONE
                 tvNoTasks.visibility = View.VISIBLE
             } else {
                 rvTasks.visibility = View.VISIBLE
                 tvNoTasks.visibility = View.GONE*/
            (rvTasks.adapter!! as TasksAdapter).setData(items, Glide.with(this))
//            }
        })
    }

    fun getOrderList(list: List<TaskModel>, session: SessionManager){
        val orderList: MutableList<OrderClass> = mutableListOf()
        list.forEachIndexed { index, task ->
            val order = OrderClass(task.id, index)
            orderList.add(order)
        }
        session.setOrderListTask(orderList)
    }

    fun showDeleteItemDialog(taskModel: TaskModel, position: Int) {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Delete Task")
        alertDialog.setMessage("Are you sure you want to delete this task?")

        alertDialog.setPositiveButton("Delete") { _, _ ->
            // Delete the item here

            /*fb.addOnDeleteDataListener(object: FirebaseDatabaseOperations.DeleteDataListener{
                override fun success() {

                }

                override fun failed() {
                    Toast.makeText(this@TaskDetailActivity, "Please try again", Toast.LENGTH_SHORT).show()
                }

            })*/

            lifecycleScope.launch {
                val fb = FirebaseDatabaseOperations()
                fb.deleteTask(taskModel){
                    (rvTasks.adapter!! as TasksAdapter).removeItem(position)
                }
            }
        }

        alertDialog.setNegativeButton("Cancel") { _, _ ->
            // Cancel the deletion
        }

        alertDialog.show()
    }

    private fun showDescriptionDialog() {
        val builder = AlertDialog.Builder(this)

        // Set the title and message of the dialog
        builder.setTitle("Enter Description")
        //        builder.setMessage("Please enter some text")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20) // Set padding for the layout

        // Create an EditText view
        val input1 = AppCompatEditText(this)
//        input1.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rext_orange_border))
        input1.inputType = InputType.TYPE_CLASS_TEXT
        input1.hint = "Please Enter description"


        layout.addView(input1)

        // Add the EditText view to the dialog
        builder.setView(layout)

        // Set a positive button on the dialog
        builder.setPositiveButton("OK") { dialog, which ->
            // Get the user's input and do something with it
            val text = input1.text.toString()
            tvDescription.text = text

            taskModel?.description = text
            val fbOp = FirebaseDatabaseOperations()
            fbOp.updateTask(taskModel!!)
        }

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun showCalendar() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Do something with the selected date
//                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                val date = "${selectedDay.toString().padStart(2, '0')}/${
                    (selectedMonth + 1).toString().padStart(2, '0')
                }/$selectedYear"
                val parseDate = AppUtils.getFormattedDateObj(date, AppUtils.formatMMddyyyy)
                val showDate = AppUtils.getFormattedDate(parseDate)
                tvDate.text = showDate

                taskModel?.deadline_date = showDate
                val fbOp = FirebaseDatabaseOperations()
                fbOp.updateTask(taskModel!!)

            },
            year,
            month,
            day
        )

        dialog.show()
    }

    private fun showTimePickerDialog() {
        val timePickerDialogListener =
            TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                tvTime.text = time

                taskModel?.deadline_time = time
                val fbOp = FirebaseDatabaseOperations()
                fbOp.updateTask(taskModel!!)
            }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(this, timePickerDialogListener, hour, minute, true).show()
    }


    private val updateTaskResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        try {
            if (it != null && it.data != null) {
                val progress = it.data?.getIntExtra("progress", 0)!!


                setUpRecyclerView()
                calculateLatestProgress()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val activityAddTaskResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        try {
            if (it != null && it.data != null) {
                val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("data_task", TaskModel::class.java)
                } else {
                    it.data?.getParcelableExtra("data_task")!!
                }
                taskModel = task
                setUpRecyclerView()
                calculateLatestProgress()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateLatestProgress() {
        val fb = FirebaseDatabaseOperations()
        val sessionManager = SessionManager(this)
        lifecycleScope.launch {
            val taskList = fb.getTaskByMainTaskId(taskModel?.id!!, sessionManager.getOrderList())
            withContext(Dispatchers.Main){
                if (taskList.isNotEmpty()) {
                    var prog = 0
                    var completedCount = 0

                    for (task in taskList) {
                        prog += task.progress.toInt()

                        if (task.progress.toInt() == 100) {
                            completedCount++
                        }

                    }

                    taskModel?.completedSubTasks = completedCount.toString()

                    val totalProgress = (prog / taskList.size).toInt()
                    taskModel?.progress = totalProgress.toString()
                    fb.updateTask(taskModel!!)
                    tvProgress.text = totalProgress.toString()
                    progressBar.progress = totalProgress
            }
        }
        /*fb.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener {
            override fun dataRecieved() {
                val taskList = fb.subtaskList
                if (taskList.isNotEmpty()) {
                    var prog = 0
                    var completedCount = 0

                    for (task in taskList) {
                        prog += task.progress.toInt()

                        if (task.progress.toInt() == 100) {
                            completedCount++
                        }

                    }

                    taskModel?.completedSubTasks = completedCount.toString()

                    val totalProgress = (prog / taskList.size).toInt()
                    taskModel?.progress = totalProgress.toString()
                    fb.updateTask(taskModel!!)
                    tvProgress.text = totalProgress.toString()
                    progressBar.progress = totalProgress
                }
            }

            override fun canceled() {

            }

        })*/
            }
    }
}