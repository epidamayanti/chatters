package com.poy.chatters.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebase.client.Firebase
import com.poy.chatters.R
import com.poy.chatters.models.NotificationModel
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.services.LocalUserService
import java.util.HashMap

class NotificationListAdapter(context: Context, list: List<NotificationModel>) :
    ArrayAdapter<NotificationModel>(context, R.layout.custom_notication_row, list) {

    private lateinit var con: Context
    private var acceptBtn: ImageButton? = null
    private var rejectBtn: ImageButton? = null


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = LayoutInflater.from(context)
        val customView = inflater.inflate(R.layout.custom_notication_row, parent, false)
        val model = getItem(position)
        // get layout
        val layout = customView.findViewById(R.id.layout_CustomNotificationRow) as LinearLayout

        // make components according to model and append to layout

        val tv_NotficationMessage = customView.findViewById(R.id.tv_NotificationMessage) as TextView
        tv_NotficationMessage.text = model!!.NotificationMessage

        // friend request
        if (model.NotificationType === 1) {
            // make button and append
            //            acceptBtn = new Button(getContext());
            //            rejectBtn = new Button(getContext());

            acceptBtn = ImageButton(context)
            rejectBtn = ImageButton(context)

            acceptBtn?.setBackgroundColor(Color.TRANSPARENT)
            rejectBtn?.setBackgroundColor(Color.TRANSPARENT)

            acceptBtn?.setImageResource(R.drawable.emoji_2705)
            rejectBtn?.setImageResource(R.drawable.emoji_274c)

            setCustomOnClick(acceptBtn!!, model.EmailFrom.toString(), model.FirstName.toString(), model.LastName.toString())
            onRejectClick(rejectBtn!!, position, model.FirstName + " " + model.LastName)
            // set layout params
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER

            acceptBtn?.setLayoutParams(layoutParams)
            rejectBtn?.setLayoutParams(layoutParams)
            acceptBtn?.setPadding(4, 4, 4, 4)
            rejectBtn?.setPadding(4, 4, 4, 4)
            layout.addView(acceptBtn)
            layout.addView(rejectBtn)
        }
        return customView
    }

    private fun setCustomOnClick(btn:ImageButton, friendEmail:String, friendFirstName:String, friendLastName:String) {
        btn.setOnClickListener {
            val user = LocalUserService.getLocalUserFromPreferences(con)
            // add to friends and remove from requests
            val fireBase = Firebase(StaticInfo.FriendsURL)
            // set each other friends
            val map1 = HashMap<String, String>()
            map1["Email"] = friendEmail
            map1["FirstName"] = friendFirstName
            map1["LastName"] = friendLastName

            fireBase.child(user.Email).child(friendEmail).setValue(map1)

            val map2 = HashMap<String, String>()
            map2["Email"] = user.Email.toString()
            map2["FirstName"] = user.FirstName.toString()
            map2["LastName"] = user.LastName.toString()
            fireBase.child(friendEmail).child(user.Email).setValue(map2)

            val frRequ = Firebase(StaticInfo.EndPoint + "/friendrequests")
            frRequ.child(user.Email).child(friendEmail).removeValue()
            acceptBtn?.setEnabled(false)

            Toast.makeText(con, "Accepted", Toast.LENGTH_SHORT).show()
            rejectBtn?.setEnabled(false)

            val notMap = HashMap<String, String>()
            notMap["SenderEmail"] = user.Email.toString()
            notMap["FirstName"] = user.FirstName.toString()
            notMap["LastName"] = user.LastName.toString()
            notMap["Message"] = "Contact request accepted start chating... "
            // accepted contact reques
            notMap["NotificationType"] = "3"
            val notRef = Firebase(StaticInfo.NotificationEndPoint + "/" + friendEmail)
            notRef.push().setValue(notMap)
        }
    }

    private fun onRejectClick(btn:ImageButton, modelPosition:Int, friedFullName:String) {
        btn.setOnClickListener(object:View.OnClickListener {
        override fun onClick(v:View) {
            AlertDialog.Builder(con)
                .setTitle(friedFullName)
                .setMessage("Are you sure to reject this contact request?")
                .setPositiveButton("Reject", object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which:Int) {
                        val user = LocalUserService.getLocalUserFromPreferences(con)
                        val fireBase = Firebase(StaticInfo.FriendRequestsEndPoint + "/" + user.Email + "/" + getItem(modelPosition)!!.FriendRequestFireBaseKey)

                        fireBase.removeValue()
                        rejectBtn?.setEnabled(false)
                        acceptBtn?.setEnabled(false)
                        Toast.makeText(con, "Rejected", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show()
        }
        })
    }
}