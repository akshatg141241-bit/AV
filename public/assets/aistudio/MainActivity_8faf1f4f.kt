package com.aryaleelastudios.kundaliguru

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.pdf.PdfExporter
import com.aryaleelastudios.kundaliguru.ui.KundaliViewModel
import com.aryaleelastudios.kundaliguru.ui.components.PersonSelectorBottomSheet
import com.aryaleelastudios.kundaliguru.ui.screens.*
import com.aryaleelastudios.kundaliguru.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: KundaliViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openTodayDirectly = intent?.getBooleanExtra("OPEN_TODAY_PREDICTION", false) ?: false

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    openTodayDirectly = openTodayDirectly
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: KundaliViewModel,
    openTodayDirectly: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isOnboarded by viewModel.isOnboarded.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isHindi = language == "hi"

    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val chartStyle by viewModel.chartStyle.collectAsStateWithLifecycle()
    val ayanamsa by viewModel.ayanamsa.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val (notifHour, notifMinute) = viewModel.notificationTime.collectAsStateWithLifecycle().value

    val allPersons by viewModel.allPersons.collectAsStateWithLifecycle()
    val selectedPerson by viewModel.selectedPerson.collectAsStateWithLifecycle()
    val kundaliData by viewModel.kundaliData.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("splash") }
    var currentTab by remember { mutableStateOf("kundali") } // "kundali" or "shaadi_milan"
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }
    var showPersonSelectorSheet by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Storage Access Framework Launchers
    // 1. PDF Export Launcher
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null && selectedPerson != null && kundaliData != null) {
            val success = PdfExporter.exportToUri(
                context = context,
                uri = uri,
                person = selectedPerson!!,
                kundali = kundaliData!!,
                isHindi = isHindi
            )
            Toast.makeText(
                context,
                if (success) (if (isHindi) "पीडीएफ सफलतापूर्वक सहेजी गई" else "PDF report saved successfully")
                else (if (isHindi) "पीडीएफ सहेजने में त्रुटि" else "Failed to export PDF"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 2. JSON Export Launcher
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonStr = viewModel.exportAllPersonsJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonStr.toByteArray())
                }
                Toast.makeText(
                    context,
                    if (isHindi) "डेटा बैकअप सहेजा गया" else "Backup saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 3. JSON Import Launcher
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
                    if (success) (if (isHindi) "डेटा सफलतापूर्वक आयात किया गया" else "Data imported successfully")
                    else (if (isHindi) "अमान्य डेटा फ़ाइल" else "Invalid JSON file"),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Import error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle deep navigation from notifications
    LaunchedEffect(openTodayDirectly) {
        if (openTodayDirectly && selectedPerson != null) {
            currentScreen = "today_prediction"
        }
    }

    // BackHandler configuration
    BackHandler(enabled = drawerState.isOpen || currentScreen != "main" || currentTab != "kundali") {
        when {
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
            currentScreen == "add_edit_person" -> currentScreen = "main"
            currentScreen == "today_prediction" -> currentScreen = "main"
            currentTab != "kundali" -> currentTab = "kundali"
        }
    }

    when (currentScreen) {
        "splash" -> {
            SplashScreen(
                isOnboarded = isOnboarded,
                onNavigateNext = {
                    when (isOnboarded) {
                        true -> currentScreen = "main"
                        false -> currentScreen = "onboarding"
                        null -> { /* Stay on splash screen */ }
                    }
                }
            )
        }

        "onboarding" -> {
            OnboardingScreen(
                currentLanguage = language,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onSaveNameAndFinish = { name ->
                    viewModel.completeOnboarding(name)
                    currentScreen = "main"
                }
            )
        }

        "add_edit_person" -> {
            AddEditPersonScreen(
                personToEdit = personToEdit,
                isHindi = isHindi,
                onSavePerson = { person ->
                    viewModel.savePerson(person)
                    currentScreen = "main"
                },
                onNavigateBack = { currentScreen = "main" }
            )
        }

        "today_prediction" -> {
            if (selectedPerson != null && kundaliData != null) {
                TodayPredictionScreen(
                    person = selectedPerson!!,
                    kundali = kundaliData!!,
                    isHindi = isHindi,
                    onNavigateBack = { currentScreen = "main" }
                )
            } else {
                currentScreen = "main"
            }
        }

        "main" -> {
            // Right-side Drawer configuration using RTL wrapper
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
                                onUpdateUserName = { viewModel.setUserName(it) },
                                onUpdateLanguage = { viewModel.setLanguage(it) },
                                onUpdateChartStyle = { viewModel.setChartStyle(it) },
                                onUpdateAyanamsa = { viewModel.setAyanamsa(it) },
                                onUpdateNotifications = { en, h, m -> viewModel.updateNotifications(en, h, m) },
                                onExportJson = { exportJsonLauncher.launch("kundali_guru_backup.json") },
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
                                            // Left: Screen Title & Logo
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { currentTab = "kundali" }
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.ic_kundali_logo),
                                                    contentDescription = "Logo",
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(CircleShape)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = "Kundali Guru",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = DeepIndigo
                                                )
                                            }

                                            // Center: Person Selector Dropdown (Kundali tab only)
                                            if (currentTab == "kundali") {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = Color.White,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                                                    modifier = Modifier
                                                        .clickable { showPersonSelectorSheet = true }
                                                        .testTag("top_bar_person_selector")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = selectedPerson?.name ?: (if (isHindi) "+ जोड़ें" else "+ Add"),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = DeepIndigo,
                                                            maxLines = 1
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Icon(
                                                            Icons.Default.ArrowDropDown,
                                                            contentDescription = "Dropdown",
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
                                        // Right: Profile Avatar 👤
                                        IconButton(
                                            onClick = { coroutineScope.launch { drawerState.open() } },
                                            modifier = Modifier.testTag("profile_avatar_btn")
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(DeepIndigo, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Profile",
                                                    tint = SaffronAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
                                )
                            },
                            bottomBar = {
                                // EXACTLY 2 TABS: Kundali & Shaadi Milan
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
                                                contentDescription = "Kundali",
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (isHindi) "कुंडली" else "Kundali",
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
                                        selected = currentTab == "shaadi_milan",
                                        onClick = { currentTab = "shaadi_milan" },
                                        icon = {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = "Shaadi Milan",
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (isHindi) "शादी मिलान" else "Shaadi Milan",
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
                                    .padding(innerPadding)
                            ) {
                                if (currentTab == "kundali") {
                                    KundaliScreen(
                                        person = selectedPerson,
                                        kundali = kundaliData,
                                        chartStyle = chartStyle,
                                        isHindi = isHindi,
                                        onChartStyleChanged = { viewModel.setChartStyle(it) },
                                        onNavigateToTodayPrediction = { currentScreen = "today_prediction" },
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
                                            val nameFmt = selectedPerson?.name?.replace(" ", "_") ?: "Kundali"
                                            exportPdfLauncher.launch("${nameFmt}_Kundali.pdf")
                                        },
                                        onShareJson = {
                                            selectedPerson?.let { p ->
                                                val shareText = "Kundali Guru Profile: ${p.name}\nBirth Date: ${p.birthDate}\nBirth Time: ${p.birthTime}\nPlace: ${p.birthPlace}\nCoordinates: ${p.latitude}, ${p.longitude}"
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "Share Horoscope")
                                                context.startActivity(shareIntent)
                                            }
                                        }
                                    )
                                } else {
                                    ShaadiMilanScreen(
                                        persons = allPersons,
                                        isHindi = isHindi,
                                        predictionsRepo = viewModel.predictionsRepo,
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

            // Person Selector Bottom Sheet
            if (showPersonSelectorSheet) {
                PersonSelectorBottomSheet(
                    persons = allPersons,
                    selectedPersonId = selectedPerson?.id,
                    isHindi = isHindi,
                    onSelectPerson = { viewModel.selectPerson(it) },
                    onAddNewPerson = {
                        personToEdit = null
                        currentScreen = "add_edit_person"
                    },
                    onEditPerson = { p ->
                        personToEdit = p
                        currentScreen = "add_edit_person"
                    },
                    onDuplicatePerson = { p -> viewModel.duplicatePerson(p) },
                    onDeletePerson = { p -> viewModel.deletePerson(p) },
                    onDismiss = { showPersonSelectorSheet = false }
                )
            }
        }
    }
}
