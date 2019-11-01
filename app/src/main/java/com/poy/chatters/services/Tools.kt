package com.poy.chatters.services

import android.content.Context
import android.net.ConnectivityManager
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object Tools {

    val ENDPOINT = "https://mychatapp-e4cb9.firebaseio.com"

    fun encodeString(string: String): String {
        return string.replace(".", ",")
    }

    fun decodeString(string: String): String {
        return string.replace(",", ".")
    }

    fun makeRetroFitApi(): IFireBaseAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(ENDPOINT)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(IFireBaseAPI::class.java)
    }

    fun toProperName(s: String): String {
        return if (s.length <= 1) s.toUpperCase() else s.substring(
            0,
            1
        ).toUpperCase() + s.substring(1).toLowerCase()
    }

    fun createUniqueIdPerUser(userEmail: String): Int {
        val email =
            userEmail.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toLowerCase()
                .replace(
                    "[^a-zA-Z0-9]".toRegex(), ""
                )
        val map: MutableMap<Char, Int>
        map = HashMap()
        map['a'] = 1
        map['b'] = 2
        map['c'] = 3
        map['d'] = 4
        map['e'] = 5
        map['f'] = 6
        map['g'] = 7
        map['h'] = 8
        map['i'] = 9
        map['j'] = 10
        map['k'] = 11
        map['l'] = 12
        map['m'] = 13
        map['n'] = 14
        map['o'] = 15
        map['p'] = 16
        map['q'] = 17
        map['r'] = 18
        map['s'] = 19
        map['t'] = 20
        map['u'] = 21
        map['v'] = 22
        map['w'] = 23
        map['x'] = 24
        map['y'] = 25
        map['z'] = 26
        var intEmail = ""

        for (c in email.toCharArray()) {
            var `val` = 0
            try {
                `val` = map[c]!!
            } catch (e: Exception) {

            }

            intEmail += `val`
        }

        if (intEmail.length > 9) {
            intEmail = intEmail.substring(0, 9)
        }

        return Integer.parseInt(intEmail)

    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-zA-Z0-9._-]+"
        return email.matches(emailPattern.toRegex())
    }

    fun toCharacterMonth(month: Int): String {
        return if (month == 1)
            "Jan"
        else if (month == 2)
            "Feb"
        else if (month == 3)
            "Mar"
        else if (month == 4)
            "Apr"
        else if (month == 5)
            "May"
        else if (month == 6)
            "Jun"
        else if (month == 7)
            "Jul"
        else if (month == 8)
            "Aug"
        else if (month == 9)
            "Sep"
        else if (month == 10)
            "Oct"
        else if (month == 11)
            "Nov"
        else
            "Dec"
    }

    fun lastSeenProper(lastSeenDate: String): String {
        val dateFormat = SimpleDateFormat("dd MM yy hh:mm a")
        val currentDate = Date()
        val cuurentDateString = dateFormat.format(currentDate)
        var nw: Date? = null
        var seen: Date? = null
        try {
            nw = dateFormat.parse(cuurentDateString)
            seen = dateFormat.parse(lastSeenDate)
            val diff = nw!!.time - seen!!.time
            val diffDays = diff / (24 * 60 * 60 * 1000)
            val diffHours = diff / (60 * 60 * 1000) % 24
            val diffMinutes = diff / (60 * 1000) % 60
            if (diffDays > 0) {
                val originalDate =
                    lastSeenDate.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return "Last seen " + originalDate[0] + " " + Tools.toCharacterMonth(
                    Integer.parseInt(
                        originalDate[1]
                    )
                ) + " " + originalDate[2]
            } else return if (diffHours > 0)
                "Last seen $diffHours hours ago"
            else if (diffMinutes > 0) {
                if (diffMinutes <= 1) {
                    "Last seen 1 minute ago"
                } else {
                    "Last seen $diffMinutes minutes ago"
                }
            } else
                "Last seen a moment ago"
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }


    }

    fun messageSentDateProper(sentDate: String): String {
        var properDate = ""
        val cal = Calendar.getInstance()
        val todayDate = Date()
        cal.time = todayDate
        val date = sentDate.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val todayMonth = cal.get(Calendar.MONTH) + 1
        val todayDay = cal.get(Calendar.DAY_OF_MONTH)
        if (todayMonth == Integer.parseInt(date[1]) && todayDay == Integer.parseInt(date[0])) {
            properDate = "Today" + " " + date[3] + " " + date[4]
            // 06 11 17 12:28 AM
        } else if (todayMonth == Integer.parseInt(date[1]) && todayDay - 1 == Integer.parseInt(date[0])) {
            properDate = "Yesterday" + " " + date[3] + " " + date[4]
        } else {
            properDate =
                date[0] + " " + Tools.toCharacterMonth(Integer.parseInt(date[1])) + " " + date[2] + " " + date[3] + " " + date[4]
        }
        return properDate
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
