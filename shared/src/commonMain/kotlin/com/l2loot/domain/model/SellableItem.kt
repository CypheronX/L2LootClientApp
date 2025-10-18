package com.l2loot.domain.model

data class SellableItem(
    val itemId: Long,
    val key: String,
    val name: String,
    val originalPrice: Long,
    val aynixPrice: Long? = null
) {

    fun getDisplayPrice(useAynixPrices: Boolean): Long {
        return if (useAynixPrices && aynixPrice != null) {
            aynixPrice
        } else {
            originalPrice
        }
    }

    fun matchesSearchWithAbbreviation(query: String, abbreviationMatch: (String, String) -> Boolean): Boolean {
        if (query.isBlank()) return true
        
        val searchString = query.trim().lowercase()
        return name.contains(searchString, ignoreCase = true) ||
               key.contains(searchString, ignoreCase = true) ||
               abbreviationMatch(key, searchString)
    }
}

