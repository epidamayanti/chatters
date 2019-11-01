package com.poy.chatters.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.poy.chatters.R
import com.poy.chatters.models.Message
import com.poy.chatters.services.Tools

class AdapterLastChat(context: Context, messageList: List<Message>) : ArrayAdapter<Message>(context, R.layout.custom_lastchat_row, messageList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val customView = inflater.inflate(R.layout.custom_lastchat_row, parent, false)
        val message = getItem(position)
        val hiddenEmail = customView.findViewById(R.id.tv_lastChat_HiddenEmail) as TextView
        val tv_Name = customView.findViewById(R.id.tv_lastChat_FriendFullName) as TextView
        val tv_MessageDate = customView.findViewById(R.id.tv_lastChat_MessageDate) as TextView
        val tv_Message = customView.findViewById(R.id.tv_lastChat_Message) as TextView
        hiddenEmail.setText(message!!.FromMail)
        tv_Name.setText(message.FriendFullName)
        val properDate = Tools.messageSentDateProper(message.SentDate.toString())
        tv_MessageDate.setText(properDate)
        if (message.Message!!.length > 20) {
            message.Message = message.Message!!.substring(0, 20)
        }
        tv_Message.setText(message.Message)
        return customView
    }

}
