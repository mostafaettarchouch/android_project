package com.ofppt.istak.ui.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.data.model.NewsArticle
import com.ofppt.istak.viewmodel.NewsUiState
import com.ofppt.istak.viewmodel.NewsViewModel

import com.ofppt.istak.ui.theme.neumorphic
import com.ofppt.istak.ui.theme.NeumorphicColors

@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Actualités",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

        when (uiState) {
            is NewsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is NewsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as NewsUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .width(120.dp)
                                .neumorphic(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                                .clickable { viewModel.loadNews() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Réessayer", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is NewsUiState.Success -> {
                val articles = (uiState as NewsUiState.Success).articles
                if (articles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucune actualité pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
                    ) {
                        items(articles) { article ->
                            NewsItem(article = article, onClick = { selectedArticle = article })
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedArticle != null) {
        NewsDetailDialog(
            article = selectedArticle!!,
            onDismiss = { selectedArticle = null }
        )
    }
}
}

@Composable
fun NewsItem(article: NewsArticle, onClick: () -> Unit) {
    val summary = remember(article.content) {
        val spanned = androidx.core.text.HtmlCompat.fromHtml(article.content, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)
        spanned.toString().trim()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphic(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .neumorphic(shape = RoundedCornerShape(8.dp), elevation = 1.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = article.created_at.take(10),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            if (article.author != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .neumorphic(shape = CircleShape, elevation = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = article.author.name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = article.author.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NewsDetailDialog(article: NewsArticle, onDismiss: () -> Unit) {
    val scrollState = androidx.compose.foundation.rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .neumorphic(shape = RoundedCornerShape(8.dp), elevation = 1.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = article.created_at.take(10),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (article.author != null) {
                        Text(
                            text = "Par: ${article.author.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Use AndroidView to render HTML
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { context ->
                        android.widget.TextView(context).apply {
                            movementMethod = android.text.method.LinkMovementMethod.getInstance()
                            setTextIsSelectable(true)
                            setTextColor(android.graphics.Color.DKGRAY)
                            textSize = 16f
                            setLineSpacing(0f, 1.2f)
                        }
                    },
                    update = { textView ->
                        textView.text = androidx.core.text.HtmlCompat.fromHtml(
                            article.content,
                            androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    }
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .height(48.dp)
                    .width(100.dp)
                    .neumorphic(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text("Fermer", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

