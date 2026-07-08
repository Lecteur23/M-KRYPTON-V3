package com.lecteur23.mkrypton

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.IOException

class MyAccessibilityService : AccessibilityService() {

    private var httpServer: KryptonHttpServer? = null
    private val handler = Handler(Looper.getMainLooper())

    // ── Démarrage du service ──────────────────────────────────────────────────
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Agent Autonome connecté")

        // Lancer le serveur HTTP local sur le port 5000
        try {
            httpServer = KryptonHttpServer(5000, this)
            httpServer?.start()
            Log.d(TAG, "Serveur HTTP démarré sur le port 5000")
        } catch (e: IOException) {
            Log.e(TAG, "Erreur démarrage serveur: ${e.message}")
        }
    }

    // ── Arrêt du service ──────────────────────────────────────────────────────
    override fun onDestroy() {
        httpServer?.stop()
        super.onDestroy()
    }

    // ── Événements d'accessibilité ────────────────────────────────────────────
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // On peut logger les événements pour débugger
        event?.let {
            Log.v(TAG, "Event: ${it.eventType} — ${it.packageName}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrompu")
    }

    // ── Cliquer sur des coordonnées X/Y ──────────────────────────────────────
    fun clickAt(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                Log.d(TAG, "Clic effectué à ($x, $y)")
            }
            override fun onCancelled(gestureDescription: GestureDescription) {
                Log.e(TAG, "Clic annulé à ($x, $y)")
            }
        }, null)
    }

    // ── Taper du texte dans un champ ──────────────────────────────────────────
    fun typeText(x: Float, y: Float, text: String) {
        // D'abord cliquer pour focus
        clickAt(x, y)
        handler.postDelayed({
            // Chercher le nœud focusé et insérer le texte
            val node = findFocusedEditText(rootInActiveWindow)
            node?.let {
                val args = android.os.Bundle()
                args.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
                it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                Log.d(TAG, "Texte '$text' inséré")
            }
        }, 300)
    }

    // ── INJECTION PLASMA: Par coordonnées + Clipboard Bypass ──────────────────
    fun injectByCoordinates(x: Float, y: Float, value: String, fieldType: String): Boolean {
        Log.d(TAG, "🎯 INJECTION $fieldType via coordonnées X:$x Y:$y")

        try {
            // ÉTAPE 1: FRAPPE DU FOCUS - Clic précis sur le champ
            clickAt(x, y)
            Thread.sleep(800) // Attendre clignotement curseur MT5

            // ÉTAPE 2: CLIPBOARD BYPASS - Charger munition dans presse-papiers
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("M-KRYPTON", value)
            clipboard.setPrimaryClip(clip)
            Log.d(TAG, "📋 Clipboard chargé: $value")

            // ÉTAPE 3: Attendre focus stable
            Thread.sleep(300)

            // ÉTAPE 4: Trouver le node avec FOCUS_INPUT actif
            val focusedNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

            if (focusedNode == null) {
                Log.e(TAG, "❌ Aucun node avec FOCUS_INPUT détecté")
                return false
            }

            Log.d(TAG, "✅ Node focus trouvé: ${focusedNode.className}")

            // ÉTAPE 5: L'ESTOCADE - Sélectionner tout puis PASTE
            val args = android.os.Bundle()
            args.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
            args.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT,
                focusedNode.text?.length ?: 999)
            focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, args)
            Thread.sleep(200)

            val pasteSuccess = focusedNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)

            if (pasteSuccess) {
                Log.d(TAG, "✅✅✅ $fieldType injecté via CLIPBOARD: $value")

                // ÉTAPE 6: L'ÉCHAPPÉE - Abaisser clavier
                Thread.sleep(300)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d(TAG, "⬅️ Clavier fermé")

                return true
            } else {
                Log.e(TAG, "❌ ACTION_PASTE échoué pour $fieldType")
                return false
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception injection $fieldType: ${e.message}")
            return false
        }
    }

    // ── Chercher un champ de texte focusable ──────────────────────────────────
    private fun findFocusedEditText(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        node ?: return null
        if (node.isFocused && node.isEditable) return node
        for (i in 0 until node.childCount) {
            val result = findFocusedEditText(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    // ── Chercher un élément par son texte et cliquer ──────────────────────────
    fun clickByText(text: String): Boolean {
        val nodes = rootInActiveWindow?.findAccessibilityNodeInfosByText(text)
        return if (!nodes.isNullOrEmpty()) {
            nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Clic sur élément texte: $text")
            true
        } else {
            Log.e(TAG, "Élément non trouvé: $text")
            false
        }
    }

    // ── Lire le texte d'un élément sur l'écran ────────────────────────────────
    fun readScreenText(): String {
        val sb = StringBuilder()
        fun traverse(node: AccessibilityNodeInfo?) {
            node ?: return
            node.text?.let { sb.appendLine(it) }
            for (i in 0 until node.childCount) traverse(node.getChild(i))
        }
        traverse(rootInActiveWindow)
        return sb.toString()
    }

    // ── Swipe (balayage) ──────────────────────────────────────────────────────
    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long = 300) {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        dispatchGesture(gesture, null, null)
    }

    // ── Ouvrir une application ────────────────────────────────────────────────
    fun openApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(it)
            Log.d(TAG, "App ouverte: $packageName")
        } ?: Log.e(TAG, "App introuvable: $packageName")
    }

    companion object {
        private const val TAG = "KryptonAgent"
        var instance: MyAccessibilityService? = null
    }

    init {
        instance = this
    }
}

// ── Serveur HTTP NanoHTTPD ────────────────────────────────────────────────────
class KryptonHttpServer(
    port: Int,
    private val service: MyAccessibilityService
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        return try {
            when (session.uri) {

                // ── Ping ──────────────────────────────────────────────────────
                "/ping" -> jsonResponse("""{"status":"ok","agent":"M-KRYPTON"}""")

                "/click" -> {
                    val body = getBody(session)
                    val json = JSONObject(body)
                    val x = json.getDouble("x").toFloat()
                    val y = json.getDouble("y").toFloat()
                    service.clickAt(x, y)
                    jsonResponse("""{"status":"clicked","x":$x,"y":$y}""")
                }

                "/type" -> {
                    val body = getBody(session)
                    val json = JSONObject(body)
                    val x = json.getDouble("x").toFloat()
                    val y = json.getDouble("y").toFloat()
                    val text = json.getString("text")
                    service.typeText(x, y, text)
                    jsonResponse("""{"status":"typed","text":"$text"}""")
                }

                "/click_text" -> {
                    val body = getBody(session)
                    val json = JSONObject(body)
                    val text = json.getString("text")
                    val success = service.clickByText(text)
                    jsonResponse("""{"status":"${if (success) "clicked" else "not_found"}","text":"$text"}""")
                }

                "/read_screen" -> {
                    val text = service.readScreenText()
                    val escaped = text.replace("\"", "\\\"").replace("\n", "\\n")
                    jsonResponse("""{"status":"ok","screen":"$escaped"}""")
                }

                "/swipe" -> {
                    val body = getBody(session)
                    val json = JSONObject(body)
                    service.swipe(
                        json.getDouble("x1").toFloat(),
                        json.getDouble("y1").toFloat(),
                        json.getDouble("x2").toFloat(),
                        json.getDouble("y2").toFloat()
                    )
                    jsonResponse("""{"status":"swiped"}""")
                }

                "/open_app" -> {
                    val body = getBody(session)
                    val json = JSONObject(body)
                    val pkg = json.getString("package")
                    service.openApp(pkg)
                    jsonResponse("""{"status":"opened","package":"$pkg"}""")
                }

                // ── INJECTION PLASMA PAR COORDONNÉES ──────────────────────────
                "/inject_by_coords" -> {
                    val body = getBody(session)
                    val json = JSONObject(body)
                    val x = json.getDouble("x").toFloat()
                    val y = json.getDouble("y").toFloat()
                    val value = json.getString("value")
                    val fieldType = json.optString("type", "FIELD")

                    val success = service.injectByCoordinates(x, y, value, fieldType)
                    jsonResponse("""{"status":"${if (success) "injected" else "failed"}","type":"$fieldType","value":"$value","x":$x,"y":$y}""")
                }

                // ── Route inconnue ────────────────────────────────────────────
                else -> newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    """{"error":"route inconnue"}"""
                )
            }
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                """{"error":"${e.message}"}"""
            )
        }
    }

    private fun getBody(session: IHTTPSession): String {
        val map = mutableMapOf<String, String>()
        session.parseBody(map)
        return map["postData"] ?: "{}"
    }

    private fun jsonResponse(body: String) =
        newFixedLengthResponse(Response.Status.OK, "application/json", body)
}
