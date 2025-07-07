package com.example.checklist.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.checklist.data.database.ChecklistDatabase
import com.example.checklist.data.repository.ChecklistRepository
import com.example.checklist.ui.screens.editproject.ProjectEditScreen
import com.example.checklist.ui.screens.home.HomeScreen
import com.example.checklist.ui.screens.projectdetails.ProjectDetailScreen
import com.example.checklist.ui.screens.savedtemplates.SavedTemplatesScreen
import com.example.checklist.ui.screens.settings.SettingsScreen
import com.example.checklist.ui.screens.splash.SplashScreen

@Composable
fun ChecklistNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.name,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { it }) },
        modifier = modifier
    ){
        // Splash → navigates to Home
        composable(route = NavRoutes.Splash.name) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(NavRoutes.Home.name) {
                        popUpTo(NavRoutes.Splash.name) { inclusive = true }
                    }
                }
            )
        }

        // Home screen
        composable(route = NavRoutes.Home.name){
            val context = LocalContext.current
            val repository = ChecklistRepository(ChecklistDatabase.getInstance(context).projectDao())
            HomeScreen(
                onAddProjectClick = {
                    navController.navigate("ProjectEdit/null")
                },
                onProjectClick = { projectId ->
                    navController.navigate("ProjectDetails/$projectId")
                },
                repository = repository
            )
        }

        // Project detail view
        composable(route = "ProjectDetails/{projectId}") { backStackEntry ->

            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: return@composable

            ProjectDetailScreen(
                onEditButtonClick = {
                    navController.navigate("ProjectEdit/$projectId")
                },
                projectId = projectId,
                onProjectDeleted = {
                    navController.popBackStack()
                }
            )
        }

        // Create/edit project screen
        composable(
            route = "ProjectEdit/{projectId}?isTemplateCopy={isTemplateCopy}",
            arguments = listOf(
                navArgument("projectId") {
                    nullable = true
                    defaultValue = null
                },
                navArgument("isTemplateCopy") {
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
            val isTemplateCopy = backStackEntry.arguments?.getString("isTemplateCopy").toBoolean()

            ProjectEditScreen(
                onSaveButtonClick = {
                    navController.navigate(NavRoutes.Home.name)
                },
                projectId = projectId,
                isTemplateCopy = isTemplateCopy
            )
        }

        // Saved templates screen
        composable(route = NavRoutes.SavedTemplates.name){
            val context = LocalContext.current
            val repository = ChecklistRepository(ChecklistDatabase.getInstance(context).projectDao())
            SavedTemplatesScreen(
                onHeaderClick = { projectId ->
                    navController.navigate("ProjectDetails/$projectId")
                },
                repository= repository,
                onUseTemplateClick = { projectId ->
                    navController.navigate("ProjectEdit/$projectId?isTemplateCopy=true")
                },
            )
        }

        // Settings screen
        composable(route = NavRoutes.Settings.name){
            SettingsScreen()
        }
    }
}