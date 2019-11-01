@file:Suppress("DEPRECATION")

package com.poy.chatters.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.poy.chatters.R
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.services.IFireBaseAPI
import com.poy.chatters.services.Tools
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class Login : AppCompatActivity() {

    //var db = DataContext(this, "", null, 1)

    var pd : ProgressDialog? = null
    var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onResume() {
        super.onResume()

        btnLoginClick.setOnClickListener {
            btnLoginClick()
        }

        btnSignUpClick.setOnClickListener {
            btnSignUpClick()
        }

    }

    fun btnLoginClick() {
        if (!Tools.isNetworkAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT)
                .show()
        } else if (etEmail.text.toString() == "") {
            etEmail.error = "Email cannot be empty"

        } else if (etPassword.text.toString() == "") {
            etPassword.error = "Password cannot be empty"
        } else {
            pd = ProgressDialog(this)
            pd?.setMessage("Loading...")
            pd?.show()
            email = Tools.encodeString(etEmail.text.toString())
            val t = LoginTask()
            t.execute()
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class LoginTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void): String? {
            val retrofit = Retrofit.Builder()
                .baseUrl(ENDPOINT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()

            val api = retrofit.create(IFireBaseAPI::class.java)
            // Call<String> call = api.getAllUsersAsJsonString();
            val call = api.getSingleUserByEmail(StaticInfo.UsersURL + "/" + email + ".json")
            return try {
                call.execute().body()
            } catch (e: Exception) {
                pd?.hide()
                "null"
            }

        }

        override fun onPostExecute(jsonString: String) {
            try {
                if (jsonString.trim { it <= ' ' } != "null") {
                    val userObj = JSONObject(jsonString)
                    val pass = etPassword.text.toString()
                    if (userObj.getString("Password") == pass) {
                        pd?.hide()
                        val pref = applicationContext.getSharedPreferences("LocalUser", 0)
                        val editor = pref.edit()
                        editor.putString("Email", email)
                        editor.putString("FirstName", userObj.getString("FirstName"))
                        editor.putString("LastName", userObj.getString("LastName"))
                        editor.apply()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        pd?.hide()
                        Toast.makeText(
                            applicationContext,
                            "Incorecct email or password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    pd?.hide()
                    Toast.makeText(
                        this@Login,
                        "Incorecct email or password",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: JSONException) {
                pd?.hide()
                e.printStackTrace()
            }

        }

    }


    fun btnSignUpClick() {

        startActivity(Intent(this, Register::class.java))
    }


    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    companion object {
        const val ENDPOINT = "https://mychatapp-e4cb9.firebaseio.com"
    }
}
