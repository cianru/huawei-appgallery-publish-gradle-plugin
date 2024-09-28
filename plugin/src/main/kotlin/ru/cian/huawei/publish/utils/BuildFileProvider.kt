package ru.cian.huawei.publish.utils

import ru.cian.huawei.publish.BuildFormat
import java.io.File

internal interface BuildFileProvider {

    fun getBuildFile(buildFormat: BuildFormat): File?
}
