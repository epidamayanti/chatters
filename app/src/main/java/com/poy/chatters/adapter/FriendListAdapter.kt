package com.poy.chatters.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.poy.chatters.R
import com.poy.chatters.models.User
import com.poy.chatters.services.Tools

class FriendListAdapter(context: Context, contactList: List<User>) :
    ArrayAdapter<User>(context, R.layout.custom_friend_list_row, contactList) {

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val customView = inflater.inflate(R.layout.custom_friend_list_row, parent, false)
        val user = getItem(position)
        val hiddenEmail = customView.findViewById(R.id.tv_HiddenEmail) as TextView
        val tv_Name = customView.findViewById(R.id.tv_FriendFullName) as TextView
        hiddenEmail.setText(user!!.Email)
        tv_Name.setText(Tools.toProperName(user.FirstName.toString()) + " " + Tools.toProperName(
            user.LastName.toString()
        ))
        return customView
    }

}
