package com.example.tatsuya.data.source

import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Page
import org.jsoup.Jsoup
import javax.inject.Inject

class KomikCastSource @Inject constructor() : MangaSource {

    override val id: String = "komikcast"
    override val baseUrl: String = "https://komikcast.cz" // Update sesuai domain aktif

    private val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
    )

    private fun addPrefix(id: String): String = "kc-$id"
    private fun removePrefix(id: String): String = id.removePrefix("kc-")

    override suspend fun getPopularManga(page: Int): List<Manga> {
        return searchManga("")
    }

    override suspend fun searchManga(query: String): List<Manga> {
        return try {
            val url = if (query.isBlank()) "$baseUrl/daftar-komik/?status=&type=&order=update" else "$baseUrl/?s=$query"
            android.util.Log.d("KomikCast", "Connecting to: $url")
            
            val doc = Jsoup.connect(url)
                .headers(headers)
                .timeout(10000) // 10s timeout
                .get()
            
            // Selector update: Support multiple layouts (list-update_item for popular, list-content for search sometimes)
            // User suggested: div.list-update_item OR div.list-content
            val items = doc.select("div.list-update_item, div.list-content, .bs") 
            android.util.Log.d("KomikCast", "Found ${items.size} items for query: '$query'")

            items.mapNotNull { element ->
                try {
                    // Title: .title OR .tt
                    val titleElement = element.selectFirst(".title, .tt")
                    val title = titleElement?.text()?.trim() ?: "No Title"
                    
                    // Link: a tag
                    val linkTag = element.selectFirst("a")
                    val href = linkTag?.attr("href") ?: ""
                    
                    // Image: img tag (src or data-src)
                    val imgTag = element.selectFirst("img")
                    val coverUrl = imgTag?.attr("src")?.ifEmpty { imgTag.attr("data-src") } ?: ""
                    
                    // ID Extraction
                    val id = href.trimEnd('/').substringAfterLast('/')
                    
                    if (id.isEmpty() || href.isEmpty()) return@mapNotNull null

                    android.util.Log.d("KomikCast", "Parsed: $title ($id)")

                    Manga(
                        id = addPrefix(id),
                        title = title,
                        coverUrl = coverUrl,
                        url = href,
                        author = "Unknown",
                        description = "",
                        genres = emptyList(),
                        chapters = emptyList(),
                        isFavorite = false
                    )
                } catch (e: Exception) {
                    android.util.Log.e("KomikCast", "Error parsing item: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("KomikCast", "Search Failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Manga {
        val realId = removePrefix(mangaId)
        val url = "$baseUrl/komik/$realId/" // Prediksi URL standard KomikCast
        
        val doc = Jsoup.connect(url).headers(headers).get()
        
        val title = doc.selectFirst(".komik_info-content-body-title")?.text() ?: doc.title()
        val description = doc.selectFirst(".komik_info-description-sinopsis")?.text() ?: ""
        val coverUrl = doc.selectFirst(".komik_info-content-thumbnail img")?.attr("src") ?: ""
        val author = doc.selectFirst(".komik_info-content-info span:contains(Author)")?.text()?.replace("Author:", "")?.trim() ?: "Unknown"

        return Manga(
            id = mangaId,
            title = title,
            coverUrl = coverUrl,
            url = url,
            author = author,
            description = description,
            genres = emptyList(), // Bisa diparse dari .genre-item
            chapters = emptyList(), // Akan diisi terpisah atau biarkan kosong di sini
            isFavorite = false
        )
    }

    override suspend fun getChapterList(mangaId: String): List<Chapter> {
        val realId = removePrefix(mangaId)
        val url = "$baseUrl/komik/$realId/"
        
        val doc = Jsoup.connect(url).headers(headers).get()
        // Selector chapter list: biasaya di #chapter_list atau .cl
        // Struktur: li -> a (link)
        val elements = doc.select("div.komik_info-chapters-item") // Cek selector terbaru
        
        // KomikCast sering pakai ul#chapter-wrapper atau div.cl
        // Backup selector
        val chapterElements = if (elements.isEmpty()) doc.select("#chapter-wrapper li, .cl li") else elements

        return chapterElements.map { element ->
            val link = element.selectFirst("a")
            val href = link?.attr("href") ?: ""
            val name = link?.text() ?: "Unknown Chapter"
            
            // ID chapter diambil dari URL: https://komikcast.cz/chapter/one-piece-chapter-1100-bahasa-indonesia/ -> one-piece-chapter-1100-bahasa-indonesia
            val chapterId = href.trimEnd('/').substringAfterLast('/')

            Chapter(
                id = addPrefix(chapterId),
                name = name,
                url = href,
                mangaId = mangaId
            )
        }
    }

    override suspend fun getPageList(chapterId: String): List<Page> {
        val realId = removePrefix(chapterId)
        val url = "$baseUrl/chapter/$realId/" // URL Chapter bisa beda pattern, biasanya /chapter/slug
        
        val doc = Jsoup.connect(url).headers(headers).get()
        
        // Cari ID #readerarea atau .main-reading-area
        val images = doc.select("#readerarea img")
        
        return images.mapIndexed { index, img ->
             val src = img.attr("src").ifEmpty { img.attr("data-src") }
             Page(index, src, chapterId)
        }
    }

    override suspend fun getChapterDetails(chapterId: String): Chapter {
        val realId = removePrefix(chapterId)
        val url = "$baseUrl/chapter/$realId/"
        val doc = Jsoup.connect(url).headers(headers).get()

        // Cari Back to Manga link, biasanya di breadcrumb atau tombol 'All Chapters'
        // <div class="allc"><a href="https://komikcast.cz/komik/one-piece/">All Chapters</a></div>
        val mangaUrl = doc.select(".allc a").attr("href")
        // extract ID: https://komikcast.cz/komik/one-piece/ -> one-piece
        val rawMangaId = mangaUrl.trimEnd('/').substringAfterLast('/')
        val mangaId = if (rawMangaId.isNotEmpty()) addPrefix(rawMangaId) else "unknown"
        
        // Title logic (optional)
        val title = doc.title().replace("KomikCast", "").trim()

        return Chapter(
            id = chapterId,
            name = title,
            url = url,
            mangaId = mangaId
        )
    }
}
