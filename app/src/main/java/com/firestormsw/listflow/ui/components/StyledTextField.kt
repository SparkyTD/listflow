package com.firestormsw.listflow.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.Container
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.BorderThickness
import com.firestormsw.listflow.ui.theme.CornerRadius
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors: TextFieldColors = OutlinedTextFieldDefaults.colors().copy(
        focusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Accent,
        focusedTextColor = TextPrimary,
        focusedLabelColor = Accent,

        unfocusedIndicatorColor = TextSecondary,
        unfocusedLabelColor = TextSecondary
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(
            rememberUpdatedState(
                if (isError)
                    colors.errorCursorColor
                else
                    colors.cursorColor
            ).value
        ),
        textStyle = TextStyle(color = colors.focusedTextColor),
        modifier = if (label.isNotBlank()) {
            modifier
                .semantics(mergeDescendants = true) {}
                .padding(top = 8.dp) // OutlinedTextFieldTopPadding
        } else {
            modifier
        }
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            interactionSource = interactionSource,
            visualTransformation = VisualTransformation.None,
            placeholder = placeholder,
            label = { Text(label) },
            colors = colors,
            container = {
                Container(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = RoundedCornerShape(CornerRadius.Medium),
                    focusedBorderThickness = BorderThickness.Medium,
                    unfocusedBorderThickness = BorderThickness.Medium,
                )
            },
        )
    }

}