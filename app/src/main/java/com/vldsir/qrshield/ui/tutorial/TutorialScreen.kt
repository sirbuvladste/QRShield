package com.vldsir.qrshield.ui.tutorial

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vldsir.qrshield.R
import com.vldsir.qrshield.data.preferences.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun TutorialScreen(
    settingsRepository: SettingsRepository,
    onFinished: () -> Unit,
) {
    val pages = tutorialPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    fun finish() {
        settingsRepository.markTutorialCompleted()
        onFinished()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { finish() }) {
                    Text(stringResource(R.string.tutorial_skip))
                }
            }

            // Page content — takes all remaining space
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { index ->
                TutorialPageContent(page = pages[index])
            }

            // Dots + navigation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    pages.indices.forEach { i ->
                        val isSelected = i == pagerState.currentPage
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else 8.dp,
                            label = "dot_width",
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant,
                            label = "dot_color",
                        )
                        Box(
                            modifier = Modifier
                                .width(dotWidth)
                                .height(8.dp)
                                .background(color = dotColor, shape = CircleShape),
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Back / Next-or-GetStarted
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                        ) {
                            Text(stringResource(R.string.tutorial_back))
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (isLastPage) {
                                finish()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                    ) {
                        Text(
                            if (isLastPage) stringResource(R.string.tutorial_get_started)
                            else stringResource(R.string.tutorial_next),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialPageContent(page: TutorialPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class TutorialPageData(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

@Composable
private fun tutorialPages(): List<TutorialPageData> = listOf(
    TutorialPageData(
        icon = Icons.Filled.Shield,
        title = stringResource(R.string.tutorial_p1_title),
        body = stringResource(R.string.tutorial_p1_body),
    ),
    TutorialPageData(
        icon = Icons.Filled.QrCodeScanner,
        title = stringResource(R.string.tutorial_p2_title),
        body = stringResource(R.string.tutorial_p2_body),
    ),
    TutorialPageData(
        icon = Icons.Filled.Image,
        title = stringResource(R.string.tutorial_p3_title),
        body = stringResource(R.string.tutorial_p3_body),
    ),
    TutorialPageData(
        icon = Icons.Filled.VerifiedUser,
        title = stringResource(R.string.tutorial_p4_title),
        body = stringResource(R.string.tutorial_p4_body),
    ),
    TutorialPageData(
        icon = Icons.Filled.History,
        title = stringResource(R.string.tutorial_p5_title),
        body = stringResource(R.string.tutorial_p5_body),
    ),
)
