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
