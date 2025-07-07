package com.example.checklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.checklist.navigation.ChecklistNavGraph
import com.example.checklist.navigation.NavRoutes
import com.example.checklist.ui.components.AppLogo
import com.example.checklist.ui.screens.settings.SettingsViewModel
import com.example.checklist.ui.screens.settings.SettingsViewModelFactory
import com.example.checklist.ui.theme.ChecklistTheme

@Composable
fun ChecklistApp(){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val canNavigateBack = currentRoute != NavRoutes.Home.name

    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))

    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    ChecklistTheme(darkTheme = isDarkMode) {
        Scaffold (
            topBar = {
                // Only show top bar after splash
                if (currentRoute != NavRoutes.Splash.name) {
                    ChecklistTopAppBar(
                        canNavigateBack = canNavigateBack,
                        onNavigateUp = { navController.navigateUp() },
                        onSavedTemplatesClicked = {
                            if(currentRoute != NavRoutes.SavedTemplates.name){
                                navController.navigate(NavRoutes.SavedTemplates.name)
                            }
                        },
                        onSettingsCLicked = {
                            if(currentRoute != NavRoutes.Settings.name){
                            navController.navigate(NavRoutes.Settings.name)
                            }
                        }
                    )
                }
            }
        ){ innerPadding ->
            ChecklistNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
//TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistTopAppBar(
    canNavigateBack: Boolean,
    onNavigateUp: () -> Unit,
    onSavedTemplatesClicked: () -> Unit,
    onSettingsCLicked: () -> Unit
){
    var menuExpanded by remember{mutableStateOf(false) }

    TopAppBar(
        title = {
            AppLogo(
                fontSize = 28,
                iconSize = 28
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
        actions = {
            IconButton(
                onClick = {menuExpanded = true}
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = {menuExpanded = false}
            ) {
                DropdownMenuItem(
                    text = {
                        Box (modifier = Modifier.width(150.dp)){
                            Text(text = "Saved Templates")
                        }
                    },
                    onClick = {
                        onSavedTemplatesClicked()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Box(modifier = Modifier.width(150.dp)) {
                            Text("Settings")
                        }
                    },
                    onClick = {
                        onSettingsCLicked()
                        menuExpanded = false
                    }
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ChecklistAppPreview(){
    ChecklistTheme {
        ChecklistApp()
    }
}