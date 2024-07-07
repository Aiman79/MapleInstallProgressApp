package com.rktechnohub.sugarbashprogressapp.project.activity

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
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
import com.rktechnohub.sugarbashprogressapp.project.viewmodel.ProjectViewModel
import com.rktechnohub.sugarbashprogressapp.project.viewmodel.ProjectViewModelFactory
import com.rktechnohub.sugarbashprogressapp.task.activity.AddTaskActivity
import com.rktechnohub.sugarbashprogressapp.task.activity.TaskDetailActivity
import com.rktechnohub.sugarbashprogressapp.task.adapter.TasksAdapter
import com.rktechnohub.sugarbashprogressapp.task.interfaces.ItemMoveCallbackTask
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModelFactory
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import com.rktechnohub.sugarbashprogressapp.utils.ColorCustomizeClass.updateProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ProjectDetailActivity : AppCompatActivity() {
    private var projectModel: Project? = null
    private lateinit var toolBar: Toolbar
    private lateinit var tvTitle: AppCompatTextView
    private lateinit var ivBack: AppCompatImageView
    private lateinit var tvProgress: AppCompatTextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStartDate: AppCompatTextView
    private lateinit var tvEndDate: AppCompatTextView
    private lateinit var tvDays: AppCompatTextView
    private lateinit var tvTarget: AppCompatTextView
    private lateinit var tvDescription: AppCompatTextView
    private lateinit var btnAddTask: AppCompatButton
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvNoTasks: AppCompatTextView
    private lateinit var ivAssign: AppCompatImageView
    private lateinit var ivEdit: AppCompatImageView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var tvTotalTasks: AppCompatTextView
    private lateinit var clTotalTask: ConstraintLayout

    private var taskViewModel: TaskViewModel? = null
    private var role = ""

    private var totalTasks = 0
    private var completedTasks = 0
//    private var progress = 0

    var list: MutableList<TaskModel> = mutableListOf()
//    private var totalTasks = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_project_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getBundle()
        registerViews()
        init()
        setUpRecyclerView()
    }

    private fun getBundle() {
        if (intent != null && intent.extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                projectModel = intent.extras!!.getParcelable("data", Project::class.java)
            } else {
                projectModel = intent.extras!!.getParcelable("data")!!
            }
        }
    }

    private fun registerViews() {
        toolBar = findViewById(R.id.toolbar)
        tvTitle = toolBar.findViewById(R.id.tv_title)
        ivBack = toolBar.findViewById(R.id.iv_back)
        ivAssign = toolBar.findViewById(R.id.iv_assign)
        ivEdit = toolBar.findViewById(R.id.iv_edit)
        progressBar = findViewById(R.id.progress_bar)
        tvProgress = findViewById(R.id.tv_progress)
        tvStartDate = findViewById(R.id.tv_start_date)
        tvEndDate = findViewById(R.id.tv_end_date)
        tvDays = findViewById(R.id.tv_days)
        tvTarget = findViewById(R.id.tv_target)
        tvDescription = findViewById(R.id.et_description)
        btnAddTask = findViewById(R.id.btn_add_task)
        tvNoTasks = findViewById(R.id.tv_no_tasks)
        rvTasks = findViewById(R.id.rv_tasks)
        nestedScrollView = findViewById(R.id.nested_scrollview)
        tvTotalTasks = findViewById(R.id.tv_completed_task)
        clTotalTask = findViewById(R.id.cl_total_tasks)

        val session = SessionManager(this)
        role = session.getRole()
        if (role == AppUtils.roleAdmin.toString() || role == AppUtils.roleSuperAdmin.toString()){
            ivAssign.visibility = View.VISIBLE
        } else {
            ivAssign.visibility = View.GONE
        }
        if (role == AppUtils.roleClient.toString()){
            btnAddTask.visibility = View.GONE
        } else {
            btnAddTask.visibility = View.VISIBLE
        }

        taskViewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(this, projectModel?.id!!, "p")
        )[TaskViewModel::class.java]

    }

    override fun onResume() {
        super.onResume()
//        taskViewModel?.getData(this, projectModel?.id!!, "p")
//        val fbOp = FirebaseDatabaseOperations()
        lifecycleScope.launch {
            taskViewModel?.getDataPr(this@ProjectDetailActivity, projectModel?.id!!, "p")
           /* list = fbOp.getTaskByProjectIdCoroutine(projectModel?.id!!).toMutableList()
            withContext(Dispatchers.Main){
                if (rvTasks != null){
                    ((rvTasks.adapter) as TasksAdapter).setData(list, Glide.with(this@ProjectDetailActivity))
                }
            }*/
        }
    }

    private fun init() {
        try {
            if (projectModel != null) {
                tvTitle.text = projectModel?.name
                if (projectModel?.progress != null) {
                    //animator
                    val animator = ObjectAnimator.ofInt(progressBar, "progress",
                        0, projectModel?.progress?.toInt()!!)
                    animator.duration = 500 // 2 seconds
                    animator.start()
//                    progressBar.progress = projectModel?.progress?.toInt()!!
                    progressBar.updateProgress(projectModel?.progress?.toInt()!!, this)
                    tvProgress.text = projectModel?.progress
                }
                tvStartDate.text = projectModel?.startDate
                tvEndDate.text = projectModel?.endDate
                tvTarget.text = "${projectModel?.target}%"
                tvDescription.text = projectModel?.description!!

                if (projectModel?.daysLeft?.toInt()!! < 0) {
                    tvDays.text = getString(R.string.overdue)
                } else {
                    tvDays.text = projectModel?.daysLeft
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tvDescription.setOnClickListener {
            showDescriptionDialog()
        }

        tvEndDate.setOnClickListener { showCalendar() }

        btnAddTask.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("data", projectModel)
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtras(bundle)
            activityAddTaskResult.launch(intent)
//            startActivity(intent)
        }

        ivAssign.setOnClickListener {
            val intent = Intent(this, AssignProjectActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", projectModel)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        ivEdit.setOnClickListener {
            showRenameDialog()
        }

        nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) { // scrolling down
                animateButton(false) // hide text
            } else if (scrollY < oldScrollY) { // scrolling up
                animateButton(true) // show text
            }
        }
    }

    private fun setUpRecyclerView(){
        rvTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TasksAdapter(list)
        }

        val callback = ItemMoveCallbackTask(rvTasks.adapter as TasksAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvTasks)

        ((rvTasks.adapter) as TasksAdapter).setOnItemClickedListener(object: TasksAdapter.OnItemClickedListener{
            override fun onItemClicked(task: TaskModel) {
                val bundle = Bundle()
                bundle.putParcelable("data", projectModel)
                bundle.putParcelable("data_task", task)
                val intent = Intent(this@ProjectDetailActivity, TaskDetailActivity::class.java)
                intent.putExtras(bundle)
                updateTaskResult.launch(intent)
            }

            override fun onItemDelete(task: TaskModel, pos: Int) {
                showDeleteItemDialog(task, pos)
            }

            override fun onDragged(list: MutableList<TaskModel>) {
                getOrderList(list, SessionManager(this@ProjectDetailActivity))
            }

        })

        // Observe changes to the ViewModel's items LiveData
        taskViewModel?.items?.observe(this, Observer { items ->
            if(items.isEmpty()){
                clTotalTask.visibility = View.GONE
                tvNoTasks.visibility = View.VISIBLE
            } else {
                clTotalTask.visibility = View.VISIBLE
                tvNoTasks.visibility = View.GONE
//                totalTasks = items.size
                showTotalTasks(items)
                (rvTasks.adapter!! as TasksAdapter).setData(items, Glide.with(this), this)
            }
        })
    }

    fun showTotalTasks(items: MutableList<TaskModel>){
        completedTasks = 0
        totalTasks = 0
        for (task in items){
            if(task.progress.isNotEmpty()){
                if (task.progress == "100") {
                    completedTasks++
                }
            }
        }
        totalTasks = items.size
        (rvTasks.adapter!! as TasksAdapter).setData(items, Glide.with(this), this)
        tvTotalTasks.text = "$completedTasks of $totalTasks"
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
                    Toast.makeText(this@ProjectDetailActivity, "Please try again", Toast.LENGTH_SHORT).show()
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
        input1.setText(projectModel?.description)


        layout.addView(input1)

        // Add the EditText view to the dialog
        builder.setView(layout)

        // Set a positive button on the dialog
        builder.setPositiveButton("OK") { dialog, which ->
            // Get the user's input and do something with it
            val text = input1.text.toString()
            tvDescription.text = text

            projectModel?.description = text
            val fbOp = FirebaseDatabaseOperations()
            fbOp.updateProject(projectModel!!)
        }

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }
    private fun showRenameDialog() {
        val builder = AlertDialog.Builder(this)

        // Set the title and message of the dialog
        builder.setTitle("Rename Project")
        //        builder.setMessage("Please enter some text")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20) // Set padding for the layout

        // Create an EditText view
        val input1 = AppCompatEditText(this)
//        input1.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rext_orange_border))
        input1.inputType = InputType.TYPE_CLASS_TEXT
        input1.hint = "Please Enter name"
        input1.setText(projectModel?.name)


        layout.addView(input1)

        // Add the EditText view to the dialog
        builder.setView(layout)

        // Set a positive button on the dialog
        builder.setPositiveButton("OK") { dialog, which ->
            // Get the user's input and do something with it
            val text = input1.text.toString()
            tvTitle.text = text

            projectModel?.name = text
            val fbOp = FirebaseDatabaseOperations()
            fbOp.updateProject(projectModel!!)
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
                tvEndDate.text = showDate

                calculateDaysAndTarget()

            },
            year,
            month,
            day
        )

        dialog.show()
    }

    private fun calculateDaysAndTarget() {
        try {
            val daysLeft =
                AppUtils.getDifference(AppUtils.getCurrentDate(), tvEndDate.text.toString())
            val remainingProgress = 100 - projectModel?.progress?.toInt()!!
            var target = 0
            if (daysLeft > 0) {
                target = remainingProgress / daysLeft
            } else {
                target = remainingProgress
            }

            //save to db
            projectModel?.endDate = tvEndDate.text.toString()
            projectModel?.daysLeft = daysLeft.toString()
            projectModel?.target = target.toString()
            val fbOp = FirebaseDatabaseOperations()
            fbOp.updateProject(projectModel!!)


            tvTarget.text = "${target.toString()}%"
            if (projectModel?.daysLeft?.toInt()!! < 0) {
                tvDays.text = getString(R.string.overdue)
            } else {
                tvDays.text = daysLeft.toString()
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
                val proj = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("data", Project::class.java)
                } else {
                    it.data?.getParcelableExtra("data")!!
                }

                projectModel?.id = proj?.id!!
                projectModel?.taskId = proj.taskId
                courotinCalculateProgress()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private val updateTaskResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        try {
            if (it != null && it.data != null) {
                val progress = it.data?.getIntExtra("progress", 0)!!


                setUpRecyclerView()
//                if (progress > 0){
                    courotinCalculateProgress()
//                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun calculateLatestProgress(){
        val fb = FirebaseDatabaseOperations()
        fb.getTaskByProjectId(projectModel?.id!!)
        fb.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
            override fun dataRecieved() {
                val taskList = fb.taskList
                if(taskList.isNotEmpty()){
                    var prog = 0

                    for (task in taskList){
                        prog += task.progress.toInt()
                    }

                    val totalProgress = (prog / taskList.size).toInt()
                    projectModel?.progress = totalProgress.toString()
                    fb.updateProject(projectModel!!)
                    tvProgress.text = totalProgress.toString()
//                    progressBar.progress = totalProgress
                    progressBar.updateProgress(totalProgress, this@ProjectDetailActivity)
                }
            }

            override fun canceled() {

            }

        })

    }

    private fun courotinCalculateProgress() = lifecycleScope.launch {
        val fb = FirebaseDatabaseOperations()
        val taskList = withContext(Dispatchers.IO){
            fb.getTaskByProjectIdFlow(projectModel?.id!!)
            fb.taskList
        }

        if (taskList.isNotEmpty()) {
            var prog = 0

            for (task in taskList) {
                prog += task.progress.toInt()
            }

            val totalProgress = (prog / taskList.size).toInt()
            projectModel?.progress = totalProgress.toString()
            withContext(Dispatchers.IO) {
                fb.updateProject(projectModel!!)
            }
            withContext(Dispatchers.Main) {
                tvProgress.text = totalProgress.toString()
//                progressBar.progress = totalProgress
                progressBar.updateProgress(totalProgress, this@ProjectDetailActivity)
            }
        }
    }

    private fun animateButton(showText: Boolean) {
        btnAddTask.animate()
            .setDuration(200) // animation duration
            .scaleX(if (showText) 1f else 0.9f) // adjust the scale to show/hide text
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                if (!showText) {
                    btnAddTask.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_vector_add_white, 0, 0, 0)
                    btnAddTask.text = ""
                } else {
                    btnAddTask.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_vector_add_white, 0, 0, 0)
                    btnAddTask.text = resources.getString(R.string.add_new_task)
                }
            }
    }
}