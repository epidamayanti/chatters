package com.poy.chatters.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.firebase.client.ChildEventListener
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.poy.chatters.views.Notifications
import com.poy.chatters.R
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.views.Chat

@Suppress("DEPRECATION")
class AppService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Firebase.setAndroidContext(applicationContext)
        val user = LocalUserService.getLocalUserFromPreferences(applicationContext)
        val reference = Firebase(StaticInfo.NotificationEndPoint + "/" + user.Email)
        reference.addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                    if (LocalUserService.getLocalUserFromPreferences(applicationContext).Email != null) {
                        val map = dataSnapshot?.getValue(Map::class.java)
                        val mess = map?.get("Message")?.toString()
                        val senderEmail = map?.get("SenderEmail")?.toString()
                        val senderFullName =
                            Tools.toProperName(map?.get("FirstName")!!.toString()) + " " + Tools.toProperName(
                                map["LastName"]!!.toString()
                            )
                        val notificationType: Int // Message
                        notificationType =
                            if (map["NotificationType"] == null) 1 else Integer.parseInt(map["NotificationType"]!!.toString())
                        // check if user is on chat activity with senderEmail
                        if (StaticInfo.UserCurrentChatFriendEmail != senderEmail) {
                            notifyUser(senderEmail.toString(), senderFullName,
                                mess.toString(), notificationType)
                            // remove notification
                            reference.child(dataSnapshot.key).removeValue()
                        } else {
                            reference.child(dataSnapshot.key).removeValue()
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {

                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

                }

                override fun onCancelled(firebaseError: FirebaseError) {

                }
            }
        )
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // check if user is login
        if (LocalUserService.getLocalUserFromPreferences(applicationContext).Email != null) {
            sendBroadcast(Intent("com.poy.chatters.restartservice"))
        }


    }

    private fun notifyUser(
        friendEmail: String,
        senderFullName: String,
        mess: String,
        notificationType: Int
    ) {
        val not = NotificationCompat.Builder(applicationContext)
        not.setAutoCancel(true)
        not.setSmallIcon(R.mipmap.ic_launcher_round)
        not.setTicker("New Message")
        not.setWhen(System.currentTimeMillis())
        not.setContentText(mess)
        val i: Intent?
        // 1) Message 3) Contact Request Accepted
        if (notificationType == 1 || notificationType == 3) {
            i = Intent(applicationContext, Chat::class.java)
            val db = DataContext(applicationContext, null)
            val frnd = db.getFriendByEmailFromLocalDB(friendEmail)
            if (frnd.FirstName != null) {
                not.setContentTitle(frnd.FirstName + " " + frnd.LastName)
                i.putExtra("FriendFullName", frnd.FirstName + " " + frnd.LastName)
            } else {
                not.setContentTitle(senderFullName)
                i.putExtra("FriendFullName", senderFullName)
            }
        } else if (notificationType == 2) {
            i = Intent(applicationContext, Notifications::class.java)
            not.setContentTitle(senderFullName)
        } else {
            i = null
        }// Contact Request
        i!!.putExtra("FriendEmail", friendEmail)
        val uniqueID = Tools.createUniqueIdPerUser(friendEmail)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            uniqueID,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        not.setContentIntent(pendingIntent)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        not.setDefaults(Notification.DEFAULT_ALL)
        nm.notify(uniqueID, not.build())
    }
}