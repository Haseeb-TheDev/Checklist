package com.example.checklist.data.repository

import com.example.checklist.data.dao.ChecklistDao
import com.example.checklist.data.model.ProjectEntity
import com.example.checklist.data.model.ProjectWithSteps
import com.example.checklist.data.model.Step
import com.example.checklist.data.model.StepEntity
import kotlinx.coroutines.flow.Flow

class ChecklistRepository(private val dao: ChecklistDao) {

    // --- Project operations ---

    // Add a new project
    suspend fun insertProject(project: ProjectEntity): Long = dao.insertProject(project)

    // Delete a project (by entity)
    suspend fun deleteProject(project: ProjectEntity) = dao.deleteProject(project)

    // Get all projects
    fun getAllProjects(): Flow<List<ProjectEntity>> = dao.getAllProjects()

    // Get single project by ID
    suspend fun getProjectById(projectId: Long): ProjectEntity? = dao.getProjectById(projectId)


    // --- Step operations ---

    // Add multiple steps
    suspend fun insertSteps(steps: List<StepEntity>) = dao.insertSteps(steps)

    // Delete all steps under a project
    suspend fun deleteStepsByProjectId(projectId: Long) = dao.deleteStepsByProjectId(projectId)

    // Get all steps for a project
    fun getStepsForProject(projectId: Long): Flow<List<StepEntity>> = dao.getStepsForProject(projectId)

    // Delete a single step by ID
    suspend fun deleteStep(stepId: Long) {
        dao.deleteStep(stepId)
    }


    // --- Template-related ---

    // Get all templates
    fun getTemplateProjects() = dao.getAllTemplates()

    // Delete a template using its name
    suspend fun deleteTemplateByName(name: String) {
        dao.deleteTemplateByName(name)
    }

    // Get project + steps together
    suspend fun getProjectWithStepsById(projectId: Long): Flow<ProjectWithSteps?> {
        return dao.getProjectWithStepsById(projectId)
    }

    // Delete project by ID (steps not auto-deleted in DAO)
    suspend fun deleteProjectWithSteps(projectId: Long) {
        dao.deleteProjectWithStepsById(projectId)
    }

    // Clone project and steps as template
    suspend fun saveTemplateProjectWithSteps(projectWithSteps: ProjectWithSteps) {
        val clonedProject = ProjectEntity(
            name = projectWithSteps.project.name,
            description = projectWithSteps.project.description,
            isTemplate = true
        )
        val newProjectId = dao.insertProject(clonedProject)

        val clonedSteps = projectWithSteps.steps.map {
            it.copy(stepId = 0, projectOwnerId = newProjectId)
        }
        dao.insertSteps(clonedSteps)
    }


    // Create a project and steps in one go
    suspend fun insertProjectWithSteps(name: String, description: String, steps: List<Step>, isTemplate: Boolean = false) {
        val projectEntity = ProjectEntity(name = name, description = description, isTemplate = isTemplate)
        val projectId = dao.insertProject(projectEntity)

        val stepEntities = steps.map {
            StepEntity(
                name = it.name,
                description = it.description,
                projectOwnerId = projectId
            )
        }

        dao.insertSteps(stepEntities)
    }


    // Update entire project and replace its steps
    suspend fun updateProjectWithSteps(
        projectId: Long,
        name: String,
        description: String,
        steps: List<Step>,
        isTemplate: Boolean = false
    ) {
        val updatedProject = ProjectEntity(
            projectId = projectId,
            name = name,
            description = description,
            isTemplate = isTemplate
        )

        dao.updateProject(updatedProject)
        dao.deleteStepsByProjectId(projectId)

        val newSteps = steps.map {
            StepEntity(
                name = it.name,
                description = it.description,
                projectOwnerId = projectId
            )
        }

        dao.insertSteps(newSteps)
    }
}
