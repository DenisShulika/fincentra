package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.TransactionCategory

object MccDirectory {
    private val mccMap = mapOf(
        // --- AGRICULTURAL SERVICES ---
        742 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_vet),
        763 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_agro),
        780 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_landscaping),

        // --- CONTRACTED SERVICES ---
        1520 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_contractors),
        1711 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_plumbing),
        1731 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_electrical),
        1740 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_construction),
        1799 to MccDetails(
            TransactionCategory.SERVICES,
            R.string.mcc_directory_special_contractors
        ),
        2741 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_printing),
        2842 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_cleaning),

        // --- TRANSPORTATION ---
        4011 to MccDetails(TransactionCategory.TRAVEL, R.string.mcc_directory_railway_cargo),
        4111 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_public_transport),
        4112 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_railway_passenger),
        4121 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_taxi),
        4131 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_bus),
        4215 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_courier),
        4411 to MccDetails(TransactionCategory.TRAVEL, R.string.mcc_directory_cruises),
        4511 to MccDetails(TransactionCategory.TRAVEL, R.string.mcc_directory_airlines),
        4582 to MccDetails(TransactionCategory.TRAVEL, R.string.mcc_directory_airports),
        4722 to MccDetails(TransactionCategory.TRAVEL, R.string.mcc_directory_travel_agencies),
        4784 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_tolls),

        // --- UTILITIES ---
        4812 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_comm_equipment),
        4814 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_mobile),
        4816 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_it_services),
        4829 to MccDetails(TransactionCategory.TRANSFERS, R.string.mcc_directory_wire_transfers),
        4899 to MccDetails(TransactionCategory.SUBSCRIPTIONS, R.string.mcc_directory_tv_radio),
        4900 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_utilities),

        // --- RETAIL OUTLET ---
        5013 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_auto_goods),
        5045 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_computers_sw),
        5094 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_jewelry),
        5122 to MccDetails(TransactionCategory.HEALTH, R.string.mcc_directory_pharmacy_goods),
        5192 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_books_news),
        5211 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_building_materials),
        5261 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_garden),
        5310 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_discount_stores),
        5311 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_department_stores),
        5331 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_low_price_stores),
        5411 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_supermarkets),
        5441 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_confectionery),
        5462 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_bakeries),
        5499 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_groceries),
        5511 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_car_sales),
        5533 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_auto_parts),
        5541 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_gas_station),
        5542 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_gas_automated),

        // --- CLOTHING ---
        5611 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_mens_clothing),
        5621 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_womens_clothing),
        5641 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_kids_clothing),
        5651 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_family_clothing),
        5661 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_shoes),

        // --- MISC STORES & EATING ---
        5712 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_furniture),
        5722 to MccDetails(TransactionCategory.HOUSING, R.string.mcc_directory_appliances),
        5732 to MccDetails(TransactionCategory.SHOPPING, R.string.mcc_directory_electronics),
        5811 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_catering),
        5812 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_restaurants),
        5813 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_bars_clubs),
        5814 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_fastfood),
        5912 to MccDetails(TransactionCategory.HEALTH, R.string.mcc_directory_pharmacy),
        5921 to MccDetails(TransactionCategory.FOOD, R.string.mcc_directory_liquor_stores),
        5941 to MccDetails(
            TransactionCategory.ENTERTAINMENT,
            R.string.mcc_directory_sporting_goods
        ),
        5942 to MccDetails(TransactionCategory.ENTERTAINMENT, R.string.mcc_directory_book_stores),
        5945 to MccDetails(TransactionCategory.ENTERTAINMENT, R.string.mcc_directory_toys_hobby),
        5977 to MccDetails(TransactionCategory.HEALTH, R.string.mcc_directory_cosmetics),
        5995 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_pet_shops),
        6010 to MccDetails(TransactionCategory.SALARY, R.string.mcc_directory_cash_desk),
        6011 to MccDetails(TransactionCategory.SALARY, R.string.mcc_directory_atm),
        6538 to MccDetails(TransactionCategory.TRANSFERS, R.string.mcc_directory_card_topup),

        // --- SERVICES ---
        7011 to MccDetails(TransactionCategory.TRAVEL, R.string.mcc_directory_hotels),
        7230 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_barber),
        7298 to MccDetails(TransactionCategory.HEALTH, R.string.mcc_directory_spa),
        7512 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_car_rental),
        7542 to MccDetails(TransactionCategory.TRANSPORT, R.string.mcc_directory_car_wash),
        7832 to MccDetails(TransactionCategory.ENTERTAINMENT, R.string.mcc_directory_cinema),
        7997 to MccDetails(TransactionCategory.ENTERTAINMENT, R.string.mcc_directory_fitness),
        8011 to MccDetails(TransactionCategory.HEALTH, R.string.mcc_directory_doctors),
        8021 to MccDetails(TransactionCategory.HEALTH, R.string.mcc_directory_dentists),
        8211 to MccDetails(TransactionCategory.EDUCATION, R.string.mcc_directory_schools),
        8220 to MccDetails(TransactionCategory.EDUCATION, R.string.mcc_directory_universities),
        8398 to MccDetails(TransactionCategory.GOVERNMENT, R.string.mcc_directory_charity),

        // --- GOVERNMENT ---
        9222 to MccDetails(TransactionCategory.GOVERNMENT, R.string.mcc_directory_fines),
        9311 to MccDetails(TransactionCategory.GOVERNMENT, R.string.mcc_directory_taxes),
        9399 to MccDetails(TransactionCategory.GOVERNMENT, R.string.mcc_directory_gov_services),
        9402 to MccDetails(TransactionCategory.SERVICES, R.string.mcc_directory_post_services)
    )

    private val subcategoriesCache = mutableMapOf<TransactionCategory, List<Int>>()

    fun getSubcategoriesFor(category: TransactionCategory): List<Int> {
        return subcategoriesCache.getOrPut(category) {
            mccMap.values
                .filter { it.category == category }
                .map { it.subCategoryRes }
                .distinct()
        }
    }

    fun getCategory(mcc: Int?): TransactionCategory {
        return mccMap[mcc]?.category ?: TransactionCategory.OTHERS
    }

    fun getDetails(mcc: Int?): MccDetails {
        return mccMap[mcc] ?: MccDetails(TransactionCategory.OTHERS, R.string.mcc_directory_others)
    }
}