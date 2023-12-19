package com.cbi.gis.apps.data.model

import androidx.room.Ignore

data class DataJobTypeModel(
    val id: Int,
    val nama: String
)

data class DataUnitModel(
    val id: Int,
    val nama: String
)

data class DataDailyModel(
    @Ignore val id: Int,
    val no_daily: String,
    val id_jnsdr: Int,
    val id_unit: Int,
    val date: String,
    val target: Int,
    val progress: Int,
    val keterangan: String,
    val archive: Int
)