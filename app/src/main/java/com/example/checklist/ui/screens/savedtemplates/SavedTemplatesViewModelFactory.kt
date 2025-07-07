package com.example.checklist.ui.screens.savedtemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.data.repository.ChecklistRepository

class SavedTemplatesViewModelFactory(
    private val repository: ChecklistRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedTemplatesViewModel::class.java)) {
            return SavedTemplatesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
