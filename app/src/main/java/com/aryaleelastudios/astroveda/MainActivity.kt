package com.aryaleelastudios.astroveda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryaleelastudios.astroveda.data.i18n.LocaleHelper
import com.aryaleelastudios.astroveda.data.i18n.findActivity
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.PredictionPeriod
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.notifications.NotificationHelper
import com.aryaleelastudios.astroveda.ui.AstroVedaViewModel
import com.aryaleelastudios.astroveda.ui.components.PersonSelectorBottomSheet
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.screens.AddEditPersonScreen
import com.aryaleelastudios.astroveda.ui.screens.DetailedCategoryScreen
import com.aryaleelastudios.astroveda.ui.screens.KundaliScreen
import com.aryaleelastudios.astroveda.ui.screens.OnboardingScreen
import com.aryaleelastudios.astroveda.ui.screens.PredictionsScreen
import com.aryaleelastudios.astroveda.ui.screens.ProfileDrawerContent
import com.aryaleelastudios.astroveda.ui.screens.ShaadiMilanScreen
import com.aryaleelastudios.astroveda.ui.screens.SplashScreen
import com.aryaleelastudios.astroveda.ui.screens.Year2027ReportScreen
import com.aryaleelastudios.astroveda.ui.theme.AstroVedaTheme
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AstroVedaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openPredictionsDaily = intent?.getBooleanExtra(
            NotificationHelper.EXTRA_OPEN_PREDICTIONS_DAILY,
            false
        ) ?: false
        val openYear2027 = (intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_YEAR_2027, false) == true) ||
            (intent?.getStringExtra(NotificationHelper.EXTRA_NAV_ROUTE) == "year_2027") ||
            (intent?.data?.scheme == "astroveda" && intent?.data?.host == "year_2027")

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            val wrappedContext = remember(this, language) {
                LocaleHelper.wrapWithLocale(this, language)
            }

            CompositionLocalProvider(
                LocalContext provides wrappedContext,
                LocalConfiguration provides wrappedContext.resources.configuration
            ) {
                AstroVedaTheme(languageCode = language) {
                    MainAppContent(
                        viewModel = viewModel,
                        openPredictionsDaily = openPredictionsDaily,
                        openYear2027 = openYear2027
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: AstroVedaViewModel,
    openPredictionsDaily: Boolean,
    openYear2027: Boolean = false
) {
    val context = LocalContext.current
    val activityContext = remember(context) { context.findActivity() ?: context }
    val coroutineScope = rememberCoroutineScope()

    val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val chartStyle by viewModel.chartStyle.collectAsStateWithLifecycle()
    val ayanamsa by viewModel.ayanamsa.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notifTime by viewModel.notificationTime.collectAsStateWithLifecycle()
    val notifHour = notifTime.first
    val notifMinute = notifTime.second
    val notificationPersonId by viewModel.notificationPersonId.collectAsStateWithLifecycle()

    val allPersons by viewModel.allPersons.collectAsStateWithLifecycle()
    val selectedPerson by viewModel.selectedPerson.collectAsStateWithLifecycle()
    val kundaliData by viewModel.kundaliData.collectAsStateWithLifecycle()
    val year2027Forecast by viewModel.year2027Forecast.collectAsStateWithLifecycle()
    val personRashis by viewModel.personRashis.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("splash") }
    var currentTab by remember { mutableStateOf("kundali") } // "kundali" | "predictions" | "shaadi_milan"
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }
    var selectedCategory by remember { mutableStateOf(ReadingCategory.CAREER) }
    var selectedPredictionPeriod by remember { mutableStateOf(PredictionPeriod.DAILY) }
    var showPersonSelectorSheet by remember { mutableStateOf(false) }
    var handledYear2027DeepLink by remember { mutableStateOf(false) }

    val kundaliListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && notificationsEnabled) {
            NotificationHelper.scheduleDailyNotification(activityContext, notifHour, notifMinute)
        }
    }

    // Request POST_NOTIFICATIONS on Android 13+ when entering main app if notifications enabled
    LaunchedEffect(currentScreen, notificationsEnabled) {
        if (currentScreen == "main" && notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(activityContext, Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // SAF Launchers
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        val person = selectedPerson
        val kundali = kundaliData
        if (uri != null && person != null && kundali != null) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_pdf_generating),
                Toast.LENGTH_SHORT
            ).show()
            coroutineScope.launch {
                val success = viewModel.exportMasterPdf(
                    context = context,
                    uri = uri,
                    person = person,
                    kundali = kundali,
                    chartStyle = chartStyle,
                    langCode = language,
                    yearForecast = year2027Forecast
                )
                Toast.makeText(
                    context,
                    context.getString(if (success) R.string.toast_pdf_saved else R.string.toast_pdf_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonStr = viewModel.exportAllPersonsJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonStr.toByteArray())
                }
                Toast.makeText(context, context.getString(R.string.toast_backup_saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_export_error, e.message.orEmpty()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: ""
                val success = viewModel.importPersonsFromJson(jsonStr)
                Toast.makeText(
                    context,
                    context.getString(if (success) R.string.toast_import_success else R.string.toast_import_invalid),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_import_error, e.message.orEmpty()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Deep link from notification tap -> Predictions tab on Daily
    LaunchedEffect(openPredictionsDaily, selectedPerson, kundaliData) {
        if (openPredictionsDaily && selectedPerson != null && kundaliData != null && currentScreen == "main") {
            selectedPredictionPeriod = PredictionPeriod.DAILY
            currentTab = "predictions"
        }
    }

    // Deep link / intent shortcut -> 2027 Comprehensive Report
    LaunchedEffect(openYear2027, selectedPerson, kundaliData, year2027Forecast, currentScreen) {
        if (openYear2027 && !handledYear2027DeepLink &&
            selectedPerson != null && kundaliData != null && year2027Forecast != null &&
            currentScreen == "main"
        ) {
            handledYear2027DeepLink = true
            currentScreen = "year_2027"
        }
    }

    BackHandler(enabled = drawerState.isOpen || currentScreen != "main" || currentTab != "kundali") {
        when {
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
            currentScreen == "add_edit_person" -> currentScreen = "main"
            currentScreen == "category_detail" -> currentScreen = "main"
            currentScreen == "year_2027" -> currentScreen = "main"
            currentTab != "kundali" -> currentTab = "kundali"
        }
    }

    when (currentScreen) {
        "splash" -> {
            SplashScreen(
                onboardingComplete = onboardingComplete,
                onNavigateNext = { dest ->
                    currentScreen = dest
                }
            )
        }

        "onboarding" -> {
            OnboardingScreen(
                currentLanguage = language,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onCompleteAndAddPerson = { name ->
                    viewModel.completeOnboarding(name) {
                        personToEdit = null
                        currentScreen = "add_edit_person"
                    }
                },
                onCompleteToKundaliTab = { name ->
                    viewModel.completeOnboarding(name) {
                        currentScreen = "main"
                    }
                }
            )
        }

        "add_edit_person" -> {
            AddEditPersonScreen(
                personToEdit = personToEdit,
                onSavePerson = { person ->
                    viewModel.savePerson(person) {
                        currentTab = "kundali"
                        currentScreen = "main"
                    }
                },
                onNavigateBack = { currentScreen = "main" }
            )
        }

        "category_detail" -> {
            if (selectedPerson != null && kundaliData != null) {
                DetailedCategoryScreen(
                    person = selectedPerson!!,
                    kundali = kundaliData!!,
                    selectedCategory = selectedCategory,
                    chartStyle = chartStyle,
                    onSelectCategory = { cat -> selectedCategory = cat },
                    onNavigateBack = { currentScreen = "main" }
                )
            } else {
                currentScreen = "main"
            }
        }

        "year_2027" -> {
            val p = selectedPerson
            val k = kundaliData
            val yf = year2027Forecast
            if (p != null && k != null && yf != null) {
                Year2027ReportScreen(
                    person = p,
                    kundali = k,
                    forecast = yf,
                    chartStyle = chartStyle,
                    onNavigateBack = { currentScreen = "main" }
                )
            } else {
                currentScreen = "main"
            }
        }

        else -> {
            // Right-side ModalNavigationDrawer using RTL wrapper
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            ProfileDrawerContent(
                                userName = userName,
                                currentLanguage = language,
                                chartStyle = chartStyle,
                                ayanamsa = ayanamsa,
                                notificationsEnabled = notificationsEnabled,
                                notificationHour = notifHour,
                                notificationMinute = notifMinute,
                                persons = allPersons,
                                notificationPersonId = notificationPersonId,
                                onUpdateUserName = { viewModel.setUserName(it) },
                                onUpdateLanguage = { viewModel.setLanguage(it) },
                                onUpdateChartStyle = { viewModel.setChartStyle(it) },
                                onUpdateAyanamsa = { viewModel.setAyanamsa(it) },
                                onUpdateNotifications = { en, h, m ->
                                    if (en && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val granted = ContextCompat.checkSelfPermission(
                                            activityContext,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (!granted) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                    viewModel.updateNotifications(en, h, m)
                                },
                                onUpdateNotificationPerson = { viewModel.updateNotificationPerson(it) },
                                onExportJson = { exportJsonLauncher.launch("astroveda_backup.json") },
                                onImportJson = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                                onClearAllData = {
                                    viewModel.clearAllData()
                                    coroutineScope.launch { drawerState.close() }
                                },
                                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                            )
                        }
                    }
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .clickable { currentTab = "kundali" }
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.ic_astroveda_logo),
                                                    contentDescription = stringResource(R.string.cd_astroveda_logo),
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(CircleShape)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(R.string.app_name),
                                                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepIndigo
                                                )
                                            }

                                            if (currentTab == "kundali" || currentTab == "predictions") {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, CreamBorder),
                                                    modifier = Modifier
                                                        .heightIn(min = 40.dp)
                                                        .clickable { showPersonSelectorSheet = true }
                                                        .testTag("top_bar_person_selector")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = selectedPerson?.name ?: stringResource(R.string.add_short),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = DeepIndigo,
                                                            maxLines = 1
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Icon(
                                                            Icons.Default.ArrowDropDown,
                                                            contentDescription = stringResource(R.string.cd_person_dropdown),
                                                            tint = SaffronDark,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Spacer(Modifier.width(1.dp))
                                            }
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = { coroutineScope.launch { drawerState.open() } },
                                            modifier = Modifier.testTag("profile_avatar_btn")
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(SaffronAccent, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = stringResource(R.string.cd_profile_avatar),
                                                    tint = DeepIndigo,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
                                )
                            },
                            bottomBar = {
                                // 3 TABS: Kundali | Predictions | Shaadi Milan
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.testTag("bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == "kundali",
                                        onClick = { currentTab = "kundali" },
                                        icon = {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = stringResource(R.string.tab_kundali),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(R.string.tab_kundali),
                                                maxLines = 1,
                                                fontWeight = if (currentTab == "kundali") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SaffronDark,
                                            selectedTextColor = DeepIndigo,
                                            indicatorColor = SaffronAccent.copy(alpha = 0.2f),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag("nav_tab_kundali")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == "predictions",
                                        onClick = { currentTab = "predictions" },
                                        icon = {
                                            Icon(
                                                Icons.Default.WbSunny,
                                                contentDescription = stringResource(R.string.tab_predictions),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(R.string.tab_predictions),
                                                maxLines = 1,
                                                fontWeight = if (currentTab == "predictions") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SaffronDark,
                                            selectedTextColor = DeepIndigo,
                                            indicatorColor = SaffronAccent.copy(alpha = 0.2f),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag("nav_tab_predictions")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == "shaadi_milan",
                                        onClick = { currentTab = "shaadi_milan" },
                                        icon = {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = stringResource(R.string.tab_shaadi_milan),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(R.string.tab_shaadi_milan),
                                                maxLines = 1,
                                                fontWeight = if (currentTab == "shaadi_milan") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SaffronDark,
                                            selectedTextColor = DeepIndigo,
                                            indicatorColor = SaffronAccent.copy(alpha = 0.2f),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag("nav_tab_shaadi_milan")
                                    )
                                }
                            },
                            containerColor = CreamBackground
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clearFocusOnTap()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    "kundali" -> {
                                        KundaliScreen(
                                            person = selectedPerson,
                                            kundali = kundaliData,
                                            chartStyle = chartStyle,
                                            onChartStyleChanged = { viewModel.setChartStyle(it) },
                                            onOpenCategory = { cat ->
                                                selectedCategory = cat
                                                currentScreen = "category_detail"
                                            },
                                            onOpenPredictions = { period ->
                                                selectedPredictionPeriod = period
                                                currentTab = "predictions"
                                            },
                                            onNavigateToAddPerson = {
                                            personToEdit = null
                                            currentScreen = "add_edit_person"
                                        },
                                        onEditPerson = { p ->
                                            personToEdit = p
                                            currentScreen = "add_edit_person"
                                        },
                                        onDeletePerson = { p -> viewModel.deletePerson(p) },
                                        onExportPdf = {
                                            val nameFmt = selectedPerson?.name?.replace(" ", "_") ?: "AstroVeda"
                                            exportPdfLauncher.launch("${nameFmt}_Master_Kundali.pdf")
                                        },
                                        onShareJson = {
                                            selectedPerson?.let { p ->
                                                val shareText = context.getString(
                                                    R.string.share_profile_text,
                                                    p.name,
                                                    p.birthDate,
                                                    if (p.isTimeUnknown) context.getString(R.string.time_unknown_display) else p.birthTime,
                                                    p.birthPlace,
                                                    p.latitude,
                                                    p.longitude,
                                                    p.timezoneId
                                                )
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(
                                                    sendIntent,
                                                    context.getString(R.string.share_chooser_title)
                                                )
                                                activityContext.startActivity(shareIntent)
                                            }
                                        },
                                            onOpenYear2027 = { currentScreen = "year_2027" },
                                            listState = kundaliListState
                                        )
                                    }

                                    "predictions" -> {
                                        PredictionsScreen(
                                            person = selectedPerson,
                                            kundali = kundaliData,
                                            selectedPeriod = selectedPredictionPeriod,
                                            onSelectPeriod = { selectedPredictionPeriod = it },
                                            languageCode = language,
                                            onNavigateToAddPerson = {
                                                personToEdit = null
                                                currentScreen = "add_edit_person"
                                            }
                                        )
                                    }

                                    else -> {
                                        ShaadiMilanScreen(
                                            persons = allPersons,
                                            ayanamsa = ayanamsa,
                                            languageCode = language,
                                            contentRepo = viewModel.contentRepo,
                                            onNavigateToAddPerson = {
                                                personToEdit = null
                                                currentScreen = "add_edit_person"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showPersonSelectorSheet) {
                PersonSelectorBottomSheet(
                    persons = allPersons,
                    personRashis = personRashis,
                    selectedPersonId = selectedPerson?.id,
                    onSelectPerson = { viewModel.selectPerson(it) },
                    onAddNewPerson = {
                        personToEdit = null
                        currentScreen = "add_edit_person"
                    },
                    onEditPerson = { p ->
                        personToEdit = p
                        currentScreen = "add_edit_person"
                    },
                    onDuplicatePerson = { p ->
                        val duplicatedName = context.getString(R.string.copy_suffix, p.name)
                        viewModel.duplicatePerson(p, duplicatedName)
                    },
                    onDeletePerson = { p -> viewModel.deletePerson(p) },
                    onDismiss = { showPersonSelectorSheet = false }
                )
            }
        }
    }
}
