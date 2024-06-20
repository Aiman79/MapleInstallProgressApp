package com.rktechnohub.sugarbashprogressapp.dashboard.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
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
import com.rktechnohub.sugarbashprogressapp.task.activity.TaskDetailActivity
import com.rktechnohub.sugarbashprogressapp.task.adapter.TasksAdapter
import com.rktechnohub.sugarbashprogressapp.task.interfaces.ItemMoveCallbackTask
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModel
import com.rktechnohub.sugarbashprogressapp.task.viewmodel.TaskViewModelFactory
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var view: View

    private lateinit var rvTasks: RecyclerView
    private lateinit var tvDate: AppCompatTextView
    private lateinit var tvTotalTasks: AppCompatTextView
    private lateinit var tvProgress: AppCompatTextView
    private lateinit var progressBar: ProgressBar

    private var taskViewModel: TaskViewModel? = null
    private var totalTasks = 0
    private var completedTasks = 0
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_home, container, false)
        registerViews()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setUpRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            taskViewModel?.getDataPr(requireContext(), "", "today")
            withContext(Dispatchers.Main){
                setUpRecyclerView()
            }
        }
    }

    private fun registerViews() {
        tvDate = view.findViewById(R.id.tv_date)
        tvTotalTasks = view.findViewById(R.id.tv_completed_task)
        rvTasks = view.findViewById(R.id.rv_tasks)
        tvProgress = view.findViewById(R.id.progress_text)
        progressBar = view.findViewById(R.id.circular_progress_bar)

        taskViewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(requireContext(), "", "today")
        )[TaskViewModel::class.java]

        tvDate.text = AppUtils.getCurrentDate()
    }

    private fun init() {

    }

    private fun setUpRecyclerView(){
        rvTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TasksAdapter(mutableListOf())
        }

        val callback = ItemMoveCallbackTask(rvTasks.adapter as TasksAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvTasks)

        ((rvTasks.adapter) as TasksAdapter).setOnItemClickedListener(object: TasksAdapter.OnItemClickedListener{
            override fun onItemClicked(task: TaskModel) {

                    val bundle = Bundle()
                    val db = FirebaseDatabaseOperations()
                    db.getProjectByProjectId(task.projectId)
                    db.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
                        override fun dataRecieved() {
                            bundle.putParcelable("data", db.project)
                            bundle.putParcelable("data_task", task)
                            val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                            intent.putExtras(bundle)
                            startActivity(intent)
//                            updateTaskResult.launch(intent)
                        }

                        override fun canceled() {
                            TODO("Not yet implemented")
                        }

                    })

            }

            override fun onItemDelete(task: TaskModel, pos: Int) {
                showDeleteItemDialog(task, pos)
            }

            override fun onDragged(list: MutableList<TaskModel>) {
                getOrderList(list, SessionManager(requireContext()))
            }

        })

        // Observe changes to the ViewModel's items LiveData
        taskViewModel?.items?.observe(viewLifecycleOwner, Observer { items ->
            completedTasks = 0
            totalTasks = 0
            progress = 0
            for (task in items){
                if(task.progress.isNotEmpty()){
                    if (task.progress == "100") {
                        completedTasks++
                        if(task.progress.isNotEmpty()){
                            progress += task.progress.toInt()
                        }
                    } else {
                       try{
                           if(task.progress.isNotEmpty()){
                               progress += task.progress.toInt()
                           }
                       } catch (e: Exception){
                           e.printStackTrace()
                       }
                    }
                }
            }
            totalTasks = items.size
            (rvTasks.adapter!! as TasksAdapter).setData(items, Glide.with(this))
            tvTotalTasks.text = "$completedTasks of $totalTasks"

           try{
               tvProgress.text = "${(progress / totalTasks)}%"
               progressBar.progress = progress / totalTasks
           } catch (e: Exception){
               e.printStackTrace()
           }
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
        val alertDialog = AlertDialog.Builder(requireContext())

        alertDialog.setTitle("Delete Task")
        alertDialog.setMessage("Are you sure you want to delete this task?")

        alertDialog.setPositiveButton("Delete") { _, _ ->
            // Delete the item here

            /*fb.addOnDeleteDataListener(object: FirebaseDatabaseOperations.DeleteDataListener{
                override fun success() {
                    (rvTasks.adapter!! as TasksAdapter).removeItem(position)
                }

                override fun failed() {
                    Toast.makeText(requireContext(), "Please try again", Toast.LENGTH_SHORT).show()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}