package com.rktechnohub.sugarbashprogressapp.project.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.authentication.model.User
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.adapter.UsersAdapter
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.project.viewmodel.AssignViewModel
import com.rktechnohub.sugarbashprogressapp.project.viewmodel.AssignViewModelFactory
import com.rktechnohub.sugarbashprogressapp.task.activity.TaskDetailActivity
import com.rktechnohub.sugarbashprogressapp.task.adapter.TasksAdapter
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModelFactory
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.launch

class AssignProjectActivity : AppCompatActivity() {
    private lateinit var ivBack: AppCompatImageView
    private lateinit var tvTitle: AppCompatTextView
    private lateinit var rgRoles: RadioGroup
    private lateinit var rbAdmin: RadioButton
    private lateinit var rbEmployee: RadioButton
    private lateinit var rbClient: RadioButton
    private lateinit var rvUsers: RecyclerView

    private var assignViewModel: AssignViewModel? = null
    private var projectModel: Project? = null
    private var selectedRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assign_project)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getBundle()
        registerViews()
        init()
    }

    private fun registerViews(){
        ivBack = findViewById(R.id.iv_back)
        tvTitle = findViewById(R.id.tv_title)
        rgRoles = findViewById(R.id.rg_roles)
        rbAdmin = findViewById(R.id.rb_admin)
        rbEmployee = findViewById(R.id.rb_employee)
        rbClient = findViewById(R.id.rb_client)
        rvUsers = findViewById(R.id.rv_users)

    }

    private fun getBundle() {
        if (intent != null && intent.extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                projectModel = intent.extras!!.getParcelable("data", Project::class.java)
            }
        }
    }

    private fun init(){
        tvTitle.text = projectModel?.name

        ivBack.setOnClickListener { finish() }

        rgRoles.setOnCheckedChangeListener(object : OnCheckedChangeListener{
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                var isAdmin = false
                when(p0?.checkedRadioButtonId){
                    rbAdmin.id -> {
                        selectedRole = AppUtils.roleAdmin.toString()
                        isAdmin = true
                    }
                    rbEmployee.id -> selectedRole = AppUtils.roleEmployee.toString()
                    rbClient.id -> selectedRole = AppUtils.roleClient.toString()
                }

                assignViewModel?.getData(selectedRole, this@AssignProjectActivity,
                    isAdmin, projectModel!!)
                setUpRecyclerView()
            }
        })

        assignViewModel = ViewModelProvider(
            this@AssignProjectActivity,
            AssignViewModelFactory(this@AssignProjectActivity, "")
        )[AssignViewModel::class.java]
    }

    private fun setUpRecyclerView(){
        rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = UsersAdapter(emptyList())
        }

        ((rvUsers.adapter) as UsersAdapter).setOnItemClickedListener(object: UsersAdapter.OnItemClickedListener{
            override fun onItemClicked(user: User) {
               //TODO assign
                Toast.makeText(this@AssignProjectActivity, user.name, Toast.LENGTH_SHORT).show()
                when(selectedRole){
                    AppUtils.roleAdmin.toString() -> {
                        projectModel?.adminId = user.uid
                    }

                    AppUtils.roleEmployee.toString() -> {
                        projectModel?.employeeId = user.uid
                    }

                    AppUtils.roleClient.toString() -> {
                        projectModel?.clientId = user.uid
                    }
                }

                lifecycleScope.launch {
                    updateProjectAndTasks(projectModel!!, user.uid)
                }

               /* val fb = FirebaseDatabaseOperations()
                fb.updateProject(projectModel!!)
                updateTaskUserId(projectModel?.id!!, user.uid, fb)
                finish()*/
            }

        })

        // Observe changes to the ViewModel's items LiveData
        assignViewModel?.items?.observe(this, Observer { items ->
            (rvUsers.adapter!! as UsersAdapter).setData(items)
        })
    }

    private suspend fun updateTaskUserId(projectId: String, userId: String, fb: FirebaseDatabaseOperations,
                         isInitialUpdate: Boolean = true){
        fb.getTaskByProjectId(projectId, isInitialUpdate)
        fb.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener {
            override fun dataRecieved() {
                val taskList = fb.taskList
                for (task in taskList){
                    task.userId += ",$userId"
                    fb.updateTask(task, false)
                }
            }

            override fun canceled() {
                TODO("Not yet implemented")
            }

        })
    }

    private suspend fun updateProjectAndTasks(project: Project, userId: String) {
        val fb = FirebaseDatabaseOperations()
        fb.updateProject(project)
        updateTaskUserId(project.id!!, userId, fb)
        finish()
    }
}