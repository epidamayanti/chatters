@file:Suppress("DEPRECATION")

package com.poy.chatters.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.firebase.client.Firebase
import com.poy.chatters.R
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.models.User
import com.poy.chatters.services.DataContext
import com.poy.chatters.services.LocalUserService
import com.poy.chatters.services.Tools
import kotlinx.android.synthetic.main.activity_friend_profile.*
import kotlinx.android.synthetic.main.custom_friend_list_row.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.HashMap

class FriendProfile : AppCompatActivity() {

    private var friendEmail: String? = null
    private lateinit var user: User
    private lateinit var f:User
    private lateinit var pd: ProgressDialog
    private lateinit var db: DataContext
    private lateinit var tv_FriendFullName: TextView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        Firebase.setAndroidContext(this)
        pd = ProgressDialog(this)

        pd.setMessage("Loading...")
        f = User()
        val extras = intent.extras
        friendEmail = extras?.getString("Email")
        tv_FriendFullName = findViewById(R.id.tv_FriendFullName_L_FriendProfile) as TextView


        user = LocalUserService.getLocalUserFromPreferences(this)

        db = DataContext(this,  null)

        // check if already friends otherwise get info from server
        val friend = db.getFriendByEmailFromLocalDB(friendEmail.toString())
        if (friend.Email == null) {
            val btnEditname = findViewById<ImageButton>(R.id.btn_EditName)
            btnEditname.visibility = View.INVISIBLE
            val t = FindFriendsTask()
            t.execute()
        } else {
            tv_FriendFullName.text = Tools.toProperName(friend.FirstName.toString()) + " " + Tools.toProperName(
                friend.LastName.toString()
            )
            btn_AddFriend.isEnabled = false
            btn_AddFriend.text = "Connected"


        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
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
                e.printStackTrace()
                pd.hide()
            }

            return null
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(jsonListString: String) {
            try {
                val jsonObjectList = JSONObject(jsonListString)
                val item = jsonObjectList.getJSONObject(friendEmail)
                f.Email = item.getString("Email")
                f.FirstName = item.getString("FirstName")
                f.LastName = item.getString("LastName")
                tv_FriendFullName.text =
                    Tools.toProperName(f.FirstName.toString()) + " " + Tools.toProperName(f.LastName.toString())
                pd.hide()
            } catch (e1: JSONException) {
                e1.printStackTrace()
                pd.hide()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    fun btnSendfriendrequestclick(view : View) {
        val firebase = Firebase("https://mychatapp-e4cb9.firebaseio.com/friendrequests")
        val notifRef = Firebase(StaticInfo.NotificationEndPoint + "/" + friendEmail)
        val map = HashMap<String, String>()
        map["FirstName"] = user.FirstName.toString()
        map["LastName"] = user.LastName.toString()
        firebase.child(Tools.encodeString(f.Email.toString())).child(Tools.encodeString(user.Email.toString()))
            .setValue(map)
        btn_AddFriend.isEnabled = false
        btn_AddFriend.text = "Request Sent"

        map["SenderEmail"] = user.Email.toString()
        map["Message"] = "Pending contact request"
        map["NotificationType"] = "2"

        notifRef.push().setValue(map)

    }

    fun btnEditnameclick(view : View) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set display name")
        val et = EditText(this)
        et.setText(tv_FriendFullName.text)
        et.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        builder.setView(et)
        builder.setPositiveButton("Save") { _, _ ->
            val newName = et.text.toString()
            db.setPreferedDisplayName(friendEmail.toString(), newName)
            tv_FriendFullName.text = newName
            setResult(Activity.RESULT_OK)
        }
        builder.setNegativeButton("Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()

    }
}
