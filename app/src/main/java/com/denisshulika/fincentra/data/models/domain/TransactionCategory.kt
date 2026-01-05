package com.denisshulika.fincentra.data.models.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SyncAlt
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
    SALARY("Зарплата та Готівка", Color(0xFFFFEB3B), materialIcon = Icons.Default.Star),
    SUBSCRIPTIONS("Підписки", Color(0xFF3F51B5), materialIcon = Icons.Default.Refresh),
    TRANSFERS("Перекази", Color(0xFF90A4AE), materialIcon = Icons.Default.SyncAlt),
    SERVICES("Послуги та Сервіс", Color(0xFF00BCD4), materialIcon = Icons.Default.Build),
    SHOPPING("Покупки (непрод)", Color(0xFFFF5722), materialIcon = Icons.Default.Store),
    TRAVEL("Подорожі", Color(0xFF3F51B5), materialIcon = Icons.Default.Flight),
    GOVERNMENT("Держпослуги", Color(0xFF607D8B), materialIcon = Icons.Default.AccountBalance),
    EDUCATION("Освіта", Color(0xFF795548), materialIcon = Icons.Default.School),

    OTHERS("Різне", Color(0xFF9E9E9E), materialIcon = Icons.AutoMirrored.Filled.List)
}