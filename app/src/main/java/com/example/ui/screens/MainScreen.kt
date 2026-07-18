package com.example.ui.screens

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.PdfGenerator
import com.example.ui.theme.RedDeduction
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Observers
    val rickshaws by viewModel.allRickshaws.collectAsState()
    val rooms by viewModel.allRooms.collectAsState()
    val weeklySummaries by viewModel.weeklyRickshawSummaries.collectAsState()
    val monthlySummaries by viewModel.monthlyShopSummaries.collectAsState()
    val monthlyRoomSummaries by viewModel.monthlyRoomSummaries.collectAsState()
    val deductions by viewModel.allDeductions.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val families by viewModel.familiesState.collectAsState()
    val paidFamiliesMap by viewModel.paidFamiliesMapState.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()
    val familyCustomAmounts by viewModel.familyCustomAmountsState.collectAsState()

    // Screen Viewer States
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var showBackupRestoreScreen by remember { mutableStateOf(false) }

    if (showBackupRestoreScreen) {
        BackupRestoreScreen(
            viewModel = viewModel,
            onBack = { showBackupRestoreScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                val (title, subtitle) = when (selectedTab) {
                    0 -> "إيرادات المجموعة" to "تحديث: ${AppViewModel.formatSimpleDate(System.currentTimeMillis())}"
                    1 -> "إيرادات الركشات" to "تسجيل الإيراد الأسبوعي للركشات وإدارة الأسطول"
                    2 -> "إيرادات الدكان" to "تسجيل الإيراد الشهري والمبيعات"
                    3 -> "إيجار الغرف" to "متابعة وتحصيل إيجار الغرف شهرياً"
                    4 -> "كشف توزيعات الأسر" to "متابعة أنصبة الأسر والأرصدة"
                    5 -> "الخصومات والمصاريف" to "تسجيل فوري للمنصرفات والخصومات"
                    6 -> "التقارير والمستندات" to "توثيق الأعمال وتصدير كشوفات الحساب"
                    else -> "إيرادات المجموعة" to ""
                }
                HighDensityHeader(
                    title = title,
                    subtitle = subtitle,
                    onNotificationClick = {
                        Toast.makeText(context, "لا توجد إشعارات جديدة حالياً", Toast.LENGTH_SHORT).show()
                    },
                    onBackupClick = {
                        showBackupRestoreScreen = true
                    }
                )
            },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3F4F9),
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = Color(0xFFC4C6CF).copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.TwoWheeler, contentDescription = "الركشات") },
                    label = { Text("الركشات", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_rickshaws")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "الدكان") },
                    label = { Text("الدكان", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_shop")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الغرف") },
                    label = { Text("الغرف", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_rooms")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.People, contentDescription = "التوزيعات") },
                    label = { Text("التوزيعات", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_distributions")
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.TrendingDown, contentDescription = "الخصومات") },
                    label = { Text("الخصومات", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_deductions")
                )
                NavigationBarItem(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    icon = { Icon(Icons.Default.Description, contentDescription = "التقارير") },
                    label = { Text("التقارير", fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0061A4),
                        selectedTextColor = Color(0xFF0061A4),
                        unselectedIconColor = Color(0xFF44474E),
                        unselectedTextColor = Color(0xFF44474E),
                        indicatorColor = Color(0xFFD1E4FF)
                    ),
                    modifier = Modifier.testTag("tab_reports")
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Animated transitions between tabs
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        weeklySummaries = weeklySummaries,
                        monthlySummaries = monthlySummaries,
                        monthlyRoomSummaries = monthlyRoomSummaries,
                        families = families,
                        onSaveFamilies = { list -> viewModel.saveFamilies(list) },
                        onViewImage = { fullScreenImagePath = it },
                        onNavigateToTab = { selectedTab = it },
                        onGenerateMockData = {
                            if (rickshaws.isEmpty()) {
                                viewModel.addRickshaw("ركشة رقم ١", "أحمد محمد")
                            }
                            val rid = if (rickshaws.isNotEmpty()) rickshaws.first().id else 1
                            viewModel.addRickshawRevenue(
                                rickshawId = rid,
                                amount = 18500.0,
                                date = System.currentTimeMillis(),
                                notes = "إيراد أسبوعي - الركشة الأولى",
                                screenshotUris = emptyList()
                            )
                            viewModel.addDeduction(
                                amount = 2500.0,
                                description = "صيانة دورية وتغيير زيت",
                                category = "RICKSHAW",
                                targetId = if (rickshaws.isNotEmpty()) rid else null,
                                date = System.currentTimeMillis(),
                                screenshotUris = emptyList()
                            )
                            Toast.makeText(context, "تم توليد بيانات إيرادات وخصومات تجريبية لتبسيط العرض!", Toast.LENGTH_LONG).show()
                        }
                    )
                    1 -> RickshawRevenuesScreen(
                        viewModel = viewModel,
                        rickshaws = rickshaws,
                        weeklySummaries = weeklySummaries,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    2 -> ShopRevenuesScreen(
                        viewModel = viewModel,
                        monthlySummaries = monthlySummaries,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    3 -> RoomRevenuesScreen(
                        viewModel = viewModel,
                        rooms = rooms,
                        monthlyRoomSummaries = monthlyRoomSummaries,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    4 -> FamilyDistributionsScreen(viewModel = viewModel)
                    5 -> DeductionsScreen(
                        viewModel = viewModel,
                        rickshaws = rickshaws,
                        deductions = deductions,
                        onViewImage = { fullScreenImagePath = it }
                    )
                    6 -> ReportsScreen(
                        viewModel = viewModel,
                        reports = reports,
                        onViewImage = { fullScreenImagePath = it }
                    )
                }
            }

            // Full screen Image Viewer Dialog
            fullScreenImagePath?.let { path ->
                ImageViewerDialog(
                    imagePath = path,
                    onDismiss = { fullScreenImagePath = null }
                )
            }
        }
    }
}
}

data class LocalFamilyItem(
    val id: Int,
    val name: String,
    val portion: String
)

// ==========================================
// 1. DASHBOARD SCREEN (الرئيسية)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyEditDialog(
    families: List<AppViewModel.FamilyConfig>,
    useFixedPortion: Boolean,
    onUseFixedPortionChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSaveFamilies: (List<AppViewModel.FamilyConfig>) -> Unit
) {
    var localFamilies by remember {
        mutableStateOf(
            families.map { 
                val portionText = if (it.portion <= 0.0) "" else String.format(Locale.US, "%.0f", it.portion)
                LocalFamilyItem(it.id, it.name, portionText) 
            }
        )
    }
    var localUseFixedPortion by remember { mutableStateOf(useFixedPortion) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إدارة أسماء وأنصبة الأسر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "عدّل اسم كل أسرة وقيمة نصيبها الأسبوعي المحدد بالمبلغ (وليس بالنسبة). الأسرة ذات النصيب 0 تُعتبر غير نشطة. يمكنك إضافة وحذف الأسر ديناميكياً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Toggle inside Dialog
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "تفعيل النصيب المحدد للأسر",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "تفعيل هذا الخيار يعتمد المبلغ الثابت المدخل بجانب كل أسرة. إلغاء التفعيل يقسم صافي أرباح الأسبوع بالتساوي.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                fontSize = 9.sp
                            )
                        }
                        Switch(
                            checked = localUseFixedPortion,
                            onCheckedChange = { localUseFixedPortion = it },
                            modifier = Modifier.testTag("dialog_use_fixed_portion_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localFamilies.forEachIndexed { i, family ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F8)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFD1E4FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${i + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF001D36)
                                    )
                                }

                                OutlinedTextField(
                                    value = family.name,
                                    onValueChange = { newValue ->
                                        localFamilies = localFamilies.toMutableList().apply {
                                            this[i] = this[i].copy(name = newValue)
                                        }
                                    },
                                    label = { Text("الاسم", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.8f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1B1B1F),
                                        unfocusedTextColor = Color(0xFF1B1B1F),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedLabelColor = Color(0xFF0061A4),
                                        unfocusedLabelColor = Color(0xFF44474E)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = family.portion,
                                    onValueChange = { newValue ->
                                        localFamilies = localFamilies.toMutableList().apply {
                                            this[i] = this[i].copy(portion = newValue)
                                        }
                                    },
                                    label = { Text("المبلغ المحدد (SDG)", fontSize = 10.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1B1B1F),
                                        unfocusedTextColor = Color(0xFF1B1B1F),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedLabelColor = Color(0xFF0061A4),
                                        unfocusedLabelColor = Color(0xFF44474E)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                IconButton(
                                    onClick = {
                                        localFamilies = localFamilies.toMutableList().apply {
                                            removeAt(i)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف الأسرة",
                                        tint = Color(0xFFBA1A1A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button to add a new family with portion field right there
                    Button(
                        onClick = {
                            val nextId = if (localFamilies.isEmpty()) 1 else (localFamilies.maxOf { it.id }) + 1
                            localFamilies = localFamilies + LocalFamilyItem(nextId, "أسرة $nextId", "")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD1E4FF),
                            contentColor = Color(0xFF001D36)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp).testTag("add_family_option_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة أسرة جديدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val updatedFamilies = localFamilies.map { family ->
                                val name = family.name.takeIf { it.isNotBlank() } ?: "أسرة ${family.id}"
                                val portion = family.portion.toDoubleOrNull() ?: 0.0
                                AppViewModel.FamilyConfig(id = family.id, name = name, portion = portion)
                            }
                            onUseFixedPortionChanged(localUseFixedPortion)
                            onSaveFamilies(updatedFamilies)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RickshawsEditDialog(
    rickshaws: List<Rickshaw>,
    onDismiss: () -> Unit,
    onAddRickshaw: (String, String) -> Unit,
    onUpdateRickshaw: (Rickshaw) -> Unit,
    onDeleteRickshaw: (Rickshaw) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newDriver by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إدارة ركشات الأسطول",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يمكنك تعديل أسماء الركشات وسائقيها، أو حذف ركشة من الأسطول، أو إضافة ركشة جديدة بالأسفل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // List of existing Rickshaws
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rickshaws.isEmpty()) {
                        Text(
                            text = "لا توجد ركشات مسجلة حالياً.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        rickshaws.forEach { r ->
                            // Local temporary state for editing each row
                            var editName by remember(r.id) { mutableStateOf(r.name) }
                            var editDriver by remember(r.id) { mutableStateOf(r.driverName) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F8)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("الاسم/الرقم", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = editDriver,
                                        onValueChange = { editDriver = it },
                                        label = { Text("السائق", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (editName.isNotBlank() && editDriver.isNotBlank()) {
                                                onUpdateRickshaw(r.copy(name = editName, driverName = editDriver))
                                                Toast.makeText(context, "تم حفظ التعديل بنجاح", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "الرجاء عدم ترك الحقول فارغة", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "حفظ التعديل",
                                            tint = Color(0xFF386A20),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteRickshaw(r) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الركشة",
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section to add a new Rickshaw
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "إضافة ركشة جديدة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061A4)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("اسم/رقم الركشة", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newDriver,
                                onValueChange = { newDriver = it },
                                label = { Text("اسم السائق", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (newName.isNotBlank() && newDriver.isNotBlank()) {
                                        onAddRickshaw(newName, newDriver)
                                        newName = ""
                                        newDriver = ""
                                        Toast.makeText(context, "تمت إضافة الركشة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الرجاء تعبئة حقول الركشة الجديدة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44474E)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsEditDialog(
    rooms: List<com.example.data.Room>,
    onDismiss: () -> Unit,
    onAddRoom: (String, String) -> Unit,
    onUpdateRoom: (com.example.data.Room) -> Unit,
    onDeleteRoom: (com.example.data.Room) -> Unit
) {
    var newRoomName by remember { mutableStateOf("") }
    var newTenant by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "إدارة غرف الإيجار",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006874),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يمكنك تعديل أسماء الغرف ومستأجريها، أو حذف غرفة، أو إضافة غرفة جديدة بالأسفل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // List of existing Rooms
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rooms.isEmpty()) {
                        Text(
                            text = "لا توجد غرف مسجلة حالياً.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        rooms.forEach { r ->
                            // Local temporary state for editing each row
                            var editName by remember(r.id) { mutableStateOf(r.name) }
                            var editTenant by remember(r.id) { mutableStateOf(r.tenantName) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F8)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("رقم/اسم الغرفة", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = editTenant,
                                        onValueChange = { editTenant = it },
                                        label = { Text("المستأجر", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = Color(0xFF1B1B1F),
                                            unfocusedTextColor = Color(0xFF1B1B1F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (editName.isNotBlank() && editTenant.isNotBlank()) {
                                                onUpdateRoom(r.copy(name = editName, tenantName = editTenant))
                                                Toast.makeText(context, "تم حفظ التعديل بنجاح", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "الرجاء عدم ترك الحقول فارغة", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "حفظ التعديل",
                                            tint = Color(0xFF386A20),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteRoom(r) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الغرفة",
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section to add a new Room
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "إضافة غرفة جديدة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006874)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newRoomName,
                                onValueChange = { newRoomName = it },
                                label = { Text("اسم/رقم الغرفة", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newTenant,
                                onValueChange = { newTenant = it },
                                label = { Text("اسم المستأجر", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF1B1B1F),
                                    unfocusedTextColor = Color(0xFF1B1B1F)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (newRoomName.isNotBlank() && newTenant.isNotBlank()) {
                                        onAddRoom(newRoomName, newTenant)
                                        newRoomName = ""
                                        newTenant = ""
                                        Toast.makeText(context, "تمت إضافة الغرفة بنجاح", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الرجاء تعبئة حقول الغرفة الجديدة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006874)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44474E)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DashboardOverviewCard(
    grandTotalRevenue: Double,
    totalRickshawRevenue: Double,
    totalShopRevenue: Double,
    totalRoomRevenue: Double,
    grandTotalDeductions: Double,
    grandTotalPaidToFamilies: Double
) {
    val netTreasuryBalance = grandTotalRevenue - (grandTotalDeductions + grandTotalPaidToFamilies)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_overview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFF0061A4),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "كشف الحساب المجمع للمجموعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0061A4)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0061A4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تحديث فوري",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grand total collected amount
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "إجمالي المبالغ الكلية المقبوضة (المجموع الكلي)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44474E),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.2f", grandTotalRevenue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "SDG",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF44474E),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Net Treasury Balance (Prominent & Large, just like grand total)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (netTreasuryBalance >= 0) Color(0xFFE8F5E9) else Color(0xFFFFE9E9),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (netTreasuryBalance >= 0) Color(0xFF386A20).copy(alpha = 0.3f) else Color(0xFFBA1A1A).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "رصيد الخزنة المتبقي (بعد الخصومات والتوزيعات)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (netTreasuryBalance >= 0) Color(0xFF2E6C2F) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.2f", netTreasuryBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netTreasuryBalance >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                    Text(
                        text = "SDG",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (netTreasuryBalance >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Breakdown Grid (3 rows)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rickshaws item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إيرادات الركشات الكلية",
                        amount = totalRickshawRevenue,
                        icon = Icons.Default.TwoWheeler,
                        iconColor = Color(0xFF0061A4),
                        bgColor = Color(0xFFE0EAFC)
                    )
                    // Shop item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إيرادات الدكان الكلية",
                        amount = totalShopRevenue,
                        icon = Icons.Default.Storefront,
                        iconColor = Color(0xFF386A20),
                        bgColor = Color(0xFFE8F5E9)
                    )
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Room item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إيرادات الغرف الكلية",
                        amount = totalRoomRevenue,
                        icon = Icons.Default.ReceiptLong,
                        iconColor = Color(0xFF006874),
                        bgColor = Color(0xFFE0F7FA)
                    )
                    // Deductions item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي المصاريف والخصومات",
                        amount = grandTotalDeductions,
                        icon = Icons.Default.TrendingDown,
                        iconColor = Color(0xFFBA1A1A),
                        bgColor = Color(0xFFFFE9E9)
                    )
                }
                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Distributed item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "المسلم والموزع للأسر",
                        amount = grandTotalPaidToFamilies,
                        icon = Icons.Default.People,
                        iconColor = Color(0xFF555F71),
                        bgColor = Color(0xFFF3F4F9)
                    )
                    // Net balance item
                    BreakdownItem(
                        modifier = Modifier.weight(1f),
                        title = "رصيد الخزنة المتبقي",
                        amount = netTreasuryBalance,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = if (netTreasuryBalance >= 0) Color(0xFF386A20) else Color(0xFFBA1A1A),
                        bgColor = if (netTreasuryBalance >= 0) Color(0xFFE8F5E9) else Color(0xFFFFE9E9)
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownItem(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF44474E),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(bgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = String.format(Locale.US, "%,.0f", amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B1B1F)
            )
            Text(
                text = "جنيه سوداني",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF44474E).copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    weeklySummaries: List<AppViewModel.WeeklySummary>,
    monthlySummaries: List<AppViewModel.MonthlySummary>,
    monthlyRoomSummaries: List<AppViewModel.RoomMonthlySummary>,
    families: List<AppViewModel.FamilyConfig>,
    onSaveFamilies: (List<AppViewModel.FamilyConfig>) -> Unit,
    onViewImage: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onGenerateMockData: () -> Unit
) {
    var showFamilyEditDialog by remember { mutableStateOf(false) }
    val paidFamiliesMap by viewModel.paidFamiliesMapState.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()
    val familyCustomAmounts by viewModel.familyCustomAmountsState.collectAsState()
    val useFixedPortion by viewModel.useFixedPortionState.collectAsState()

    val totalRickshawRevenue = weeklySummaries.sumOf { it.totalRevenue }
    val totalShopRevenue = monthlySummaries.sumOf { it.totalRevenue }
    val totalRoomRevenue = monthlyRoomSummaries.sumOf { it.totalRevenue }
    val grandTotalRevenue = totalRickshawRevenue + totalShopRevenue + totalRoomRevenue

    val grandTotalDeductions = weeklySummaries.sumOf { it.totalDeductions } +
            monthlySummaries.sumOf { it.totalDeductions } +
            monthlyRoomSummaries.sumOf { it.totalDeductions }

    // Calculate total paid/distributed to families
    var grandTotalPaidToFamilies = 0.0
    for (summary in weeklySummaries) {
        val paidIds = paidFamiliesMap[summary.weekKey] ?: emptySet()
        val activeFamiliesCount = families.count { it.portion > 0 }
        
        val weekUseFixedPortion = viewModel.getUseFixedPortionForWeek(summary.weekKey)
        val defaultFamilyShare = if (weekUseFixedPortion) 0.0 else {
            if (summary.netRevenue > 0 && activeFamiliesCount > 0) summary.netRevenue / activeFamiliesCount else 0.0
        }

        for (family in families) {
            val customAmount = familyCustomAmounts["${summary.weekKey}_${family.id}"]
            val isCustomActive = customAmount != null
            val isFamilyActive = family.portion > 0 || isCustomActive
            if (isFamilyActive && paidIds.contains(family.id)) {
                val familyShare = if (family.portion <= 0) 0.0 else {
                    customAmount ?: (if (weekUseFixedPortion) (family.portion * summary.sharePerFamily) else defaultFamilyShare)
                }
                grandTotalPaidToFamilies += familyShare
            }
        }
    }

    if (showFamilyEditDialog) {
        FamilyEditDialog(
            families = families,
            useFixedPortion = useFixedPortion,
            onUseFixedPortionChanged = { viewModel.setUseFixedPortion(it) },
            onDismiss = { showFamilyEditDialog = false },
            onSaveFamilies = onSaveFamilies
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grand Overview Card
        item {
            DashboardOverviewCard(
                grandTotalRevenue = grandTotalRevenue,
                totalRickshawRevenue = totalRickshawRevenue,
                totalShopRevenue = totalShopRevenue,
                totalRoomRevenue = totalRoomRevenue,
                grandTotalDeductions = grandTotalDeductions,
                grandTotalPaidToFamilies = grandTotalPaidToFamilies
            )
        }

        // Section 1: Rickshaw Weekly Revenues & Division
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تقسيم إيرادات الركشات أسبوعياً",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4)
                )
                Button(
                    onClick = { showFamilyEditDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E2EC),
                        contentColor = Color(0xFF1B1B1F)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("edit_families_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إدارة الأسر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (weeklySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لم يتم تسجيل أي إيراد ركشات حتى الآن. توجه لتبويب الإيرادات لإضافة أول إدخال أسبوعي، أو استخدم زر سكرين شوت أدناه للتوليد السريع.",
                    icon = Icons.Default.TwoWheeler
                )
            }
        } else {
            items(weeklySummaries.take(3)) { summary ->
                val currentPaid = paidFamiliesMap[summary.weekKey] ?: emptySet()
                val isReported = reportedWeeks.contains(summary.weekKey)
                val weekUseFixedPortion = viewModel.getUseFixedPortionForWeek(summary.weekKey)
                WeeklySummaryCard(
                    summary = summary,
                    families = families,
                    currentPaid = currentPaid,
                    isReported = isReported,
                    useFixedPortion = weekUseFixedPortion,
                    customAmounts = familyCustomAmounts,
                    onTogglePaid = { familyId, isPaid ->
                        viewModel.toggleFamilyPayment(summary.weekKey, familyId, isPaid)
                    },
                    onGenerateReport = {
                        viewModel.generateWeeklyDistributionReport(summary)
                    },
                    onViewImage = onViewImage
                )
            }
        }

        // Section 2: Shop Monthly Revenues
        item {
            Text(
                text = "متابعة إيرادات الدكان شهرياً",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1F),
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }

        if (monthlySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لم يتم تسجيل أي إيراد للدكان حتى الآن. توجه لتبويب الإيرادات لإضافة إدخال الدكان الشهري.",
                    icon = Icons.Default.Storefront
                )
            }
        } else {
            items(monthlySummaries.take(3)) { summary ->
                MonthlySummaryCard(summary = summary, onViewImage = onViewImage)
            }
        }

        // Quick action buttons at the bottom of the scrollable column to match the HTML design
        item {
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionsRow(
                onScreenshotClick = onGenerateMockData,
                onNewReportClick = { onNavigateToTab(6) },
                onDeductionClick = { onNavigateToTab(5) }
            )
        }
    }
}

@Composable
fun HighDensityHeader(
    title: String,
    subtitle: String,
    onNotificationClick: () -> Unit = {},
    onBackupClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF6F8FC))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFD1E4FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF001D36),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Backup/Cloud configuration button
            IconButton(
                onClick = onBackupClick,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f), CircleShape)
                    .size(40.dp)
                    .testTag("header_backup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "النسخ الاحتياطي والربط",
                    tint = Color(0xFF0061A4),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.5f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1B1B1F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onScreenshotClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onDeductionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 📷 Screenshot button
        Button(
            onClick = onScreenshotClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD1E4FF),
                contentColor = Color(0xFF001D36)
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📷", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("سكرين شوت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 📝 New Report button
        Button(
            onClick = onNewReportClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD1E4FF),
                contentColor = Color(0xFF001D36)
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📝", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("تقرير جديد", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 💸 Deduct button
        Button(
            onClick = onDeductionClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBA1A1A),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("💸", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("خصم مبلغ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FamilyDistributionGrid(
    weekKey: String,
    sharePerFamily: Double,
    families: List<AppViewModel.FamilyConfig>,
    currentPaid: Set<Int>,
    useFixedPortion: Boolean,
    customAmounts: Map<String, Double> = emptyMap(),
    onTogglePaid: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF0061A4), CircleShape)
                    )
                    Text(
                        text = "حالة توزيع الأسر للأسبوع الحالي",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1F)
                    )
                }
                Text(
                    text = "اضغط للتغيير",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF44474E),
                    fontSize = 10.sp
                )
            }

            // 3-column chunked grid
            val chunkedFamilies = families.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedFamilies.forEach { rowFamilies ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowFamilies.forEach { family ->
                            val index = family.id
                            val isPaid = currentPaid.contains(index)
                            val bg = if (isPaid) Color(0xFFF1F3F8) else Color(0xFFFFF4E5)
                            val textColor = if (isPaid) Color(0xFF1B1B1F) else Color(0xFF8B5000)
                            val borderMod = if (isPaid) {
                                Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(12.dp))
                            } else {
                                Modifier.border(1.dp, Color(0xFFE9C3C3), RoundedCornerShape(12.dp))
                            }

                            val familyName = family.name
                            val customAmount = customAmounts["${weekKey}_${family.id}"]
                            val hasCustom = customAmount != null
                            val familyShare = if (family.portion <= 0) 0.0 else {
                                customAmount ?: (if (useFixedPortion) (family.portion * sharePerFamily) else sharePerFamily)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .then(borderMod)
                                    .clickable {
                                        onTogglePaid(index, !isPaid)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = familyName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1B1B1F),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 10.sp
                                    )
                                    if (familyShare > 0) {
                                        Text(
                                            text = String.format(Locale.US, "%,.0f SDG", familyShare),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (hasCustom) MaterialTheme.colorScheme.primary else Color(0xFF0061A4),
                                            fontSize = 9.sp,
                                            fontWeight = if (hasCustom) FontWeight.Bold else FontWeight.Medium
                                        )
                                    } else {
                                        Text(
                                            text = "غير نشط",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFBA1A1A),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isPaid) "تم الاستلام" else "لم يستلم",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        if (rowFamilies.size < 3) {
                            for (i in 0 until (3 - rowFamilies.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(
    summary: AppViewModel.WeeklySummary,
    families: List<AppViewModel.FamilyConfig>,
    currentPaid: Set<Int>,
    isReported: Boolean,
    useFixedPortion: Boolean,
    customAmounts: Map<String, Double> = emptyMap(),
    onTogglePaid: (Int, Boolean) -> Unit,
    onGenerateReport: () -> Unit,
    onViewImage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Week Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF0061A4), CircleShape)
                )
                Text(
                    text = summary.weekKey,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F)
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFD1E4FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ركشات - أسبوعي",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF001D36),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Grid of values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column 1: Total Weekly Revenue Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي الأسبوعي",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+",
                            color = Color(0xFF0061A4),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF44474E).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }

            // Column 2: Deductions Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE9E9))
                    .border(1.dp, Color(0xFFE9C3C3), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الخصومات (صيانة)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "-",
                            color = Color(0xFFBA1A1A),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalDeductions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBA1A1A)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFBA1A1A).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Main Net Profit Distribution Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0061A4))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeCount = families.count { 
                        it.portion > 0 || (customAmounts["${summary.weekKey}_${it.id}"] ?: 0.0) > 0.0 
                    }
                    Text(
                        text = "صافي الربح للتوزيع ($activeCount أسر نشطة)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "رقمي",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.netRevenue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeFamiliesCount = families.count { it.portion > 0 }
                    val defaultFamilyShare = if (useFixedPortion) 0.0 else {
                        if (summary.netRevenue > 0 && activeFamiliesCount > 0) summary.netRevenue / activeFamiliesCount else 0.0
                    }
                    val totalActivePayout = families.sumOf { family ->
                        if (family.portion <= 0) 0.0 else {
                            customAmounts["${summary.weekKey}_${family.id}"] ?: (if (useFixedPortion) (family.portion * summary.sharePerFamily) else defaultFamilyShare)
                        }
                    }
                    Text(
                        text = "إجمالي أنصبة الأسر المحددة للأسبوع:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%,.0f SDG", totalActivePayout),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Interactive Families Status Grid
        FamilyDistributionGrid(
            weekKey = summary.weekKey,
            sharePerFamily = summary.sharePerFamily,
            families = families,
            currentPaid = currentPaid,
            useFixedPortion = useFixedPortion,
            customAmounts = customAmounts,
            onTogglePaid = onTogglePaid
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (!isReported) {
            Button(
                onClick = onGenerateReport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0061A4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("settle_week_button_${summary.weekKey}")
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ التوزيع وعمل التقرير النهائي للأسبوع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0061A4), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تم حفظ التوزيع وعمل التقرير النهائي للأسبوع بنجاح", color = Color(0xFF0061A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Attached screenshots list if any
        val pathWithScreenshots = (summary.revenues.mapNotNull { it.screenshotPath } +
                summary.deductions.mapNotNull { it.screenshotPath })
                .flatMap { it.split("|") }
                .filter { it.isNotEmpty() }
                .distinct()

        if (pathWithScreenshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "لقطات الشاشة المرفقة بهذا الأسبوع:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                pathWithScreenshots.forEach { path ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(8.dp))
                            .clickable { onViewImage(path) }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Screenshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    summary: AppViewModel.MonthlySummary,
    onViewImage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Month Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF0061A4), CircleShape)
                )
                Text(
                    text = summary.monthKey,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1F)
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFFE0E2EC), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "الدكان - شهري",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1B1B1F),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Grid of values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column 1: Total Revenue Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إيراد الدكان",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+",
                            color = Color(0xFF0061A4),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF44474E).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }

            // Column 2: Deductions Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE9E9))
                    .border(1.dp, Color(0xFFE9C3C3), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "خصومات ومصاريف",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44474E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "-",
                            color = Color(0xFFBA1A1A),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.totalDeductions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBA1A1A)
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFBA1A1A).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Main Net Profit Distribution Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0061A4))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "صافي ربح الدكان للتوزيع",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "شهري",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.0f", summary.netRevenue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "جنيه سوداني",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        // Attached screenshots list if any
        val pathWithScreenshots = (summary.revenues.mapNotNull { it.screenshotPath } +
                summary.deductions.mapNotNull { it.screenshotPath })
                .flatMap { it.split("|") }
                .filter { it.isNotEmpty() }
                .distinct()

        if (pathWithScreenshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "لقطات الشاشة المرفقة بهذا الشهر:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                pathWithScreenshots.forEach { path ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFC4C6CF), RoundedCornerShape(8.dp))
                            .clickable { onViewImage(path) }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Screenshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialMiniCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.US, "%,.0f", amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "SDG",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}


// ==========================================
// 2. RICKSHAW REVENUES SCREEN (إيرادات الركشات)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RickshawRevenuesScreen(
    viewModel: AppViewModel,
    rickshaws: List<Rickshaw>,
    weeklySummaries: List<AppViewModel.WeeklySummary>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current
    val families by viewModel.familiesState.collectAsState()
    val useFixedPortion by viewModel.useFixedPortionState.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()

    // Forms fields
    var rickshawAmounts by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var rickshawNotes by remember { mutableStateOf("") }
    var rickshawDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var rickshawScreenshotUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dialog state
    var showRickshawsEditDialog by remember { mutableStateOf(false) }
    var pendingRevenuesToSave by remember { mutableStateOf<List<Pair<Int, Double>>?>(null) }
    var showFamilySharesDialog by remember { mutableStateOf(false) }

    // Setup photo picker
    val rickshawPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) rickshawScreenshotUris = rickshawScreenshotUris + uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تسجيل إيرادات الركشات الأسبوعية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Fleet management settings button
                        Button(
                            onClick = { showRickshawsEditDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E2EC),
                                contentColor = Color(0xFF1B1B1F)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("manage_rickshaws_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدارة الأسطول", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Loop through all registered rickshaws and show input fields
                    if (rickshaws.isEmpty()) {
                        Text(
                            text = "لا توجد ركشات مضافة حالياً. الرجاء إضافة ركشة أولاً من زر 'إدارة الأسطول'.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "الرجاء إدخال إيراد كل ركشة نشطة لهذا الأسبوع:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        rickshaws.forEach { r ->
                            val amtText = rickshawAmounts[r.id] ?: ""
                            OutlinedTextField(
                                value = amtText,
                                onValueChange = { newValue ->
                                    rickshawAmounts = rickshawAmounts.toMutableMap().apply {
                                        put(r.id, newValue)
                                    }
                                },
                                label = { Text("إيراد ${r.name} (السائق: ${r.driverName})") },
                                placeholder = { Text("0 SDG") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("rickshaw_amount_${r.id}"),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                        }

                        // Display dynamic aggregated total
                        val totalAggregatedRevenue = rickshaws.sumOf { r ->
                            rickshawAmounts[r.id]?.toDoubleOrNull() ?: 0.0
                        }

                        if (totalAggregatedRevenue > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("إجمالي الإيرادات المجمعة للركشات:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E40AF))
                                    Text("${String.format(Locale.US, "%,.0f", totalAggregatedRevenue)} SDG", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1D4ED8))
                                }
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = rickshawNotes,
                        onValueChange = { rickshawNotes = it },
                        label = { Text("ملاحظات إضافية...") },
                        modifier = Modifier.fillMaxWidth().testTag("rickshaw_notes"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Past/Custom Date Selector
                    PastDateSelector(
                        selectedDate = rickshawDate,
                        onDateSelected = { rickshawDate = it }
                    )

                    // Screenshot Selection & Simulated Preview
                    ScreenshotPickerSection(
                        selectedUris = rickshawScreenshotUris,
                        onSelectRealPhotos = {
                            rickshawPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            rickshawScreenshotUris = rickshawScreenshotUris + MockImageGenerator.generate(context)
                        },
                        onRemovePhoto = { uri ->
                            rickshawScreenshotUris = rickshawScreenshotUris.filter { it != uri }
                        }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val revenuesList = rickshaws.mapNotNull { r ->
                                val amt = rickshawAmounts[r.id]?.toDoubleOrNull()
                                if (amt != null && amt > 0) r.id to amt else null
                            }
                            
                            if (revenuesList.isEmpty()) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ إيراد صحيح لركشة واحدة على الأقل!", Toast.LENGTH_SHORT).show()
                            } else {
                                val weekKey = AppViewModel.getWeekRangeString(rickshawDate)
                                if (!useFixedPortion) {
                                    // Manual share entry flow
                                    pendingRevenuesToSave = revenuesList
                                    showFamilySharesDialog = true
                                } else {
                                    // Automatic fixed portion flow
                                    viewModel.saveRickshawRevenuesAndDistribute(
                                        weekKey = weekKey,
                                        revenues = revenuesList,
                                        date = rickshawDate,
                                        notes = rickshawNotes,
                                        screenshotUris = rickshawScreenshotUris,
                                        customShares = null
                                    )
                                    Toast.makeText(context, "تم حفظ الإيرادات وحساب الأنصبة المحددة تلقائياً بنجاح!", Toast.LENGTH_LONG).show()
                                    // Reset fields
                                    rickshawAmounts = emptyMap()
                                    rickshawNotes = ""
                                    rickshawScreenshotUris = emptyList()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_rickshaw_revenue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val btnText = if (!useFixedPortion) "حفظ الإيرادات وتوزيع الأنصبة يدوياً" else "حفظ الإيرادات وتوزيعها تلقائياً"
                        Text(btnText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Section: Registered Fleet or Fleet Summary
        item {
            Text(
                text = "سجل إيرادات الركشات الأسبوعية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of all entries
        if (weeklySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد إيرادات ركشات مسجلة حتى الآن.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(weeklySummaries) { summary ->
                val isReported = reportedWeeks.contains(summary.weekKey)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(summary.weekKey, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            
                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isReported) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isReported) "تم التوزيع والترحيل" else "قيد التوزيع والانتظار",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReported) Color(0xFF15803D) else Color(0xFFB45309)
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        
                        summary.revenues.forEach { rev ->
                            val rName = rickshaws.find { it.id == rev.rickshawId }?.name ?: "ركشة غير معروفة"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "$rName • ${AppViewModel.formatSimpleDate(rev.date)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (rev.notes.isNotEmpty()) {
                                        Text(text = rev.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = String.format(Locale.US, "%,.0f SDG", rev.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    
                                    rev.screenshotPath?.split("|")?.filter { it.isNotEmpty() }?.forEachIndexed { index, path ->
                                        IconButton(onClick = { onViewImage(path) }) {
                                            Icon(Icons.Default.Image, contentDescription = "عرض لقطة الشاشة ${index + 1}", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteRickshawRevenue(rev) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedDeduction, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        // Reopen Button
                        if (isReported) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.reopenWeekForEditing(summary.weekKey)
                                    Toast.makeText(context, "تم إعادة فتح الأسبوع لتعديل التوزيع وإعادة التسجيل!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEE2E2),
                                    contentColor = Color(0xFFDC2626)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة فتح الأسبوع للتعديل والتوزيع", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRickshawsEditDialog) {
        RickshawsEditDialog(
            rickshaws = rickshaws,
            onDismiss = { showRickshawsEditDialog = false },
            onAddRickshaw = { name, driver -> viewModel.addRickshaw(name, driver) },
            onUpdateRickshaw = { r -> viewModel.updateRickshaw(r) },
            onDeleteRickshaw = { r -> viewModel.deleteRickshaw(r) }
        )
    }

    if (showFamilySharesDialog && pendingRevenuesToSave != null) {
        FamilySharesInputDialog(
            families = families,
            totalRevenue = pendingRevenuesToSave!!.sumOf { it.second },
            onDismiss = {
                showFamilySharesDialog = false
                pendingRevenuesToSave = null
            },
            onConfirm = { customSharesMap ->
                val weekKey = AppViewModel.getWeekRangeString(rickshawDate)
                viewModel.saveRickshawRevenuesAndDistribute(
                    weekKey = weekKey,
                    revenues = pendingRevenuesToSave!!,
                    date = rickshawDate,
                    notes = rickshawNotes,
                    screenshotUris = rickshawScreenshotUris,
                    customShares = customSharesMap
                )
                Toast.makeText(context, "تم تسجيل الإيرادات وتوزيع الأنصبة المخصصة للأسر بنجاح!", Toast.LENGTH_LONG).show()
                // Reset fields
                rickshawAmounts = emptyMap()
                rickshawNotes = ""
                rickshawScreenshotUris = emptyList()
                showFamilySharesDialog = false
                pendingRevenuesToSave = null
            }
        )
    }
}

@Composable
fun FamilySharesInputDialog(
    families: List<AppViewModel.FamilyConfig>,
    totalRevenue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, Double>) -> Unit
) {
    var familyShares by remember {
        mutableStateOf(
            families.associate { it.id to "" }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "توزيع الأنصبة على الأسر للأسبوع",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061A4),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "صافي الإيراد المجمع للركشات: ${String.format(Locale.US, "%,.0f", totalRevenue)} SDG. الرجاء إدخال نصيب (مبلغ بالجنيه) كل أسرة نشطة من هذا الإيراد:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF44474E),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable list of active families
                val activeFamilies = families.filter { it.portion > 0 }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeFamilies.isEmpty()) {
                        Text(
                            text = "لا توجد أسر نشطة مضافة في النظام حالياً! يرجى إضافتها أولاً من صفحة إدارة الأسر.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        activeFamilies.forEach { family ->
                            val shareText = familyShares[family.id] ?: ""
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = family.name,
                                    modifier = Modifier.weight(1.2f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                                OutlinedTextField(
                                    value = shareText,
                                    onValueChange = { newValue ->
                                        familyShares = familyShares.toMutableMap().apply {
                                            put(family.id, newValue)
                                        }
                                    },
                                    placeholder = { Text("0 SDG", fontSize = 11.sp, color = Color(0xFF64748B)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.1f).height(48.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color(0xFF1B1B1F),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1B1B1F),
                                        unfocusedTextColor = Color(0xFF1B1B1F),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF0061A4),
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // Dynamic sum of distributed shares
                val totalDistributedShares = activeFamilies.sumOf { family ->
                    familyShares[family.id]?.toDoubleOrNull() ?: 0.0
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إجمالي الموزع للأسر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    Text("${String.format(Locale.US, "%,.0f", totalDistributedShares)} SDG", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val map = activeFamilies.associate { family ->
                                family.id to (familyShares[family.id]?.toDoubleOrNull() ?: 0.0)
                            }
                            onConfirm(map)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأكيد التوزيع والحفظ")
                    }
                }
            }
        }
    }
}


// ==========================================
// 2B. SHOP REVENUES SCREEN (إيرادات الدكان)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopRevenuesScreen(
    viewModel: AppViewModel,
    monthlySummaries: List<AppViewModel.MonthlySummary>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    // Forms fields
    var shopAmount by remember { mutableStateOf("") }
    var shopNotes by remember { mutableStateOf("") }
    var shopDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var shopScreenshotUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Setup photo picker
    val shopPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) shopScreenshotUris = shopScreenshotUris + uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "تسجيل إيراد الدكان الشهري",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Amount
                    OutlinedTextField(
                        value = shopAmount,
                        onValueChange = { shopAmount = it },
                        label = { Text("المبلغ الشهري الإجمالي (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("shop_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = shopNotes,
                        onValueChange = { shopNotes = it },
                        label = { Text("ملاحظات (الشهر، المبيعات...)") },
                        modifier = Modifier.fillMaxWidth().testTag("shop_notes"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Past/Custom Month Selector
                    PastMonthSelector(
                        selectedDate = shopDate,
                        onDateSelected = { shopDate = it }
                    )

                    // Screenshot Selection
                    ScreenshotPickerSection(
                        selectedUris = shopScreenshotUris,
                        onSelectRealPhotos = {
                            shopPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            shopScreenshotUris = shopScreenshotUris + MockImageGenerator.generate(context)
                        },
                        onRemovePhoto = { uri ->
                            shopScreenshotUris = shopScreenshotUris.filter { it != uri }
                        }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val amt = shopAmount.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addShopRevenue(
                                    amount = amt,
                                    date = shopDate,
                                    notes = shopNotes,
                                    screenshotUris = shopScreenshotUris
                                )
                                Toast.makeText(context, "تم حفظ إيراد الدكان شهرياً بنجاح", Toast.LENGTH_SHORT).show()
                                // Reset fields
                                shopAmount = ""
                                shopNotes = ""
                                shopScreenshotUris = emptyList()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_shop_revenue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ إيراد الدكان", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "سجل إيرادات الدكان الشهرية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of all entries
        if (monthlySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد إيرادات دكان مسجلة حتى الآن.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(monthlySummaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(summary.monthKey, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        summary.revenues.forEach { rev ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "إيراد دكان • ${AppViewModel.formatSimpleDate(rev.date)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (rev.notes.isNotEmpty()) {
                                        Text(text = rev.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = String.format(Locale.US, "%,.0f SDG", rev.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                                    
                                    rev.screenshotPath?.split("|")?.filter { it.isNotEmpty() }?.forEachIndexed { index, path ->
                                        IconButton(onClick = { onViewImage(path) }) {
                                            Icon(Icons.Default.Image, contentDescription = "عرض لقطة الشاشة ${index + 1}", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteShopRevenue(rev) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedDeduction, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. DEDUCTIONS SCREEN (الخصومات)
// ==========================================
@Composable
fun DeductionsScreen(
    viewModel: AppViewModel,
    rickshaws: List<Rickshaw>,
    deductions: List<Deduction>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") } // "RICKSHAW", "SHOP", "GENERAL"
    var selectedRickshaw by remember { mutableStateOf<Rickshaw?>(null) }
    var screenshotUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) screenshotUris = screenshotUris + uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deduction addition Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "خصم مبالغ لتنفيذ عمل معين (مصاريف)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RedDeduction
                    )

                    // Description / Specific Work description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("نوع العمل المنجز / سبب الخصم") },
                        placeholder = { Text("مثال: صيانة محرك، تغيير زيت، فواتير كهرباء الدكان...") },
                        modifier = Modifier.fillMaxWidth().testTag("deduction_desc"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category selection (Chip row)
                    Text("الجهة المستهدفة بالخصم:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    listOf(
                            "RICKSHAW" to "ركشة",
                            "SHOP" to "الدكان",
                            "ROOM" to "الغرف",
                            "GENERAL" to "عام / آخر"
                        ).forEach { (cat, label) ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f).testTag("chip_$cat")
                            )
                        }
                    }

                    // If RICKSHAW selected, choose which rickshaw
                    if (category == "RICKSHAW") {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRickshaw?.name ?: "اختر ركشة للتخصيص (اختياري)...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الركشة المستهدفة") },
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("deduction_rickshaw_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("عام لجميع الركشات") },
                                    onClick = {
                                        selectedRickshaw = null
                                        dropdownExpanded = false
                                    }
                                )
                                rickshaws.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.name) },
                                        onClick = {
                                            selectedRickshaw = r
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ المخصوم (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("deduction_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Photo selector
                    ScreenshotPickerSection(
                        selectedUris = screenshotUris,
                        onSelectRealPhotos = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            screenshotUris = screenshotUris + MockImageGenerator.generate(context)
                        },
                        onRemovePhoto = { uri ->
                            screenshotUris = screenshotUris.filter { it != uri }
                        }
                    )

                    // Submit button
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (description.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة تفاصيل العمل/الخصم", Toast.LENGTH_SHORT).show()
                            } else if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح للخصم", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addDeduction(
                                    amount = amt,
                                    description = description,
                                    category = category,
                                    targetId = if (category == "RICKSHAW") selectedRickshaw?.id else null,
                                    date = System.currentTimeMillis(),
                                    screenshotUris = screenshotUris
                                )
                                Toast.makeText(context, "تم تسجيل عملية الخصم بنجاح", Toast.LENGTH_SHORT).show()
                                amount = ""
                                description = ""
                                screenshotUris = emptyList()
                                selectedRickshaw = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedDeduction),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_deduction"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تسجيل وحفظ الخصم", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "جميع الخصومات والمصاريف المسجلة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of deductions
        if (deductions.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا يوجد أي خصومات مسجلة.",
                    icon = Icons.Default.TrendingDown
                )
            }
        } else {
            items(deductions) { ded ->
                val badgeText = when (ded.category) {
                    "RICKSHAW" -> "ركشات"
                    "SHOP" -> "الدكان"
                    "ROOM" -> "الغرف"
                    else -> "عام"
                }
                val badgeColor = when (ded.category) {
                    "RICKSHAW" -> MaterialTheme.colorScheme.primary
                    "SHOP" -> MaterialTheme.colorScheme.secondary
                    "ROOM" -> Color(0xFF006874)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = AppViewModel.formatSimpleDate(ded.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ded.description,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "-%,.0f SDG", ded.amount),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = RedDeduction
                            )

                            ded.screenshotPath?.split("|")?.filter { it.isNotEmpty() }?.forEachIndexed { index, path ->
                                IconButton(onClick = { onViewImage(path) }) {
                                    Icon(Icons.Default.Image, contentDescription = "عرض لقطة الشاشة ${index + 1}", tint = RedDeduction, modifier = Modifier.size(20.dp))
                                }
                            }

                            IconButton(onClick = { viewModel.deleteDeduction(ded) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الخصم", tint = RedDeduction.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. REPORTS SCREEN (التقارير)
// ==========================================
@Composable
fun ReportsScreen(
    viewModel: AppViewModel,
    reports: List<Report>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var screenshotUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dialog Viewer State for detailed report view
    var selectedReportForDetails by remember { mutableStateOf<Report?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) screenshotUris = screenshotUris + uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "كتابة تقرير مالي / عام جديد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان التقرير") },
                        placeholder = { Text("مثال: تقرير أعمال الصيانة الأسبوعية") },
                        modifier = Modifier.fillMaxWidth().testTag("report_title"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Content / Body of Report
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("محتوى التقرير والتفاصيل") },
                        placeholder = { Text("اكتب تفاصيل التقرير هنا بوضوح...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("report_content"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Screenshot attaching
                    ScreenshotPickerSection(
                        selectedUris = screenshotUris,
                        onSelectRealPhotos = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            screenshotUris = screenshotUris + MockImageGenerator.generate(context)
                        },
                        onRemovePhoto = { uri ->
                            screenshotUris = screenshotUris.filter { it != uri }
                        }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            if (title.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة عنوان للتقرير", Toast.LENGTH_SHORT).show()
                            } else if (content.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة محتوى التقرير", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addReport(
                                    title = title,
                                    content = content,
                                    date = System.currentTimeMillis(),
                                    screenshotUris = screenshotUris
                                )
                                if (viewModel.getTelegramAutoSend()) {
                                    viewModel.sendReportToTelegram(title, content, title) { success ->
                                        if (success) {
                                            Toast.makeText(context, "تم إرسال التقرير تلقائياً إلى تليجرام ✈️", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "فشل الإرسال التلقائي إلى تليجرام. يرجى التحقق من الإعدادات ⚠️", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                Toast.makeText(context, "تم حفظ التقرير بنجاح", Toast.LENGTH_SHORT).show()
                                title = ""
                                content = ""
                                screenshotUris = emptyList()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_report"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ التقرير ونشره", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Section Header
        item {
            Text(
                text = "جميع التقارير المسجلة والمحفوظة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List of reports
        if (reports.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد أي تقارير مسجلة حتى الآن.",
                    icon = Icons.Default.Description
                )
            }
        } else {
            items(reports) { rep ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReportForDetails = rep },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rep.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = AppViewModel.formatSimpleDate(rep.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(onClick = {
                                    val weekName = if (rep.title.contains("لـ ")) {
                                        rep.title.substringAfter("لـ ").trim()
                                    } else {
                                        rep.title.replace(":", "_").replace("/", "_").trim()
                                    }
                                    PdfGenerator.saveReportToPdf(context, rep.title, rep.content, weekName)
                                }) {
                                    Icon(Icons.Default.Save, contentDescription = "حفظ كـ PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    val weekName = if (rep.title.contains("لـ ")) {
                                        rep.title.substringAfter("لـ ").trim()
                                    } else {
                                        rep.title.replace(":", "_").replace("/", "_").trim()
                                    }
                                    if (viewModel.getTelegramToken().isEmpty() || viewModel.getTelegramChatId().isEmpty()) {
                                        Toast.makeText(context, "الرجاء ضبط إعدادات تليجرام في قسم النسخ الاحتياطي أولاً ⚠️", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.sendReportToTelegram(rep.title, rep.content, weekName) { success ->
                                            if (success) {
                                                Toast.makeText(context, "تم إرسال التقرير بنجاح إلى تليجرام ✈️", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "فشل إرسال التقرير لتليجرام. تحقق من الإعدادات أو الاتصال ❌", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "إرسال إلى تليجرام", tint = Color(0xFF0088CC), modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = { viewModel.deleteReport(rep) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف التقرير", tint = RedDeduction.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rep.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (rep.screenshotPath != null) {
                            val count = rep.screenshotPath.split("|").filter { it.isNotEmpty() }.size
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (count > 1) "يحتوي على لقطات شاشة مرفقة ($count)" else "يحتوي على لقطة شاشة مرفقة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog of selected report
    selectedReportForDetails?.let { rep ->
        AlertDialog(
            onDismissRequest = { selectedReportForDetails = null },
            title = {
                Column {
                    Text(rep.title, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "التاريخ: ${AppViewModel.formatSimpleDate(rep.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = rep.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val paths = rep.screenshotPath?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
                    if (paths.isNotEmpty()) {
                        Column {
                            Text("الصور المرفقة بالتقرير:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                paths.forEach { path ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                            .clickable { onViewImage(path) }
                                    ) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Report Attachment",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val weekName = if (rep.title.contains("لـ ")) {
                                rep.title.substringAfter("لـ ").trim()
                            } else {
                                rep.title.replace(":", "_").replace("/", "_").trim()
                            }
                            PdfGenerator.saveReportToPdf(context, rep.title, rep.content, weekName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ كـ PDF")
                    }
                    Button(
                        onClick = {
                            val weekName = if (rep.title.contains("لـ ")) {
                                rep.title.substringAfter("لـ ").trim()
                            } else {
                                rep.title.replace(":", "_").replace("/", "_").trim()
                            }
                            if (viewModel.getTelegramToken().isEmpty() || viewModel.getTelegramChatId().isEmpty()) {
                                Toast.makeText(context, "الرجاء ضبط إعدادات تليجرام في قسم النسخ الاحتياطي أولاً ⚠️", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.sendReportToTelegram(rep.title, rep.content, weekName) { success ->
                                    if (success) {
                                        Toast.makeText(context, "تم إرسال التقرير بنجاح إلى تليجرام ✈️", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "فشل إرسال التقرير لتليجرام. تحقق من الإعدادات أو الاتصال ❌", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0088CC),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال تليجرام ✈️")
                    }
                    Button(
                        onClick = { selectedReportForDetails = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("إغلاق التقرير")
                    }
                }
            }
        )
    }
}


// ==========================================
// COMMON UI COMPOSABLES & HELPERS
// ==========================================

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ScreenshotPickerSection(
    selectedUris: List<Uri>,
    onSelectRealPhotos: () -> Unit,
    onSelectMockPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "إرفاق إيصالات / لقطات شاشة (Screenshots):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (selectedUris.isNotEmpty()) {
                // Horizontal scroll list of chosen screenshots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    selectedUris.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { onRemovePhoto(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .padding(2.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Button Row for choosing or generating mock photo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectRealPhotos,
                    modifier = Modifier.weight(1f).testTag("select_real_photo"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedUris.isEmpty()) "اختر صور" else "إضافة صور أخرى", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSelectMockPhoto,
                    modifier = Modifier.weight(1f).testTag("select_mock_photo"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("توليد إيصال مالي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ImageViewerDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "FullScreen Screenshot",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
            }
        }
    }
}


// ==========================================
// ==========================================
// MOCK RECEIPT IMAGE GENERATION ENGINE
// ==========================================
object MockImageGenerator {
    fun generate(context: Context): Uri {
        val dir = File(context.filesDir, "screenshots")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "receipt_${System.currentTimeMillis()}.png")

        val bitmap = android.graphics.Bitmap.createBitmap(500, 500, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()

        // 1. Clean background
        paint.color = android.graphics.Color.rgb(248, 250, 252) // slate-50 background
        canvas.drawRect(0f, 0f, 500f, 500f, paint)

        // 2. Draw outer elegant border
        paint.color = android.graphics.Color.rgb(15, 118, 110) // teal-700
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRect(12f, 12f, 488f, 488f, paint)

        // Inner elegant thin border
        paint.color = android.graphics.Color.rgb(203, 213, 225) // slate-300
        paint.strokeWidth = 1.5f
        canvas.drawRect(20f, 20f, 480f, 480f, paint)

        // 3. Header background (Teal banner)
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.rgb(13, 148, 136) // teal-600
        canvas.drawRect(21f, 21f, 479f, 90f, paint)

        // Header Title (centered)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 24f
        paint.isAntiAlias = true
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("إيصال مالي معتمد", 250f, 62f, paint)

        // 4. Content Area
        paint.isFakeBoldText = false
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        
        val startX = 450f
        
        // System title
        paint.color = android.graphics.Color.rgb(15, 23, 42) // slate-900
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("نظام متابعة الركشات والدكان المالي", startX, 135f, paint)

        // Divider
        paint.color = android.graphics.Color.rgb(226, 232, 240) // slate-200
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawLine(40f, 155f, 460f, 155f, paint)

        // Draw Fields with elegant styling
        paint.style = android.graphics.Paint.Style.FILL
        paint.isFakeBoldText = false
        paint.textSize = 15f

        // Helper to draw row: label and value
        fun drawReceiptRow(label: String, value: String, y: Float) {
            // Label
            paint.color = android.graphics.Color.rgb(100, 116, 139) // slate-500
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.drawText(label, startX, y, paint)

            // Value
            paint.color = android.graphics.Color.rgb(15, 23, 42) // slate-900
            paint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.drawText(value, 40f, y, paint)
        }

        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date())
        drawReceiptRow("تاريخ المعاملة:", dateStr, 190f)
        drawReceiptRow("نوع العملية:", "إثبات استلام إيراد مالي رسمي", 230f)
        drawReceiptRow("حالة الاستلام:", "تم الدفع نقداً بالكامل", 270f)
        drawReceiptRow("التأمين والتوزيع:", "موزع وحصين بالرصيد للأسر", 310f)
        drawReceiptRow("إصدار عبر:", "تطبيق الركشات والدكان الذكي", 350f)

        // Second Divider
        paint.color = android.graphics.Color.rgb(226, 232, 240) // slate-200
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawLine(40f, 380f, 460f, 380f, paint)

        // 5. Official Stamp/Seal in Gold
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = android.graphics.Color.rgb(202, 138, 4) // dark gold (yellow-600)
        canvas.drawCircle(110f, 435f, 40f, paint)

        // Inner dashed circle
        paint.strokeWidth = 1f
        canvas.drawCircle(110f, 435f, 35f, paint)

        // Seal text
        paint.style = android.graphics.Paint.Style.FILL
        paint.isFakeBoldText = true
        paint.textSize = 13f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.color = android.graphics.Color.rgb(202, 138, 4)
        canvas.drawText("معتمد رسمياً", 110f, 432f, paint)
        paint.textSize = 11f
        canvas.drawText("صالح كإثبات", 110f, 448f, paint)

        // Authorized Signature line
        paint.color = android.graphics.Color.rgb(71, 85, 105) // slate-600
        paint.textSize = 13f
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("التوقيع والختم الإداري", 440f, 425f, paint)

        paint.color = android.graphics.Color.rgb(148, 163, 184) // slate-400
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawLine(320f, 445f, 450f, 445f, paint)

        // Footnote
        paint.style = android.graphics.Paint.Style.FILL
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.rgb(148, 163, 184) // slate-400
        paint.textSize = 10f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("هذا الإيصال تم توليده آلياً وهو مستند مالي معتمد محلياً في النظام.", 250f, 475f, paint)

        try {
            val out = java.io.FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Uri.fromFile(file)
    }
}

@Composable
fun FamilyDistributionsScreen(viewModel: AppViewModel) {
    val families by viewModel.familiesState.collectAsState()
    val savedBalances by viewModel.familySavedBalancesState.collectAsState()
    val weeklySummaries by viewModel.weeklyRickshawSummaries.collectAsState()
    val reportedWeeks by viewModel.reportedWeeksState.collectAsState()
    val customAmounts by viewModel.familyCustomAmountsState.collectAsState()

    var selectedFamilyForPay by remember { mutableStateOf<AppViewModel.FamilyConfig?>(null) }
    var payAmount by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Expanded states for weekly summaries - collapses older weeks, expands current/active week by default
    var expandedWeeks by remember(weeklySummaries) {
        mutableStateOf(
            weeklySummaries.firstOrNull { !reportedWeeks.contains(it.weekKey) }?.let { setOf(it.weekKey) } ?: emptySet()
        )
    }

    fun getFamilyInitial(name: String): String {
        return if (name.isNotEmpty()) name.trim().take(1) else "ع"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card showing total saved balances
        item {
            val totalSaved = savedBalances.values.sum()
            Card(
                modifier = Modifier.fillMaxWidth().testTag("distributions_hero_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F766E), Color(0xFF0D9488)) // Teal-700 to Teal-600
                            )
                        )
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "إجمالي الأرصدة والأنصبة المحفوظة للأسر",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%,.0f SDG", totalSaved),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تتراكم الأرصدة تلقائياً للأسر عند ترحيل نصيبها للرصيد بدلاً من الاستلام النقدي لتسهيل التوفير.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Section Title: Families List
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(20.dp))
                Text(
                    text = "كشف أرصدة وأنصبة العائلات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (families.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد أسر مضافة حالياً. يمكنك إضافتها من الصفحة الرئيسية > إدارة الأسر.",
                    icon = Icons.Default.People
                )
            }
        } else {
            items(families) { family ->
                val balance = savedBalances[family.id] ?: 0.0
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("family_balance_card_${family.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Round Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (balance > 0) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getFamilyInitial(family.name),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance > 0) Color(0xFF0369A1) else Color(0xFF475569)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = family.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "النصيب الأسبوعي المحدد: ${String.format(Locale.US, "%,.0f", family.portion)} SDG",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Balance Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (balance > 0) Color(0xFFFEF3C7) else Color(0xFFF8FAFC)
                                    )
                                    .border(
                                        1.dp,
                                        if (balance > 0) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.US, "%,.0f SDG", balance),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (balance > 0) Color(0xFFB45309) else Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "الرصيد المحفوظ",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (balance > 0) Color(0xFFB45309) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        if (balance > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Clear/Pay all button
                                Button(
                                    onClick = {
                                        viewModel.clearFamilySavedBalance(family.id)
                                        Toast.makeText(context, "تم صرف كامل الرصيد المحفوظ لـ ${family.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFDCFCE7),
                                        contentColor = Color(0xFF15803D)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("pay_all_balance_${family.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("صرف كامل الرصيد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Pay part button
                                OutlinedButton(
                                    onClick = {
                                        selectedFamilyForPay = family
                                        payAmount = ""
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF0F766E)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF0F766E)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("pay_part_balance_${family.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("صرف جزء من الرصيد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "ليس لديه أي مستحقات متراكمة في الرصيد المحفوظ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Weekly summary status overview
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF0061A4), modifier = Modifier.size(20.dp))
                Text(
                    text = "حصص وأنصبة الأسر لكل أسبوع (عرض فقط)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (weeklySummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد أسابيع مسجلة بعد لعرض الأنصبة.",
                    icon = Icons.Default.DateRange
                )
            }
        } else {
            items(weeklySummaries) { summary ->
                val isExpanded = expandedWeeks.contains(summary.weekKey)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("week_settlement_card_${summary.weekKey}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Week Header Row - Clickable to expand/collapse
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedWeeks = if (isExpanded) expandedWeeks - summary.weekKey else expandedWeeks + summary.weekKey
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF0061A4), CircleShape)
                                )
                                Text(
                                    text = "الأسبوع: ${summary.weekKey}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "إغلاق" else "عرض المزيد",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Summary Statistics Row (Always visible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إجمالي إيرادات الركشات:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.0f SDG", summary.totalRevenue),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                val weekUseFixedPortion = viewModel.getUseFixedPortionForWeek(summary.weekKey)
                                Text(
                                    text = "آلية التوزيع:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (weekUseFixedPortion) "النسب المحددة تلقائياً" else "مبالغ مخصصة / بالتساوي",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                        }

                        // Expanded view displaying the list of active families and their exact share
                        if (isExpanded) {
                            Divider()
                            
                            val weekUseFixedPortion = viewModel.getUseFixedPortionForWeek(summary.weekKey)
                            val activeFamiliesCount = families.count { it.portion > 0 }
                            val defaultFamilyShare = if (weekUseFixedPortion) 0.0 else {
                                if (summary.netRevenue > 0 && activeFamiliesCount > 0) summary.netRevenue / activeFamiliesCount else 0.0
                            }

                            Text(
                                text = "توزيع الحصص والأنصبة للأسر لهذا الأسبوع:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val activeFamilies = families.filter { 
                                it.portion > 0 || (customAmounts["${summary.weekKey}_${it.id}"] ?: 0.0) > 0.0 
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                activeFamilies.forEach { family ->
                                    val customAmount = customAmounts["${summary.weekKey}_${family.id}"]
                                    val hasCustom = customAmount != null
                                    
                                    val familyShare = if (family.portion <= 0) 0.0 else {
                                        customAmount ?: (if (weekUseFixedPortion) (family.portion * summary.sharePerFamily) else defaultFamilyShare)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (hasCustom) Color(0xFFFFFBEB)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = family.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (hasCustom) "نصيب مخصص من المستخدم" else if (weekUseFixedPortion) "نصيب محدد مسبقاً تلقائي" else "موزع بالتساوي",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = String.format(Locale.US, "%,.0f SDG", familyShare),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F766E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Amount Claim Dialog
    selectedFamilyForPay?.let { family ->
        val balance = savedBalances[family.id] ?: 0.0
        AlertDialog(
            onDismissRequest = { selectedFamilyForPay = null },
            title = {
                Text(
                    text = "صرف جزء من رصيد ${family.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "الرصيد المحفوظ المتوفر: ${String.format(Locale.US, "%,.0f SDG", balance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("المبلغ المراد صرفه") },
                        placeholder = { Text("مثال: 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmount.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                        } else if (amount > balance) {
                            Toast.makeText(context, "المبلغ المدخل أكبر من الرصيد المتوفر!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.payAmountFromSavedBalance(family.id, amount)
                            Toast.makeText(context, "تم صرف $amount SDG بنجاح من رصيد ${family.name}", Toast.LENGTH_SHORT).show()
                            selectedFamilyForPay = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Text("تأكيد الصرف")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFamilyForPay = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomRevenuesScreen(
    viewModel: AppViewModel,
    rooms: List<com.example.data.Room>,
    monthlyRoomSummaries: List<AppViewModel.RoomMonthlySummary>,
    onViewImage: (String) -> Unit
) {
    val context = LocalContext.current

    // Form fields
    var selectedRoom by remember { mutableStateOf<com.example.data.Room?>(null) }
    var roomAmount by remember { mutableStateOf("") }
    var roomNotes by remember { mutableStateOf("") }
    var roomDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var roomScreenshotUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Dialog state
    var showRoomsEditDialog by remember { mutableStateOf(false) }

    // Setup photo picker
    val roomPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) roomScreenshotUris = roomScreenshotUris + uri }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FORM CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFF006874),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "تسجيل إيراد إيجار الغرف شهرياً",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006874)
                            )
                        }

                        // Rooms management settings button (just like family settings!)
                        Button(
                            onClick = { showRoomsEditDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0F7FA),
                                contentColor = Color(0xFF004D40)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("manage_rooms_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدارة الغرف", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Room Selection Dropdown
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRoom?.let { "${it.name} - ${it.tenantName}" } ?: "اختر الغرفة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الغرفة") },
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("room_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            if (rooms.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("لا توجد غرف مسجلة، أضف غرف أولاً") },
                                    onClick = { dropdownExpanded = false }
                                )
                            } else {
                                rooms.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text("${r.name} - ${r.tenantName}") },
                                        onClick = {
                                            selectedRoom = r
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = roomAmount,
                        onValueChange = { roomAmount = it },
                        label = { Text("المبلغ المستلم (SDG)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("room_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = roomNotes,
                        onValueChange = { roomNotes = it },
                        label = { Text("ملاحظات (تفاصيل الدفع، الشهر...)") },
                        modifier = Modifier.fillMaxWidth().testTag("room_notes_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Past/Custom Month Selector
                    PastMonthSelector(
                        selectedDate = roomDate,
                        onDateSelected = { roomDate = it }
                    )

                    // Screenshot Selection
                    ScreenshotPickerSection(
                        selectedUris = roomScreenshotUris,
                        onSelectRealPhotos = {
                            roomPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSelectMockPhoto = {
                            roomScreenshotUris = roomScreenshotUris + MockImageGenerator.generate(context)
                        },
                        onRemovePhoto = { uri ->
                            roomScreenshotUris = roomScreenshotUris.filter { it != uri }
                        }
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val amt = roomAmount.toDoubleOrNull()
                            if (selectedRoom == null) {
                                Toast.makeText(context, "الرجاء اختيار الغرفة", Toast.LENGTH_SHORT).show()
                            } else if (amt == null || amt <= 0) {
                                Toast.makeText(context, "الرجاء إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addRoomRevenue(
                                    roomName = "${selectedRoom!!.name} - ${selectedRoom!!.tenantName}",
                                    amount = amt,
                                    date = roomDate,
                                    notes = roomNotes,
                                    screenshotUris = roomScreenshotUris
                                )
                                Toast.makeText(context, "تم حفظ إيراد الغرفة بنجاح وتوليد التقرير تلقائياً", Toast.LENGTH_SHORT).show()
                                // Reset fields
                                roomAmount = ""
                                roomNotes = ""
                                roomScreenshotUris = emptyList()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006874)),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_room_revenue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ إيراد الغرفة وتحصيلها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // SECTION HEADER
        item {
            Text(
                text = "سجل الإيرادات المسجلة للغرف",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // LIST OF ENTRIES
        if (monthlyRoomSummaries.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد إيرادات غرف مسجلة حتى الآن.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(monthlyRoomSummaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شهر ${summary.monthKey}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF006874)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0F7FA), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "المجموع: ${String.format(Locale.US, "%,.0f", summary.totalRevenue)} SDG",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006874)
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            summary.revenues.forEach { rev ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFC4C6CF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = rev.roomName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color(0xFF1B1B1F)
                                            )
                                            Text(
                                                text = "المبلغ: ${String.format(Locale.US, "%,.2f", rev.amount)} SDG",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF386A20)
                                            )
                                            if (rev.notes.isNotBlank()) {
                                                Text(
                                                    text = "ملاحظات: ${rev.notes}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF44474E)
                                                )
                                            }
                                            Text(
                                                text = "التاريخ: ${AppViewModel.formatSimpleDate(rev.date)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF44474E).copy(alpha = 0.6f)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            rev.screenshotPath?.split("|")?.filter { it.isNotEmpty() }?.forEachIndexed { index, path ->
                                                IconButton(
                                                    onClick = { onViewImage(path) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Image,
                                                        contentDescription = "عرض السند ${index + 1}",
                                                        tint = Color(0xFF0061A4)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteRoomRevenue(rev)
                                                    Toast.makeText(context, "تم حذف الإيراد بنجاح", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف الإيراد",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRoomsEditDialog) {
        RoomsEditDialog(
            rooms = rooms,
            onDismiss = { showRoomsEditDialog = false },
            onAddRoom = { name, tenant -> viewModel.addRoom(name, tenant) },
            onUpdateRoom = { r -> viewModel.updateRoom(r) },
            onDeleteRoom = { r -> viewModel.deleteRoom(r) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastMonthSelector(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    var isCustomMonthEnabled by remember { mutableStateOf(false) }

    // Month options list (relative offsets in months)
    val monthOffsets = remember {
        listOf(
            "الشهر الحالي" to 0,
            "الشهر الماضي (الشهر الفائت)" to 1,
            "قبل شهرين" to 2,
            "قبل 3 أشهر" to 3,
            "قبل 4 أشهر" to 4,
            "قبل 5 أشهر" to 5,
            "قبل 6 أشهر" to 6
        )
    }

    // List of months for custom selection
    val arabicMonths = remember {
        listOf(
            "يناير" to Calendar.JANUARY,
            "فبراير" to Calendar.FEBRUARY,
            "مارس" to Calendar.MARCH,
            "أبريل" to Calendar.APRIL,
            "مايو" to Calendar.MAY,
            "يونيو" to Calendar.JUNE,
            "يوليو" to Calendar.JULY,
            "أغسطس" to Calendar.AUGUST,
            "سبتمبر" to Calendar.SEPTEMBER,
            "أكتوبر" to Calendar.OCTOBER,
            "نوفمبر" to Calendar.NOVEMBER,
            "ديسمبر" to Calendar.DECEMBER
        )
    }

    // List of years for custom selection
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val years = remember(currentYear) {
        listOf(currentYear + 1, currentYear, currentYear - 1, currentYear - 2, currentYear - 3)
    }

    var selectedOffsetIndex by remember { mutableStateOf(0) }
    var selectedMonthIndex by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        mutableStateOf(cal.get(Calendar.MONTH))
    }
    var selectedYear by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        mutableStateOf(cal.get(Calendar.YEAR))
    }

    val formattedDateLabel = remember(selectedDate) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val monthNames = arrayOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        "${monthNames[month]} $year"
    }

    // Helper to calculate a timestamp given a relative month offset
    fun calculateMonthOffsetTimestamp(offset: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -offset)
            set(Calendar.DAY_OF_MONTH, 15) // Use middle of the month to avoid timezone shifts
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    // Helper to calculate custom month/year timestamp
    fun calculateCustomMonthTimestamp(month: Int, year: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "شهر استحقاق الإيراد:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = formattedDateLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isCustomMonthEnabled,
                onCheckedChange = {
                    isCustomMonthEnabled = it
                    if (!it) {
                        val calculated = calculateMonthOffsetTimestamp(monthOffsets[selectedOffsetIndex].second)
                        onDateSelected(calculated)
                    } else {
                        val calculated = calculateCustomMonthTimestamp(selectedMonthIndex, selectedYear)
                        onDateSelected(calculated)
                    }
                }
            )
            Text(
                text = "اختيار شهر وسنة مخصصين",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (!isCustomMonthEnabled) {
            // Month dropdown offset
            var monthDropdownExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { monthDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthOffsets[selectedOffsetIndex].first,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = monthDropdownExpanded,
                    onDismissRequest = { monthDropdownExpanded = false }
                ) {
                    monthOffsets.forEachIndexed { index, pair ->
                        DropdownMenuItem(
                            text = { Text(pair.first, fontSize = 13.sp) },
                            onClick = {
                                selectedOffsetIndex = index
                                monthDropdownExpanded = false
                                val calculated = calculateMonthOffsetTimestamp(pair.second)
                                onDateSelected(calculated)
                            }
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Month selector
                var customMonthDropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1.1f)) {
                    OutlinedButton(
                        onClick = { customMonthDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = arabicMonths[selectedMonthIndex].first,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = customMonthDropdownExpanded,
                        onDismissRequest = { customMonthDropdownExpanded = false }
                    ) {
                        arabicMonths.forEachIndexed { index, pair ->
                            DropdownMenuItem(
                                text = { Text(pair.first, fontSize = 12.sp) },
                                onClick = {
                                    selectedMonthIndex = index
                                    customMonthDropdownExpanded = false
                                    val calculated = calculateCustomMonthTimestamp(index, selectedYear)
                                    onDateSelected(calculated)
                                }
                            )
                        }
                    }
                }

                // Year selector
                var customYearDropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(0.9f)) {
                    OutlinedButton(
                        onClick = { customYearDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedYear.toString(),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = customYearDropdownExpanded,
                        onDismissRequest = { customYearDropdownExpanded = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString(), fontSize = 12.sp) },
                                onClick = {
                                    selectedYear = year
                                    customYearDropdownExpanded = false
                                    val calculated = calculateCustomMonthTimestamp(selectedMonthIndex, year)
                                    onDateSelected(calculated)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastDateSelector(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    var isCustomDateEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Day of week list
    val daysOfWeek = remember {
        listOf(
            "الأحد" to Calendar.SUNDAY,
            "الإثنين" to Calendar.MONDAY,
            "الثلاثاء" to Calendar.TUESDAY,
            "الأربعاء" to Calendar.WEDNESDAY,
            "الخميس" to Calendar.THURSDAY,
            "الجمعة" to Calendar.FRIDAY,
            "السبت" to Calendar.SATURDAY
        )
    }

    // Week offsets list
    val weeksOffset = remember {
        listOf(
            "الأسبوع الحالي" to 0,
            "الأسبوع الماضي" to 1,
            "قبل أسبوعين" to 2,
            "قبل 3 أسابيع" to 3,
            "قبل 4 أسابيع" to 4
        )
    }

    var selectedWeekIndex by remember { mutableStateOf(0) }
    var selectedDayIndex by remember { mutableStateOf(0) }

    val formattedDateLabel = remember(selectedDate) {
        val sdf = SimpleDateFormat("EEEE, yyyy/MM/dd", Locale("ar"))
        sdf.format(Date(selectedDate))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "تاريخ التوريدة:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = formattedDateLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isCustomDateEnabled,
                onCheckedChange = {
                    isCustomDateEnabled = it
                    if (!it) {
                        onDateSelected(System.currentTimeMillis())
                    } else {
                        val weekOffset = weeksOffset[selectedWeekIndex].second
                        val dayOfWeek = daysOfWeek[selectedDayIndex].second
                        val calculated = calculateTimestamp(weekOffset, dayOfWeek)
                        onDateSelected(calculated)
                    }
                }
            )
            Text(
                text = "توريدة لأسبوع أو يوم مخصص / مضى",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isCustomDateEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Week Dropdown
                var weekDropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { weekDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = weeksOffset[selectedWeekIndex].first,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = weekDropdownExpanded,
                        onDismissRequest = { weekDropdownExpanded = false }
                    ) {
                        weeksOffset.forEachIndexed { index, pair ->
                            DropdownMenuItem(
                                text = { Text(pair.first, fontSize = 12.sp) },
                                onClick = {
                                    selectedWeekIndex = index
                                    weekDropdownExpanded = false
                                    val calculated = calculateTimestamp(pair.second, daysOfWeek[selectedDayIndex].second)
                                    onDateSelected(calculated)
                                }
                            )
                        }
                    }
                }

                // Day Dropdown
                var dayDropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { dayDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = daysOfWeek[selectedDayIndex].first,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = dayDropdownExpanded,
                        onDismissRequest = { dayDropdownExpanded = false }
                    ) {
                        daysOfWeek.forEachIndexed { index, pair ->
                            DropdownMenuItem(
                                text = { Text(pair.first, fontSize = 12.sp) },
                                onClick = {
                                    selectedDayIndex = index
                                    dayDropdownExpanded = false
                                    val calculated = calculateTimestamp(weeksOffset[selectedWeekIndex].second, pair.second)
                                    onDateSelected(calculated)
                                }
                            )
                        }
                    }
                }
            }

            // Or native Date Picker
            OutlinedButton(
                onClick = {
                    val currentCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val chosenCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                set(Calendar.HOUR_OF_DAY, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDateSelected(chosenCal.timeInMillis)
                        },
                        currentCal.get(Calendar.YEAR),
                        currentCal.get(Calendar.MONTH),
                        currentCal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تحديد تاريخ دقيق من التقويم...", fontSize = 11.sp)
            }
        }
    }
}

private fun calculateTimestamp(weekOffset: Int, dayOfWeek: Int): Long {
    val cal = Calendar.getInstance(Locale("ar")).apply {
        // Find the day of the week in the current week first
        set(Calendar.DAY_OF_WEEK, dayOfWeek)
        // Adjust for week offset
        add(Calendar.WEEK_OF_YEAR, -weekOffset)
        // Set fixed hour to avoid timezone boundary issues
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}


