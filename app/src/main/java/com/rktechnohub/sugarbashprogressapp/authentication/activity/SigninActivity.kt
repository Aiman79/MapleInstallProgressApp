package com.rktechnohub.sugarbashprogressapp.authentication.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.rktechnohub.sugarbashprogressapp.R
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.rktechnohub.sugarbashprogressapp.dashboard.activity.MainActivity
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.authentication.model.User
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations

class SigninActivity : AppCompatActivity() {
    lateinit var btnGoogleSingnIn: Button

    private lateinit var credentialManager: CredentialManager
    private lateinit var request: GetCredentialRequest

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setContent { setUpSignIn() }

    }

    private fun init() {
        btnGoogleSingnIn = findViewById(R.id.btn_g_signin)
    }

    /* @Composable
     fun SigninScreen(onSignInClick: () -> Unit) {
         Column(modifier = Modifier.fillMaxSize()) {
             Button(onClick = onSignInClick) {
                 Text("Sign in with Google")
             }
         }
     }*/


    /*@Composable
    private fun addListeners() {
        btnGoogleSingnIn.setOnClickListener{
            this.setUpSignIn()
        }
    }*/

    @Composable
    private fun setUpSignIn() {
        val courentines = rememberCoroutineScope()
// Initialize credentialManager and request here
        if (::credentialManager.isInitialized.not()) {
            credentialManager = CredentialManager.create(this@SigninActivity)
            val ranNounce = UUID.randomUUID().toString()
            val bytes = ranNounce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashNounce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleOptions = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.web_client_id))
                .setNonce(hashNounce)
                .build()

            request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOptions)
                .build()

            val onClick: () -> Unit = {
                courentines.launch {
                    try {
                        val result = credentialManager.getCredential(
                            request = request,
                            context = this@SigninActivity,
                        )
                        val credential = result.credential

                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val googleIdToken = googleIdTokenCredential.idToken

                        auth = Firebase.auth

                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this@SigninActivity) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success")
                                    val user = auth.currentUser
                                    if (user != null){
                                            setUserDataDB(user)
                                        }
                                    }
                                else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                                    updateUI(null)
                                }
                            }

                        Log.i(TAG, googleIdToken)
                        Toast.makeText(applicationContext, "You are signed in", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: GetCredentialException) {
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    } catch (e: GetCredentialCancellationException) {
                        Toast.makeText(applicationContext, "Authentication cancelled", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: GoogleIdTokenParsingException) {
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Surface (
                Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
            ){
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onClick
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }

        }
    }

    fun setUserDataDB(fUser: FirebaseUser) {
        val dbOp = FirebaseDatabaseOperations()
        val session = SessionManager(this@SigninActivity)
        val user = User(fUser.uid, fUser.displayName!!, fUser.email!!, session.getRole())

        dbOp.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
            override fun dataRecieved() {
                if (dbOp.user != null){
                    session.apply {
                        this.setRole(dbOp.user!!.role)
                        this.setName(dbOp.user!!.name)
                        this.setEmail(dbOp.user!!.email)
                        this.setUId(dbOp.user!!.uid)

                    }
                } else {
                    session.apply {
                        this.setName(user.name)
                        this.setEmail(user.email)
                        this.setUId(user.uid)
                        this.setIsLoggedIn(true)

                        dbOp.saveUserToDb(this@SigninActivity)
                    }
                }
                session.setIsLoggedIn(true)
                openMainScreen()
            }

            override fun canceled() {
                Toast.makeText(this@SigninActivity, "Please try again", Toast.LENGTH_SHORT).show()
            }

        })
        dbOp.getUserFromDb( user)

    }

    private fun openMainScreen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}