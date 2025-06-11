package my.zaif.navigation

object NavigationRoutes {
    const val LEDGER = "ledger"
    const val CREDENTIALS = "credentials"
    const val SETTINGS = "settings"
    const val PERSON_DETAIL = "person_detail/{personId}"
    const val ENTITY_DETAIL = "entity_detail/{entityId}"
    
    // Helper function for creating person detail route with parameter
    fun personDetail(personId: Long): String {
        return "person_detail/$personId"
    }
    
    // Helper function for creating entity detail route with parameter
    fun entityDetail(entityId: Long): String {
        return "entity_detail/$entityId"
    }
} 