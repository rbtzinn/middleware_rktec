package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RfidViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RfidViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RfidViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
