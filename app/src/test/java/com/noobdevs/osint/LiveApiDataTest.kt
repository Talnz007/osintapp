package com.noobdevs.osint

import com.noobdevs.osint.data.AuthManager
import com.noobdevs.osint.data.OsintApiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LiveApiDataTest {

    @Test
    fun testLiveBackendDataFetching() = runBlocking {
        val authManager = AuthManager()
        val client = OsintApiClient(authManager)

        // 1. Verify Stats
        val statsResult = client.getStats()
        assertTrue("Stats fetch must succeed", statsResult.isSuccess)
        val stats = statsResult.getOrThrow()
        println("=== LIVE STATS VERIFICATION ===")
        println("Total Posts: ${stats.totalPosts}")
        println("Estimated Reach: ${stats.estimatedReach}")
        println("Incidents: ${stats.totalShaheedIncidents}")
        println("Attack types count: ${stats.attackTypes.size}")
        assertTrue("Total posts should be greater than 1000", stats.totalPosts > 1000)

        // 2. Verify Posts
        val postsResult = client.getPosts(page = 1, limit = 5)
        assertTrue("Posts fetch must succeed", postsResult.isSuccess)
        val posts = postsResult.getOrThrow()
        println("=== LIVE POSTS VERIFICATION ===")
        println("Fetched ${posts.data.size} posts, total available: ${posts.total}")
        assertTrue("Must have returned posts", posts.data.isNotEmpty())
        val firstPost = posts.data.first()
        println("First post author: ${firstPost.account}")
        println("First post text: ${firstPost.content?.take(80)}...")
        println("First post detected province: ${firstPost.detectProvince()}")

        // 3. Verify Decks
        val decksResult = client.getDecks()
        assertTrue("Decks fetch must succeed", decksResult.isSuccess)
        val decks = decksResult.getOrThrow()
        println("=== LIVE DECKS VERIFICATION ===")
        println("Available decks: ${decks.decks.size}")
        decks.decks.forEach {
            println("Deck: ${it.filename} (${it.sizeFormatted})")
        }
        assertTrue("Should have decks", decks.decks.isNotEmpty())
    }
}
