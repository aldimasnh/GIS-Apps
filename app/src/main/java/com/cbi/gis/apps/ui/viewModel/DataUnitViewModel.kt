package com.cbi.gis.apps.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cbi.gis.apps.data.model.DataUnitModel
import com.cbi.gis.apps.data.repository.DataUnitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataUnitViewModel(application: Application, private val dataUnitRepo: DataUnitRepository) : AndroidViewModel(application) {

    private val _insertionResult = MutableLiveData<Boolean>()
    val insertionResult: LiveData<Boolean> get() = _insertionResult
    private val _dataUnitList = MutableLiveData<List<DataUnitModel>>()
    val dataUnitList: LiveData<List<DataUnitModel>> get() = _dataUnitList

    fun insertDataUnit(
        id: Int,
        nama: String
    ) {
        viewModelScope.launch {
            try {
                val dataUnit = DataUnitModel(
                    id,
                    nama
                )
                val isInserted = dataUnitRepo.insertDataUnit(dataUnit)
                _insertionResult.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertionResult.value = false
            }
        }
    }

    fun deleteDataUnit() {
        viewModelScope.launch {
            dataUnitRepo.deleteDataUnit()
        }
    }

    fun loadDataUnit() {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                dataUnitRepo.getAllDataUnit()
            }
            _dataUnitList.value = dataUnit
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val dataUnitRepo: DataUnitRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DataUnitViewModel::class.java)) {
                return DataUnitViewModel(application, dataUnitRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}