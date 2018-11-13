package com.lezhnin.project.sodium.store

object Sodium {
    const val MAP_NAME = "sodiumMap"
}

object Store {
    const val STORES = "stores"
    const val CONFIG = "config"
    const val FORMAT = "format"
    const val TYPE = "type"
    const val SCAN_PERIOD = "scanPeriod"

    object Type {
        const val FILE = "file"
        const val GIT = "git"
        const val JSON = "json"
    }
}

object Web {
    const val PATH = "/data/"
    const val PARAMETER = "name"
    const val PORT = "http.port"
}
