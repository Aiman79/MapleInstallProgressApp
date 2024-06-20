package com.rktechnohub.sugarbashprogressapp.splash

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
import com.rktechnohub.sugarbashprogressapp.authentication.activity.RoleActivity
import com.rktechnohub.sugarbashprogressapp.authentication.activity.SigninActivity
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.dashboard.activity.MainActivity
import com.rktechnohub.sugarbashprogressapp.map.activity.MapEditActivity


class SplashActivity : AppCompatActivity() {
    private lateinit var spRole: AppCompatSpinner
    private lateinit var btnNext: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val session = SessionManager(this)
        if (session.getIsLoggedIn()){
            openMainScreen()
        } else {
            openSignInScreen()
        }
//        openMapScreen()
        finish()
    }

    private fun openMapScreen(){
        val intent = Intent(this, MapEditActivity::class.java)
        startActivity(intent)
    }

    private fun openSignInScreen(){
        val intent = Intent(this, RoleActivity::class.java)
        startActivity(intent)
    }
    private fun openMainScreen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}