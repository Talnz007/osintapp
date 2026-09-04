package com.noobdevs.osint.util

import android.content.Context
import android.content.Intent
import com.noobdevs.osint.data.PostItem

object ShareHelper {

    fun sharePostAsMilitarySitrep(context: Context, post: PostItem) {
        val province = post.detectProvince()
        val attackType = post.attackType?.replace("_", " ") ?: "GENERAL"
        val cleanContent = post.content.orEmpty()
        val url = post.url.orEmpty()

        val text = buildString {
            append("AOA Sir,\n\n")
            append("🔶 *OSINT Tactical Update – $province [$attackType]*\n\n")
            append("🔷 *Source*: ${post.account ?: "Regional Intelligence Monitor"}\n")
            append("🔷 *Date*: ${post.scrapedDate?.take(16)?.replace("T", " ") ?: "Recent"}\n\n")
            append("🔷 *Intercepted Text*:\n$cleanContent\n\n")
            if (url.isNotEmpty()) {
                append("Link: $url\n\n")
            }
            append("Regards")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Dispatch Tactical SITREP to...")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    fun sendPostToWhatsApp(context: Context, post: PostItem) {
        val province = post.detectProvince()
        val attackType = post.attackType?.replace("_", " ") ?: "GENERAL"
        val cleanContent = post.content.orEmpty()
        val url = post.url.orEmpty()

        val text = buildString {
            append("AOA Sir,\n\n")
            append("🚨 *OSINT TACTICAL SITREP – $province [$attackType]*\n\n")
            append("👤 *Account*: ${post.account ?: "@Unknown"}\n")
            append("📅 *Intercepted*: ${post.scrapedDate?.take(16)?.replace("T", " ") ?: "Recent"}\n\n")
            append("📝 *Intel Details*:\n$cleanContent\n\n")
            if (url.isNotEmpty()) {
                append("🔗 *Source Link*: $url\n\n")
            }
            append("Transmitted via OSINT Command Center\nRegards")
        }
        dispatchDirectWhatsApp(context, text)
    }

    fun sendBulletinToWhatsApp(context: Context, bulletinText: String) {
        dispatchDirectWhatsApp(context, bulletinText)
    }

    fun dispatchDirectWhatsApp(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to WhatsApp Business or general share chooser
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(fallbackIntent, "Send via WhatsApp or other app...")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        }
    }

    fun shareRawBulletin(context: Context, bulletinText: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, bulletinText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Forward DOR Bulletin to...")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    fun shareEvidenceDossier(context: Context, posts: List<PostItem>) {
        if (posts.isEmpty()) return

        val text = buildString {
            append("CONFIDENTIAL // OSINT INTELLIGENCE DOSSIER\n")
            append("========================================\n")
            append("Total Intercepted Items in Bag: ${posts.size}\n")
            append("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}\n\n")

            posts.forEachIndexed { index, post ->
                val province = post.detectProvince()
                val attack = post.attackType?.replace("_", " ") ?: "GENERAL"
                append("[Item ${index + 1}] $province - $attack\n")
                append("• Source: ${post.account ?: "Unknown"}\n")
                append("• Date: ${post.scrapedDate?.take(16)?.replace("T", " ") ?: "Recent"}\n")
                append("• Intel: ${post.content?.trim()}\n")
                if (!post.url.isNullOrBlank()) {
                    append("• Link: ${post.url}\n")
                }
                append("----------------------------------------\n")
            }
            append("\n[END OF TRANSMISSION - TALKWALKER OSINT RADAR]")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Intelligence Dossier to...")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }
}
