package com.example.checklist.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.checklist.data.model.ProjectEntity
import com.example.checklist.data.model.ProjectWithSteps
import com.example.checklist.data.model.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    // Add or replace a project
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    // Add or replace a single step
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: StepEntity): Long

    // Add or replace multiple steps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    // Update a project
    @Update
    suspend fun updateProject(project: ProjectEntity)

    // Update a step
    @Update
    suspend fun updateStep(step: StepEntity)

    // Delete a project
    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Delete all steps linked to a project
    @Query("DELETE FROM steps WHERE projectOwnerId = :projectId")
    suspend fun deleteStepsByProjectId(projectId: Long)

    // Get all projects (latest first)
    @Query("SELECT * FROM projects ORDER BY projectId DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    // Get steps for a project
    @Query("SELECT * FROM steps WHERE projectOwnerId = :projectId ORDER BY stepId ASC")
    fun getStepsForProject(projectId: Long): Flow<List<StepEntity>>

    // Get a project by ID
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectEntity?

    // Get a step by ID
    @Query("SELECT * FROM steps WHERE stepId = :stepId")
    suspend fun getStepById(stepId: Long): StepEntity?

    // Get all templates
    @Query("SELECT * FROM projects WHERE isTemplate = 1")
    fun getAllTemplates(): Flow<List<ProjectEntity>>

    // Delete a template by name
    @Query("DELETE FROM projects WHERE isTemplate = 1 AND name = :name")
    suspend fun deleteTemplateByName(name: String)

    // Get project and its steps
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectWithStepsById(projectId: Long): Flow<ProjectWithSteps?>

    // Delete a project by ID
    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProjectWithStepsById(projectId: Long)

    // Delete a single step
    @Query("DELETE FROM steps WHERE stepId = :stepId")
    suspend fun deleteStep(stepId: Long)
}
