package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppDatabase
import com.example.data.model.AppThemeMode
import com.example.data.repository.InvoiceRepository
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.screens.CreateInvoiceScreen
import com.example.ui.screens.CustomerLedgerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InvoiceHistoryScreen
import com.example.ui.screens.InvoicePreviewScreen
import com.example.ui.screens.VendorProfileScreen
import com.example.ui.theme.CharcoalBody
import com.example.ui.theme.GreyText
import com.example.ui.theme.JMDDigiSignTheme
import com.example.ui.theme.LightTintBg
import com.example.ui.theme.LogoBlue
import com.example.ui.theme.SurfaceAccentBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantBg
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.ui.viewmodel.InvoiceViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: InvoiceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = InvoiceRepository(database.invoiceDao(), database.customerDao(), applicationContext)
        val factory = InvoiceViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[InvoiceViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDynamicColor by viewModel.isDynamicColor.collectAsStateWithLifecycle()

            JMDDigiSignTheme(
                appThemeMode = themeMode,
                dynamicColor = isDynamicColor
            ) {
                JMDInvoiceApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JMDInvoiceApp(viewModel: InvoiceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicColor by viewModel.isDynamicColor.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }

    val titleMap = mapOf(
        "home" to "Billing & Ledger Dashboard",
        "create" to "Create Invoice & Bill",
        "ledger" to "Customer Ledger & Accounts",
        "preview" to "Invoice PDF Preview",
        "history" to "Invoice Records Archive",
        "profile" to "Shop & Store Settings"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = titleMap[currentRoute] ?: "Shop Billing & Khata",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("theme_toggle_top_bar_btn")
                    ) {
                        val icon = when (themeMode) {
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Theme: ${themeMode.titleEn}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp
            ) {
                val items = listOf(
                    Triple("home", "Dashboard", Icons.Default.Dashboard),
                    Triple("create", "New Bill", Icons.Default.AddCircle),
                    Triple("ledger", "Ledger", Icons.Default.MenuBook),
                    Triple("history", "Records", Icons.Default.History),
                    Triple("profile", "Shop Profile", Icons.Default.Business)
                )

                items.forEach { (route, label, icon) ->
                    val isSelected = currentRoute == route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != route) {
                                if (route == "create") {
                                    viewModel.resetForm()
                                }
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_$route")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it / 4 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it / 4 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it / 4 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it / 4 }) }
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onCreateInvoice = { navController.navigate("create") },
                    onViewHistory = { navController.navigate("history") },
                    onManageProfile = { navController.navigate("profile") },
                    onSelectInvoice = { navController.navigate("preview") },
                    onOpenLedger = { navController.navigate("ledger") }
                )
            }

            composable("create") {
                CreateInvoiceScreen(
                    viewModel = viewModel,
                    onPreviewPdf = { navController.navigate("preview") }
                )
            }

            composable("ledger") {
                CustomerLedgerScreen(viewModel = viewModel)
            }

            composable("preview") {
                InvoicePreviewScreen(
                    viewModel = viewModel,
                    onDone = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("history") {
                InvoiceHistoryScreen(
                    viewModel = viewModel,
                    onSelectInvoice = { navController.navigate("preview") }
                )
            }

            composable("profile") {
                VendorProfileScreen(
                    viewModel = viewModel,
                    onSaved = { navController.navigate("home") }
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentThemeMode = themeMode,
            isDynamicColor = isDynamicColor,
            onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
            onToggleDynamicColor = { enabled -> viewModel.setDynamicColor(enabled) },
            onDismiss = { showThemeDialog = false }
        )
    }
}

