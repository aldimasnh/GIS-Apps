package com.cbi.gis.apps.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "db_gis_apps"
        const val DATABASE_VERSION = 1

        const val DB_TAB_JOB_TYPE = "tbl_job_type"
        const val DB_TAB_UNIT = "tbl_unit"
        const val DB_TAB_DAILY = "tbl_daily_report"

        const val DB_ID = "id"
        const val DB_NAMA = "nama"
        const val DB_NODAILY = "no_dailyreport"
        const val DB_IDJNSDR = "id_jnsdr"
        const val DB_IDUNIT = "id_satuan"
        const val DB_DATE = "tanggal"
        const val DB_TARGET = "target"
        const val DB_PROGRESS = "progress"
        const val DB_KETERANGAN = "keterangan"
        const val DB_ARCHIVE = "archive"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableJobType = "CREATE TABLE IF NOT EXISTS $DB_TAB_JOB_TYPE (" +
                "$DB_ID INTEGER PRIMARY KEY, " +
                "$DB_NAMA VARCHAR)"

        val createTableUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_UNIT (" +
                "$DB_ID INTEGER PRIMARY KEY, " +
                "$DB_NAMA VARCHAR)"

        val createTableReport = "CREATE TABLE IF NOT EXISTS $DB_TAB_DAILY (" +
                "$DB_ID INTEGER PRIMARY KEY, " +
                "$DB_NODAILY VARCHAR, " +
                "$DB_IDJNSDR INTEGER, " +
                "$DB_IDUNIT INTEGER, " +
                "$DB_DATE VARCHAR, " +
                "$DB_TARGET INTEGER, " +
                "$DB_PROGRESS INTEGER, " +
                "$DB_KETERANGAN VARCHAR, " +
                "$DB_ARCHIVE INTEGER)"

        db.execSQL(createTableJobType)
        db.execSQL(createTableUnit)
        db.execSQL(createTableReport)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Gunakan kalau sudah dipakai user / rilis playstore
    }

}