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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.firestormsw.listflow.ui.theme.Accent
import com.firestormsw.listflow.ui.theme.BorderThickness
import com.firestormsw.listflow.ui.theme.CornerRadius
import com.firestormsw.listflow.ui.theme.TextPrimary
import com.firestormsw.listflow.ui.theme.TextSecondary
import com.firestormsw.listflow.ui.theme.Typography

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
    initialCursorAtEnd: Boolean = false // New parameter to control cursor position
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Convert the string to TextFieldValue and manage cursor position
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = if (initialCursorAtEnd && value.isNotEmpty())
                    TextRange(value.length) // Cursor at end
                else
                    TextRange(0) // Default cursor at start
            )
        )
    }

    // Update textFieldValue when the external value changes
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = if (initialCursorAtEnd)
                    TextRange(value.length)
                else
                    textFieldValue.selection
            )
        }
    }

    val colors: TextFieldColors = OutlinedTextFieldDefaults.colors().copy(
        focusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Accent,
        focusedTextColor = TextPrimary,
        focusedLabelColor = Accent,

        unfocusedIndicatorColor = TextSecondary,
        unfocusedLabelColor = TextSecondary
    )

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            if (newValue.text != value) {
                onValueChange(newValue.text)
            }
        },
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
        textStyle = Typography.bodyLarge.copy(color = colors.focusedTextColor),
        modifier = if (label.isNotBlank()) {
            modifier
                .semantics(mergeDescendants = true) {}
                .padding(top = 8.dp) // OutlinedTextFieldTopPadding
        } else {
            modifier
        }
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = textFieldValue.text,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            interactionSource = interactionSource,
            visualTransformation = VisualTransformation.None,
            placeholder = placeholder,
            label = { Text(label, style = Typography.bodyLarge) },
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