@file:Suppress("DEPRECATION")

package com.poy.chatters.views

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.firebase.client.Firebase
import com.poy.chatters.R
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.services.Tools
import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Register : AppCompatActivity() {

    var pd: ProgressDialog? = null
    var email : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Firebase.setAndroidContext(this)

        pd = ProgressDialog(this)
        pd?.setMessage("Loading...")
    }

    fun btnRegclick(view: View) {
        if (!Tools.isNetworkAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT)
                .show()
        } else if (etFirstName.text.toString() == "") {
            etFirstName.error = "Enter Firstname"
        } else if (etLastName.text.toString() == "") {
            etLastName.error = "Enter Lastname"
        } else if (etEmail.text.toString() == "" || !Tools.isValidEmail(etEmail.text.toString())) {
            etEmail.error = "Enter Valid Email"
        } else if (etPassword.text.toString() == "") {
            etPassword.error = "Enter Password"
        } else {
            email = Tools.encodeString(etEmail.text.toString())
            val t = RegisterUserTask()
            t.execute()
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class RegisterUserTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            pd?.show()
        }

        override fun doInBackground(vararg params: Void): String? {

            val api = Tools.makeRetroFitApi()
            val call = api.getSingleUserByEmail(StaticInfo.UsersURL + "/" + email + ".json")
            try {
                return call.execute().body()
            } catch (e: IOException) {
                e.printStackTrace()
                pd?.hide()
            }

            return null
        }

        @SuppressLint("SimpleDateFormat")
        override fun onPostExecute(jsonString: String) {
            try {
                if (jsonString.trim { it <= ' ' } == "null") {
                    val firebase = Firebase(StaticInfo.UsersURL)
                    firebase.child(email).child("FirstName")
                        .setValue(etFirstName.text.toString())
                    firebase.child(email).child("LastName")
                        .setValue(etLastName.text.toString())
                    firebase.child(email).child("Email").setValue(email)
                    firebase.child(email).child("Password")
                        .setValue(etPassword.text.toString())
                    val dateFormat = SimpleDateFormat("dd MM yy hh:mm a")
                    val date = Date()
                    firebase.child(email).child("Status").setValue(dateFormat.format(date))
                    Toast.makeText(applicationContext, "Signup Success", Toast.LENGTH_SHORT).show()
                    pd?.hide()
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Email already exists", Toast.LENGTH_SHORT)
                        .show()
                    pd?.hide()
                }
            } catch (e: Exception) {
                pd?.hide()
                e.printStackTrace()
            }

        }
    }


}
