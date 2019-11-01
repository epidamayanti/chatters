package com.poy.chatters.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.client.ChildEventListener
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.poy.chatters.R
import com.poy.chatters.adapter.NotificationListAdapter
import com.poy.chatters.models.NotificationModel
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.services.LocalUserService
import com.poy.chatters.services.Tools
import kotlinx.android.synthetic.main.activity_notifications.*

class Notifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        Firebase.setAndroidContext(this)
    }

    override fun onStart() {
        super.onStart()

        var notificationList = ArrayList<NotificationModel>()
        var user = LocalUserService.getLocalUserFromPreferences(this)
        val reqRef = Firebase(StaticInfo.EndPoint + "/friendrequests/" + user.Email)
        reqRef.addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String) {
                    val map = dataSnapshot.getValue(Map::class.java)
                    val firstName = map["FirstName"]!!.toString()
                    val lastName = map["LastName"]!!.toString()
                    val key = dataSnapshot.key
                    val not = NotificationModel()
                    not.FirstName = firstName
                    not.LastName = lastName
                    not.NotificationType = 1 // friend request
                    notificationList.add(not)
                    not.EmailFrom = key
                    not.FriendRequestFireBaseKey = dataSnapshot.key
                    not.NotificationMessage =
                        Tools.toProperName(firstName) + " " + Tools.toProperName(lastName)
                    val adp = NotificationListAdapter(applicationContext, notificationList)
                    lv_NotificationList.setAdapter(adp)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {

                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val friendEmail = dataSnapshot.key
                    var index = -1
                    for (i in notificationList.indices) {
                        val item = notificationList[i]
                        if (item.EmailFrom.equals(friendEmail))
                            index = i
                    }
                    notificationList.removeAt(index)
                    val adp = NotificationListAdapter(applicationContext, notificationList)
                    lv_NotificationList.setAdapter(adp)

                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

                }

                override fun onCancelled(firebaseError: FirebaseError) {

                }
            }
        )


    }
}
