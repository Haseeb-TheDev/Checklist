package com.example.checklist.ui.screens.editproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checklist.data.database.ChecklistDatabase
import com.example.checklist.data.repository.ChecklistRepository
import com.example.checklist.ui.theme.ChecklistTheme
import kotlinx.coroutines.launch

// Main composable
@Composable
fun ProjectEditScreen(
    modifier: Modifier = Modifier,
    onSaveButtonClick: () -> Unit,
    projectId: Long? = null,
    isTemplateCopy: Boolean = false
) {
    val context = LocalContext.current
    val database = ChecklistDatabase.getInstance(context)
    val repository = ChecklistRepository(database.projectDao())

    val viewModel: ProjectEditViewModel = viewModel(
        factory = ProjectEditViewModelFactory(repository, projectId, isTemplateCopy)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Check if keyboard is visible for layout decisions
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Used to manage focus when adding new steps
    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }

    // Layout with bottom save button
    Scaffold(
        bottomBar = {
            SaveProjectButton(
                enabled = uiState.projectName.isNotBlank() && uiState.steps.isNotEmpty(),
                onClick = { viewModel.saveProject { onSaveButtonClick() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding()
            )
        }
    ) { paddingValues ->

        // Scrollable form for title, description, and steps
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = 0.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 4.dp,
                    end = 4.dp
                )
        ) {
            item { ProjectTitleField(uiState.projectName, viewModel::updateProjectName) }
            item { ProjectDescriptionField(uiState.projectDesc, viewModel::updateProjectDesc) }

            // Existing step items
            itemsIndexed(uiState.steps) { index, step ->
                val requester = remember { FocusRequester() }
                LaunchedEffect(uiState.steps.size) {
                    if (index == uiState.steps.lastIndex) {
                        requester.requestFocus()
                    }
                }
                focusRequesters.add(requester)
                StepItem(
                    stepName = step.name,
                    stepDesc = step.description,
                    onNameChange = { viewModel.updateStep(index, it, step.description) },
                    onDescChange = { viewModel.updateStep(index, step.name, it) },
                    onDeleteClick = { viewModel.deleteStep(index) },
                    nameFieldFocusRequester = requester
                )
            }

            // Fields for adding a new step
            item { StepTitleField(uiState.stepName, viewModel::updateStepName) }
            item { StepDescriptionField(uiState.stepDesc, viewModel::updateStepDesc) }
            item {
                AddStepButton(
                    enabled = uiState.stepName.isNotBlank(),
                    onClick = viewModel::addStep
                )
            }

            // Prevents Save button from covering content when keyboard is hidden
            if (!isKeyboardVisible) {
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        }
    }
}

// Project Title input field
@Composable
private fun ProjectTitleField(value: String, onChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onChange,
        placeholder = {
            Text(
                "Add Title",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.titleLarge,
        colors = textFieldColors()
    )
}

// Project Description input field
@Composable
private fun ProjectDescriptionField(value: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.AutoMirrored.Filled.Notes,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(start = 14.dp).size(16.dp)
        )
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(
                    "Add details",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = textFieldColors()
        )
    }
}

// Each editable step row with name, description, and deletion controls
@Composable
private fun StepItem(
    stepName: String,
    stepDesc: String,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onDeleteClick: () -> Unit,
    nameFieldFocusRequester: FocusRequester
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val coroutineScope = rememberCoroutineScope()
            var isChecked by remember { mutableStateOf(false) }

            IconButton(
                onClick = {
                    isChecked = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(250)
                        onDeleteClick()
                    }
                },
                modifier = Modifier.padding(start= 12.dp, end = 0.1.dp).size(20.dp)
            ) {
                Icon(
                    imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null
                )
            }
            // Step name field
            TextField(
                value = stepName,
                onValueChange = onNameChange,
                placeholder = {
                    Text(
                        "Step Name",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(nameFieldFocusRequester),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = textFieldColors()
            )
            IconButton(
                onClick = { onDeleteClick() },
                modifier = Modifier.padding(end = 8.dp).size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }
        // Step description field
        TextField(
            value = stepDesc,
            onValueChange = onDescChange,
            placeholder = {
                Text(
                    "Step Description",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = textFieldColors()
        )
    }
}

// Step Title input field
@Composable
private fun StepTitleField(value: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.SubdirectoryArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(start = 14.dp).size(20.dp)
        )
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(
                    "Add Step",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = textFieldColors()
        )
    }
}

// Step Description input field
@Composable
private fun StepDescriptionField(value: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.AutoMirrored.Filled.Notes,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(start = 52.dp).size(16.dp)
        )
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(
                    "Step Description",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = textFieldColors()
        )
    }
}

// Button to add a new step
@Composable
private fun AddStepButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.padding(14.dp)
    ) {
        Text("Add Step")
    }
}

// Button to save the entire project
@Composable
private fun SaveProjectButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text("Save")
    }
}

// Common text field color styling
@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent
)

@Preview(showBackground = true)
@Composable
fun ProjectEditScreenPreview() {
    ChecklistTheme {
        ProjectEditScreen(
            onSaveButtonClick = {}
        )
    }
}
