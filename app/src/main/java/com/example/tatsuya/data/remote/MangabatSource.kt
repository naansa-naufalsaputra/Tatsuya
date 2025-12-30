package com.example.tatsuya.data.remote

import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Page
import kotlinx.coroutines.delay
import javax.inject.Inject

class MangabatSource @Inject constructor() {

    // --- DATA DUMMY (Manual) ---
    // Kita pakai ini agar UI tampil bagus dulu tanpa tergantung mood website
    
    suspend fun getPopularManga(page: Int): List<Manga> {
        delay(1000) // Pura-pura loading internet
        return listOf(
            Manga(
                id = "manga-1",
                title = "One Piece",
                coverUrl = "https://upload.wikimedia.org/wikipedia/en/9/90/One_Piece%2C_Volume_61_Cover_%28Japanese%29.jpg",
                url = "https://manganato.com/manga-aa951409",
                author = "Eiichiro Oda",
                description = "Gol D. Roger was known as the 'Pirate King', the strongest and most infamous being to have sailed the Grand Line.",
                genres = listOf("Action", "Adventure")
            ),
            Manga(
                id = "manga-2",
                title = "Naruto",
                coverUrl = "https://upload.wikimedia.org/wikipedia/en/9/94/NarutoCoverTankobon1.jpg",
                url = "https://manganato.com/manga-ng952689",
                author = "Masashi Kishimoto",
                description = "Naruto Uzumaki, a hyperactive and knucklehead ninja, searches for approval and recognition.",
                genres = listOf("Ninja", "Shounen")
            ),
            Manga(
                id = "manga-3",
                title = "Solo Leveling",
                coverUrl = "https://upload.wikimedia.org/wikipedia/en/9/9c/Solo_Leveling_Webtoon_cover.png",
                url = "https://manganato.com/manga-pn981358",
                author = "Chu-Gong",
                description = "In a world where hunters, humans who possess magical abilities, must battle deadly monsters...",
                genres = listOf("Action", "Fantasy")
            ),
            Manga(
                id = "manga-4",
                title = "Attack on Titan",
                coverUrl = "https://upload.wikimedia.org/wikipedia/en/d/d6/Shingeki_no_Kyojin_manga_volume_1.jpg",
                url = "https://manganato.com/manga-mc951590",
                author = "Hajime Isayama",
                description = "It is set in a world where humanity gets eaten by giant titans.",
                genres = listOf("Horror", "Drama")
            )
        )
    }

    suspend fun searchManga(query: String): List<Manga> {
        delay(500)
        // Kembalikan One Piece saja kalau dicari
        return listOf(
             Manga(
                id = "manga-1",
                title = "One Piece (Result)",
                coverUrl = "https://upload.wikimedia.org/wikipedia/en/9/90/One_Piece%2C_Volume_61_Cover_%28Japanese%29.jpg",
                url = "https://manganato.com/manga-aa951409",
                author = "Eiichiro Oda",
                description = "Search Result for One Piece",
                genres = listOf("Action")
            )
        )
    }

    suspend fun getMangaDetails(mangaId: String): Manga {
        delay(500)
        // Kita kembalikan detail One Piece tidak peduli apa yang diklik
        // (Supaya tidak crash)
        return Manga(
            id = mangaId,
            title = "One Piece (Detail Mode)",
            coverUrl = "https://upload.wikimedia.org/wikipedia/en/9/90/One_Piece%2C_Volume_61_Cover_%28Japanese%29.jpg",
            url = "https://manganato.com/manga-aa951409",
            author = "Eiichiro Oda",
            description = "Ini adalah deskripsi dummy. Fitur detail berfungsi dengan baik! Anda bisa menambahkan manga ini ke Library/Favorite dengan menekan tombol hati.",
            genres = listOf("Action", "Adventure", "Fantasy"),
            chapters = listOf(
                Chapter("ch-1", "Chapter 1: Romance Dawn", "", mangaId),
                Chapter("ch-2", "Chapter 2: They Call Him Luffy", "", mangaId),
                Chapter("ch-3", "Chapter 3: Zoro the Pirate Hunter", "", mangaId)
            ),
            isFavorite = false
        )
    }

    suspend fun getChapterPages(chapterId: String): List<Page> {
        delay(500)
        // Halaman komik dummy (Gambar statis)
        return listOf(
            Page(0, "https://upload.wikimedia.org/wikipedia/commons/e/ea/Peer_Gynt_-_Per_Aabel_-_The_Play_Of_The_Year_-_1955_-_Program_-_page_4.jpg", chapterId),
            Page(1, "https://upload.wikimedia.org/wikipedia/commons/e/ea/Peer_Gynt_-_Per_Aabel_-_The_Play_Of_The_Year_-_1955_-_Program_-_page_4.jpg", chapterId)
        )
    }
}
