package com.denisshulika.fincentra.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionCategory(
    val displayName: String,
    val color: Color,
    val materialIcon: ImageVector
) {
    FOOD("Їжа", Color(0xFF4CAF50), materialIcon = Icons.Default.ShoppingCart),
    TRANSPORT("Транспорт", Color(0xFF2196F3), materialIcon = Icons.Default.DirectionsBus),
    HOUSING("Житло", Color(0xFFFF9800), materialIcon = Icons.Default.Home),
    HEALTH("Здоров'я", Color(0xFFE91E63), materialIcon = Icons.Default.Favorite),
    ENTERTAINMENT("Розваги", Color(0xFF9C27B0), materialIcon = Icons.Default.PlayArrow),
    SALARY("Зарплата", Color(0xFFFFEB3B), materialIcon = Icons.Default.Star),
    SUBSCRIPTIONS("Підписки", Color(0xFF3F51B5), materialIcon = Icons.Default.Refresh),
    OTHERS("Різне", Color(0xFF9E9E9E), materialIcon = Icons.AutoMirrored.Filled.List)
}