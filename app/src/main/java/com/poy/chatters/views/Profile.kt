package com.poy.chatters.views

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.poy.chatters.R
import com.poy.chatters.services.LocalUserService
import com.poy.chatters.services.Tools
import kotlinx.android.synthetic.main.activity_profile.*

class Profile : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val user = LocalUserService.getLocalUserFromPreferences(this)
        tv_UserFullName.text = Tools.toProperName(user.FirstName.toString()) + " " + Tools.toProperName(user.LastName.toString())
    }
}
