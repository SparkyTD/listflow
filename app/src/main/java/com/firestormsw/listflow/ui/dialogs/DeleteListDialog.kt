package com.firestormsw.listflow.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.firestormsw.listflow.R
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.ui.icons.Delete
import com.firestormsw.listflow.ui.theme.CornerRadius
import com.firestormsw.listflow.ui.theme.PanelActive
import com.firestormsw.listflow.ui.theme.TextDanger
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.Typography

@Composable
fun DeleteListDialog(
    listModel: ListModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(Delete, contentDescription = null, tint = TextPrimary) },
        title = { Text(text = stringResource(R.string.delete_list_confirm_title, listModel.name), style = Typography.bodyLarge, color = TextPrimary) },
        text = { Text(text = stringResource(R.string.delete_list_confirm_text), style = Typography.bodyLarge, color = TextPrimary) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete_list), style = Typography.bodyLarge, color = TextDanger)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel), style = Typography.bodyLarge, color = TextPrimary)
            }
        },
        containerColor = PanelActive,
        shape = RoundedCornerShape(CornerRadius.Medium),
    )
}