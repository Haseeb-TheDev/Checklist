package com.example.checklist.ui.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checklist.data.model.ProjectEntity
import com.example.checklist.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ChecklistRepository
) : ViewModel() {

    // List of all user projects (excluding templates)
    private val _projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val projects: StateFlow<List<ProjectEntity>> = _projects

    // Tracks currently selected tab index
    private val _selectedProjectIndex = mutableStateOf(0)
    val selectedProjectIndex: State<Int> = _selectedProjectIndex

    // Returns selected project (nullable)
    private val selectedProject: ProjectEntity?
        get() = _projects.value.getOrNull(_selectedProjectIndex.value)

    // Whether selected project is already saved as template
    private val _isTemplateSaved = mutableStateOf(false)
    val isTemplateSaved: State<Boolean> = _isTemplateSaved

    // Tracks which step descriptions are expanded
    private val _expandedSteps = mutableStateOf(setOf<Int>())
    val expandedSteps: State<Set<Int>> = _expandedSteps

    init {
        observeProjects()
    }

    // Watches all non-template projects from DB
    private fun observeProjects() {
        viewModelScope.launch {
            repository.getAllProjects()
                .map { list -> list
                    .filter { !it.isTemplate }
                    .sortedBy { it.projectId } }
                .collectLatest { list ->
                _projects.value = list
                checkIfCurrentIsSaved()
            }
        }
    }

    // Updates selected tab and re-checks template status
    fun onProjectSelected(index: Int) {
        _selectedProjectIndex.value = index
        checkIfCurrentIsSaved()
    }

    // Clones selected project as template
    fun saveSelectedProjectAsTemplate() {
        val project = selectedProject ?: return
        if (_isTemplateSaved.value) return // already saved

        viewModelScope.launch {
            val template = project.copy(
                projectId = 0L,
                name = "${project.name} (Template)",
                isTemplate = true
            )
            val newProjectId = repository.insertProject(template)
            val originalSteps = repository.getStepsForProject(project.projectId).first()
            val clonedSteps = originalSteps.map {
                it.copy(stepId = 0L, projectOwnerId = newProjectId)
            }
            repository.insertSteps(clonedSteps)
            _isTemplateSaved.value = true
        }
    }

    // Updates _isTemplateSaved state based on current project
    private fun checkIfCurrentIsSaved() {
        val project = selectedProject
        _isTemplateSaved.value = project?.isTemplate == true
    }

    // Toggles a step’s expanded state
    fun toggleStepExpanded(index: Int) {
        _expandedSteps.value = if (_expandedSteps.value.contains(index)) {
            _expandedSteps.value - index
        } else {
            _expandedSteps.value + index
        }
    }

    // Deletes current project and its steps
    fun deleteSelectedProject() {
        val project = selectedProject ?: return
        viewModelScope.launch {
            repository.deleteProject(project)
            repository.deleteStepsByProjectId(project.projectId)
            _selectedProjectIndex.value = 0 // Reset to first project
        }
    }

    // Deletes a step by ID
    fun deleteStep(stepId: Long) {
        viewModelScope.launch {
            repository.deleteStep(stepId)
        }
    }
}
