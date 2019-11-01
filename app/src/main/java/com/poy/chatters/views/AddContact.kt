@file:Suppress("DEPRECATION")

package com.poy.chatters.views

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.poy.chatters.R
import com.poy.chatters.adapter.FriendListAdapter
import com.poy.chatters.models.User
import com.poy.chatters.services.LocalUserService
import com.poy.chatters.services.Tools
import kotlinx.android.synthetic.main.activity_add_contact.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList

class AddContact : AppCompatActivity() {

    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        pd = ProgressDialog(this)
        pd.setMessage("Seraching...")

        lv_AddContactList.setOnItemClickListener { _, view, _, _ ->
            val email = view.findViewById(R.id.tv_HiddenEmail) as TextView
            // start FriendProfileFull
            val intent = Intent(this@AddContact, FriendProfile::class.java)
            intent.putExtra("Email", email.text.toString())
            startActivity(intent)
        }
    }

    fun btnSearchclick(view:View) {
        if (et_SearchKey.text.toString().trim { it <= ' ' } != "" && et_SearchKey.text.toString().length > 2) {

            if (Tools.isNetworkAvailable(this)) {
                val t = FindFriendsTask()
                t.execute()
            } else {
                Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT)
                    .show()
            }

        } else {
            et_SearchKey.setText("")
            Toast.makeText(this, "Input at least 3 characters", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class FindFriendsTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            pd.show()
        }

        override fun doInBackground(vararg params: Void): String? {
            val api = Tools.makeRetroFitApi()
            val call = api.allUsersAsJsonString
            try {
                return call.execute().body()
            } catch (e: IOException) {
                pd.hide()
                Toast.makeText(
                    this@AddContact,
                    "Please check your internet connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            return null
        }


        @SuppressLint("DefaultLocale")
        override fun onPostExecute(jsonListString: String) {

            try {
                val user = LocalUserService.getLocalUserFromPreferences(applicationContext)
                val jsonObjectList = JSONObject(jsonListString)
                val friendList = ArrayList<User>()
                val iterator = jsonObjectList.keys()
                while (iterator.hasNext()) {
                    try {
                        val key = iterator.next() as String
                        val item = jsonObjectList.getJSONObject(key)
                        val f = User()
                        f.Email = item.getString("Email")
                        f.FirstName = item.getString("FirstName")
                        f.LastName = item.getString("LastName")
                        val serKey =
                            Tools.encodeString(et_SearchKey.text.toString()).toLowerCase().trim()
                        val fullName = f.FirstName.toString().toLowerCase() + " " + f.LastName.toString().toLowerCase()
                        if (f.Email.toString().toLowerCase().contains(serKey) || fullName.contains(serKey)) {
                            if (!f.Email.equals(user.Email)) {
                                friendList.add(f)
                            }
                        }
                    } catch (exx: Exception) {
                        continue
                    }

                }
                val adp = FriendListAdapter(this@AddContact, friendList)
                lv_AddContactList.adapter = adp
                pd.hide()

            } catch (e: JSONException) {
                pd.hide()
                e.printStackTrace()
            }


        }
    }
}
