package com.rktechnohub.sugarbashprogressapp.authentication.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.adapter.CustomSpinnerAdapter
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.dashboard.activity.MainActivity

class RoleActivity : AppCompatActivity() {
    private lateinit var spRole: AppCompatSpinner
    private lateinit var btnNext: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_role)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerViews()
        addListener()
    }

    private fun registerViews(){
        spRole = findViewById(R.id.sp_role)
        btnNext = findViewById(R.id.btn_next)

        val data: Array<String> = resources.getStringArray(R.array.role_array)

        val customAdapter = CustomSpinnerAdapter(
            this,
            R.layout.raw_spinner_color,
            data
        )

        /*val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.role_array,
            android.R.layout.simple_spinner_item
        )*/
//        customAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRole.setAdapter(customAdapter)
    }

    private fun addListener(){
        btnNext.setOnClickListener{
            if (spRole.selectedItemPosition == 0){
                Toast.makeText(this, "Please select correct role", Toast.LENGTH_SHORT).show()
            } else {
                val session = SessionManager(this)
                session.setRole((spRole.selectedItemPosition).toString())
                openSignInScreen()
            }
        }
    }

    private fun openSignInScreen(){
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }
}