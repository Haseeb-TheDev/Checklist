package com.example.checklist.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checklist.ui.theme.ChecklistTheme

@Composable
fun SettingsScreen(modifier: Modifier = Modifier){
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )

    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Main settings layout
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        SettingsHeader()
        DarkMode(
            isDarkMode = isDarkMode,
            onToggle = { viewModel.toggleDarkMode(it) }
        )
        HorizontalDivider()
    }
}

// Screen title
@Composable
fun SettingsHeader(modifier: Modifier = Modifier){
    Row {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
                .weight(1f)
                .padding(start = 24.dp, top = 20.dp, bottom = 12.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Dark mode toggle UI
@Composable
fun DarkMode(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
){

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(32.dp)
    ) {
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary
        )
        Switch(
            checked = isDarkMode,
            onCheckedChange = onToggle,
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ChecklistTheme {
        SettingsScreen()
    }
}