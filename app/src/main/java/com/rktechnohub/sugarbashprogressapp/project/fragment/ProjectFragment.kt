package com.rktechnohub.sugarbashprogressapp.project.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.dashboard.fragment.DashboardFragment
import com.rktechnohub.sugarbashprogressapp.project.activity.AddProjectActivity
import com.rktechnohub.sugarbashprogressapp.project.adapter.ViewPagerAdapter
import com.rktechnohub.sugarbashprogressapp.task.activity.AddTaskActivity
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [ProjectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProjectFragment : Fragment() {
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var view: View? = null
    private lateinit var pager: ViewPager2// creating object of ViewPager
    private lateinit var tab: TabLayout  // creating object of TabLayout
    private lateinit var btnAddProject: AppCompatButton
    private lateinit var nestedScrollView: NestedScrollView

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
        view = inflater.inflate(R.layout.fragment_project, container, false)
        registerViews()
        setUpPager()
        return view
    }

    private fun registerViews(){
        pager = view?.findViewById(R.id.viewPager)!!
        tab = view?.findViewById(R.id.tabs)!!
        btnAddProject = view?.findViewById(R.id.btn_add_project)!!
        nestedScrollView = view?.findViewById(R.id.nested_scrollview)!!

        val session = SessionManager(requireContext())
        val role = session.getRole()
        if (role == AppUtils.roleAdmin.toString() || role == AppUtils.roleSuperAdmin.toString()){
            btnAddProject.visibility = View.VISIBLE
        } else {
            btnAddProject.visibility = View.GONE
        }

        btnAddProject.setOnClickListener {
            val intent = Intent(requireContext(), AddProjectActivity::class.java)
            startActivity(intent)
        }

        nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) { // scrolling down
                animateButton(false) // hide text
            } else if (scrollY < oldScrollY) { // scrolling up
                animateButton(true) // show text
            }
        }
    }

    private fun animateButton(showText: Boolean) {
        btnAddProject.animate()
            .setDuration(200) // animation duration
            .scaleX(if (showText) 1f else 0.9f) // adjust the scale to show/hide text
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                if (!showText) {
                    btnAddProject.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_vector_add_white,
                        0, 0, 0)
                    btnAddProject.text = ""
                } else {
                    btnAddProject.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_vector_add_white,
                        0, 0, 0)
                    btnAddProject.text = resources.getString(R.string.add_new_task)
                }
            }
    }

    private fun setUpPager(){
        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)

        // add fragment to the list


        // bind the viewPager with the TabLayout.
        val session = SessionManager(requireContext())
        if (session.getRole() == AppUtils.roleSuperAdmin.toString()){
            adapter.addFragment(ProjectListFragment.newInstance(false))
            adapter.addFragment(ProjectListFragment.newInstance(true))
            adapter.addFragment(TasksListFragment())
            adapter.addFragment(DashboardFragment())

            // Adding the Adapter to the ViewPager
            pager.adapter = adapter

            TabLayoutMediator(tab, pager) { tab, position ->
//            tab.text = "object ${(position + 1)}".lowercase()
                tab.text = when (position) {
                    0 -> "Projects"
                    1 -> "Maple's Install"
                    2 -> "Tasks"
                    else -> "Today"
                }.lowercase()

            }.attach()
        } else {
            adapter.addFragment(ProjectListFragment())
            adapter.addFragment(TasksListFragment())
            adapter.addFragment(DashboardFragment())

            // Adding the Adapter to the ViewPager
            pager.adapter = adapter

            TabLayoutMediator(tab, pager) { tab, position ->
//            tab.text = "object ${(position + 1)}".lowercase()
                tab.text = when (position) {
                    0 -> "Projects"
                    1 -> "Tasks"
                    else -> "Today"
                }.lowercase()

            }.attach()
        }

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
            ProjectFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}