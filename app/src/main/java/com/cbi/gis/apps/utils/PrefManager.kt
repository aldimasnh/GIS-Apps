package com.cbi.gis.apps.utils

import android.content.Context
import android.content.SharedPreferences

class PrefManager(_context: Context) {
    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor

    // shared pref mode
    private var privateMode = 0

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    var lastUpdate: String?
        get() = pref.getString(LASTUPDATE, "")
        set(update) {
            editor.putString(LASTUPDATE, update)
            editor.commit()
        }

    var session: Boolean
        get() = pref.getBoolean(SESSION, false)
        set(sessionActive) {
            editor.putBoolean(SESSION, sessionActive)
            editor.commit()
        }

    var userid: String?
        get() = pref.getString(USERID, "")
        set(userId) {
            editor.putString(USERID, userId)
            editor.commit()
        }

    var userno: String?
        get() = pref.getString(USERNO, "")
        set(userNo) {
            editor.putString(USERNO, userNo)
            editor.commit()
        }

    var name: String?
        get() = pref.getString(NAME, "")
        set(sureName) {
            editor.putString(NAME, sureName)
            editor.commit()
        }

    var username: String?
        get() = pref.getString(USERNAME, "")
        set(username) {
            editor.putString(USERNAME, username)
            editor.commit()
        }

    var email: String?
        get() = pref.getString(EMAIL, "")
        set(mail) {
            editor.putString(EMAIL, mail)
            editor.commit()
        }

    var password: String?
        get() = pref.getString(PASSWORD, "")
        set(pass) {
            editor.putString(PASSWORD, pass)
            editor.commit()
        }

    var id_jabatan: Int
        get() = pref.getInt(POSID, 0)
        set(posId) {
            editor.putInt(POSID, posId)
            editor.commit()
        }

    var remember: Boolean
        get() = pref.getBoolean(REMEMBERME, false)
        set(rememberMe) {
            editor.putBoolean(REMEMBERME, rememberMe)
            editor.commit()
        }

    var md5Job: String?
        get() = pref.getString(HEXJOB, "")
        set(md5Job) {
            editor.putString(HEXJOB, md5Job)
            editor.commit()
        }

    var md5Unit: String?
        get() = pref.getString(HEXUNIT, "")
        set(md5Unit) {
            editor.putString(HEXUNIT, md5Unit)
            editor.commit()
        }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "gis_apps"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val SESSION = "Session"

        const val REMEMBERME = "remember_me"
        const val LASTUPDATE = "last_update"
        const val USERID = "user_id"
        const val USERNO = "no_user"
        const val NAME = "nama"
        const val USERNAME = "username"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val POSID = "jabatan_id"
        const val HEXJOB = "md5job"
        const val HEXUNIT = "md5unit"
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, privateMode)
        editor = pref.edit()
    }
}