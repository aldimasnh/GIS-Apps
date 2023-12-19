package com.cbi.gis.apps.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.cbi.gis.apps.data.database.DatabaseHelper
import com.cbi.gis.apps.data.model.DataUnitModel

class DataUnitRepository(context: Context) {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)

    fun insertDataUnit(dataUnit: DataUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA, dataUnit.nama)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_UNIT, null, values)
        db.close()

        return rowsAffected > 0
    }

    fun deleteDataUnit() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_UNIT, null, null)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllDataUnit(): List<DataUnitModel> {
        val dataUnitList = mutableListOf<DataUnitModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_UNIT}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val nama = it.getString(it.getColumnIndex("nama"))

                val dataUnit = DataUnitModel(
                    id,
                    nama
                )

                dataUnitList.add(dataUnit)
            }
        }

        db.close()

        return dataUnitList
    }
}