@file:Suppress("DEPRECATION")
package com.poy.chatters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.firebase.client.Firebase
import com.google.android.material.tabs.TabLayout
import com.poy.chatters.adapter.AdapterLastChat
import com.poy.chatters.adapter.FriendListAdapter
import com.poy.chatters.models.Message
import com.poy.chatters.models.StaticInfo
import com.poy.chatters.models.User
import com.poy.chatters.services.AppService
import com.poy.chatters.services.DataContext
import com.poy.chatters.services.LocalUserService
import com.poy.chatters.services.Tools
import com.poy.chatters.views.*
import kotlinx.android.synthetic.main.fragment_contact.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    private var user: User? = null
    private var refUser: Firebase? = null
    private var db: DataContext? = null
    private var pd: ProgressDialog? = null
    private var userLastChatList: List<Message>? = null
    private var userFriednList: List<User>? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.setAndroidContext(this)

        db = DataContext(this, null)

        pd = ProgressDialog(this)
        pd?.setMessage("Refreshing...")

        //val toolbar = findViewById(R.id.toolbar) as Toolbar
        //setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<ViewPager>(R.id.container)
        mViewPager?.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(mViewPager)

        // check if user exists in local db
        user = LocalUserService.getLocalUserFromPreferences(this)
        if (user?.Email == null) {
            // send to activitylogin
            //            Intent intent = new Intent(this, ActivityLogin.class);
            //            startActivityForResult(intent, 100);
            //
        } else {
            startService(Intent(this, AppService::class.java))
            if (refUser == null) {
                refUser = Firebase(StaticInfo.UsersURL + "/" + user?.Email.toString())
            }

        }
    }

    override fun onStart() {
        super.onStart()

        // check if user exists in local db
        user = LocalUserService.getLocalUserFromPreferences(this)
        if (user?.Email == null) {
            // send to activitylogin
            val intent = Intent(this, Login::class.java)
            startActivityForResult(intent, 100)
            return
        }
            // refresh last chat
            userLastChatList = db?.getUserLastChatList(user?.Email.toString())
            val lastChatAdp = AdapterLastChat(this, userLastChatList!!)
            val lastChatList = findViewById<ListView>(R.id.lastChatList)

        if (lastChatList != null) {
            lastChatList.adapter = lastChatAdp
            // reset listener


            lastChatList.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { _, _, position, _ ->
                    if (userLastChatList?.size!! <= position) return@OnItemLongClickListener false
                    val selectedMessageItem = userLastChatList?.get(position)
                    val options = arrayOf<CharSequence>("Delete Chat")
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle(selectedMessageItem?.FriendFullName)
                    builder.setItems(
                        options
                    ) { _, index ->
                        // the user clicked on list[index]
                        if (index == 0) {
                            // Delete Chat
                            AlertDialog.Builder(this)
                                .setTitle(selectedMessageItem?.FriendFullName)
                                .setMessage("Are you sure to delete this chat?")
                                .setPositiveButton(
                                    "Delete"
                                ) { _, _ ->
                                    db?.deleteChat(
                                        user?.Email.toString(),
                                        selectedMessageItem?.FromMail.toString()
                                    )
                                    Toast.makeText(
                                        applicationContext,
                                        "Chat deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    userLastChatList =
                                        db?.getUserLastChatList(user?.Email.toString())
                                    val adp =
                                        AdapterLastChat(applicationContext, userLastChatList!!)
                                    lastChatList.adapter = adp
                                }
                                .setNegativeButton(android.R.string.no, null)
                                .show()
                        }
                    }

                    builder.setNegativeButton(
                        "Cancel"
                    ) { dialog, _ -> dialog.cancel() }
                    builder.show()
                    true
                }
        }

        // refresh contacts
        userFriednList = db?.userFriendList
        val adp = FriendListAdapter(this, userFriednList!!)
        val lvFriendlist = findViewById<ListView>(R.id.lv_FriendList)

        if (lvFriendlist != null) {
            lvFriendlist.adapter = adp
            lvFriendlist.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { _, _, position, _ ->
                    if (userFriednList?.size!! <= position) return@OnItemLongClickListener false
                    val selectedUser = userFriednList?.get(position)
                    val options = arrayOf<CharSequence>("Profile", "Delete Contact")
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(selectedUser?.FirstName + " " + selectedUser?.LastName)
                    builder.setItems(
                        options
                    ) { _, index ->
                        // the user clicked on list[index]
                        if (index == 0) {
                            // Profile
                            val intent =
                                Intent(this, FriendProfile::class.java)
                            intent.putExtra("Email", selectedUser?.Email)
                            startActivityForResult(intent, StaticInfo.ChatAciviityRequestCode)
                        } else {
                            // Delete Contact
                            AlertDialog.Builder(this)
                                .setTitle(selectedUser?.FirstName + " " + selectedUser?.LastName)
                                .setMessage("Are you sure to delete this contact?")
                                .setPositiveButton(
                                    "Delete"
                                ) { _, _ ->
                                    val ref =
                                        Firebase(StaticInfo.EndPoint + "/friends/" + user?.Email + "/" + selectedUser?.Email)
                                    ref.removeValue()
                                    // delete from local database
                                    db?.deleteFriendByEmailFromLocalDB(selectedUser?.Email.toString())
                                    Toast.makeText(
                                        this,
                                        "Contact deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    userFriednList = db?.userFriendList
                                    val adp = FriendListAdapter(this, userFriednList!!)
                                    lvFriendlist.adapter = adp
                                }
                                .setNegativeButton(android.R.string.no, null)
                                .show()
                        }
                    }

                    builder.setNegativeButton(
                        "Cancel"
                    ) { dialog, _ -> dialog.cancel() }
                    builder.show()
                    true
                }
        }

        // set online status
        user = LocalUserService.getLocalUserFromPreferences(this)
        if (user?.Email != null) {
            if (refUser == null) {
                refUser = Firebase(StaticInfo.UsersURL + "/" + user?.Email)
            }
        }
        if (refUser != null)
            refUser?.child("Status")?.setValue("Online")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("SimpleDateFormat")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_logout) {
            AlertDialog.Builder(this)
                .setTitle("Logout?")
                .setMessage("Are you sure to logout, you will no longer receive notifications.")
                .setPositiveButton("Logout") { dialog, which ->
                    // set last seen
                    val dateFormat = SimpleDateFormat("dd MM yy hh:mm a")
                    val date = Date()
                    refUser?.child("Status")?.setValue(dateFormat.format(date))
                    if (LocalUserService.deleteLocalUserFromPreferences(applicationContext)) {
                        db?.deleteAllFriendsFromLocalDB()
                        // stopService(new Intent(getApplicationContext(), AppService.class));
                        Toast.makeText(applicationContext, "Logout Success", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(applicationContext, Login::class.java)
                        startActivityForResult(intent, 100)
                    } else {
                        Toast.makeText(applicationContext, "Logout Success", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(applicationContext, Login::class.java)
                        startActivityForResult(intent, 100)
                    }
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
            return true
        }
        if (id == R.id.menu_profile) {
            startActivity(Intent(this, Profile::class.java))

        }

        if (id == R.id.menu_addContacts) {
            startActivity(Intent(this, AddContact::class.java))
            return true
        }

        if (id == R.id.menu_notification) {
            startActivity(Intent(this, Notifications::class.java))
            return true
        }

        if (id == R.id.menu_refresh) {

            if (Tools.isNetworkAvailable(this)) {

                val t = FriendListTask()
                t.execute()
            } else {
                Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onDestroy() {
        super.onDestroy()
        // set last seen
        val dateFormat = SimpleDateFormat("dd MM yy hh:mm a")
        val date = Date()
        refUser?.child("Status")?.setValue(dateFormat.format(date))
    }

    class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "CHATS"
                1 -> return "CONTACTS"
            }
            return null
        }
    }

    @Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")
    class PlaceholderFragment : Fragment() {
        private var rootView: View? = null
        private var lvLastChatList: ListView? = null
        private var db: DataContext? = null
        private var user: User? = null
        private var userFriendList: List<User>? = null
        private var userLastChatList: List<Message>? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            if (user == null) {
                user = LocalUserService.getLocalUserFromPreferences(activity!!)
            }
            db = DataContext(activity!!, null)
            // Chat tab
            if (arguments?.getInt(ARG_SECTION_NUMBER) === 1) {
                rootView = inflater.inflate(R.layout.fragment_chat, container, false)
                userLastChatList = db!!.getUserLastChatList(user?.Email.toString())

                val adp = AdapterLastChat(activity!!, userLastChatList!!)

                lvLastChatList = rootView!!.findViewById(R.id.lastChatList) as ListView
                lvLastChatList!!.adapter = adp
                lvLastChatList!!.onItemClickListener =
                    AdapterView.OnItemClickListener { _, view, _, _ ->
                        val email = view.findViewById(R.id.tv_lastChat_HiddenEmail) as TextView
                        val tvName = view.findViewById(R.id.tv_lastChat_FriendFullName) as TextView
                        val intend = Intent(activity, Chat::class.java)
                        intend.putExtra("FriendEmail", email.text.toString())
                        intend.putExtra("FriendFullName", tvName.text.toString())
                        startActivity(intend)
                    }

                lvLastChatList!!.onItemLongClickListener =
                    AdapterView.OnItemLongClickListener { _, _, position, _ ->
                        if (userLastChatList!!.size <= position) return@OnItemLongClickListener false
                        val selectedMessageItem = userLastChatList!![position]
                        val options = arrayOf<CharSequence>("Delete Chat")
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(selectedMessageItem.FriendFullName)
                        builder.setItems(
                            options
                        ) { _, index ->
                            // the user clicked on list[index]
                            if (index == 0) {
                                // Delete Chat
                                AlertDialog.Builder(activity)
                                    .setTitle(selectedMessageItem.FriendFullName)
                                    .setMessage("Are you sure to delete this chat?")
                                    .setPositiveButton(
                                        "Delete"
                                    ) { _, _ ->
                                        db!!.deleteChat(user?.Email.toString(), selectedMessageItem.FromMail.toString())
                                        Toast.makeText(
                                            activity,
                                            "Chat deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        userLastChatList = db!!.getUserLastChatList(user?.Email.toString())
                                        @Suppress("NAME_SHADOWING") val adp = AdapterLastChat(activity!!, userLastChatList!!)
                                        lvLastChatList =
                                            rootView!!.findViewById(R.id.lastChatList) as ListView
                                        lvLastChatList!!.adapter = adp
                                    }
                                    .setNegativeButton(android.R.string.no, null)
                                    .show()
                            }
                        }

                        builder.setNegativeButton(
                            "Cancel"
                        ) { dialog, _ -> dialog.cancel() }

                        builder.show()

                        true
                    }

                return rootView
            } else {
                rootView = inflater.inflate(R.layout.fragment_contact, container, false)
                userFriendList = db!!.userFriendList
                val adp = FriendListAdapter(activity!!, userFriendList!!)
                val lvFriendlist = rootView!!.findViewById(R.id.lv_FriendList) as ListView
                lvFriendlist.adapter = adp
                lvFriendlist.onItemClickListener =
                    AdapterView.OnItemClickListener { _, view, _, _ ->
                        val email = view.findViewById(R.id.tv_HiddenEmail) as TextView
                        val tvName = view.findViewById(R.id.tv_FriendFullName) as TextView
                        val intend = Intent(activity, Chat::class.java)
                        intend.putExtra("FriendEmail", email.text.toString())
                        intend.putExtra("FriendFullName", tvName.text.toString())
                        startActivity(intend)
                    }

                lvFriendlist.onItemLongClickListener =
                    AdapterView.OnItemLongClickListener { _, _, position, _ ->
                        if (userFriendList!!.size <= position) return@OnItemLongClickListener false
                        val selectedUser = userFriendList!![position]
                        val options = arrayOf<CharSequence>("Profile", "Delete Contact")
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(selectedUser.FirstName + " " + selectedUser.LastName)
                        builder.setItems(
                            options
                        ) { _, index ->
                            // the user clicked on list[index]
                            if (index == 0) {
                                // Profile
                                val intent = Intent(activity, FriendProfile::class.java)
                                intent.putExtra("Email", selectedUser.Email)
                                startActivityForResult(intent, StaticInfo.ChatAciviityRequestCode)
                            } else {
                                // Delete Contact
                                AlertDialog.Builder(activity)
                                    .setTitle(selectedUser.FirstName + " " + selectedUser.LastName)
                                    .setMessage("Are you sure to delete this contact?")
                                    .setPositiveButton(
                                        "Delete"
                                    ) { _, _ ->
                                        val ref =
                                            Firebase(StaticInfo.EndPoint + "/friends/" + user!!.Email + "/" + selectedUser.Email)
                                        ref.removeValue()
                                        // delete from local database
                                        db!!.deleteFriendByEmailFromLocalDB(selectedUser.Email.toString())
                                        Toast.makeText(
                                            activity,
                                            "Contact deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        userFriendList = db!!.userFriendList
                                        @Suppress("NAME_SHADOWING") val adp = FriendListAdapter(activity!!, userFriendList!!)
                                        val lvFriendList =
                                            rootView!!.findViewById(R.id.lv_FriendList) as ListView
                                        lvFriendList.adapter = adp
                                    }
                                    .setNegativeButton(android.R.string.no, null)
                                    .show()
                            }
                        }

                        builder.setNegativeButton(
                            "Cancel"
                        ) { dialog, _ -> dialog.cancel() }

                        builder.show()


                        true
                    }
                return rootView
            }// Contacts tab
        }



        companion object {

            private const val ARG_SECTION_NUMBER = "section_number"


            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class FriendListTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {

            pd?.show()
        }

        override fun doInBackground(vararg params: Void): String? {
            user = LocalUserService.getLocalUserFromPreferences(applicationContext)
            val api = Tools.makeRetroFitApi()
            val call =
                api.getUserFriendsListAsJsonString(StaticInfo.FriendsURL + "/" + user?.Email.toString() + ".json")
            try {
                return call.execute().body()
            } catch (e: IOException) {
                e.printStackTrace()
                pd?.hide()
            }

            return null
        }

        override fun onPostExecute(jsonListString: String) {

            try {
                user = LocalUserService.getLocalUserFromPreferences(applicationContext)
                val friendList = ArrayList<User>()
                val userFriendTree = JSONObject(jsonListString)
                val iterator = userFriendTree.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next() as String
                    val friend = User()
                    val friendJson = userFriendTree.getJSONObject(key)
                    friend.Email = friendJson.getString("Email")
                    friend.FirstName = friendJson.getString("FirstName")
                    friend.LastName = friendJson.getString("LastName")
                    friendList.add(friend)
                }

                // refresh local database
                db = DataContext(applicationContext, null)
                db?.refreshUserFriendList(friendList)

                // set to adapter
                val adp = FriendListAdapter(applicationContext, db!!.userFriendList)
                lv_FriendList.adapter = adp
                pd?.hide()
                Toast.makeText(this@MainActivity, "Contact list is updated", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: JSONException) {
                pd?.hide()
                e.printStackTrace()
            }

        }
    }


}