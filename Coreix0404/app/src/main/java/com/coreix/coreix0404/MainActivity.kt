package com.coreix.coreix0404

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*
import java.util.Arrays.asList



class MainActivity : AppCompatActivity() {
    var btnLogin : Button?= null
    private var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private val LOGIN_PERMISSION : Int = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnLogin = findViewById(R.id.signinbtn)
        val providers = Arrays.asList(
                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())
        btnLogin?.setOnClickListener {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.mipmap.coreix_launcher)
                            .build(),LOGIN_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOGIN_PERMISSION) {
            startActivity(resultCode, data)
        }
    }

    private fun startActivity(resultCode: Int, data: Intent?){
        if(resultCode == Activity.RESULT_OK)
        {
            var intent = Intent()
            intent.setClass(this,ListOnline::class.java)
            startActivity(intent)
            finish()
        }
        else
        {
            Toast.makeText(this,"Login Failed!!",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        var currentUser : FirebaseUser = mAuth.currentUser!!

    }
}
