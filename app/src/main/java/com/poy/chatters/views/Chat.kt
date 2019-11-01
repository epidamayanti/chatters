package com.poy.chatters.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.client.ChildEventListener
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.poy.chatters.R
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.models.User
import com.poy.chatters.services.DataContext
import com.poy.chatters.services.LocalUserService
import com.poy.chatters.services.Tools
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.message_area.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NAME_SHADOWING")
class Chat : AppCompatActivity() {
    private var db = DataContext(this, null)

    private lateinit var reference1: Firebase
    private lateinit var reference2: Firebase
    private lateinit var refNotMess:Firebase
    private lateinit var refFriend:Firebase
    private lateinit var refUser: Firebase

    private lateinit var reference1Listener:ChildEventListener
    private lateinit var refFriendListener: ChildEventListener

    private lateinit var user: User

    private var friendEmail: String? = ""
    private var friendFullName: String? = ""

    private var pageNo = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<Toolbar>(R.id.toolbarChatActivity)
        setSupportActionBar(toolbar)

        user = LocalUserService.getLocalUserFromPreferences(this)
        Firebase.setAndroidContext(this)

        reference1Listener = object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {

                if (dataSnapshot?.key != StaticInfo.TypingStatus) {
                    val map = dataSnapshot?.getValue(Map::class.java)
                    val mess = map?.get("Message")!!.toString()
                    val senderEmail = map["SenderEmail"]!!.toString()
                    val sentDate = map["SentDate"]!!.toString()
                    try {
                        // remove from server
                        reference1.child(dataSnapshot?.key).removeValue()
                        // save message on local db
                        db.saveMessageOnLocakDB(senderEmail, user.Email.toString(), mess, sentDate)
                        if (senderEmail == user.Email) {
                            // login user
                            appendMessage(mess, sentDate, 1, false)
                        } else {
                            appendMessage(mess, sentDate, 2, false)
                        }
                    } catch (e: Exception) {

                    }

                } else {
                    // show typing status
                    val typingStatus = dataSnapshot?.value.toString()
                    if (typingStatus == "Typing") {
                        supportActionBar?.subtitle = "$typingStatus..."
                    }
                }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {
                val typingStatus = dataSnapshot?.value.toString()
                if (typingStatus == "Typing") {
                    supportActionBar?.setSubtitle("$typingStatus...")
                } else {
                    // check if online
                    supportActionBar?.setSubtitle("Online")
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                //layout.removeAllViews();
                if (dataSnapshot?.key == "TypingStatus") {
                    supportActionBar?.subtitle = "Online"

                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, s: String?) {

            }

            override fun onCancelled(firebaseError: FirebaseError) {

            }
        }

        refFriendListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                if (dataSnapshot?.key == "Status") {
                    // check if subtitle is not Typing
                    val subTitle = supportActionBar?.subtitle
                    if (subTitle != null) {
                        if (subTitle != "Typing...") {
                            var friendStatus = dataSnapshot.value.toString()
                            if (friendStatus != "Online") {
                                friendStatus = Tools.lastSeenProper(friendStatus)
                            }
                            supportActionBar?.subtitle = friendStatus
                        }
                    } else {
                        var friendStatus = dataSnapshot.value.toString()
                        if (friendStatus != "Online") {
                            friendStatus = Tools.lastSeenProper(friendStatus)
                        }
                        supportActionBar?.setSubtitle(friendStatus)
                    }


                }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {
                var friendStatus = dataSnapshot.value.toString()
                if (friendStatus != "Online") {
                    friendStatus = Tools.lastSeenProper(friendStatus)
                }
                supportActionBar?.subtitle = friendStatus
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

            }

            override fun onCancelled(firebaseError: FirebaseError) {

            }
        }

        val extras = intent.extras
        friendEmail = extras?.getString("FriendEmail")
        val chatList = db.getChat(user.Email.toString(), friendEmail.toString(), 1)

        for (item in chatList) {
            val messageType = if (item.FromMail.equals(user.Email)) 1 else 2
            appendMessage(item.Message.toString(), item.SentDate.toString(), messageType, false)
        }

        friendFullName = extras?.getString("FriendFullName")

        supportActionBar?.title = friendFullName
        reference1 = Firebase(StaticInfo.MessagesEndPoint + "/" + user.Email + "-@@-" + friendEmail)
        reference2 = Firebase(StaticInfo.MessagesEndPoint + "/" + friendEmail + "-@@-" + user.Email)
        refFriend = Firebase(StaticInfo.UsersURL + "/" + friendEmail)
        refNotMess = Firebase(StaticInfo.NotificationEndPoint + "/" + friendEmail)
        refFriend.addChildEventListener(refFriendListener)

        StaticInfo.UserCurrentChatFriendEmail = friendEmail.toString()
        refUser = Firebase(StaticInfo.UsersURL + "/" + user.Email)

        et_Message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (et_Message.text.toString().isEmpty()) {
                    reference2.child(StaticInfo.TypingStatus).setValue("")
                } else if (et_Message.text.toString().length == 1) {
                    reference2.child(StaticInfo.TypingStatus).setValue("Typing")
                    // change color here
                    //  submit_btn.setColorFilter(R.color.colorPrimary);

                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        val rootView: View = findViewById<LinearLayout>(R.id.rootLayout)
        val emojiconEditText = findViewById<EmojiconEditText>(R.id.et_Message)
        val emojiImageView = findViewById<ImageView>(R.id.emoji_btn)

        val emojIcon = EmojIconActions(
            this,
            rootView,
            emojiconEditText,
            emojiImageView,
            "#1c2764",
            "#e8e8e8",
            "#f4f4f4"
        )
        emojIcon.ShowEmojIcon()

        emojIcon.setKeyboardListener(object : EmojIconActions.KeyboardListener {
            override fun onKeyboardOpen() {

                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            }

            override fun onKeyboardClose() {

            }
        })

        swiperefresh.setOnRefreshListener {
            val chatList = db.getChat(user.Email.toString(), friendEmail.toString(), pageNo)
            layout1.removeAllViews()
            for (item in chatList) {
                val messageType = if (item.FromMail.equals(user.Email)) 1 else 2
                appendMessage(item.Message.toString(), item.SentDate.toString(), messageType, true)
            }
            swiperefresh.isRefreshing = false
            pageNo++
        }


        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnClickListener {
            val intent = Intent(this@Chat, FriendProfile::class.java)
            intent.putExtra("Email", friendEmail)
            startActivityForResult(intent, StaticInfo.ChatAciviityRequestCode)
        }

    }

    override fun onStart() {
        super.onStart()
        val extras = intent.extras
        friendEmail = extras?.getString("FriendEmail")

        // getSupportActionBar().setTitle(extras.getString("FriendFullName"));
        // getSupportActionBar().setIcon(R.drawable.dp_placeholder_sm);

        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        StaticInfo.UserCurrentChatFriendEmail = friendEmail.toString()
        // update status to online
        refUser.child("Status").setValue("Online")
        reference1.addChildEventListener(reference1Listener)
    }


    override fun onPause() {
        super.onPause()
        reference1.removeEventListener(reference1Listener)
    }

    override fun onRestart() {
        super.onRestart()
        StaticInfo.UserCurrentChatFriendEmail = friendEmail.toString()
        refUser.child("Status").setValue("Online")
    }

    override fun onStop() {
        super.onStop()
        StaticInfo.UserCurrentChatFriendEmail = ""
        reference1.removeEventListener(reference1Listener)
        reference2.child(StaticInfo.TypingStatus).setValue("")
    }

    @SuppressLint("SimpleDateFormat")
    override fun onDestroy() {
        super.onDestroy()
        StaticInfo.UserCurrentChatFriendEmail = ""
        // set last seen
        val dateFormat = SimpleDateFormat("dd MM yy hh:mm a")
        val date = Date()
        refUser.child("Status").setValue(dateFormat.format(date))
        reference1.removeEventListener(reference1Listener)
        reference2.child(StaticInfo.TypingStatus).setValue("")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val extras = intent.extras
        layout1.removeAllViews()
        friendEmail = extras!!.getString("FriendEmail")
        friendFullName = extras.getString("FriendFullName")
        supportActionBar?.title = friendFullName
        val chatList = db.getChat(user.Email.toString(), friendEmail.toString(), 1)
        for (item in chatList) {
            val messageType = if (item.FromMail.equals(user.Email)) 1 else 2
            appendMessage(item.Message.toString(), item.SentDate.toString(), messageType, false)
        }

        StaticInfo.UserCurrentChatFriendEmail = friendEmail.toString()
        reference1.removeEventListener(reference1Listener)
        reference1 = Firebase(StaticInfo.MessagesEndPoint + "/" + user.Email + "-@@-" + friendEmail)
        reference1.addChildEventListener(reference1Listener)

        refFriend.removeEventListener(refFriendListener)
        refFriend = Firebase(StaticInfo.UsersURL + "/" + friendEmail)
        refFriend.addChildEventListener(refFriendListener)

        reference2 = Firebase(StaticInfo.MessagesEndPoint + "/" + friendEmail + "-@@-" + user.Email)

    }

    @SuppressLint("SimpleDateFormat")
    fun btnSendmessageclick(view: View) {

        val message = et_Message.text.toString().trim { it <= ' ' }
        et_Message.setText("")
        if (message != "") {
            val map = HashMap<String, String>()
            map["Message"] = message
            map["SenderEmail"] = user.Email.toString()
            map["FirstName"] = user.FirstName.toString()
            map["LastName"] = user.LastName.toString()

            val dateFormat = SimpleDateFormat("dd MM yy hh:mm a")
            val date = Date()
            val sentDate = dateFormat.format(date)

            map["SentDate"] = sentDate
            //reference1.push().setValue(map);
            reference2.push().setValue(map)
            refNotMess.push().setValue(map)

            // save in local db
            db.saveMessageOnLocakDB(user.Email.toString(), friendEmail.toString(), message, sentDate)

            // appendmessage
            appendMessage(message, sentDate, 1, false)
        }
    }

    @SuppressLint("SetTextI18n", "RtlHardcoded")
    fun appendMessage(mess: String, sentDate: String, messType: Int, scrollUp: Boolean) {
        var sentDate = sentDate

        val textView = EmojiconTextView(this)
        textView.setEmojiconSize(30)
        sentDate = Tools.messageSentDateProper(sentDate)
        val dateString = SpannableString(sentDate)
        dateString.setSpan(RelativeSizeSpan(0.7f), 0, sentDate.length, 0)
        dateString.setSpan(ForegroundColorSpan(Color.GRAY), 0, sentDate.length, 0)

        textView.text = mess + "\n"
        textView.append(dateString)
        textView.setTextColor(Color.parseColor("#000000"))


        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            6f
        )
        lp.setMargins(0, 0, 0, 5)
        // 1 user
        if (messType == 1) {
            textView.setBackgroundResource(R.drawable.messagebg1)
            lp.gravity = Gravity.RIGHT
        } else {
            textView.setBackgroundResource(R.drawable.messagebg2)
            lp.gravity = Gravity.LEFT
        }//  2 friend

        textView.setPadding(12, 4, 12, 4)

        textView.layoutParams = lp
        layout1.addView(textView)
        scrollView.post {
            if (scrollUp)
                scrollView.fullScroll(View.FOCUS_UP)
            else
                scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_deleteConservation) {
            AlertDialog.Builder(this)
                .setTitle(friendFullName)
                .setMessage("Are you sure to delete this chat?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteChat(user.Email.toString(), friendEmail.toString())
                    layout1.removeAllViews()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
            return true
        }
        if (id == R.id.menu_deleteContact) {
            AlertDialog.Builder(this)
                .setTitle(friendFullName)
                .setMessage("Are you sure to delete this contact?")
                .setPositiveButton("Delete") { _, _ ->
                    val ref =
                        Firebase(StaticInfo.EndPoint + "/friends/" + user.Email + "/" + friendEmail)
                    ref.removeValue()
                    // delete from local database
                    db.deleteFriendByEmailFromLocalDB(friendEmail.toString())
                    finish()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
            return true
        }

        if (id == R.id.menu_friendProfile) {
            val intent = Intent(this@Chat, FriendProfile::class.java)
            intent.putExtra("Email", friendEmail)
            startActivityForResult(intent, StaticInfo.ChatAciviityRequestCode)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == StaticInfo.ChatAciviityRequestCode && resultCode == Activity.RESULT_OK) {
            val updatedFriend = db.getFriendByEmailFromLocalDB(friendEmail.toString())
            friendFullName = updatedFriend.FirstName
            supportActionBar?.title = updatedFriend.FirstName
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
