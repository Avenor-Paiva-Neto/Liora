package com.example.liora.ui.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

@Composable
fun SideActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    size: Dp
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(size * 0.45f))
    }
}


