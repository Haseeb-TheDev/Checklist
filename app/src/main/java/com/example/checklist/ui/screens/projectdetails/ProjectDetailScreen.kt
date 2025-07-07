package com.example.checklist.ui.screens.projectdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checklist.data.database.ChecklistDatabase
import com.example.checklist.data.model.Project
import com.example.checklist.data.model.Step
import com.example.checklist.data.repository.ChecklistRepository
import com.example.checklist.ui.theme.ChecklistTheme
import kotlinx.coroutines.launch

// Main screen composable
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    onEditButtonClick: () -> Unit,
    onProjectDeleted: () -> Unit,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    val database = ChecklistDatabase.getInstance(context)
    val repository = ChecklistRepository(database.projectDao())

    val viewModel: ProjectDetailViewModel = viewModel(
        factory = ProjectDetailViewModelFactory(repository, projectId)
    )
    val project by viewModel.project.collectAsState()

    val isDeleted by viewModel.isProjectDeleted.collectAsState()
    if (isDeleted) {
        onProjectDeleted()
    }

    Column (
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        project?.let {
            ProjectDetailCard(
                project = it,
                onEditButtonClick = { onEditButtonClick() },
                onDeleteClick = { viewModel.deleteProject() },
                onSaveClick = { viewModel.saveProjectAsTemplate() },
                viewModel = viewModel
            )
        }
    }
}

// Card composable
@Composable
fun ProjectDetailCard(
    onEditButtonClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    project: Project,
    viewModel: ProjectDetailViewModel
){
    Card (
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ){
            ProjectDetailHeaderRow(
                onEditButtonClick = {onEditButtonClick()},
                onDeleteClick = onDeleteClick,
                onSaveClick = onSaveClick,
                project = project)
            HorizontalDivider()
            LazyColumn{
                items(project.steps) { step ->
                    DetailStepItem(
                        step = step,
                        onDeleteStep = { viewModel.deleteStep(step.stepId) })
                }
            }
        }
    }
}

// Header row
@Composable
fun ProjectDetailHeaderRow(
    onEditButtonClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    project: Project
){
    var menuExpanded by remember { mutableStateOf(false) }
    var checked by remember(project.projectId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 6.dp, top = 4.dp)
    ){
        IconButton(
            onClick = {
                checked = true
                scope.launch {
                    kotlinx.coroutines.delay(250)
                    onDeleteClick()
                }
            }
        ) {
            Icon(
                imageVector = if (checked)
                    Icons.Filled.CheckBox
                else
                    Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(0.5f),
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(
                    onClick = {onEditButtonClick()},
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Box (modifier = Modifier.width(150.dp)){
                                    Text(text = "Save Template")
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                onSaveClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Box(modifier = Modifier.width(150.dp)) {
                                    Text("Delete")
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp, end = 48.dp)
            )
        }
    }
}

// Individual step item
@Composable
fun DetailStepItem(
    step: Step,
    onDeleteStep: () -> Unit
){
    var checked by remember(step.stepId) { mutableStateOf(false) } // keyed by step ID
    val scope = rememberCoroutineScope()

    Row (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = 16.dp, bottom = 8.dp)
    ){
        IconButton(
            onClick = {
                checked = true
                scope.launch {
                    kotlinx.coroutines.delay(250)
                    onDeleteStep()
                }
            }
        ) {
            Icon(
                imageVector = if (checked)
                    Icons.Filled.CheckBox
                else
                    Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(0.5f),
                    text = step.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp, end = 48.dp)
            )
        }
    }
}
@Preview
@Composable
fun ProjectDetailScreenPreview(){
    ChecklistTheme {
        ProjectDetailScreen(
            onEditButtonClick = {},
            projectId = 1L,
            onProjectDeleted = {}
        )
    }
}