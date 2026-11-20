package app.vela.ui.settings.sections

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.vela.R
import app.vela.ui.ContactsSearch
import app.vela.ui.map.MapViewModel
import app.vela.ui.settings.GroupDivider
import app.vela.ui.settings.SettingsGroup
import app.vela.ui.settings.SettingsScaffold
import app.vela.ui.settings.ToggleRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Text-search preferences. Voice input is intentionally absent from this build. */
@Composable
internal fun SearchSettingsScreen(vm: MapViewModel, onBack: () -> Unit, onOpenQuickChips: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contactsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            ContactsSearch.set(context, true)
            scope.launch(Dispatchers.IO) { app.vela.data.ContactAddresses.reload(context) }
        }
    }

    SettingsScaffold(stringResource(R.string.settings_search), onBack) {
        Spacer(Modifier.height(4.dp))
        SettingsGroup {
            OtherLocationsRow()
            ToggleRow(
                label = stringResource(R.string.settings_contacts_search),
                checked = ContactsSearch.enabled.value,
                onCheckedChange = { on ->
                    when {
                        !on -> ContactsSearch.set(context, false)
                        app.vela.data.ContactAddresses.hasPermission(context) -> {
                            ContactsSearch.set(context, true)
                            scope.launch(Dispatchers.IO) { app.vela.data.ContactAddresses.reload(context) }
                        }
                        else -> contactsPermission.launch(android.Manifest.permission.READ_CONTACTS)
                    }
                },
                hint = stringResource(R.string.settings_contacts_search_hint),
            )
            GroupDivider()
            QuickChipsRow(onOpenQuickChips)
        }
        Spacer(Modifier.height(24.dp))
    }
}
