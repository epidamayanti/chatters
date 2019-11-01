package com.poy.chatters.services

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.poy.chatters.models.User
import com.poy.chatters.models.Message


import java.util.*

class DataContext(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) : SQLiteOpenHelper(context, "chatter.db", factory, 3) {

    val userFriendList: List<User>
        get() {
            val friendList = ArrayList<User>()
            val db = readableDatabase
            val query = "select * from Friends"
            val c = db.rawQuery(query, null)
            c.moveToFirst()
            while (!c.isAfterLast) {
                try {
                    val friend = User()
                    friend.Email = c.getString(c.getColumnIndex("Email"))
                    friend.FirstName = c.getString(c.getColumnIndex("FirstName"))
                    friend.LastName = c.getString(c.getColumnIndex("LastName"))
                    friendList.add(friend)
                    c.moveToNext()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            c.close()

            friendList.sortWith(Comparator { o1, o2 -> o1.FirstName!!.compareTo(o2.FirstName.toString()) })

            return friendList

        }

    override fun onCreate(db: SQLiteDatabase) {
        //String tblLocalUser = "create table if not exists LocalUser (ID integer ,Email text, FirstName text, LastName text); ";
        val tblFriends =
            "create table if not exists Friends (Email text, FirstName text, LastName text);"
        val tblMessages =
            "create table if not exists Messages (FromMail text, ToMail text, Message text, SentDate text);"
        //db.execSQL(tblLocalUser);
        db.execSQL(tblFriends)
        db.execSQL(tblMessages)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //String dropLocalUser = "drop table if exists LocalUser;";
        val dropFriends = "drop table if exists Friends; "
        val dropMessages = "drop table if exists Messages;"
        // db.execSQL(dropLocalUser);
        db.execSQL(dropFriends)
        db.execSQL(dropMessages)
        onCreate(db)
    }

    fun refreshUserFriendList(friendList: List<User>) {

        for (item in friendList) {
            // check if user already exists
            if (checkFriendAlreadyExists(item.Email.toString()) == 0) {
                // insert
                val db = writableDatabase
                val query =
                    "insert into Friends (Email,FirstName,LastName) values('" + item.Email + "', '" + item.FirstName + "', '" + item.LastName + "');"
                db.execSQL(query)
                // db.close();
            }
        }
    }

    private fun checkFriendAlreadyExists(email: String): Int {
        var c: Cursor? = null
        var db: SQLiteDatabase? = null
        try {
            db = readableDatabase
            val query = "select count(*) from Friends where Email = '$email'"
            c = db!!.rawQuery(query, null)
            return if (c!!.moveToFirst()) {
                c.getInt(0)
            } else 0
        } finally {
            c?.close()
            db?.close()
        }
    }

    fun deleteAllFriendsFromLocalDB() {
        val query = "delete from Friends"
        // String queryMess = "delete from Messages";
        val db = writableDatabase
        db.execSQL(query)
        //db.execSQL(queryMess);
    }

    fun deleteFriendByEmailFromLocalDB(email: String) {
        val query = "delete from Friends where Email = '$email';"
        writableDatabase.execSQL(query)
    }

    @SuppressLint("Recycle")
    fun getFriendByEmailFromLocalDB(friendEmail: String): User {
        val query = "select * from Friends where Email = '$friendEmail';"
        val db = readableDatabase
        val c = db.rawQuery(query, null)
        c.moveToFirst()
        val friend = User()

        if (c.count > 0) {
            friend.Email = c.getString(c.getColumnIndex("Email"))
            friend.FirstName = c.getString(c.getColumnIndex("FirstName"))
            friend.LastName = c.getString(c.getColumnIndex("LastName"))
        }
        return friend
    }

    fun saveMessageOnLocakDB(from: String, to: String, message: String, sentDate: String) {
        val db = writableDatabase
        val query =
            "insert into Messages (FromMail, ToMail, Message, SentDate) values('$from', '$to', '" + message.replace(
                "'",
                "\""
            ) + "','" + sentDate + "');"
        db.execSQL(query)
    }

    fun getChat(userMail: String, friendMail: String, pageNo: Int): List<Message> {
        val messageList = ArrayList<Message>()
        val db = readableDatabase
        try {
            val limit = 5 * pageNo + 35
            val whereCondition =
                "((FromMail = '$userMail' and ToMail='$friendMail') or (ToMail = '$userMail' and FromMail='$friendMail'))"
            val query =
                "select * from ( select rowid, * from Messages where $whereCondition order by rowid desc limit $limit)  order by rowid "
            val c = db.rawQuery(query, null)
            c.moveToFirst()
            while (!c.isAfterLast) {
                val mess = Message()
                mess.FromMail = c.getString(c.getColumnIndex("FromMail"))
                mess.ToMail = c.getString(c.getColumnIndex("ToMail"))
                mess.Message = c.getString(c.getColumnIndex("Message"))

                mess.SentDate = c.getString(c.getColumnIndex("SentDate"))
                messageList.add(mess)
                c.moveToNext()
            }
            c.close()
            return messageList
        } catch (e: Exception) {
            e.printStackTrace()
            return messageList
        }

    }

    fun deleteChat(userMail: String, friendMail: String) {
        val deleteQuery =
            "delete from  Messages where (FromMail = '$userMail' and ToMail='$friendMail') or (ToMail = '$userMail' and FromMail='$friendMail')  "
        writableDatabase.execSQL(deleteQuery)

    }

    @SuppressLint("Recycle")
    fun getUserLastChatList(userMail: String): List<Message> {
        val userFriendList = userFriendList
        val userLastChat = ArrayList<Message>()
        val db = readableDatabase
        for (friend in userFriendList) {
            val query =
                "select rowid, * from Messages where (FromMail = '" + userMail + "' and ToMail='" + friend.Email + "') or (ToMail = '" + userMail + "' and FromMail='" + friend.Email + "')  order by rowid desc limit 1  "
            val c = db.rawQuery(query, null)
            c.moveToFirst()
            try {
                val mess = Message()
                // set from email to friend so when user click on list to navigate to chat activity
                mess.FromMail = friend.Email
                mess.Message = c.getString(c.getColumnIndex("Message"))
                mess.Message = mess.Message.toString().replace("\n", "")
                mess.SentDate = c.getString(c.getColumnIndex("SentDate"))
                mess.FriendFullName = friend.FirstName + " " + friend.LastName
                mess.rowid = c.getInt(c.getColumnIndex("rowid"))
                userLastChat.add(mess)
            } catch (e: Exception) {

            }

        }
        userLastChat.sortWith(Comparator { o1, o2 ->
            // -1) Less Than 0) equal 1) Greater than
            if (o1.rowid > o2.rowid) -1 else 1
        })

        return userLastChat
    }

    fun setPreferedDisplayName(friendEmail: String, newName: String) {
        val query =
            "update Friends set FirstName = '$newName', LastName='' where Email='$friendEmail' "
        writableDatabase.execSQL(query)
    }

}
