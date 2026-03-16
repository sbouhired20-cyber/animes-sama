package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class ExampleProvider : MainAPI() {
    // Les informations de base de notre extension
    override var mainUrl = "https://animes-sama.fr"
    override var name = "Anime-Sama"
    override val hasMainPage = true
    override var lang = "fr"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    // ==========================================
    // 1. LA RECHERCHE
    // ==========================================
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("a.asn-search-result").mapNotNull { element ->
            val title = element.selectFirst("h3.asn-search-result-title")?.text() ?: return@mapNotNull null
            val href = fixUrl(element.attr("href"))
            val posterUrl = element.selectFirst("img.asn-search-result-img")?.attr("src")

            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }
    }

    // ==========================================
    // 2. LA PAGE DE L'ANIME
    // ==========================================
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        
        val title = document.selectFirst("h1")?.text() ?: "Titre inconnu"
        val poster = document.selectFirst("img.w-full.h-auto")?.attr("src")
        val plot = document.selectFirst("h2:contains(Synopsis) + p")?.text()

        val episodes = mutableListOf<Episode>()
        // Note: La logique d'extraction des épisodes réels n'est pas complète ici 
        // car elle nécessite d'analyser les scripts JS complexes du site.

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = plot
            this.episodes = episodes
        }
    }

    // ==========================================
    // 3. L'EXTRACTION DE LA VIDÉO FINALE
    // ==========================================
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        loadExtractor(data, subtitleCallback, callback)
        return true
    }
}
