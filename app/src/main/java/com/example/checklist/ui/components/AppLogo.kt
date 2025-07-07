package com.example.checklist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checklist.ui.theme.ChecklistTheme

// App logo with icon and optional text
@Composable
fun AppLogo(
    fontSize: Int = 32,
    iconSize: Int = 32,
    showText: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckBox,
            contentDescription = "Checkmark",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(iconSize.dp)
        )
        if (showText) {
            Text(
                text = "Checklist",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fontSize.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// Preview for AppLogo
@Preview(showBackground = false)
@Composable
fun AppLogoPreview(){
    ChecklistTheme {
        AppLogo()
    }
}
