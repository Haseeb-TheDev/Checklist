package com.example.checklist.ui.screens.editproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checklist.data.model.Step
import com.example.checklist.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectEditViewModel(
    private val repository: ChecklistRepository,
    private val projectId: Long? = null,
    private val isTemplateCopy: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectEditUiState())
    val uiState: StateFlow<ProjectEditUiState> = _uiState

    init {
        // Load existing project and its steps, if editing or using a template
        projectId?.let { id ->
            viewModelScope.launch {
                val project = repository.getProjectById(id)
                val stepsFlow = repository.getStepsForProject(id)

                project?.let { nonNullProject ->
                    stepsFlow.collect { stepEntities ->
                        _uiState.value = _uiState.value.copy(
                            projectName = nonNullProject.name,
                            projectDesc = nonNullProject.description,
                            steps = stepEntities.map { Step(it.stepId, it.name, it.description) }
                        )
                    }

                    // If this is a template copy, strip IDs so they aren't reused
                    if (isTemplateCopy) {
                        _uiState.value = _uiState.value.copy(
                            steps = _uiState.value.steps.map { it.copy(stepId = 0L) }
                        )
                    }
                }
            }
        }
    }

    // Project field updates
    fun updateProjectName(name: String) {
        _uiState.value = _uiState.value.copy(projectName = name)
    }

    fun updateProjectDesc(desc: String) {
        _uiState.value = _uiState.value.copy(projectDesc = desc)
    }

    // Step field updates
    fun updateStepName(name: String) {
        _uiState.value = _uiState.value.copy(stepName = name)
    }

    fun updateStepDesc(desc: String) {
        _uiState.value = _uiState.value.copy(stepDesc = desc)
    }

    // Adds a new step
    fun addStep() {
        val newStep = Step(stepId = 0L,_uiState.value.stepName, _uiState.value.stepDesc)
        _uiState.value = _uiState.value.copy(
            steps = _uiState.value.steps + newStep,
            stepName = "",
            stepDesc = "",
            showStepDescField = false
        )
    }

    // Saves the full project (either inserts or updates)
    fun saveProject(onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = _uiState.value
            if (projectId != null) {
                // Update existing project
                repository.updateProjectWithSteps(
                    projectId = projectId,
                    name = current.projectName,
                    description = current.projectDesc,
                    steps = current.steps
                )
            } else {
                // Insert new project
                repository.insertProjectWithSteps(
                    name = current.projectName,
                    description = current.projectDesc,
                    steps = current.steps
                )
            }
            onComplete()
        }
    }

    // Step list operations
    fun updateStep(index: Int, newName: String, newDesc: String) {
        val updatedSteps = _uiState.value.steps.toMutableList()
        updatedSteps[index] = updatedSteps[index].copy(name = newName, description = newDesc)
        _uiState.value = _uiState.value.copy(steps = updatedSteps)
    }
    fun deleteStep(index: Int) {
        val updatedSteps = _uiState.value.steps.toMutableList()
        if (index in updatedSteps.indices) {
            updatedSteps.removeAt(index)
            _uiState.value = _uiState.value.copy(steps = updatedSteps)
        }
    }
}

// Holds all UI-related values
data class ProjectEditUiState(
    val projectName: String = "",
    val projectDesc: String = "",
    val showProjectDescField: Boolean = false,
    val stepName: String = "",
    val stepDesc: String = "",
    val showStepDescField: Boolean = false,
    val steps: List<Step> = emptyList(),
    val showStepForm: Boolean = false
)

// Factory class for providing dependencies
class ProjectEditViewModelFactory(
    private val repository: ChecklistRepository,
    private val projectId: Long? = null,
    private val isTemplateCopy: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProjectEditViewModel(repository, projectId, isTemplateCopy) as T
    }
}