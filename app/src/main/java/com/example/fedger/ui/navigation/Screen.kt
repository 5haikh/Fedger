package com.example.fedger.ui.navigation

sealed class Screen(val route: String) {
    object PersonList : Screen("personList")
    object AddPerson : Screen("addPerson")
    object PersonDetails : Screen("personDetails/{personId}") {
        fun createRoute(personId: Int) = "personDetails/$personId"
    }
    object AddTransaction : Screen("addTransaction/{personId}") {
        fun createRoute(personId: Int) = "addTransaction/$personId"
    }
    object AddTransactionGlobal : Screen("addTransactionGlobal")
    object DataImportExport : Screen("dataImportExport")
    object BalanceSummary : Screen("balanceSummary")
    
    object AppSelection : Screen("appSelection")
    
    object PasswordList : Screen("passwordList")
    object AddPasswordEntry : Screen("addPasswordEntry")
    object PasswordDetails : Screen("passwordDetails/{entryId}") {
        fun createRoute(entryId: Int) = "passwordDetails/$entryId"
    }
    object EditPasswordEntry : Screen("editPasswordEntry/{entryId}") {
        fun createRoute(entryId: Int) = "editPasswordEntry/$entryId"
    }
    object PasswordImportExport : Screen("passwordImportExport")
}
