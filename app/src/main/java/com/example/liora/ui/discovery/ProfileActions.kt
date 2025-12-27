package com.example.liora.ui.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.liora.ui.theme.ChatButtonBackgroundColor
import com.example.liora.ui.theme.ChatButtonIconColor
import com.example.liora.ui.theme.LikeButtonBackgroundColor
import com.example.liora.ui.theme.LikeButtonIconColor

@Composable
fun ProfileActions(
    onAction: (DiscoveryAction) -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        val buttonSize = if (screenWidth < 360.dp) 56.dp else 60.dp

        // Botão de curtir
        SideActionButton(
            onClick = { onAction(DiscoveryAction.Like) },
            icon = Icons.Outlined.FavoriteBorder,
            backgroundColor = LikeButtonBackgroundColor,
            iconColor = LikeButtonIconColor,
            size = buttonSize
        )

        // Botão de chat
        SideActionButton(
            onClick = { onAction(DiscoveryAction.Chat) },
            icon = Icons.Outlined.ChatBubbleOutline,
            backgroundColor = ChatButtonBackgroundColor,
            iconColor = ChatButtonIconColor,
            size = buttonSize
        )
    }
}
