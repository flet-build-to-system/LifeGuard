package com.lifeguard.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.lifeguard.app.model.AppState
import com.lifeguard.app.service.LifeGuardVpnService
import com.lifeguard.app.ui.LifeGuardTheme

class MainActivity : ComponentActivity() {
    private val lifeGuardViewModel: LifeGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by lifeGuardViewModel.state.collectAsState()
            LifeGuardTheme(darkTheme = state.darkTheme) {
                CompositionDirection(state.language) { LifeGuardApp(state, lifeGuardViewModel) }
            }
        }
    }
}

@Composable
private fun CompositionDirection(language: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr, content = content)
}

private fun tr(state: AppState, arabic: String, english: String): String = if (state.language == "ar") arabic else english
private fun minutes(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeGuardApp(state: AppState, vm: LifeGuardViewModel) {
    var tab by remember { mutableStateOf("dashboard") }
    val tabs = listOf(
        Triple("dashboard", tr(state, "نظرة اليوم", "Today"), Icons.Default.Home),
        Triple("focus", tr(state, "التركيز", "Focus"), Icons.Default.CenterFocusStrong),
        Triple("guard", tr(state, "الحماية", "Guard"), Icons.Default.Shield),
        Triple("device", tr(state, "الهاتف", "Device"), Icons.Default.Smartphone),
        Triple("settings", tr(state, "الإعدادات", "Settings"), Icons.Default.Tune),
    )
    Scaffold(
        topBar = { TopAppBar(title = { Text("LifeGuard", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = { tab = "settings" }) { Icon(Icons.Default.Settings, null) } }) },
        bottomBar = {
            NavigationBar {
                tabs.forEach { (route, label, icon) -> NavigationBarItem(selected = tab == route, onClick = { tab = route }, icon = { Icon(icon, label) }, label = { Text(label, fontSize = 10.sp) }) }
            }
        },
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            when (tab) {
                "focus" -> FocusScreen(state, vm)
                "guard" -> GuardScreen(state, vm)
                "device" -> DeviceScreen(state, vm)
                "settings" -> SettingsScreen(state, vm)
                else -> DashboardScreen(state, vm)
            }
        }
    }
}

@Composable
private fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), content = content)
}

@Composable
private fun PageColumn(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp, bottom = 24.dp), content = { item { content() } })
}

@Composable
private fun DashboardScreen(state: AppState, vm: LifeGuardViewModel) {
    val context = LocalContext.current
    PageColumn {
        Text(tr(state, "استعد زمام يومك", "Take back your day"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(tr(state, "لوحة عافية محلية وشفافة", "A local, transparent wellbeing cockpit"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr(state, "درجة معاينة العافية", "Preview wellbeing score"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.size(104.dp).background(Color(0xFF173B32), CircleShape), contentAlignment = Alignment.Center) { Text("82", color = Color(0xFF8FE3B2), fontSize = 30.sp, fontWeight = FontWeight.Black) }
                    Column { Text(tr(state, "بيانات نموذجية محلية", "Local preview data"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold); Text(tr(state, "تتحول إلى قراءة شخصية بعد ربط وصول الاستخدام", "Connect Usage Access to calculate a personal reading"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                LinearProgressIndicator(progress = { 0.82f }, modifier = Modifier.fillMaxWidth())
            }
        }
        Text(tr(state, "اليوم", "Today"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(tr(state, "وقت الشاشة", "Screen time"), "—", tr(state, "يتطلب إذناً", "Permission needed"), Icons.Default.Bolt, Modifier.weight(1f))
            MetricCard(tr(state, "وقت التركيز", "Focus time"), "${state.completedSessions * 25}m", "${state.completedSessions} sessions", Icons.Default.CenterFocusStrong, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(tr(state, "محاولات محجوبة", "Blocked attempts"), state.blockedAttempts.toString(), tr(state, "محلياً", "On device"), Icons.Default.Lock, Modifier.weight(1f))
            MetricCard(tr(state, "فتح الهاتف", "Unlocks"), "—", tr(state, "يتطلب إذناً", "Permission needed"), Icons.Default.Smartphone, Modifier.weight(1f))
        }
        AppCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr(state, "ملاحظة عملية", "Practical insight"), fontWeight = FontWeight.Bold)
                Text(tr(state, "لا توجد بيانات استخدام أندرويد بعد. فعّل وصول الاستخدام لتظهر توصيات مبنية على سجلك فقط.", "Android usage data is not connected yet. Grant Usage Access to receive recommendations from your own records."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text(tr(state, "فتح إعدادات وصول الاستخدام", "Open Usage Access settings")) }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    AppCard(modifier) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp); Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) } }
}

@Composable
private fun FocusScreen(state: AppState, vm: LifeGuardViewModel) {
    PageColumn {
        Text(tr(state, "احمِ انتباهك", "Protect your attention"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(tr(state, "جلسة واحدة هادئة أفضل من عشر نوايا.", "One quiet session beats ten intentions."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(minutes(state.focusRemainingSeconds), fontSize = 48.sp, fontWeight = FontWeight.Black); Text(if (state.focusRunning) tr(state, "جلسة عمل", "Work session") else tr(state, "جاهز", "Ready"), color = MaterialTheme.colorScheme.primary); LinearProgressIndicator(progress = { 1f - state.focusRemainingSeconds / (25f * 60f) }, modifier = Modifier.fillMaxWidth()); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = { if (state.focusRunning) vm.pauseFocus() else vm.startFocus() }) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(if (state.focusRunning) tr(state, "إيقاف", "Pause") else tr(state, "بدء", "Start")) }; OutlinedButton(onClick = vm::resetFocus) { Icon(Icons.Default.RestartAlt, null); Spacer(Modifier.width(6.dp)); Text(tr(state, "إعادة ضبط", "Reset")) } } } }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(tr(state, "المهمة التالية", "Next task"), fontWeight = FontWeight.Bold); Text(tr(state, "مراجعة خطة اليوم وتحديد أهم خطوة", "Review today’s plan and choose the next right step")); Text("${state.completedSessions} ${tr(state, "جلسات مكتملة", "sessions completed")}", color = MaterialTheme.colorScheme.primary) } }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(tr(state, "تذكير العادة", "Habit prompt"), fontWeight = FontWeight.Bold); Text(tr(state, "إذا شعرت برغبة في فتح التطبيقات، خذ ثلاثة أنفاس وأكمل خمس دقائق.", "If you feel the urge to open distracting apps, take three breaths and continue for five minutes."), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
private fun GuardScreen(state: AppState, vm: LifeGuardViewModel) {
    val context = LocalContext.current
    var vpnMessage by remember { mutableStateOf<String?>(null) }
    PageColumn {
        Text(tr(state, "اجعل هاتفك يعمل لأجلك", "Make your phone work for you"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(tr(state, "حدود شفافة، قابلة للتراجع، ومن دون لوم.", "Transparent, reversible boundaries without shame."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Column { Text(tr(state, "حالة الحماية", "Protection status"), fontWeight = FontWeight.Bold); Text(tr(state, "${state.protectionProfile} · على الجهاز", "${state.protectionProfile} · on device"), color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked = state.protectionEnabled, onCheckedChange = vm::setProtectionEnabled) }; LinearProgressIndicator(progress = { if (state.protectionEnabled) .74f else .16f }, modifier = Modifier.fillMaxWidth()) } }
        Text(tr(state, "ملف الحماية", "Protection profile"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        listOf("gentle" to tr(state, "لطيف — تذكيرات فقط", "Gentle — reminders only"), "balanced" to tr(state, "متوازن — جداول وحدود", "Balanced — schedules and limits"), "strict" to tr(state, "صارم — حدود أقوى بإذن واضح", "Strict — stronger limits with consent")).forEach { (key, label) -> AppCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Switch(checked = state.protectionProfile == key, onCheckedChange = { if (it) vm.setProtectionProfile(key) }) } } } }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(tr(state, "حماية المواقع", "Website protection"), fontWeight = FontWeight.Bold); Text(tr(state, "تصفية DNS محلية اختيارية. لا نفك تشفير HTTPS ولا نرسل سجل التصفح.", "Optional local DNS filtering. No HTTPS decryption or browsing logs are sent."), color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = { val prepare = VpnService.prepare(context); if (prepare != null) context.startActivity(prepare) else { ContextCompat.startForegroundService(context, Intent(context, LifeGuardVpnService::class.java)); vpnMessage = tr(state, "تم طلب تشغيل VPN المحلي.", "Local VPN start requested.") } }) { Text(tr(state, "إعداد موافقة VPN", "Set up VPN consent")) }; vpnMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) } } }
    }
}

@Composable
private fun DeviceScreen(state: AppState, vm: LifeGuardViewModel) {
    val context = LocalContext.current
    PageColumn {
        Text(tr(state, "مركز الهاتف", "Phone center"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(tr(state, "قراءة صادقة لما يكشفه النظام.", "An honest view of what the system exposes."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { DeviceCard(tr(state, "البطارية", "Battery"), Icons.Default.BatteryFull, Modifier.weight(1f)); DeviceCard(tr(state, "التخزين", "Storage"), Icons.Default.Storage, Modifier.weight(1f)) }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(tr(state, "الأذونات", "Permissions"), fontWeight = FontWeight.Bold); PermissionLine(tr(state, "وصول الاستخدام", "Usage Access"), vm.usageAccessGranted()); PermissionLine(tr(state, "الإشعارات", "Notifications"), false); PermissionLine(tr(state, "موافقة VPN", "VPN consent"), false); OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text(tr(state, "فتح إعدادات النظام", "Open system settings")) } } }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.tertiary); Text(tr(state, "الأمان والصيانة", "Security & maintenance"), fontWeight = FontWeight.Bold); Text(tr(state, "راجع Play Protect وتحديثات النظام وأذونات التطبيقات من إعدادات Android الرسمية.", "Review Play Protect, system updates, and app permissions in official Android Settings."), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
private fun DeviceCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) { AppCard(modifier) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp); Text("—", fontSize = 26.sp, fontWeight = FontWeight.Black); Text("Android only", color = MaterialTheme.colorScheme.tertiary, fontSize = 11.sp) } } }

@Composable
private fun PermissionLine(label: String, granted: Boolean) { Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(if (granted) "Granted" else "Needs permission", color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold) } }

@Composable
private fun SettingsScreen(state: AppState, vm: LifeGuardViewModel) {
    var deleteConfirm by remember { mutableStateOf(false) }
    PageColumn {
        Text(tr(state, "إعدادات هادئة", "Quiet settings"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(tr(state, "تحكم شفاف في بياناتك وحمايتك.", "Transparent control over your data and protection."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(tr(state, "اللغة", "Language"), fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = { vm.setLanguage("ar") }) { Text("العربية") }; OutlinedButton(onClick = { vm.setLanguage("en") }) { Text("English") } } }
        }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(tr(state, "المظهر الداكن", "Dark theme"), fontWeight = FontWeight.Bold); Switch(checked = state.darkTheme, onCheckedChange = vm::setDarkTheme) }; Divider(); Text(tr(state, "البيانات محلية على الجهاز افتراضياً.", "Data stays on device by default."), color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = { deleteConfirm = true }) { Icon(Icons.Default.DeleteOutline, null); Spacer(Modifier.width(6.dp)); Text(tr(state, "حذف السجل المحلي", "Delete local history")) }; if (deleteConfirm) { Text(tr(state, "هل تريد حذف البيانات المحلية؟", "Delete local data?"), color = MaterialTheme.colorScheme.error); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = { vm.resetLocalData(); deleteConfirm = false }) { Text(tr(state, "تأكيد", "Confirm")) }; OutlinedButton(onClick = { deleteConfirm = false }) { Text(tr(state, "إلغاء", "Cancel")) } } } } }
        AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary); Text(tr(state, "التشخيص", "Diagnostics"), fontWeight = FontWeight.Bold); Text(tr(state, "واجهة Compose جاهزة. تكامل VPN وUsage Access يمر عبر موافقة Android الرسمية.", "Compose UI is ready. VPN and Usage Access integrations use official Android consent."), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}
