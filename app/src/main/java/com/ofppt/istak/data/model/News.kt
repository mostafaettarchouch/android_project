package com.ofppt.istak.data.model

data class NewsResponse(
    val success: Boolean,
    val data: List<NewsArticle>
)

data class NewsArticle(
    val id: Int,
    val title: String,
    val content: String,
    val created_at: String,
    val author: Author?
)

data class Author(
    val id: Int,
    val name: String
)
