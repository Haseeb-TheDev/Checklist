package com.example.checklist.ui.screens.savedtemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checklist.data.model.ProjectHeader
import com.example.checklist.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SavedTemplatesViewModel(
    private val repository: ChecklistRepository
) : ViewModel() {

    // Holds list of template project headers
    private val _templates = MutableStateFlow<List<ProjectHeader>>(emptyList())
    val projects: StateFlow<List<ProjectHeader>> = _templates

    init {
        loadTemplates() // Start loading templates on creation
    }

    // Collect templates from repository and update state
    private fun loadTemplates() {
        viewModelScope.launch {
            repository.getTemplateProjects().collect { projectEntities ->
                _templates.value = projectEntities.map {
                    ProjectHeader(
                        projectId = it.projectId,
                        name = it.name.removeSuffix(" (Template)")
                    )
                }
            }
        }
    }

    // Delete template by project name
    fun deleteTemplate(projectName: String) {
        viewModelScope.launch {
            repository.deleteTemplateByName(projectName)
        }
    }
}
