package com.cbi.gis.apps.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.cbi.gis.apps.data.database.DatabaseHelper
import com.cbi.gis.apps.data.model.DataDailyModel

@Suppress("NAME_SHADOWING")
class DailyReportRepository(context: Context) {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)

    fun insertDailyReport(dailyMod: DataDailyModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_NODAILY, dailyMod.no_daily)
            put(DatabaseHelper.DB_IDJNSDR, dailyMod.id_jnsdr)
            put(DatabaseHelper.DB_IDUNIT, dailyMod.id_unit)
            put(DatabaseHelper.DB_DATE, dailyMod.date)
            put(DatabaseHelper.DB_TARGET, dailyMod.target)
            put(DatabaseHelper.DB_PROGRESS, dailyMod.progress)
            put(DatabaseHelper.DB_KETERANGAN, dailyMod.keterangan)
            put(DatabaseHelper.DB_ARCHIVE, dailyMod.archive)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_DAILY, null, values)
        db.close()

        return rowsAffected > 0
    }

    fun updateArchiveDailyReport(id: String): Boolean {
        val db = databaseHelper.writableDatabase

        val values = ContentValues()
        values.put(DatabaseHelper.DB_ARCHIVE, 1)

        val rowsAffected = db.update(DatabaseHelper.DB_TAB_DAILY, values, "id=?", arrayOf(id))
        db.close()

        return rowsAffected > 0
    }

    fun getCountDailyReport(archive: Int? = 0, date: String? = ""): Int {
        val db = databaseHelper.readableDatabase
        val query = if (date!!.isNotEmpty()) {
            "SELECT COUNT(*) FROM ${DatabaseHelper.DB_TAB_DAILY} WHERE ${DatabaseHelper.DB_DATE} LIKE '%$date%'"
        } else {
            "SELECT COUNT(*) FROM ${DatabaseHelper.DB_TAB_DAILY} WHERE ${DatabaseHelper.DB_ARCHIVE} = '$archive'"
        }
        val cursor = db.rawQuery(query, null)

        var count = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
        }

        db.close()
        return count
    }

    @SuppressLint("Range")
    fun getAllDailyReport(archive: Int? = 0): List<DataDailyModel> {
        val dataDailyReportList = mutableListOf<DataDailyModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.DB_TAB_DAILY} WHERE ${DatabaseHelper.DB_ARCHIVE} = '$archive' ORDER BY ${DatabaseHelper.DB_DATE} DESC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val no_daily = it.getString(it.getColumnIndex("no_dailyreport"))
                val id_jnsdr = it.getInt(it.getColumnIndex("id_jnsdr"))
                val id_unit = it.getInt(it.getColumnIndex("id_satuan"))
                val date = it.getString(it.getColumnIndex("tanggal"))
                val target = it.getInt(it.getColumnIndex("target"))
                val progress = it.getInt(it.getColumnIndex("progress"))
                val keterangan = it.getString(it.getColumnIndex("keterangan"))
                val archive = it.getInt(it.getColumnIndex("archive"))

                val dataDailyReport = DataDailyModel(
                    id,
                    no_daily,
                    id_jnsdr,
                    id_unit,
                    date,
                    target,
                    progress,
                    keterangan,
                    archive
                )

                dataDailyReportList.add(dataDailyReport)
            }
        }

        /*db.close()*/

        return dataDailyReportList
    }

    fun deleteItem(id: String): Boolean {
        val db = databaseHelper.writableDatabase
        val rowsAffected = db.delete(DatabaseHelper.DB_TAB_DAILY, "id=?", arrayOf(id))
        db.close()

        return rowsAffected > 0
    }

    fun deleteAllDataDaily() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_DAILY, null, null)
        db.close()
    }
}