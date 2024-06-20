package com.rktechnohub.sugarbashprogressapp.project.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.model.OrderClass
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.map.activity.MapEditActivity
import com.rktechnohub.sugarbashprogressapp.project.activity.AddProjectActivity
import com.rktechnohub.sugarbashprogressapp.project.activity.ProjectDetailActivity
import com.rktechnohub.sugarbashprogressapp.project.adapter.ProjectAdapter
import com.rktechnohub.sugarbashprogressapp.project.interfaces.ItemMoveCallback
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.project.viewmodel.ProjectViewModel
import com.rktechnohub.sugarbashprogressapp.project.viewmodel.ProjectViewModelFactory
import com.rktechnohub.sugarbashprogressapp.task.adapter.TasksAdapter
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [ProjectListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProjectListFragment : Fragment() {
    private var view: View? = null
    private lateinit var rvProjects: RecyclerView
    private var projectViewModel: ProjectViewModel? = null

    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_project_list, container, false)
        init()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setUpRecyclerView()
    }

    private fun init(){
        fabAdd = view?.findViewById(R.id.fab_add)!!
        rvProjects = view?.findViewById(R.id.rv_project)!!

        fabAdd.setOnClickListener{
            val intent = Intent(requireContext(), AddProjectActivity::class.java)
            startActivity(intent)
        }

        projectViewModel = ViewModelProvider(this, ProjectViewModelFactory(requireContext()))
            .get(ProjectViewModel::class.java)
    }

    private fun setUpRecyclerView() {
        rvProjects.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ProjectAdapter(mutableListOf())
        }

        val callback = ItemMoveCallback(rvProjects.adapter as ProjectAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvProjects)

        (rvProjects.adapter as ProjectAdapter).setOnItemClickedListener(
            object : ProjectAdapter.OnItemClickedListener{
                override fun onItemClicked(project: Project) {
                    val bundle = Bundle()
                    bundle.putParcelable("data", project)
                    val intent = Intent(requireContext(), ProjectDetailActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }

                override fun onMapClicked(project: Project) {
                    val bundle = Bundle()
                    bundle.putParcelable("data", project)
                    val intent = Intent(requireContext(), MapEditActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }

                override fun onItemDelete(project: Project, pos: Int) {
                    showDeleteItemDialog(project, pos)
                }

                override fun onDragged(list: List<Project>) {
                    getOrderList(list, SessionManager(requireContext()))
                }

            })

        // Observe changes to the ViewModel's items LiveData
        projectViewModel?.items?.observe(viewLifecycleOwner, Observer { items ->
            val reqManager = Glide.with(requireContext())
            val session = SessionManager(requireContext())
            var isDelete = false
            when(session.getRole().toInt()){
                AppUtils.roleSuperAdmin -> isDelete = true
                AppUtils.roleAdmin -> isDelete = true
                else -> false
            }
            (rvProjects.adapter as ProjectAdapter).setData(items, reqManager, isDelete)
        })
    }

    fun showDeleteItemDialog(project: Project, position: Int) {
        val alertDialog = AlertDialog.Builder(requireContext())

        alertDialog.setTitle("Delete Project")
        alertDialog.setMessage("Are you sure you want to delete this project?")

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
                fb.deleteProject(project){
                    (rvProjects.adapter!! as TasksAdapter).removeItem(position)
                }
            }

        }

        alertDialog.setNegativeButton("Cancel") { _, _ ->
            // Cancel the deletion
        }

        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        val fbOp = FirebaseDatabaseOperations()
        val session = SessionManager(requireContext())
        lifecycleScope.launch {
//            val list = fbOp.getAllProjectsForSuperAdminCoroutine()

            val list: MutableList<Project> = mutableListOf()
            when (session.getRole()) {
                AppUtils.roleSuperAdmin.toString() -> {
                    list.addAll(fbOp.getAllProjectsForSuperAdminCoroutine(session.getOrderList()))
                }

                AppUtils.roleAdmin.toString() -> {
                    list.addAll(fbOp.getAllProjectsForAdminCoroutine(session.getUId(), session.getOrderList()))
                }

                AppUtils.roleEmployee.toString() -> {
                    list.addAll(fbOp.getAllProjectsForEmployee(session.getUId(), session.getOrderList()))
                }

                AppUtils.roleClient.toString() -> {
                    list.addAll(fbOp.getAllProjectsForClient(session.getUId(), session.getOrderList()))
                }

                else -> {
                    emptyList<Project>()
                }
            }

            withContext(Dispatchers.Main){
                projectViewModel?.updateData(list)
                setUpRecyclerView()
            }
            getOrderList(list, session)
        }


//        projectViewModel?.getData(requireContext())

    }

    fun getOrderList(list: List<Project>, session: SessionManager){
        val orderList: MutableList<OrderClass> = mutableListOf()
        list.forEachIndexed { index, project ->
            val order = OrderClass(project.id, index)
            orderList.add(order)
        }
        session.setOrderList(orderList)
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
            ProjectListFragment().apply {
                /*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }
}