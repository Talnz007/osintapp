package com.noobdevs.osint.data

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("user") val user: String? = null,
    @SerializedName("detail") val detail: String? = null
)

data class StatsResponse(
    @SerializedName("total_posts") val totalPosts: Long = 0,
    @SerializedName("total_comments") val totalComments: Long = 0,
    @SerializedName("total_hashtags") val totalHashtags: Long = 0,
    @SerializedName("total_mentions") val totalMentions: Long = 0,
    @SerializedName("total_high_profile") val totalHighProfile: Long = 0,
    @SerializedName("total_shaheed_incidents") val totalShaheedIncidents: Long = 0,
    @SerializedName("estimated_reach") val estimatedReach: Long = 0,
    @SerializedName("posts_with_images") val postsWithImages: Long = 0,
    @SerializedName("top_hashtags") val topHashtags: List<HashtagCount> = emptyList(),
    @SerializedName("top_mentions") val topMentions: List<MentionCount> = emptyList(),
    @SerializedName("pipelines") val pipelines: List<PipelineCount> = emptyList(),
    @SerializedName("categories") val categories: List<CategoryCount> = emptyList(),
    @SerializedName("platforms") val platforms: List<PlatformCount> = emptyList(),
    @SerializedName("attack_types") val attackTypes: List<AttackTypeCount> = emptyList(),
    @SerializedName("activity") val activity: List<ActivityPoint> = emptyList()
)

data class HashtagCount(
    @SerializedName("hashtag") val hashtag: String,
    @SerializedName("count") val count: Long
)

data class MentionCount(
    @SerializedName("mention") val mention: String,
    @SerializedName("count") val count: Long
)

data class PipelineCount(
    @SerializedName("source_pipeline") val sourcePipeline: String,
    @SerializedName("count") val count: Long
)

data class CategoryCount(
    @SerializedName("category_label") val categoryLabel: String,
    @SerializedName("count") val count: Long
)

data class PlatformCount(
    @SerializedName("platform") val platform: String,
    @SerializedName("count") val count: Long
)

data class AttackTypeCount(
    @SerializedName("attack_type") val attackType: String,
    @SerializedName("count") val count: Long
)

data class ActivityPoint(
    @SerializedName("date") val date: String,
    @SerializedName("count") val count: Long
)

data class PostsResponse(
    @SerializedName("total") val total: Long = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("pages") val pages: Int = 1,
    @SerializedName("data") val data: List<PostItem> = emptyList()
)

data class PostItem(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String? = null,
    @SerializedName("platform") val platform: String? = "twitter",
    @SerializedName("account") val account: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("scraped_date") val scrapedDate: String? = null,
    @SerializedName("source_pipeline") val sourcePipeline: String? = null,
    @SerializedName("category_label") val categoryLabel: String? = null,
    @SerializedName("attack_type") val attackType: String? = null,
    @SerializedName("had_image") val hadImage: Int = 0,
    @SerializedName("sentiment_image_path") val sentimentImagePath: String? = null,
    @SerializedName("is_high_profile") val isHighProfile: Int = 0,
    @SerializedName("is_shaheed_incident") val isShaheedIncident: Int = 0,
    @SerializedName("whatsapp_message_sent") val whatsappMessageSent: String? = null,
    @SerializedName("comments") val comments: List<String> = emptyList(),
    @SerializedName("hashtags") val hashtags: List<String> = emptyList()
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

data class DeckListResponse(
    @SerializedName("decks") val decks: List<DeckItem> = emptyList()
)

data class DeckItem(
    @SerializedName("filename") val filename: String,
    @SerializedName("size_bytes") val sizeBytes: Long = 0,
    @SerializedName("size_formatted") val sizeFormatted: String = "",
    @SerializedName("modified_at") val modifiedAt: String = "",
    @SerializedName("modified_timestamp") val modifiedTimestamp: Double = 0.0
)

enum class TimeRange(val label: String, val hours: Int?) {
    LIVE("Live Feed", null),
    HOURS_6("Last 6 Hours", 6),
    HOURS_24("Last 24 Hours", 24),
    MONTH("This Month", 720)
}

enum class PipelineFilter(val label: String, val value: String?) {
    ALL("All Pipelines", null),
    REGIONAL_NEWS("Regional News", "regional_news"),
    SENTIMENT_ONLY("Sentiment Only", "sentiment_only")
}

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
