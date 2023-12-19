package com.cbi.gis.apps.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cbi.gis.apps.data.model.DataJobTypeModel
import com.cbi.gis.apps.data.repository.DataJobTypeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataJobTypeViewModel(application: Application, private val dataJobTypeRepo: DataJobTypeRepository) : AndroidViewModel(application) {

    private val _insertionResult = MutableLiveData<Boolean>()
    val insertionResult: LiveData<Boolean> get() = _insertionResult
    private val _dataJobTypeList = MutableLiveData<List<DataJobTypeModel>>()
    val dataJobTypeList: LiveData<List<DataJobTypeModel>> get() = _dataJobTypeList

    fun insertDataJobType(
        id: Int,
        nama: String
    ) {
        viewModelScope.launch {
            try {
                val dataJobType = DataJobTypeModel(
                    id,
                    nama
                )
                val isInserted = dataJobTypeRepo.insertDataJobType(dataJobType)
                _insertionResult.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertionResult.value = false
            }
        }
    }

    fun deleteDataJobType() {
        viewModelScope.launch {
            dataJobTypeRepo.deleteDataJobType()
        }
    }

    fun loadDataJobType() {
        viewModelScope.launch {
            val dataJobType = withContext(Dispatchers.IO) {
                dataJobTypeRepo.getAllDataJobType()
            }
            _dataJobTypeList.value = dataJobType
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val dataJobTypeRepo: DataJobTypeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DataJobTypeViewModel::class.java)) {
                return DataJobTypeViewModel(application, dataJobTypeRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}