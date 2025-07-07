package com.example.checklist.ui.screens.savedtemplates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checklist.data.database.ChecklistDatabase
import com.example.checklist.data.model.ProjectHeader
import com.example.checklist.data.repository.ChecklistRepository
import com.example.checklist.ui.theme.ChecklistTheme

@Composable
fun SavedTemplatesScreen(
    onHeaderClick: (Long) -> Unit,
    onUseTemplateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    repository: ChecklistRepository
){
    val viewModel: SavedTemplatesViewModel = viewModel(
        factory = SavedTemplatesViewModelFactory(repository)
    )
    val projects by viewModel.projects.collectAsState()

    // Full screen layout for template list
    Column (
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp)
    ){
        SavedTemplatesHeader() // Title bar
        LazyColumn {
            items(projects) { item ->
                TemplateHeaderRow(
                    project = item,
                    onHeaderClick = {onHeaderClick(item.projectId)},
                    onDeleteClick = { viewModel.deleteTemplate(item.name) },
                    onUseTemplateClick = { onUseTemplateClick(item.projectId) }
                    )
            }
        }
    }
}

// Screen title
@Composable
fun SavedTemplatesHeader(modifier: Modifier = Modifier){
    Row {
        Text(
            text = "Saved Templates",
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
                .weight(1f)
                .padding(start = 24.dp, top = 4.dp, bottom = 12.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TemplateHeaderRow(
    onHeaderClick: (Long) -> Unit,
    onDeleteClick: () -> Unit,
    onUseTemplateClick: () -> Unit,
    project: ProjectHeader,
    modifier: Modifier = Modifier
){
    var menuExpanded by remember { mutableStateOf(false) }

// Single template row card with menu
    Card (
        onClick = {onHeaderClick(project.projectId)},
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .fillMaxWidth()
                    .padding(start = 24.dp, bottom = 10.dp, top = 10.dp)
            ) {
                Text(
                    modifier = Modifier.weight(0.5f),
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                // Dropdown menu for actions
                Box {
                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                        text = {
                            Box(modifier = Modifier.width(250.dp)) {
                                Text("Use this template")
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onUseTemplateClick()
                        }
                    )
                        DropdownMenuItem(
                            text = {
                                Box(modifier = Modifier.width(250.dp)) {
                                    Text("Delete template")
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
    }
}

@Preview
@Composable
fun SavedTemplatesScreenPreview(){
    ChecklistTheme {
        val context = LocalContext.current
        val repository = ChecklistRepository(ChecklistDatabase.getInstance(context).projectDao())

        SavedTemplatesScreen(
            onHeaderClick = {},
            repository = repository,
            onUseTemplateClick = {}
        )
    }
}
