package com.vldsir.qrshield.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vldsir.qrshield.di.AppContainer
import com.vldsir.qrshield.ui.history.HistoryScreen
import com.vldsir.qrshield.ui.history.HistoryViewModel
import com.vldsir.qrshield.ui.result.ResultScreen
import com.vldsir.qrshield.ui.result.ResultViewModel
import com.vldsir.qrshield.ui.scanner.ScannerScreen
import com.vldsir.qrshield.ui.scanner.ScannerViewModel
import com.vldsir.qrshield.ui.settings.SettingsScreen
import com.vldsir.qrshield.ui.settings.SettingsViewModel
import com.vldsir.qrshield.ui.tutorial.TutorialScreen
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QRShieldNavHost(container: AppContainer) {
    val navController = rememberNavController()

    // Show the tutorial first if the user hasn't completed it yet.
    val startDestination = if (container.settingsRepository.isTutorialCompleted())
        Routes.SCANNER else Routes.TUTORIAL

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.TUTORIAL) {
            TutorialScreen(
                settingsRepository = container.settingsRepository,
                onFinished = {
                    // Navigate to scanner and remove the tutorial from the back stack.
                    navController.navigate(Routes.SCANNER) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.SCANNER) {
            val vm: ScannerViewModel = viewModel(factory = viewModelFactory {
                initializer { ScannerViewModel(container.orchestrator, container.networkAvailability) }
            })
            ScannerScreen(
                viewModel = vm,
                onNavigateToResult = { id ->
                    navController.navigate(Routes.result(id)) {
                        popUpTo(Routes.SCANNER) { inclusive = false }
                    }
                },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument("scanId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getLong("scanId") ?: return@composable
            val vm: ResultViewModel = viewModel(factory = viewModelFactory {
                initializer { ResultViewModel(scanId, container.historyRepository) }
            })
            ResultScreen(
                viewModel = vm,
                intentLauncher = container.intentLauncher,
                wifiParser = container.wifiParser,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.HISTORY) {
            val vm: HistoryViewModel = viewModel(factory = viewModelFactory {
                initializer { HistoryViewModel(container.historyRepository) }
            })
            HistoryScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { id -> navController.navigate(Routes.result(id)) },
            )
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = viewModelFactory {
                initializer { SettingsViewModel(container.settingsRepository) }
            })
            SettingsScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTutorial = {
                    // Navigate to the tutorial and clear settings from the back stack
                    // so "Done" in the tutorial lands on the scanner cleanly.
                    navController.navigate(Routes.TUTORIAL) {
                        popUpTo(Routes.SETTINGS) { inclusive = true }
                    }
                },
            )
        }
    }
}
