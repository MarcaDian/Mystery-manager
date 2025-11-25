package app.revanced.manager.ui.screen.settings

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.settings.SettingsListItem
import app.revanced.manager.ui.model.navigation.Settings
import app.revanced.manager.ui.viewmodel.AboutViewModel
import app.revanced.manager.ui.viewmodel.AboutViewModel.Companion.DEVELOPER_OPTIONS_TAPS
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    onBackClick: () -> Unit,
    navigate: (Settings.Destination) -> Unit,
    viewModel: AboutViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val icon = rememberDrawablePainter(
        drawable = remember {
            AppCompatResources.getDrawable(context, R.mipmap.ic_launcher)
        }
    )

    val showDeveloperSettings by viewModel.showDeveloperSettings.getAsState()
    var developerTaps by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(developerTaps) {
        if (developerTaps == 0) return@LaunchedEffect
        if (showDeveloperSettings) {
            snackbarHostState.showSnackbar(context.getString(R.string.developer_options_already_enabled))
            developerTaps = 0
            return@LaunchedEffect
        }

        val remaining = DEVELOPER_OPTIONS_TAPS - developerTaps
        if (remaining > 0) {
            snackbarHostState.showSnackbar(
                context.getString(R.string.developer_options_taps, remaining),
                duration = SnackbarDuration.Long
            )
        } else {
            viewModel.showDeveloperSettings.update(true)
            snackbarHostState.showSnackbar(context.getString(R.string.developer_options_enabled))
        }
        developerTaps = 0
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.about),
                scrollBehavior = scrollBehavior,
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        ColumnWithScrollbar(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = icon,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { developerTaps += 1 }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            OutlinedCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.about_revanced_manager),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.revanced_manager_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SettingsListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navigate(Settings.Licenses) },
                headlineContent = stringResource(R.string.opensource_licenses),
                supportingContent = stringResource(R.string.opensource_licenses_description)
            )
        }
    }
}
