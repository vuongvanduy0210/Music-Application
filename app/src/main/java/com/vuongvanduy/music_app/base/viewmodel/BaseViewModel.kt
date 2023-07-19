package com.vuongvanduy.music_app.base.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job

open class BaseViewModel : ViewModel() {

    var job: Job? = null

    var isLoadingOnline =MutableLiveData(true)
        private set
    var isLoadingFavourite =MutableLiveData(true)
        private set
    var isLoadingDevice =MutableLiveData(true)
        private set

    var exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("Duy", "Exception ${throwable.message}")
    }
}