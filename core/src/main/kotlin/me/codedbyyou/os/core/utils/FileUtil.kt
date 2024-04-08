package me.codedbyyou.os.core.utils

fun getOSName(): String {
    return System.getProperty("os.name")
}

fun getOSVersion(): String {
    return System.getProperty("os.version")
}

fun getWorkingDirectorySTR(): String {
    return System.getProperty("user.dir")
}
