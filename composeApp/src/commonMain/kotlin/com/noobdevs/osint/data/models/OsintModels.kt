package com.noobdevs.osint.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("user") val user: String? = null,
    @SerialName("detail") val detail: String? = null
)

@Serializable
data class StatsResponse(
    @SerialName("total_posts") val totalPosts: Long = 0,
    @SerialName("total_comments") val totalComments: Long = 0,
    @SerialName("total_hashtags") val totalHashtags: Long = 0,
    @SerialName("total_mentions") val totalMentions: Long = 0,
    @SerialName("total_high_profile") val totalHighProfile: Long = 0,
    @SerialName("total_shaheed_incidents") val totalShaheedIncidents: Long = 0,
    @SerialName("estimated_reach") val estimatedReach: Long = 0,
    @SerialName("posts_with_images") val postsWithImages: Long = 0,
    @SerialName("top_hashtags") val topHashtags: List<HashtagCount> = emptyList(),
    @SerialName("top_mentions") val topMentions: List<MentionCount> = emptyList(),
    @SerialName("pipelines") val pipelines: List<PipelineCount> = emptyList(),
    @SerialName("categories") val categories: List<CategoryCount> = emptyList(),
    @SerialName("platforms") val platforms: List<PlatformCount> = emptyList(),
    @SerialName("attack_types") val attackTypes: List<AttackTypeCount> = emptyList(),
    @SerialName("activity") val activity: List<ActivityPoint> = emptyList()
)

@Serializable
data class HashtagCount(
    @SerialName("hashtag") val hashtag: String,
    @SerialName("count") val count: Long
)

@Serializable
data class MentionCount(
    @SerialName("mention") val mention: String,
    @SerialName("count") val count: Long
)

@Serializable
data class PipelineCount(
    @SerialName("source_pipeline") val sourcePipeline: String,
    @SerialName("count") val count: Long
)

@Serializable
data class CategoryCount(
    @SerialName("category_label") val categoryLabel: String,
    @SerialName("count") val count: Long
)

@Serializable
data class PlatformCount(
    @SerialName("platform") val platform: String,
    @SerialName("count") val count: Long
)

@Serializable
data class AttackTypeCount(
    @SerialName("attack_type") val attackType: String,
    @SerialName("count") val count: Long
)

@Serializable
data class ActivityPoint(
    @SerialName("date") val date: String,
    @SerialName("count") val count: Long
)

@Serializable
data class PostsResponse(
    @SerialName("total") val total: Long = 0,
    @SerialName("page") val page: Int = 1,
    @SerialName("limit") val limit: Int = 20,
    @SerialName("pages") val pages: Int = 1,
    @SerialName("data") val data: List<PostItem> = emptyList()
)

@Serializable
data class PostItem(
    @SerialName("id") val id: Long,
    @SerialName("url") val url: String? = null,
    @SerialName("platform") val platform: String? = "twitter",
    @SerialName("account") val account: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("scraped_date") val scrapedDate: String? = null,
    @SerialName("source_pipeline") val sourcePipeline: String? = null,
    @SerialName("category_label") val categoryLabel: String? = null,
    @SerialName("attack_type") val attackType: String? = null,
    @SerialName("had_image") val hadImage: Int = 0,
    @SerialName("sentiment_image_path") val sentimentImagePath: String? = null,
    @SerialName("is_high_profile") val isHighProfile: Int = 0,
    @SerialName("is_shaheed_incident") val isShaheedIncident: Int = 0,
    @SerialName("whatsapp_message_sent") val whatsappMessageSent: String? = null,
    @SerialName("comments") val comments: List<String> = emptyList(),
    @SerialName("hashtags") val hashtags: List<String> = emptyList()
) {
    val hasMedia: Boolean get() = hadImage == 1 || !sentimentImagePath.isNullOrBlank()
    val isDorReport: Boolean get() = !whatsappMessageSent.isNullOrBlank()

    fun detectProvince(): String {
        val text = (content.orEmpty() + " " + hashtags.joinToString(" ") + " " + account.orEmpty()).lowercase()
        return when {
            text.contains("baloch") || text.contains("quetta") || text.contains("gwadar") || text.contains("kech") || text.contains("turbat") || text.contains("bla") || text.contains("blf") -> "Balochistan"
            text.contains("bannu") || text.contains("waziristan") || text.contains("kpk") || text.contains("khyber") || text.contains("peshawar") || text.contains("kurram") || text.contains("bajaur") || text.contains("swat") || text.contains("pashtun") -> "KPK"
            text.contains("kashmir") || text.contains("ajk") || text.contains("pajk") || text.contains("muzaffarabad") || text.contains("rightsmovement") || text.contains("jkjaac") -> "AJK / Kashmir"
            text.contains("sindh") || text.contains("karachi") || text.contains("hyderabad") || text.contains("sukkur") -> "Sindh"
            text.contains("punjab") || text.contains("lahore") || text.contains("rawalpindi") || text.contains("islamabad") -> "Punjab"
            categoryLabel.equals("GLOBAL", ignoreCase = true) || text.contains("berlin") || text.contains("geneva") || text.contains("diaspora") || text.contains("un ") -> "International"
            else -> "National / Other"
        }
    }
}

@Serializable
data class DeckListResponse(
    @SerialName("decks") val decks: List<DeckItem> = emptyList()
)

@Serializable
data class DeckItem(
    @SerialName("filename") val filename: String,
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    @SerialName("size_formatted") val sizeFormatted: String = "",
    @SerialName("modified_at") val modifiedAt: String = "",
    @SerialName("modified_timestamp") val modifiedTimestamp: Double = 0.0
)

@Serializable
enum class TimeRange(val label: String, val hours: Int?) {
    LIVE("Live Feed", null),
    HOURS_6("Last 6 Hours", 6),
    HOURS_24("Last 24 Hours", 24),
    MONTH("This Month", 720)
}

@Serializable
enum class PipelineFilter(val label: String, val value: String?) {
    ALL("All Pipelines", null),
    REGIONAL_NEWS("Regional News", "regional_news"),
    SENTIMENT_ONLY("Sentiment Only", "sentiment_only")
}

@Serializable
data class TerminologyEntry(
    val code: String,
    val fullName: String,
    val category: String,
    val description: String,
    val example: String = ""
)

object OsintLexicon {
    val entries = listOf(
        TerminologyEntry(
            code = "FAK",
            fullName = "Fitna al-Khawarij",
            category = "Militant Faction (Northern)",
            description = "Official Pakistani state designation for Tehreek-e-Taliban Pakistan (TTP) and associated northern/Afghan border militant networks.",
            example = "FAK proj exaggerated claims to demo kinetic ascendancy."
        ),
        TerminologyEntry(
            code = "FAH",
            fullName = "Fitna al-Haram",
            category = "Separatist Faction (Southern)",
            description = "Designation for Baloch militant/terrorist groups including BLA (Majeed Brigade), BLF, and BRAS operating in Balochistan.",
            example = "FAH propagated exaggerated successes while alleging state repression."
        ),
        TerminologyEntry(
            code = "Ks",
            fullName = "Khawarij",
            category = "Combatant Classification",
            description = "Designation for TTP / ISKP fighters and cadres operating along the western frontier.",
            example = "4x Ks neutralized during sanitization op."
        ),
        TerminologyEntry(
            code = "Ts",
            fullName = "Terrorists",
            category = "Combatant Classification",
            description = "Designation for BLA / BLF / separatist insurgents in the southern theater.",
            example = "Ts attempted sabotaging infrastructure near Gwadar."
        ),
        TerminologyEntry(
            code = "SFs",
            fullName = "Security Forces",
            category = "Forces",
            description = "Encompasses Pakistan Armed Forces, Frontier Corps (FC North/South), and specialized law enforcement units.",
            example = "SFs conducted intelligence-based operation (IBO)."
        ),
        TerminologyEntry(
            code = "IBO",
            fullName = "Intelligence-Based Operation",
            category = "Tactical Action",
            description = "Targeted kinetic military or CT operation executed based on human or signals intelligence.",
            example = "High-value commander apprehended during midnight IBO."
        ),
        TerminologyEntry(
            code = "CP",
            fullName = "Check Post",
            category = "Infrastructure",
            description = "Static security checkpoint or border monitoring station.",
            example = "Brief standoff at peripheral CP repelled by SFs."
        ),
        TerminologyEntry(
            code = "Lki",
            fullName = "Lakki Marwat",
            category = "Geographic Code",
            description = "District in southern Khyber Pakhtunkhwa province.",
            example = "Activity reported in rural Lki sector."
        ),
        TerminologyEntry(
            code = "Bjr",
            fullName = "Bajaur",
            category = "Geographic Code",
            description = "Tribal district in Malakand Division along the Pak-Afghan border.",
            example = "Border movement intercepted in Bjr."
        ),
        TerminologyEntry(
            code = "Khy",
            fullName = "Khyber",
            category = "Geographic Code",
            description = "Historic tribal pass and district connecting Peshawar to Torkham border.",
            example = "Tirah valley operations within Khy district."
        ),
        TerminologyEntry(
            code = "Psc",
            fullName = "Peshawar",
            category = "Geographic Code",
            description = "Provincial capital of Khyber Pakhtunkhwa.",
            example = "Urban CT sweep conducted in suburban Psc."
        ),
        TerminologyEntry(
            code = "Zhob / Sherani",
            fullName = "Zhob & Sherani Districts",
            category = "Geographic Code",
            description = "Northern Balochistan districts bordering South Waziristan and KP.",
            example = "Infiltration attempt thwarted along Zhob-Sherani axis."
        )
    )
}
