package com.coreix.coreix0404

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI

class MainActivity : AppCompatActivity() {
    var btnLogin : Button?= null
    private val LOGIN_PERMISSION : Int = 1000;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.signinbtn)
        btnLogin?.setOnClickListener(object :View.OnClickListener{
            override fun onClick(view: View){
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder().setAllowNewEmailAccounts(true).build(),LOGIN_PERMISSION)
            }
        })
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
}
