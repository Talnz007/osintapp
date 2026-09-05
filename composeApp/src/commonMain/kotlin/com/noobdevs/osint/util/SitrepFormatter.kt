package com.noobdevs.osint.util

import com.noobdevs.osint.data.models.PostItem

object SitrepFormatter {

    fun formatMilitarySitrep(post: PostItem): String {
        val province = post.detectProvince()
        val attackType = post.attackType?.replace("_", " ") ?: "GENERAL"
        val cleanContent = post.content.orEmpty()
        val url = post.url.orEmpty()

        return buildString {
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
    }

    fun formatWhatsAppSitrep(post: PostItem): String {
        val province = post.detectProvince()
        val attackType = post.attackType?.replace("_", " ") ?: "GENERAL"
        val cleanContent = post.content.orEmpty()
        val url = post.url.orEmpty()

        return buildString {
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
    }

    fun formatEvidenceDossier(posts: List<PostItem>): String {
        if (posts.isEmpty()) return ""

        return buildString {
            append("CONFIDENTIAL // OSINT INTELLIGENCE DOSSIER\n")
            append("========================================\n")
            append("Total Intercepted Items in Bag: ${posts.size}\n\n")

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
    }
}
