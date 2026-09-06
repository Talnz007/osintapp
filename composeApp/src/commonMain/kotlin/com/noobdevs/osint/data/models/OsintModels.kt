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

    fun detectTheater(): TheaterCategory {
        val text = (content.orEmpty() + " " + hashtags.joinToString(" ") + " " + account.orEmpty() + " " + whatsappMessageSent.orEmpty()).lowercase()
        val cat = categoryLabel.orEmpty().uppercase()
        val pipe = sourcePipeline.orEmpty().lowercase()
        if (cat.contains("GLOBAL") || pipe.startsWith("global") || text.contains("un ") || text.contains("geneva") || text.contains("diaspora")) {
            return TheaterCategory.INTL
        }
        val fahHits = StrategicGazetteer.fahKeywords.count { text.contains(it) }
        val fakHits = StrategicGazetteer.fakKeywords.count { text.contains(it) }
        return when {
            fahHits > 0 && fahHits >= fakHits -> TheaterCategory.FAH
            fakHits > 0 -> TheaterCategory.FAK
            text.contains("baloch") -> TheaterCategory.FAH
            text.contains("kpk") || text.contains("afghan") -> TheaterCategory.FAK
            else -> TheaterCategory.ALL
        }
    }

    fun detectActivityType(): ActivityCategory {
        val text = (content.orEmpty() + " " + hashtags.joinToString(" ") + " " + whatsappMessageSent.orEmpty()).lowercase()
        val hasActivity = StrategicGazetteer.activityKeywords.any { text.contains(it) }
        val hasAttack = StrategicGazetteer.attackKeywords.any { text.contains(it) }
        return when {
            hasActivity && !hasAttack -> ActivityCategory.ACTIVITIES
            hasAttack -> ActivityCategory.ATTACKS
            hasActivity -> ActivityCategory.ACTIVITIES
            else -> ActivityCategory.ALL
        }
    }

    fun detectLocationCoordinates(): Pair<Double, Double>? {
        val text = (content.orEmpty() + " " + hashtags.joinToString(" ") + " " + account.orEmpty()).lowercase()
        return StrategicGazetteer.findCoordinates(text)
    }

    fun detectLocationName(): String {
        val text = (content.orEmpty() + " " + hashtags.joinToString(" ") + " " + account.orEmpty()).lowercase()
        return StrategicGazetteer.findDistrictName(text) ?: detectProvince()
    }

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
enum class TheaterCategory(val label: String, val shortCode: String) {
    ALL("All Theaters", "ALL"),
    FAK("FAK (Khawarij / KP / Afghan)", "FAK"),
    FAH("FAH (Baloch Separatists / Ts)", "FAH"),
    INTL("International & Regional", "INTL")
}

@Serializable
enum class ActivityCategory(val label: String, val shortCode: String) {
    ALL("All Intel Types", "ALL"),
    ATTACKS("Kinetic Attacks & Operations", "ATTACKS"),
    ACTIVITIES("Media, Statements & Propaganda", "ACTIVITIES")
}

@Serializable
enum class MapSortOrder(val label: String, val shortCode: String) {
    NEWEST("Newest First", "NEWEST"),
    OLDEST("Oldest First", "OLDEST"),
    CRITICAL("Most Critical", "CRITICAL")
}

@Serializable
data class ThreatMapMarker(
    val id: Long,
    val title: String,
    val snippet: String,
    val latitude: Double,
    val longitude: Double,
    val district: String,
    val theater: TheaterCategory,
    val activityType: ActivityCategory,
    val date: String,
    val post: PostItem
)

object StrategicGazetteer {
    val fahKeywords = listOf(
        "bla", "blf", "bra", "brg", "fah", "baloch", "balochistan", "bolan", "mand", "gwadar",
        "quetta", "turbat", "panjgur", "kech", "chaman", "sibi", "kalat", "kharan", "nushki",
        "dera bugti", "kohlu", "awaran", "jhal magsi", "zamuran", "jeeyand", "bashir zeb",
        "majeed brigade", "byc", "yakjehti", "mahrang", "sarbaz", "balochistanpost", "tbp"
    )

    val fakKeywords = listOf(
        "ttp", "khawarij", "fak", "ks", "waziristan", "bannu", "tank", "peshawar", "bajaur", "kurram",
        "dir", "swat", "d.i.khan", "di khan", "deraismailkhan", "khyber", "hafiz gul bahadur",
        "tehreek-e-taliban", "iskp", "is-k", "daesh", "kpk", "bara", "akakhel", "ambar", "mohmand",
        "afghanistan", "kabul", "kandahar", "badakhshan", "kunar", "shakai", "domail", "jandola"
    )

    val attackKeywords = listOf(
        "attack", "clash", "ied", "blast", "strike", "ambush", "killed", "martyred",
        "raid", "shot", "gunfire", "targeted", "drone", "quadcopter", "mine", "explosion",
        "checkpost", "bombed", "sabotage", "assassination", "kidnap", "firing", "casualties"
    )

    val activityKeywords = listOf(
        "propaganda", "video", "footage", "media", "claim", "trend", "trending",
        "drive", "pamphlet", "poster", "recruit", "statement", "speech", "spokesperson",
        "press release", "rally", "martyrdom"
    )

    private val locationMap = listOf(
        // Southern Theater (FAH / Balochistan)
        Pair("gwadar", Pair(25.1216, 62.3254)),
        Pair("turbat", Pair(26.0081, 63.0540)),
        Pair("kech", Pair(25.9930, 63.0336)),
        Pair("quetta", Pair(30.1798, 66.9750)),
        Pair("panjgur", Pair(26.9667, 64.0833)),
        Pair("chaman", Pair(30.9236, 66.4512)),
        Pair("sibi", Pair(29.5448, 67.8764)),
        Pair("kalat", Pair(29.0222, 66.5916)),
        Pair("nushki", Pair(29.5539, 66.0215)),
        Pair("dera bugti", Pair(29.0307, 69.1825)),
        Pair("kohlu", Pair(29.8965, 69.2532)),
        Pair("awaran", Pair(26.4568, 65.2314)),
        Pair("bolan", Pair(29.7428, 67.5761)),
        Pair("mand", Pair(26.0644, 62.1558)),
        Pair("mastung", Pair(29.7997, 66.8455)),
        Pair("khuzdar", Pair(27.8119, 66.6044)),
        Pair("hub", Pair(25.0298, 66.8837)),
        Pair("lasbela", Pair(25.7500, 66.6000)),
        Pair("mach", Pair(29.8631, 67.3303)),
        Pair("kolpur", Pair(29.9833, 67.1333)),
        Pair("harnai", Pair(30.1008, 67.9382)),
        Pair("ziarat", Pair(30.3824, 67.7256)),
        Pair("pishin", Pair(30.5803, 66.9961)),
        Pair("qila abdullah", Pair(30.7289, 66.6611)),
        Pair("killa abdullah", Pair(30.7289, 66.6611)),
        Pair("zhob", Pair(31.3417, 69.4486)),
        Pair("sherani", Pair(31.5000, 69.8000)),
        Pair("musakhel", Pair(30.8667, 69.8167)),
        Pair("barkhan", Pair(29.8978, 69.5256)),
        Pair("loralai", Pair(30.3705, 68.5979)),
        Pair("pasni", Pair(25.2631, 63.4714)),
        Pair("ormara", Pair(25.2088, 64.6357)),
        Pair("jiwani", Pair(25.0485, 61.7410)),
        Pair("surab", Pair(28.4914, 66.2586)),
        Pair("kharan", Pair(28.5833, 65.4167)),
        Pair("washuk", Pair(27.8000, 64.7000)),
        Pair("chagai", Pair(29.3000, 64.7000)),
        Pair("dalbandin", Pair(28.8885, 64.4062)),
        Pair("taftan", Pair(28.9714, 61.5794)),
        Pair("sui", Pair(28.6385, 69.1925)),

        // Northern Theater (FAK / KP / FATA)
        Pair("bannu", Pair(32.9854, 70.6027)),
        Pair("tank", Pair(32.2217, 70.3793)),
        Pair("d.i.khan", Pair(31.8327, 70.9024)),
        Pair("di khan", Pair(31.8327, 70.9024)),
        Pair("deraismailkhan", Pair(31.8327, 70.9024)),
        Pair("waziristan", Pair(32.3000, 69.8000)),
        Pair("miranshah", Pair(33.0016, 70.0717)),
        Pair("mir ali", Pair(32.9833, 70.2667)),
        Pair("mirali", Pair(32.9833, 70.2667)),
        Pair("razmak", Pair(32.6833, 69.8500)),
        Pair("spinwam", Pair(33.1500, 70.3333)),
        Pair("datta khel", Pair(32.9500, 69.7500)),
        Pair("shewa", Pair(33.2000, 70.4000)),
        Pair("wana", Pair(32.2989, 69.5725)),
        Pair("shakai", Pair(32.3333, 69.7000)),
        Pair("sararogha", Pair(32.4500, 70.0500)),
        Pair("ladha", Pair(32.5667, 69.8833)),
        Pair("kaniguram", Pair(32.5167, 69.8000)),
        Pair("peshawar", Pair(34.0151, 71.5249)),
        Pair("bajaur", Pair(34.7865, 71.5249)),
        Pair("khar", Pair(34.7397, 71.5249)),
        Pair("nawagai", Pair(34.6833, 71.3167)),
        Pair("kurram", Pair(33.8992, 70.1008)),
        Pair("parachinar", Pair(33.8992, 70.1008)),
        Pair("sadda", Pair(33.6833, 70.3000)),
        Pair("khyber", Pair(34.0984, 71.1444)),
        Pair("bara", Pair(33.9167, 71.4667)),
        Pair("jamrud", Pair(34.0000, 71.3833)),
        Pair("landikotal", Pair(34.0984, 71.1444)),
        Pair("torkham", Pair(34.1250, 71.0917)),
        Pair("tirah", Pair(33.7333, 70.8167)),
        Pair("swat", Pair(34.7717, 72.3602)),
        Pair("mingora", Pair(34.7717, 72.3602)),
        Pair("lakki marwat", Pair(32.6079, 70.9114)),
        Pair("mohmand", Pair(34.5356, 71.2874)),
        Pair("ghallanai", Pair(34.5356, 71.2874)),
        Pair("hangu", Pair(33.5319, 71.0595)),
        Pair("thall", Pair(33.3667, 70.5500)),
        Pair("tall", Pair(33.3667, 70.5500)),
        Pair("orakzai", Pair(33.6500, 70.9500)),
        Pair("kalaya", Pair(33.7333, 70.9667)),
        Pair("kohat", Pair(33.5869, 71.4414)),
        Pair("darra adam khel", Pair(33.6833, 71.5000)),
        Pair("dara adam khel", Pair(33.6833, 71.5000)),
        Pair("mardan", Pair(34.1989, 72.0403)),
        Pair("charsadda", Pair(34.1482, 71.7406)),
        Pair("nowshera", Pair(34.0153, 71.9747)),
        Pair("swabi", Pair(34.1202, 72.4698)),
        Pair("dir", Pair(35.2000, 71.8667)),
        Pair("timargara", Pair(34.8281, 71.8408)),
        Pair("chitral", Pair(35.8510, 71.7864)),
        Pair("malakand", Pair(34.5000, 71.9000)),

        // Cross-Border & Regional
        Pair("kabul", Pair(34.5553, 69.2075)),
        Pair("kandahar", Pair(31.6289, 65.7372)),
        Pair("jalalabad", Pair(34.4265, 70.4515)),
        Pair("kunar", Pair(34.8466, 71.0973)),
        Pair("asadabad", Pair(34.8731, 71.1500)),
        Pair("khost", Pair(33.3333, 69.9167)),
        Pair("paktia", Pair(33.6000, 69.2000)),
        Pair("paktika", Pair(32.5000, 68.8000)),
        Pair("spin boldak", Pair(31.0069, 66.3967)),

        // Federal & Metros
        Pair("karachi", Pair(24.8607, 67.0011)),
        Pair("islamabad", Pair(33.6844, 73.0479)),
        Pair("rawalpindi", Pair(33.5651, 73.0169)),
        Pair("lahore", Pair(31.5204, 74.3587))
    )

    fun findCoordinates(text: String): Pair<Double, Double>? {
        for ((name, coords) in locationMap) {
            if (text.contains(name)) {
                return coords
            }
        }
        return null
    }

    fun findDistrictName(text: String): String? {
        for ((name, _) in locationMap) {
            if (text.contains(name)) {
                return name.replaceFirstChar { it.uppercase() }
            }
        }
        return null
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
