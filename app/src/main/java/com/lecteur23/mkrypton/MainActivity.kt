package com.lecteur23.mkrypton

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lecteur23.mkrypton.ui.theme.KryptonAmber
import com.lecteur23.mkrypton.ui.theme.KryptonBackground
import com.lecteur23.mkrypton.ui.theme.KryptonBlue
import com.lecteur23.mkrypton.ui.theme.KryptonCyan
import com.lecteur23.mkrypton.ui.theme.KryptonLime
import com.lecteur23.mkrypton.ui.theme.KryptonMuted
import com.lecteur23.mkrypton.ui.theme.KryptonLine
import com.lecteur23.mkrypton.ui.theme.KryptonPanel
import com.lecteur23.mkrypton.ui.theme.KryptonSurface
import com.lecteur23.mkrypton.ui.theme.KryptonText
import com.lecteur23.mkrypton.ui.theme.KryptonTheme

private enum class KryptonTab(val label: String) {
    Flux("Flux"),
    Reflexion("Reflexion"),
    Historique("Historique"),
    Reglages("Reglages")
}

private data class SignalCard(
    val title: String,
    val detail: String,
    val tag: String,
    val accent: Color
)

private data class LogEntry(
    val time: String,
    val title: String,
    val detail: String,
    val accent: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KryptonTheme {
                KryptonApp()
            }
        }
    }
}

@Composable
private fun KryptonApp() {
    var selectedTab by rememberSaveable { mutableStateOf(KryptonTab.Flux) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val secretsStore = remember(context) { KryptonSecretsStore(context) }
    var savedSecrets by remember { mutableStateOf(secretsStore.load()) }

    val liveSignals = remember(savedSecrets.demoMode) {
        if (savedSecrets.demoMode) {
            listOf(
                SignalCard("Demo Telegram", "Signal d'essai charge localement", "Demo", KryptonCyan),
                SignalCard("Demo Reflexion", "Aucune action sans validation manuelle", "Demo", KryptonBlue),
                SignalCard("Demo Historique", "Trace locale de test sur 14 jours", "Demo", KryptonAmber)
            )
        } else {
            listOf(
                SignalCard("Coach live", "Nouvelle impulsion detectee", "Instant", KryptonCyan),
                SignalCard("Analyse locale", "Signal clair, attente de validation", "Local", KryptonBlue),
                SignalCard("Filtre risque", "Aucune action sans confirmation", "Safe", KryptonLime)
            )
        }
    }

    val history = remember(savedSecrets.demoMode) {
        if (savedSecrets.demoMode) {
            listOf(
                LogEntry("19:05", "Mode demo", "Session de test activee sur l'appareil.", KryptonCyan),
                LogEntry("19:02", "Flux test", "Le moteur local simule une entree Telegram.", KryptonBlue),
                LogEntry("18:58", "Memoire demo", "Donnees temporaires visibles pour verification.", KryptonAmber)
            )
        } else {
            listOf(
                LogEntry("18:42", "Flux Telegram", "Message prioritaire absorbe et classe.", KryptonBlue),
                LogEntry("18:39", "Reflexion", "Le message contient une entree mais pas encore de contexte.", KryptonCyan),
                LogEntry("18:31", "Memoire", "Evenement archive pour consultation rapide.", KryptonAmber)
            )
        }
    }

    Scaffold(
        containerColor = KryptonBackground,
        bottomBar = {
            KryptonBottomBar(selectedTab = selectedTab, onSelect = { selectedTab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            KryptonBackdrop()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                KryptonHeader(isDemo = savedSecrets.demoMode)
                KryptonHeroDeck()
                StatusStrip()

                when (selectedTab) {
                    KryptonTab.Flux -> FluxSection(liveSignals)
                    KryptonTab.Reflexion -> ReflexionSection()
                    KryptonTab.Historique -> HistoriqueSection(history)
                    KryptonTab.Reglages -> ReglagesSection(
                        secretsStore = secretsStore,
                        currentSecrets = savedSecrets,
                        onSecretsSaved = { saved -> savedSecrets = saved }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun KryptonBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(KryptonBackground, KryptonPanel, KryptonBackground)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
                .size(180.dp)
                .background(KryptonBlue.copy(alpha = 0.16f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 96.dp, start = 12.dp)
                .size(220.dp)
                .background(KryptonCyan.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
                .size(120.dp)
                .background(KryptonAmber.copy(alpha = 0.08f), CircleShape)
        )
    }
}

@Composable
private fun KryptonHeader(isDemo: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "M-KRYPTON",
                color = KryptonText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Flux mobile, reflexion locale et suivi instantane.",
                color = KryptonMuted,
                fontSize = 13.sp
            )
        }

        AssistChip(
            onClick = {},
            label = { Text(if (isDemo) "Demo" else "Live", color = KryptonText, fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = if (isDemo) Icons.Filled.History else Icons.Filled.Sync,
                    contentDescription = null,
                    tint = if (isDemo) KryptonAmber else KryptonCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = KryptonPanel.copy(alpha = 0.95f),
                labelColor = KryptonText
            )
        )
    }
}

@Composable
private fun KryptonHeroDeck() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        MiniDeckCard(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp)
                .width(148.dp)
                .graphicsLayer(rotationZ = -12f),
            title = "Flux",
            subtitle = "Telegram / groupes",
            icon = Icons.Filled.Hub,
            accent = KryptonBlue
        )

        HeroCenterCard(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            onPrimary = {}
        )

        MiniDeckCard(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp)
                .width(148.dp)
                .graphicsLayer(rotationZ = 10f),
            title = "Logs",
            subtitle = "Historique local",
            icon = Icons.Filled.History,
            accent = KryptonAmber
        )
    }
}

@Composable
private fun HeroCenterCard(
    modifier: Modifier = Modifier,
    onPrimary: () -> Unit
) {
    Card(
        modifier = modifier.height(190.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KryptonSurface),
        border = BorderStroke(1.dp, KryptonLine)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Moteur local", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = KryptonAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = KryptonPanel.copy(alpha = 0.9f),
                            labelColor = KryptonText
                        )
                    )
                }

                Text(
                    text = "M-KRYPTON",
                    color = KryptonText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Une interface differente pour absorber, classer et suivre tes signaux.",
                    color = KryptonMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPrimary,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KryptonBlue)
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ouvrir le flux")
                }
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KryptonPanel)
                ) {
                    Icon(imageVector = Icons.Filled.AutoGraph, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyser")
                }
            }
        }
    }
}

@Composable
private fun MiniDeckCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Card(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KryptonSurface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = KryptonText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(subtitle, color = KryptonMuted, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun StatusStrip() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatusCard(label = "Flux", value = "24/24", tint = KryptonBlue, modifier = Modifier.weight(1f))
        StatusCard(label = "Reflexion", value = "Locale", tint = KryptonCyan, modifier = Modifier.weight(1f))
        StatusCard(label = "Memoire", value = "14j", tint = KryptonAmber, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatusCard(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KryptonSurface),
        border = BorderStroke(1.dp, KryptonLine)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, color = KryptonMuted, fontSize = 11.sp)
            Text(value, color = tint, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun FluxSection(signals: List<SignalCard>) {
    SectionPanel(
        title = "Flux recents",
        subtitle = "Messages absorbes par l'agent local."
    ) {
        signals.forEach { signal ->
            SignalRow(signal)
            if (signal != signals.last()) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ReflexionSection() {
    SectionPanel(
        title = "Reflexion",
        subtitle = "Un espace de lecture rapide avant toute action."
    ) {
        InsightStep(
            index = "01",
            title = "Identifier",
            detail = "Repere si le message contient une entree, une sortie ou une simple discussion.",
            accent = KryptonBlue
        )
        Spacer(modifier = Modifier.height(10.dp))
        InsightStep(
            index = "02",
            title = "Filtrer",
            detail = "Ignorer les messages flous et conserver seulement les signaux exploitables.",
            accent = KryptonCyan
        )
        Spacer(modifier = Modifier.height(10.dp))
        InsightStep(
            index = "03",
            title = "Valider",
            detail = "Montrer une decision claire, sans bruit ni interface lourde.",
            accent = KryptonAmber
        )
    }
}

@Composable
private fun HistoriqueSection(entries: List<LogEntry>) {
    SectionPanel(
        title = "Historique",
        subtitle = "Jusqu'a 14 jours d'archives locales."
    ) {
        entries.forEachIndexed { index, entry ->
            HistoryRow(entry)
            if (index != entries.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ReglagesSection(
    secretsStore: KryptonSecretsStore,
    currentSecrets: KryptonSecrets,
    onSecretsSaved: (KryptonSecrets) -> Unit
) {
    var telegramApiId by rememberSaveable(currentSecrets.telegramApiId) { mutableStateOf(currentSecrets.telegramApiId) }
    var telegramApiHash by rememberSaveable(currentSecrets.telegramApiHash) { mutableStateOf(currentSecrets.telegramApiHash) }
    var telegramPhone by rememberSaveable(currentSecrets.telegramPhone) { mutableStateOf(currentSecrets.telegramPhone) }
    var geminiApiKey by rememberSaveable(currentSecrets.geminiApiKey) { mutableStateOf(currentSecrets.geminiApiKey) }
    var exnessLogin by rememberSaveable(currentSecrets.exnessLogin) { mutableStateOf(currentSecrets.exnessLogin) }
    var exnessPassword by rememberSaveable(currentSecrets.exnessPassword) { mutableStateOf(currentSecrets.exnessPassword) }
    var exnessServer by rememberSaveable(currentSecrets.exnessServer) { mutableStateOf(currentSecrets.exnessServer) }
    var demoMode by rememberSaveable(currentSecrets.demoMode) { mutableStateOf(currentSecrets.demoMode) }
    var saveMessage by rememberSaveable { mutableStateOf("Aucune sauvegarde encore.") }

    SectionPanel(
        title = "Connexions",
        subtitle = "Secrets gardes localement sur l'appareil."
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Mode demo", color = KryptonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Charge des donnees de test pour verifier l'interface.", color = KryptonMuted, fontSize = 12.sp)
            }
            Switch(
                checked = demoMode,
                onCheckedChange = { demoMode = it }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        SecretField(
            label = "Telegram API ID",
            value = telegramApiId,
            onValueChange = { telegramApiId = it }
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecretField(
            label = "Telegram API Hash",
            value = telegramApiHash,
            onValueChange = { telegramApiHash = it },
            obscure = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecretField(
            label = "Telegram Phone",
            value = telegramPhone,
            onValueChange = { telegramPhone = it }
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecretField(
            label = "Gemini API Key",
            value = geminiApiKey,
            onValueChange = { geminiApiKey = it },
            obscure = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecretField(
            label = "Exness Login",
            value = exnessLogin,
            onValueChange = { exnessLogin = it }
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecretField(
            label = "Exness Password",
            value = exnessPassword,
            onValueChange = { exnessPassword = it },
            obscure = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecretField(
            label = "Exness Server",
            value = exnessServer,
            onValueChange = { exnessServer = it }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val saved = KryptonSecrets(
                    telegramApiId = telegramApiId,
                    telegramApiHash = telegramApiHash,
                    telegramPhone = telegramPhone,
                    geminiApiKey = geminiApiKey,
                    exnessLogin = exnessLogin,
                    exnessPassword = exnessPassword,
                    exnessServer = exnessServer,
                    demoMode = demoMode
                )
                onSecretsSaved(saved)
                secretsStore.save(saved)
                saveMessage = "Sauvegarde locale enregistree."
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KryptonBlue)
        ) {
            Icon(imageVector = Icons.Filled.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enregistrer")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                telegramApiId = ""
                telegramApiHash = ""
                telegramPhone = ""
                geminiApiKey = ""
                exnessLogin = ""
                exnessPassword = ""
                exnessServer = ""
                demoMode = false
                secretsStore.clear()
                onSecretsSaved(KryptonSecrets())
                saveMessage = "Tous les identifiants locaux ont ete effaces."
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KryptonPanel)
        ) {
            Icon(imageVector = Icons.Filled.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reinitialiser")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(saveMessage, color = KryptonMuted, fontSize = 12.sp)
    }
}

@Composable
private fun SectionPanel(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KryptonPanel),
        border = BorderStroke(1.dp, KryptonLine)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = KryptonText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = KryptonMuted, fontSize = 12.sp)
            }
            Divider(color = KryptonLine)
            content()
        }
    }
}

@Composable
private fun SignalRow(signal: SignalCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KryptonSurface, RoundedCornerShape(8.dp))
            .border(1.dp, KryptonLine, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(signal.accent.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Timeline,
                contentDescription = null,
                tint = signal.accent
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(signal.title, color = KryptonText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    signal.tag,
                    color = signal.accent,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(signal.accent.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(signal.detail, color = KryptonMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun InsightStep(
    index: String,
    title: String,
    detail: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KryptonSurface, RoundedCornerShape(8.dp))
            .border(1.dp, KryptonLine, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accent.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(index, color = accent, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = KryptonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(detail, color = KryptonMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HistoryRow(entry: LogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KryptonSurface, RoundedCornerShape(8.dp))
            .border(1.dp, KryptonLine, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(54.dp)
                .background(entry.accent.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(entry.time, color = entry.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(entry.title, color = KryptonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(entry.detail, color = KryptonMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingLine(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KryptonSurface, RoundedCornerShape(8.dp))
            .border(1.dp, KryptonLine, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accent.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = KryptonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, color = KryptonMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SecretField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    obscure: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (obscure) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = KryptonSurface,
            unfocusedContainerColor = KryptonSurface,
            disabledContainerColor = KryptonSurface,
            focusedTextColor = KryptonText,
            unfocusedTextColor = KryptonText,
            focusedLabelColor = KryptonCyan,
            unfocusedLabelColor = KryptonMuted,
            focusedIndicatorColor = KryptonCyan,
            unfocusedIndicatorColor = KryptonLine,
            cursorColor = KryptonCyan
        )
    )
}

@Composable
private fun KryptonBottomBar(
    selectedTab: KryptonTab,
    onSelect: (KryptonTab) -> Unit
) {
    NavigationBar(containerColor = KryptonPanel, tonalElevation = 0.dp) {
        KryptonTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            KryptonTab.Flux -> Icons.Filled.Hub
                            KryptonTab.Reflexion -> Icons.Filled.AutoGraph
                            KryptonTab.Historique -> Icons.Filled.History
                            KryptonTab.Reglages -> Icons.Filled.Settings
                        },
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}
