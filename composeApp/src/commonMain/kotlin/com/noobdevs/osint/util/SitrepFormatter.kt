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

    fun generate24hStrategicSitrep(posts: List<PostItem>, stats: com.noobdevs.osint.data.models.StatsResponse?): String {
        val total = posts.size
        val fakPosts = posts.filter { it.detectTheater() == com.noobdevs.osint.data.models.TheaterCategory.FAK }
        val fahPosts = posts.filter { it.detectTheater() == com.noobdevs.osint.data.models.TheaterCategory.FAH }
        val kinetic = posts.filter { it.detectActivityType() == com.noobdevs.osint.data.models.ActivityCategory.ATTACKS }
        val propaganda = posts.filter { it.detectActivityType() == com.noobdevs.osint.data.models.ActivityCategory.ACTIVITIES }
        val highProfile = posts.filter { it.isHighProfile == 1 }

        val severityLevel = when {
            kinetic.size >= 15 || highProfile.size >= 5 -> "LEVEL 4 // SEVERE OPERATIONAL THREAT"
            kinetic.size >= 8 || highProfile.size >= 2 -> "LEVEL 3 // ELEVATED THREAT ENVIRONMENT"
            kinetic.isNotEmpty() -> "LEVEL 2 // GUARDED OPERATIONAL POSTURE"
            else -> "LEVEL 1 // ROUTINE MONITORING"
        }

        return buildString {
            append("AOA Sir,\n\n")
            append("🔴 *CONFIDENTIAL // FLASH OSINT THREAT SITREP*\n")
            append("📅 *Reporting Window*: Past 24 Hours\n")
            append("📊 *Advisory*: $severityLevel\n\n")

            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("1. *STRATEGIC SECTOR METRICS*\n")
            append("• *Total Intercepted Wire*: $total dispatches\n")
            append("• *Northern Theater (FAK / KP & Afghan)*: ${fakPosts.size} (${fakPosts.count { it.detectActivityType() == com.noobdevs.osint.data.models.ActivityCategory.ATTACKS }} Kinetic, ${fakPosts.count { it.detectActivityType() == com.noobdevs.osint.data.models.ActivityCategory.ACTIVITIES }} Media)\n")
            append("• *Southern Theater (FAH / Baloch Axis)*: ${fahPosts.size} (${fahPosts.count { it.detectActivityType() == com.noobdevs.osint.data.models.ActivityCategory.ATTACKS }} Kinetic, ${fahPosts.count { it.detectActivityType() == com.noobdevs.osint.data.models.ActivityCategory.ACTIVITIES }} Media)\n")
            append("• *Total Kinetic Incidents*: ${kinetic.size}\n")
            append("• *High-Profile Alerts*: ${highProfile.size}\n\n")

            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("2. *KEY INTERCEPTED INCIDENTS*\n")
            val topEvents = (kinetic + highProfile).distinctBy { it.id }.take(4)
            if (topEvents.isEmpty()) {
                append("• No critical kinetic standoffs reported in current window.\n")
            } else {
                topEvents.forEachIndexed { i, p ->
                    val dist = p.detectLocationName()
                    val th = p.detectTheater().shortCode
                    val snippet = (p.content ?: p.whatsappMessageSent.orEmpty()).trim().take(140)
                    append("• [${i + 1}] *$dist ($th)*: $snippet\n")
                }
            }
            append("\n")

            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("3. *ANALYST OPERATIONAL ASSESSMENT*\n")
            if (fakPosts.size > fahPosts.size) {
                append("• Hostile activity concentrated in Northern frontier (FAK). Heightened vigil recommended along border checkposts & transit arteries.\n")
            } else {
                append("• Southern axis (FAH) indicates ongoing narrative propaganda campaigns coordinated with localized kinetic probes.\n")
            }
            append("\nTransmitted via OSINT Mobile Command Center\nRegards")
        }
    }
}
