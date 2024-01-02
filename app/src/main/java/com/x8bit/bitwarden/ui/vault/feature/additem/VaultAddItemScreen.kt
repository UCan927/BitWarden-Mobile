package com.x8bit.bitwarden.ui.vault.feature.additem

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.ui.platform.base.util.EventsEffect
import com.x8bit.bitwarden.ui.platform.base.util.PermissionsManager
import com.x8bit.bitwarden.ui.platform.base.util.PermissionsManagerImpl
import com.x8bit.bitwarden.ui.platform.base.util.asText
import com.x8bit.bitwarden.ui.platform.components.BasicDialogState
import com.x8bit.bitwarden.ui.platform.components.BitwardenBasicDialog
import com.x8bit.bitwarden.ui.platform.components.BitwardenLoadingDialog
import com.x8bit.bitwarden.ui.platform.components.BitwardenScaffold
import com.x8bit.bitwarden.ui.platform.components.BitwardenTextButton
import com.x8bit.bitwarden.ui.platform.components.BitwardenTopAppBar
import com.x8bit.bitwarden.ui.platform.components.LoadingDialogState
import com.x8bit.bitwarden.ui.vault.feature.additem.handlers.VaultAddIdentityItemTypeHandlers
import com.x8bit.bitwarden.ui.vault.feature.additem.handlers.VaultAddItemCommonHandlers
import com.x8bit.bitwarden.ui.vault.feature.additem.handlers.VaultAddLoginItemTypeHandlers

/**
 * Top level composable for the vault add item screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun VaultAddItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: VaultAddItemViewModel = hiltViewModel(),
    permissionsManager: PermissionsManager =
        PermissionsManagerImpl(LocalContext.current as Activity),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            is VaultAddItemEvent.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }

            VaultAddItemEvent.NavigateBack -> onNavigateBack.invoke()
        }
    }

    val loginItemTypeHandlers = remember(viewModel) {
        VaultAddLoginItemTypeHandlers.create(viewModel = viewModel)
    }

    val commonTypeHandlers = remember(viewModel) {
        VaultAddItemCommonHandlers.create(viewModel = viewModel)
    }

    val identityItemTypeHandlers = remember(viewModel) {
        VaultAddIdentityItemTypeHandlers.create(viewModel = viewModel)
    }

    VaultAddEditItemDialogs(
        dialogState = state.dialog,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(VaultAddItemAction.Common.DismissDialog) }
        },
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    BitwardenScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BitwardenTopAppBar(
                title = state.screenDisplayName(),
                navigationIcon = painterResource(id = R.drawable.ic_close),
                navigationIconContentDescription = stringResource(id = R.string.close),
                onNavigationIconClick = remember(viewModel) {
                    { viewModel.trySendAction(VaultAddItemAction.Common.CloseClick) }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    BitwardenTextButton(
                        label = stringResource(id = R.string.save),
                        onClick = remember(viewModel) {
                            { viewModel.trySendAction(VaultAddItemAction.Common.SaveClick) }
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        when (val viewState = state.viewState) {
            is VaultAddItemState.ViewState.Content -> {
                AddEditItemContent(
                    state = viewState,
                    isAddItemMode = state.isAddItemMode,
                    onTypeOptionClicked = remember(viewModel) {
                        { viewModel.trySendAction(VaultAddItemAction.Common.TypeOptionSelect(it)) }
                    },
                    loginItemTypeHandlers = loginItemTypeHandlers,
                    commonTypeHandlers = commonTypeHandlers,
                    permissionsManager = permissionsManager,
                    identityItemTypeHandlers = identityItemTypeHandlers,
                    modifier = Modifier
                        .imePadding()
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
            }

            is VaultAddItemState.ViewState.Error -> {
                VaultAddEditError(
                    viewState = viewState,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
            }

            VaultAddItemState.ViewState.Loading -> {
                VaultAddEditItemLoading(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun VaultAddEditItemDialogs(
    dialogState: VaultAddItemState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is VaultAddItemState.DialogState.Loading -> {
            BitwardenLoadingDialog(
                visibilityState = LoadingDialogState.Shown(dialogState.label),
            )
        }

        is VaultAddItemState.DialogState.Error -> BitwardenBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = R.string.an_error_has_occurred.asText(),
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        null -> Unit
    }
}
