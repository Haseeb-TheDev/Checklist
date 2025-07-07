package com.example.checklist.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.checklist.data.model.ProjectEntity
import com.example.checklist.data.model.Step
import com.example.checklist.data.model.toDomain
import com.example.checklist.data.repository.ChecklistRepository
import com.example.checklist.ui.theme.ChecklistTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAddProjectClick: () -> Unit,
    onProjectClick: (Long) -> Unit,
    repository: ChecklistRepository,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    val selectedIndex by viewModel.selectedProjectIndex

    // Whole screen layout with FAB
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {onAddProjectClick()}
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { _ ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding()
        ) {
            val projects by viewModel.projects.collectAsState()

            // Show empty state if no projects
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Projects yet, Tap + to Add a Project",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Horizontal tab row for projects
                ProjectTabRow(
                    projects = projects,
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        if (index == projects.size) {
                            onAddProjectClick()
                        } else {
                            viewModel.onProjectSelected(index)
                        }
                    }
                )
                HorizontalDivider()

                val pagerState = rememberPagerState(
                    initialPage = selectedIndex,
                    pageCount = {projects.size}
                )
//                val coroutineScope = rememberCoroutineScope()

                // Keep pager and selected tab in sync
                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage != selectedIndex) {
                        viewModel.onProjectSelected(pagerState.currentPage)
                    }
                }

                LaunchedEffect(selectedIndex) {
                    if (!pagerState.isScrollInProgress && pagerState.currentPage != selectedIndex) {
                        pagerState.scrollToPage(selectedIndex)
                    }
                }

                // Horizontal pager for project cards
                Box (
                    modifier = modifier.weight(1f)
                ){
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { pageIndex ->
                        val entity = projects[pageIndex]
                        val stepsFlow = remember(entity.projectId) {
                            repository.getStepsForProject(entity.projectId)
                        }
                        val stepEntities by stepsFlow.collectAsState(initial = emptyList())

                        val project = Project(
                            name = entity.name,
                            description = entity.description,
                            steps = stepEntities.map { it.toDomain() },
                            projectId = entity.projectId
                        )

                        // Project card inside pager
                        Column (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.Top
                        ){
                            ProjectCard(
                                project = project,
                                onProjectClick = { onProjectClick(entity.projectId) },
                                onDeleteClick = { viewModel.deleteSelectedProject() },
                                onSaveTemplateClick = { viewModel.saveSelectedProjectAsTemplate() },
                                isTemplateSaved = viewModel.isTemplateSaved.value,
                                viewModel = viewModel
                            )
                        }
                    }
                }

            }
        }
    }
}

// Tabs + Add new project
@Composable
fun ProjectTabRow(
    projects: List<ProjectEntity>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabLabels = projects.map { it.name } + "Add+"

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(tabLabels.size) { index ->
            val label = tabLabels[index]
            val isSelected = index == selectedIndex
            val isAddProjectTab = index == tabLabels.lastIndex

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isAddProjectTab -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.surface,
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onTabSelected(index) }
            )
        }
    }
}

// Whole project card UI
@Composable
fun ProjectCard(
    project: Project,
    onProjectClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveTemplateClick: () -> Unit,
    isTemplateSaved: Boolean,
    viewModel: HomeViewModel
){
    Card (
        onClick = {onProjectClick()},
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
            ProjectHeaderRow(
                project = project,
                onDeleteClick = onDeleteClick,
                onSaveTemplateClick= onSaveTemplateClick,
                isTemplateSaved= isTemplateSaved,
            )
            HorizontalDivider()
            LazyColumn{
                items(project.steps.size) { index ->
                    val isExpanded = viewModel.expandedSteps.value.contains(index)
                    StepItem(
                        step = project.steps[index],
                        isExpanded = isExpanded,
                        onToggleExpand = { viewModel.toggleStepExpanded(index) },
                        onDeleteStep = {
                            viewModel.deleteStep(project.steps[index].stepId)
                        }
                    )
                }
            }
        }
    }
}

// Header with checkbox + dropdown
@Composable
fun ProjectHeaderRow(
    project: Project,
    onDeleteClick: () -> Unit,
    onSaveTemplateClick: () -> Unit,
    isTemplateSaved: Boolean,
){
    var menuExpanded by remember { mutableStateOf(false) }
    Row (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 6.dp, top = 4.dp)
    ){
        var projectChecked by remember(project.projectId) { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        IconButton(
            onClick = {
                projectChecked = true
                coroutineScope.launch {
                    kotlinx.coroutines.delay(250)
                    onDeleteClick()
                }
            }
        ) {
            Icon(
                imageVector = if (projectChecked)
                    Icons.Default.CheckBox
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
                                Text(text = if (isTemplateSaved) "Saved" else "Save Template")
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            if (!isTemplateSaved) onSaveTemplateClick()
                        },
                        enabled = !isTemplateSaved
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

// Individual step item with expand/delete
@Composable
fun StepItem(
    step: Step,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteStep: () -> Unit,
){

    Row (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = 16.dp, bottom = 8.dp)
    ){
        var checked by remember(step.stepId) { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        IconButton(
            onClick = {
                checked = true
                coroutineScope.launch {
                    kotlinx.coroutines.delay(250)
                    onDeleteStep()
                }
            }
        ) {
            Icon(
                imageVector = if (checked)
                    Icons.Default.CheckBox
                else
                    Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 10.dp)
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
                IconButton(
                    onClick = onToggleExpand
                ) {
                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 48.dp)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectTabsScreenPreview(){
    ChecklistTheme {
        val context = LocalContext.current
        val repository = ChecklistRepository(ChecklistDatabase.getInstance(context).projectDao())
        HomeScreen(
            onAddProjectClick = {},
            onProjectClick = {},
            repository = repository
        )
    }
}