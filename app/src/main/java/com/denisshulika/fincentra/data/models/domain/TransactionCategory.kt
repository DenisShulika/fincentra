package com.denisshulika.fincentra.data.models.domain

import androidx.annotation.StringRes
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
import com.denisshulika.fincentra.R

enum class TransactionCategory(
    @StringRes val displayNameRes: Int,
    val color: Color,
    val materialIcon: ImageVector
) {
    FOOD(R.string.transaction_category_food, Color(0xFF4CAF50), Icons.Default.ShoppingCart),
    TRANSPORT(
        R.string.transaction_category_transport,
        Color(0xFF2196F3),
        Icons.Default.DirectionsBus
    ),
    HOUSING(R.string.transaction_category_housing, Color(0xFFFF9800), Icons.Default.Home),
    HEALTH(R.string.transaction_category_health, Color(0xFFE91E63), Icons.Default.Favorite),
    ENTERTAINMENT(
        R.string.transaction_category_entertainment,
        Color(0xFF9C27B0),
        Icons.Default.PlayArrow
    ),
    SALARY(R.string.transaction_category_salary, Color(0xFFFFEB3B), Icons.Default.Star),
    SUBSCRIPTIONS(
        R.string.transaction_category_subscriptions,
        Color(0xFF3F51B5),
        Icons.Default.Refresh
    ),
    TRANSFERS(R.string.transaction_category_transfers, Color(0xFF90A4AE), Icons.Default.SyncAlt),
    SERVICES(R.string.transaction_category_services, Color(0xFF00BCD4), Icons.Default.Build),
    SHOPPING(R.string.transaction_category_shopping, Color(0xFFFF5722), Icons.Default.Store),
    TRAVEL(R.string.transaction_category_travel, Color(0xFF3F51B5), Icons.Default.Flight),
    GOVERNMENT(
        R.string.transaction_category_government,
        Color(0xFF607D8B),
        Icons.Default.AccountBalance
    ),
    EDUCATION(R.string.transaction_category_education, Color(0xFF795548), Icons.Default.School),
    OTHERS(R.string.transaction_category_others, Color(0xFF9E9E9E), Icons.AutoMirrored.Filled.List)
}