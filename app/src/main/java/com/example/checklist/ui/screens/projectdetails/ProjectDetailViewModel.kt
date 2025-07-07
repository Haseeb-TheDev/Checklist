package com.example.checklist.ui.screens.projectdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checklist.data.model.Project
import com.example.checklist.data.model.ProjectWithSteps
import com.example.checklist.data.model.Step
import com.example.checklist.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    private val repository: ChecklistRepository,
    private val projectId: Long
) : ViewModel() {

    // Holds the current project with its steps
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project

    // Flag used to trigger navigation when a project is deleted
    private val _isProjectDeleted = MutableStateFlow(false)
    val isProjectDeleted: StateFlow<Boolean> = _isProjectDeleted

    init {
        loadProject()
    }

    // Loads the project and its steps from the repository
    private fun loadProject() {
        viewModelScope.launch {
            val projectWithSteps = repository.getProjectWithStepsById(projectId).first()
            _project.value = projectWithSteps?.toDomainModel()
        }
    }

    // Saves the current project as a template
    fun saveProjectAsTemplate() {
        viewModelScope.launch {
            val projectWithSteps = repository.getProjectWithStepsById(projectId).first() ?: return@launch
            repository.saveTemplateProjectWithSteps(projectWithSteps)
        }
    }

    // Deletes a single step and refreshes project state
    fun deleteStep(stepId: Long) {
        viewModelScope.launch {
            repository.deleteStep(stepId)
            loadProject() // refresh after deletion
        }
    }

    // Deletes the entire project and marks it as deleted
    fun deleteProject() {
        viewModelScope.launch {
            repository.deleteProjectWithSteps(projectId)
            _isProjectDeleted.value = true
        }
    }

}

// Factory class for providing dependencies
class ProjectDetailViewModelFactory(
    private val repository: ChecklistRepository,
    private val projectId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProjectDetailViewModel(repository, projectId) as T
    }
}

// Extension function to convert ProjectWithSteps to domain model Project
fun ProjectWithSteps.toDomainModel(): Project {
    return Project(
        name = project.name,
        description = project.description,
        steps = steps.map { Step(it.stepId, it.name, it.description) },
        projectId = project.projectId
    )
}

