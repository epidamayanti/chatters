package com.poy.chatters.services

import android.content.Context
import com.poy.chatters.models.User

object LocalUserService {
    fun getLocalUserFromPreferences(context: Context): User {
        val pref = context.getSharedPreferences("LocalUser", 0)
        val user = User()
        user.Email = pref.getString("Email", null)
        user.FirstName = pref.getString("FirstName", null)
        user.LastName = pref.getString("LastName", null)
        return user
    }

    fun deleteLocalUserFromPreferences(context: Context): Boolean {
        try {
            val pref = context.getSharedPreferences("LocalUser", 0)
            val editor = pref.edit()
            editor.clear()
            editor.commit()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }

    }


}