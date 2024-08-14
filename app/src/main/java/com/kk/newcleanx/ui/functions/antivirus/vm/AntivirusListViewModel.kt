package com.kk.newcleanx.ui.functions.antivirus.vm

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.newcleanx.R
import com.kk.newcleanx.data.local.VirusBean
import com.kk.newcleanx.data.local.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class AntivirusListViewModel : ViewModel() {

    val deleteObserver = MutableLiveData<String>()
    fun deleteFile(data: VirusBean) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            runCatching {
                val result = File(data.path).deleteRecursively()
                if (result) {
                    deleteObserver.postValue(data.path)
                } else Toast.makeText(app, app.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

}