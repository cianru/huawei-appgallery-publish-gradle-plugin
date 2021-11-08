package ru.cian.huawei.publish.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import ru.cian.huawei.publish.models.Credential
import java.io.File
import java.io.FileReader

internal object CredentialHelper {
    fun getCredentials(credentialsFile: File): Credential {
        val reader = JsonReader(FileReader(credentialsFile.absolutePath))
        val type = object : TypeToken<Credential>() {}.type
        return Gson().fromJson(reader, type)
    }
}
