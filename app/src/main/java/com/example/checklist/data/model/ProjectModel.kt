package com.example.checklist.data.model

// Main project model used in app logic
data class Project(
    val projectId: Long,
    val name: String,
    val description: String,
    val steps : List<Step> = emptyList()
)

// Step model used in app logic
data class Step(
    val stepId: Long,
    val name: String,
    val description: String
)

// Lightweight project info for headers/lists(Templates screen)
data class ProjectHeader(
    val projectId: Long,
    val name: String
)



