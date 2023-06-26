package com.kgurgul.cpuinfo.features.applications

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kgurgul.cpuinfo.R
import com.kgurgul.cpuinfo.domain.model.ExtendedApplicationData
import com.kgurgul.cpuinfo.ui.components.CpuSnackbar
import com.kgurgul.cpuinfo.ui.components.DraggableBox
import com.kgurgul.cpuinfo.ui.theme.CpuInfoTheme
import com.kgurgul.cpuinfo.ui.theme.rowActionIconSize
import com.kgurgul.cpuinfo.ui.theme.spacingSmall
import com.kgurgul.cpuinfo.ui.theme.spacingXSmall
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ApplicationsScreen(
    uiState: NewApplicationsViewModel.UiState,
    onAppClicked: (packageName: String) -> Unit,
    onRefreshApplications: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onCardExpanded: (id: String) -> Unit,
    onCardCollapsed: (id: String) -> Unit,
    onAppUninstallClicked: (id: String) -> Unit,
    onAppSettingsClicked: (id: String) -> Unit,
    onNativeLibsClicked: (nativeLibraryDir: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        scope.launch {
            if (uiState.snackbarMessage != -1) {
                val result = snackbarHostState.showSnackbar(
                    context.getString(uiState.snackbarMessage)
                )
                if (result == SnackbarResult.Dismissed) {
                    onSnackbarDismissed()
                }
            }
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                CpuSnackbar(data)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPaddingModifier ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.isLoading,
            onRefresh = { onRefreshApplications() },
        )
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .padding(innerPaddingModifier),
        ) {
            ApplicationsList(
                appList = uiState.applications,
                revealedCardId = uiState.revealedCardId,
                onAppClicked = onAppClicked,
                onCardExpanded = onCardExpanded,
                onCardCollapsed = onCardCollapsed,
                onAppUninstallClicked = onAppUninstallClicked,
                onAppSettingsClicked = onAppSettingsClicked,
                onNativeLibsClicked = onNativeLibsClicked,
            )
            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun ApplicationsList(
    appList: List<ExtendedApplicationData>,
    revealedCardId: String?,
    onAppClicked: (packageName: String) -> Unit,
    onCardExpanded: (id: String) -> Unit,
    onCardCollapsed: (id: String) -> Unit,
    onAppUninstallClicked: (id: String) -> Unit,
    onAppSettingsClicked: (id: String) -> Unit,
    onNativeLibsClicked: (nativeLibraryDir: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = appList,
            key = { app -> app.packageName }
        ) {
            DraggableBox(
                isRevealed = revealedCardId == it.packageName,
                onExpand = { onCardExpanded(it.packageName) },
                onCollapse = { onCardCollapsed(it.packageName) },
                actionRow = {
                    Row {
                        IconButton(
                            modifier = Modifier.size(rowActionIconSize),
                            onClick = { onAppSettingsClicked(it.packageName) },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_settings),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    contentDescription = stringResource(id = R.string.settings),
                                )
                            }
                        )
                        IconButton(
                            modifier = Modifier.size(56.dp),
                            onClick = { onAppUninstallClicked(it.packageName) },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_thrash),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    contentDescription = null,
                                )
                            }
                        )
                    }
                },
                content = {
                    ApplicationItem(
                        appData = it,
                        onAppClicked = onAppClicked,
                        onNativeLibsClicked = onNativeLibsClicked,
                    )
                }
            )
        }
    }
}

@Composable
private fun ApplicationItem(
    appData: ExtendedApplicationData,
    onAppClicked: (packageName: String) -> Unit,
    onNativeLibsClicked: (nativeLibraryDir: String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .clickable(onClick = { onAppClicked(appData.packageName) })
            .padding(spacingSmall),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(appData.appIconUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(50.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = spacingXSmall)
        ) {
            Text(
                text = appData.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = appData.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (appData.hasNativeLibs) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(spacingXSmall))
            Image(
                painter = painterResource(id = R.drawable.ic_c_plus_plus),
                contentDescription = stringResource(id = R.string.native_libs),
                modifier = Modifier
                    .requiredSize(40.dp)
                    .clickable { appData.nativeLibraryDir?.let { onNativeLibsClicked(it) } }
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ApplicationInfoPreview() {
    CpuInfoTheme {
        ApplicationsScreen(
            uiState = NewApplicationsViewModel.UiState(
                applications = persistentListOf(previewAppData1, previewAppData2)
            ),
            onAppClicked = {},
            onRefreshApplications = {},
            onSnackbarDismissed = {},
            onCardExpanded = {},
            onCardCollapsed = {},
            onAppSettingsClicked = {},
            onAppUninstallClicked = {},
            onNativeLibsClicked = {},
        )
    }
}

private val previewAppData1 = ExtendedApplicationData(
    "Cpu Info",
    "com.kgurgul.cpuinfo",
    "/testDir",
    null,
    false,
    Uri.parse("https://avatars.githubusercontent.com/u/6407041?s=32&v=4")
)

private val previewAppData2 = ExtendedApplicationData(
    "Cpu Info1",
    "com.kgurgul.cpuinfo1",
    "/testDir",
    null,
    false,
    Uri.parse("https://avatars.githubusercontent.com/u/6407041?s=32&v=4")
)
