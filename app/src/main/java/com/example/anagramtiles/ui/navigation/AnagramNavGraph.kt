package com.example.anagramtiles.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.anagramtiles.ui.components.BottomNavBar
import com.example.anagramtiles.ui.components.HowToPlaySheet
import com.example.anagramtiles.ui.screens.AboutScreen
import com.example.anagramtiles.ui.screens.GameScreen
import com.example.anagramtiles.ui.screens.HomeScreen
import com.example.anagramtiles.ui.screens.ResultScreen
import com.example.anagramtiles.ui.screens.SettingsScreen
import com.example.anagramtiles.ui.screens.SplashScreen
import com.example.anagramtiles.ui.viewmodel.AnagramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnagramNavGraph(
    viewModel: AnagramViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gameState by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val showHowToPlay by viewModel.showHowToPlaySheet.collectAsState()
    val showAnswersSheet by viewModel.showAnswersSheet.collectAsState()
    val showRestartDialog by viewModel.showRestartDialog.collectAsState()

    val howToPlaySheetState = rememberModalBottomSheetState()

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Settings.route,
        Screen.About.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    settings = settings,
                    statistics = statistics,
                    onStartGame = { diff ->
                        viewModel.startNewGame(diff)
                        navController.navigate(Screen.Game.route)
                    },
                    onShowHowToPlay = { viewModel.setHowToPlayVisible(true) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onDifficultySelected = { diff ->
                        viewModel.updateSettings(settings.copy(cpuDifficulty = diff))
                    }
                )
            }

            composable(Screen.Game.route) {
                GameScreen(
                    state = gameState,
                    settings = settings,
                    showAnswersSheet = showAnswersSheet,
                    showRestartDialog = showRestartDialog,
                    onTileClicked = { pos -> viewModel.selectOrSwapTile(pos) },
                    onShuffle = { viewModel.shuffleTiles() },
                    onReset = { viewModel.resetTiles() },
                    onCheckWord = { viewModel.checkWord() },
                    onPass = { viewModel.passTurn() },
                    onNextPuzzle = { viewModel.nextPuzzle() },
                    onOpenAnswersSheet = { show -> viewModel.setAnswersSheetVisible(show) },
                    onRequestRestart = { viewModel.requestRestartGame() },
                    onConfirmRestart = { viewModel.confirmRestart() },
                    onDismissRestart = { viewModel.dismissRestartDialog() },
                    onNavigateResult = {
                        navController.navigate(Screen.Result.route) {
                            popUpTo(Screen.Game.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Result.route) {
                ResultScreen(
                    state = gameState,
                    onPlayAgain = {
                        viewModel.startNewGame(settings.cpuDifficulty)
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Result.route) { inclusive = true }
                        }
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    onSettingsChanged = { updated -> viewModel.updateSettings(updated) },
                    onResetSettings = { viewModel.resetSettings() },
                    onResetStatistics = { viewModel.resetStatistics() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }

        // How to Play Bottom Sheet
        if (showHowToPlay) {
            HowToPlaySheet(
                sheetState = howToPlaySheetState,
                onDismissRequest = { viewModel.setHowToPlayVisible(false) }
            )
        }
    }
}
