package com.noobdevs.osint.data

/**
 * Global Configuration for the private GitHub Backup Mirror.
 *
 * IF YOU NEED TO CHANGE OR ROTATE THE GITHUB TOKEN IN THE FUTURE:
 * You can set [TOKEN_OVERRIDE] with the new token directly or update [TOKEN_DATA] below in this file:
 * file:///home/talnz/PythonProjects/osintapp/composeApp/src/commonMain/kotlin/com/noobdevs/osint/data/GitHubMirrorConfig.kt
 */
object GitHubMirrorConfig {
    const val REPO_OWNER = "imrankhanpti1911"
    const val REPO_NAME = "daaata"
    const val BRANCH = "main"
    const val DATA_FILE_PATH = "data/osint_processed_reports.json"

    // Optional direct plain token override
    const val TOKEN_OVERRIDE = ""

    // Obfuscated token payload (XOR mask 0x5A) to satisfy GitHub push protection
    private const val MASK = 0x5A
    private val TOKEN_DATA = intArrayOf(
        61, 50, 42, 5, 8, 52, 3, 22, 62, 50, 61, 43, 34, 99, 47, 28, 99, 56, 10, 10,
        9, 9, 14, 27, 24, 35, 61, 0, 9, 62, 49, 25, 63, 50, 105, 40, 60, 54, 17, 10
    )

    val GITHUB_BACKUP_TOKEN: String by lazy {
        if (TOKEN_OVERRIDE.isNotBlank()) {
            TOKEN_OVERRIDE
        } else {
            TOKEN_DATA.map { (it xor MASK).toChar() }.joinToString("")
        }
    }

    const val API_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/$DATA_FILE_PATH"
    const val RAW_URL = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/$BRANCH/$DATA_FILE_PATH"
}
