package com.cbi.gis.apps.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.cbi.gis.apps.data.database.DatabaseHelper
import com.cbi.gis.apps.data.model.DataJobTypeModel

class DataJobTypeRepository(context: Context) {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)

    fun insertDataJobType(dataJob: DataJobTypeModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataJob.id)
            put(DatabaseHelper.DB_NAMA, dataJob.nama)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_JOB_TYPE, null, values)
        db.close()

        return rowsAffected > 0
    }

    fun deleteDataJobType() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_JOB_TYPE, null, null)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllDataJobType(): List<DataJobTypeModel> {
        val dataJobTypeList = mutableListOf<DataJobTypeModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_JOB_TYPE}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val nama = it.getString(it.getColumnIndex("nama"))

                val dataJobType = DataJobTypeModel(
                    id,
                    nama
                )

                dataJobTypeList.add(dataJobType)
            }
        }

        db.close()

        return dataJobTypeList
    }
}