package me.codedbyyou.os.game.ui.theme

import androidx.compose.material.ContentAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

/**
 * a function that returns the text field colors from the theme
 * @return [TextFieldColors]
 * @author Abdollah Kandrani
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() : TextFieldColors {
    return TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.medium),
        cursorColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.medium),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.medium),
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
    )
}
