package com.example.checklist.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.data.datastore.SettingsDatastore

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val settingsDataStore = SettingsDatastore(context.applicationContext)
        return SettingsViewModel(settingsDataStore) as T
    }
}