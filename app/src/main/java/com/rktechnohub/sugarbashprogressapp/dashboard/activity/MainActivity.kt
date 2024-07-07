package com.rktechnohub.sugarbashprogressapp.dashboard.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.activity.RoleActivity
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.dashboard.fragment.DashboardFragment
import com.rktechnohub.sugarbashprogressapp.project.activity.AddProjectActivity
import com.rktechnohub.sugarbashprogressapp.project.fragment.ProjectFragment
import com.rktechnohub.sugarbashprogressapp.setting.freagment.SettingFragment
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils

class MainActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    //nav fields
    lateinit var tvName: AppCompatTextView
    lateinit var tvEmail: AppCompatTextView
    lateinit var tvRole: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        registerViews()
        checkIfUserIsDisabled()
        init()
    }

    private fun getNavigationBarHeight(): Int {
        val resources = resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }


    private fun registerViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val menu = navView.menu
        menu.clear()

        val session = SessionManager(this)
        val menuToInflate = if (session.getRole() == AppUtils.roleSuperAdmin.toString() ||
            session.getRole() == AppUtils.roleAdmin.toString() ) {
            R.menu.drawer_menu
        } else {
            R.menu.drawer_menu_client
        }

        navView.inflateMenu(menuToInflate)


        toolbar = findViewById(R.id.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, windowInsets ->
            // Get the insets for the system bars
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Set the top margin of the DrawerLayout to the status bar height
            val layoutParams = drawerLayout.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.topMargin = insets.top
                drawerLayout.layoutParams = layoutParams
            }

            // Consume the insets
            WindowInsetsCompat.CONSUMED
        }

        // If the device has a navigation bar, set the bottom margin of the DrawerLayout to the navigation bar height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val navBarHeight = getNavigationBarHeight()
            if (navBarHeight > 0) {
                val layoutParams = drawerLayout.layoutParams
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.bottomMargin = navBarHeight
                    drawerLayout.layoutParams = layoutParams
                }
            }
        }


        navView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            tvName = navView.getHeaderView(0).findViewById(R.id.tv_name)
            tvEmail = navView.getHeaderView(0).findViewById(R.id.tv_email)
            tvRole = navView.getHeaderView(0).findViewById(R.id.tv_role)
//            tvName = navView.findViewById(R.id.tv_name)
//            tvEmail = navView.findViewById(R.id.tv_email)
            val session = SessionManager(this)
            tvName.text = session.getName()
            tvEmail.text = session.getEmail()
            tvRole.text = when(session.getRole()){
                AppUtils.roleSuperAdmin.toString() -> "Super Admin"
                AppUtils.roleEmployee.toString() -> "Employee"
                AppUtils.roleClient.toString() -> "Client"
                else -> "Admin"
            }
        }

        setSupportActionBar(toolbar);

        // This will display an Up icon (<-), we will replace it with hamburger later
//        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar()?.setDisplayShowHomeEnabled(true);


    }

    private fun init() {

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        navView.setNavigationItemSelectedListener {
            it.isChecked = true
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(DashboardFragment(), it.title.toString())
                }

                R.id.nav_projects -> {
                    replaceFragment(ProjectFragment(), "Project")
                }

                R.id.nav_add -> {
                    val intent = Intent(this, AddProjectActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_setting -> {
                    replaceFragment(SettingFragment(), "Setting")
                }
                R.id.nav_signout -> {
                    Firebase.auth.signOut()
                    FirebaseAuth.getInstance().signOut()
                    val sessionManager = SessionManager(this)
                    sessionManager.logout()
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    openRoleScreen()
                    finish()
                }
            }

            true
        }

        replaceFragment(DashboardFragment(), "Dashboard")
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fm_layout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

    private fun openRoleScreen() {
        val intent = Intent(this, RoleActivity::class.java)
        startActivity(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun checkIfUserIsDisabled() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        currentUser?.reload()?.addOnCompleteListener{
            if (it.isSuccessful){
                if (currentUser == null){
                    logOutUser()
                }
            }
        }
        /*auth.currentUser?.reload()

        auth.(uid).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result
                if (user.disabled) {
                    // User is disabled, log out and revoke access
                    logOutUser()
                } else {
                    // User is enabled, allow access
                }
            } else {
                // Error fetching user data
            }
        }*/
    }

    fun logOutUser() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        // Remove user data from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}