package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.SavedGalleryScreen
import com.example.ui.theme.KadyBlue
import com.example.ui.theme.KadyCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ConverterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ConverterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    KadyTechApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KadyTechApp(viewModel: ConverterViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.activeTab) {
                            0 -> "محوّل الصور إلى FHD"
                            1 -> "الصور المحفوظة"
                            else -> "عن كادي تك"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    AsyncImage(
                        model = R.drawable.kady_logo,
                        contentDescription = "شعار كادي تك",
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .size(34.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = state.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "المحوّل"
                        )
                    },
                    label = { Text("المحوّل") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KadyBlue,
                        indicatorColor = KadyBlue
                    ),
                    modifier = Modifier.testTag("nav_tab_converter")
                )

                NavigationBarItem(
                    selected = state.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "الصور المحفوظة"
                        )
                    },
                    label = { Text("المحفوظات") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KadyBlue,
                        indicatorColor = KadyBlue
                    ),
                    modifier = Modifier.testTag("nav_tab_gallery")
                )

                NavigationBarItem(
                    selected = state.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "عن كادي تك"
                        )
                    },
                    label = { Text("عن التطبيق") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = KadyBlue,
                        indicatorColor = KadyBlue
                    ),
                    modifier = Modifier.testTag("nav_tab_about")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.activeTab) {
                0 -> ConverterScreen(
                    state = state,
                    onImageSelected = { viewModel.onImageSelected(it) },
                    onSelectSampleImage = { resId, name -> viewModel.selectSampleImage(resId, name) },
                    onApplyCrop = { l, t, r, b, rot -> viewModel.applyCrop(l, t, r, b, rot) },
                    onPresetChanged = { viewModel.setPreset(it) },
                    onFilterChanged = { viewModel.setFilter(it) },
                    onSharpnessChanged = { viewModel.setSharpness(it) },
                    onSharpnessIntensityChanged = { viewModel.setSharpnessIntensity(it) },
                    onSmoothingIntensityChanged = { viewModel.setSmoothingIntensity(it) },
                    onQualityChanged = { viewModel.setQuality(it) },
                    onSplitPositionChanged = { viewModel.setSplitPosition(it) },
                    onConvertClicked = { viewModel.convertImage() },
                    onSaveClicked = { viewModel.saveConvertedImage() },
                    onShareClicked = { viewModel.shareConvertedImage() },
                    onResetClicked = { viewModel.reset() }
                )
                1 -> SavedGalleryScreen(
                    savedImages = state.savedImages,
                    onViewImage = { viewModel.viewSavedImage(it) },
                    onShareImage = { viewModel.shareSavedImage(it) },
                    onDeleteImage = { viewModel.deleteSavedImage(it) },
                    onNavigateToConverter = { viewModel.setActiveTab(0) }
                )
                2 -> AboutScreen()
            }
        }
    }
}
