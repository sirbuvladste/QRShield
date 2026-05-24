package com.vldsir.qrshield.domain

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.provider.ContactsContract
import android.provider.Settings

class IntentLauncher(private val context: Context) {

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        context.startActivity(intent)
    }

    fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun copyToClipboard(label: String, text: String, isSensitive: Boolean = false) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        cm.setPrimaryClip(clip)
    }

    fun dial(phone: String) {
        val number = phone.removePrefix("tel:")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun sendEmail(to: String, subject: String?, body: String?) {
        val address = to.removePrefix("mailto:")
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address")).apply {
            subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            body?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun addContact(vcardOrMecard: String) {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, extractNameFromVcard(vcardOrMecard))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun extractNameFromVcard(raw: String): String {
        if (raw.startsWith("MECARD:", ignoreCase = true)) {
            return raw.removePrefix("MECARD:")
                .split(";")
                .firstOrNull { it.startsWith("N:", ignoreCase = true) }
                ?.removePrefix("N:")
                ?.removePrefix("n:")
                ?: ""
        }
        return raw.lines()
            .firstOrNull { it.startsWith("FN:", ignoreCase = true) }
            ?.removePrefix("FN:")
            ?.removePrefix("fn:")
            ?.trim()
            ?: ""
    }
}
