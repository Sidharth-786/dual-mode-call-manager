package com.example.dualmodecallmanager.data.model

enum class AppMode {
    WORK,
    PERSONAL;

    val displayName: String
        get() = when (this) {
            WORK -> "Work Mode"
            PERSONAL -> "Personal Mode"
        }

    val description: String
        get() = when (this) {
            WORK -> "Filtering active: Only Work contacts allowed"
            PERSONAL -> "Filtering active: Only Personal contacts allowed"
        }
}

enum class ContactCategory {
    WORK,
    PERSONAL,
    BOTH;

    val displayName: String
        get() = when (this) {
            WORK -> "Work"
            PERSONAL -> "Personal"
            BOTH -> "Both"
        }
}
