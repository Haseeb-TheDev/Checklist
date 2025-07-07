package com.example.checklist.data.model

// Project (domain) → ProjectEntity
fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        name = this.name,
        description = this.description
    )
}

// Step (domain) → StepEntity
fun Step.toEntity(projectId: Long): StepEntity {
    return StepEntity(
        name = this.name,
        description = this.description,
        projectOwnerId = projectId
    )
}

// ProjectEntity → Project (domain)
fun ProjectEntity.toDomain(steps: List<StepEntity>): Project {
    return Project(
        name = this.name,
        description = this.description,
        steps = steps.map { it.toDomain() },
        projectId = this.projectId
    )
}

// StepEntity → Step (domain)
fun StepEntity.toDomain(): Step {
    return Step(
        name = this.name,
        description = this.description,
        stepId = this.stepId,
    )
}
