package com.lmizuno.smallnotesmanager.models

data class BreadcrumbItem(val id: String?, val name: String) {
    // Null id represents the root level
}