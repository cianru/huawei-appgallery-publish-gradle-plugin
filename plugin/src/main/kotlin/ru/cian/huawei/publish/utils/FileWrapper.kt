package ru.cian.huawei.publish.utils

import java.io.File

internal class FileWrapper {
    fun getFile(path: String): File {
        return File(path)
    }
}