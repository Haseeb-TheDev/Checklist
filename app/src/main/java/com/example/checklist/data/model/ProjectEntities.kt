package com.example.checklist.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

// Room entity for projects
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val projectId: Long = 0L,
    val name: String,
    val description: String,
    val isTemplate: Boolean = false
)

// Room entity for steps
@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val stepId: Long = 0L,
    val name: String,
    val description: String,
    val projectOwnerId: Long
)

data class ProjectWithSteps(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectOwnerId"
    )
    val steps: List<StepEntity>
)
