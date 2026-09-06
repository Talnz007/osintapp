package com.noobdevs.osint.data

import com.noobdevs.osint.data.models.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.*

class GitHubBackupDataSource(
    private val client: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) {
    @Volatile
    private var cachedPosts: List<PostItem> = emptyList()

    @Volatile
    private var lastETag: String? = null

    @Volatile
    private var lastFetchTimestamp: Long = 0L

    private val hashtagRegex = Regex("""#[\w_]+""")
    private val mentionRegex = Regex("""@[\w_]+""")

    suspend fun fetchReports(forceRefresh: Boolean = false): Result<List<PostItem>> {
        val now = currentTimeMillis()
        // If cached and less than 10 minutes old without forceRefresh, return immediately
        if (!forceRefresh && cachedPosts.isNotEmpty() && (now - lastFetchTimestamp) < 600_000L) {
            return Result.success(cachedPosts)
        }

        // Try GitHub API endpoint first (via api.github.com), fallback to raw.githubusercontent.com
        val endpoints = listOf(
            GitHubMirrorConfig.API_URL to "application/vnd.github.v3.raw",
            GitHubMirrorConfig.RAW_URL to "application/json"
        )

        var lastError: Exception? = null

        for ((url, acceptHeader) in endpoints) {
            try {
                val response = client.get(url) {
                    header("Authorization", "token ${GitHubMirrorConfig.GITHUB_BACKUP_TOKEN}")
                    header("Accept", acceptHeader)
                    header("User-Agent", "OSINT-Mobile-App")
                    lastETag?.let { header("If-None-Match", it) }
                }

                if (response.status == HttpStatusCode.NotModified && cachedPosts.isNotEmpty()) {
                    lastFetchTimestamp = now
                    return Result.success(cachedPosts)
                }

                if (response.status.isSuccess()) {
                    val newEtag = response.headers["ETag"]
                    if (!newEtag.isNullOrBlank()) {
                        lastETag = newEtag
                    }

                    val body = response.bodyAsText()
                    val parsed = parseRawReports(body)
                    cachedPosts = parsed
                    lastFetchTimestamp = now
                    return Result.success(parsed)
                } else {
                    lastError = Exception("HTTP ${response.status.value} from $url")
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        if (cachedPosts.isNotEmpty()) {
            return Result.success(cachedPosts)
        }
        return Result.failure(lastError ?: Exception("Unable to connect to GitHub Backup Mirror"))
    }

    private fun parseRawReports(jsonString: String): List<PostItem> {
        val element = json.parseToJsonElement(jsonString)
        val array = element.jsonArray

        return array.mapIndexedNotNull { index, item ->
            try {
                val obj = item.jsonObject
                val url = obj["url"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val content = obj["original_text"]?.jsonPrimitive?.contentOrNull
                    ?: obj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val account = obj["account"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                val platform = obj["platform"]?.jsonPrimitive?.contentOrNull ?: "twitter"
                val scrapedDate = obj["scraped_date"]?.jsonPrimitive?.contentOrNull
                    ?: obj["date"]?.jsonPrimitive?.contentOrNull
                val pipeline = obj["source_pipeline"]?.jsonPrimitive?.contentOrNull ?: "regional_news"
                val categoryLabel = obj["category_label"]?.jsonPrimitive?.contentOrNull ?: "REGIONAL"
                val whatsappMsg = obj["whatsapp_message_sent"]?.jsonPrimitive?.contentOrNull

                val hadImagePrimitive = obj["had_image"]?.jsonPrimitive
                val hadImageInt = when {
                    hadImagePrimitive?.booleanOrNull == true -> 1
                    hadImagePrimitive?.intOrNull == 1 -> 1
                    else -> 0
                }

                val sentimentImage = obj["screenshot"]?.jsonPrimitive?.contentOrNull
                    ?: obj["sentiment_image_path"]?.jsonPrimitive?.contentOrNull

                val textForAnalysis = (content + " " + whatsappMsg.orEmpty()).lowercase()
                val isHighProfile = if (
                    textForAnalysis.contains("corps commander") ||
                    textForAnalysis.contains("ied") ||
                    textForAnalysis.contains("shaheed") ||
                    textForAnalysis.contains("martyred") ||
                    textForAnalysis.contains("ambush") ||
                    textForAnalysis.contains("wmd") ||
                    textForAnalysis.contains("clash")
                ) 1 else 0

                val isShaheed = if (textForAnalysis.contains("shaheed") || textForAnalysis.contains("martyred")) 1 else 0

                val attackType = when {
                    isShaheed == 1 -> "AMBUSH"
                    isHighProfile == 1 -> "TARGETED"
                    textForAnalysis.contains("kinetic") -> "KINETIC"
                    else -> "GENERAL"
                }

                val hashtags = hashtagRegex.findAll(content).map { it.value }.toList()
                val positiveId = (url.hashCode().toLong() and 0x7FFFFFFF) * 10000L + index

                PostItem(
                    id = positiveId,
                    url = url,
                    platform = platform,
                    account = account,
                    content = content,
                    scrapedDate = scrapedDate,
                    sourcePipeline = pipeline,
                    categoryLabel = categoryLabel,
                    attackType = attackType,
                    hadImage = hadImageInt,
                    sentimentImagePath = sentimentImage,
                    isHighProfile = isHighProfile,
                    isShaheedIncident = isShaheed,
                    whatsappMessageSent = whatsappMsg,
                    comments = emptyList(),
                    hashtags = hashtags
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.scrapedDate.orEmpty() }
    }

    suspend fun getStats(): Result<StatsResponse> {
        val result = fetchReports()
        return result.map { list ->
            val totalPosts = list.size.toLong()
            val totalHashtags = list.sumOf { it.hashtags.size }.toLong()
            val totalMentions = list.sumOf { mentionRegex.findAll(it.content.orEmpty()).count() }.toLong()
            val totalHighProfile = list.count { it.isHighProfile == 1 }.toLong()
            val totalShaheed = list.count { it.isShaheedIncident == 1 }.toLong()
            val withImages = list.count { it.hasMedia }.toLong()
            val reach = totalPosts * 15L + totalHighProfile * 45L

            val pipelineCounts = list.groupingBy { it.sourcePipeline ?: "regional_news" }
                .eachCount()
                .map { (k, v) -> PipelineCount(k, v.toLong()) }

            val categoryCounts = list.groupingBy { it.categoryLabel ?: "UNASSIGNED" }
                .eachCount()
                .map { (k, v) -> CategoryCount(k, v.toLong()) }

            val platformCounts = list.groupingBy { it.platform ?: "twitter" }
                .eachCount()
                .map { (k, v) -> PlatformCount(k, v.toLong()) }

            val attackTypeCounts = list.groupingBy { it.attackType ?: "GENERAL" }
                .eachCount()
                .map { (k, v) -> AttackTypeCount(k, v.toLong()) }

            val activityPoints = list.mapNotNull { it.scrapedDate?.take(10) }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedBy { it.key }
                .takeLast(14)
                .map { ActivityPoint(it.key, it.value.toLong()) }

            StatsResponse(
                totalPosts = totalPosts,
                totalComments = 0L,
                totalHashtags = totalHashtags,
                totalMentions = totalMentions,
                totalHighProfile = totalHighProfile,
                totalShaheedIncidents = totalShaheed,
                estimatedReach = reach,
                postsWithImages = withImages,
                topHashtags = emptyList(),
                topMentions = emptyList(),
                pipelines = pipelineCounts,
                categories = categoryCounts,
                platforms = platformCounts,
                attackTypes = attackTypeCounts,
                activity = activityPoints
            )
        }
    }

    suspend fun getPosts(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        attackType: String? = null,
        search: String? = null,
        pipeline: String? = null,
        hours: Int? = null
    ): Result<PostsResponse> {
        val result = fetchReports()
        return result.map { allPosts ->
            var filtered = allPosts

            if (!category.isNullOrBlank() && category != "ALL") {
                filtered = filtered.filter { it.categoryLabel.equals(category, ignoreCase = true) }
            }
            if (!attackType.isNullOrBlank() && attackType != "ALL") {
                filtered = filtered.filter { it.attackType.equals(attackType, ignoreCase = true) }
            }
            if (!pipeline.isNullOrBlank() && pipeline != "ALL") {
                filtered = filtered.filter { it.sourcePipeline.equals(pipeline, ignoreCase = true) }
            }
            if (!search.isNullOrBlank()) {
                val q = search.trim().lowercase()
                filtered = filtered.filter {
                    it.content?.lowercase()?.contains(q) == true ||
                    it.account?.lowercase()?.contains(q) == true ||
                    it.whatsappMessageSent?.lowercase()?.contains(q) == true ||
                    it.hashtags.any { h -> h.lowercase().contains(q) }
                }
            }

            val total = filtered.size.toLong()
            val pages = maxOf(1, ((total + limit - 1) / limit).toInt())
            val safePage = page.coerceIn(1, pages)
            val pagedData = filtered.drop((safePage - 1) * limit).take(limit)

            PostsResponse(
                total = total,
                page = safePage,
                limit = limit,
                pages = pages,
                data = pagedData
            )
        }
    }

    private fun currentTimeMillis(): Long {
        return kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
    }
}
