package ru.cian.huawei.publish

import org.gradle.api.Project
import ru.cian.huawei.publish.utils.GradleProperty

open class HuaweiPublishExtension(
        project: Project
) {

    val instances = project.container(HuaweiPublishCredential::class.java) { name ->
        HuaweiPublishCredential(name, project)
    }

    companion object {
        const val NAME = "huaweiPublish"
    }
}

class HuaweiPublishCredential(
    val name: String,
    project: Project
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("Name must not be blank nor empty")
        }
    }

    var credentialsPath by GradleProperty(project, String::class.java, null)
    var isSubmitOnUser by GradleProperty(project, Boolean::class.java,  true)

    override fun toString(): String {
        return "HuaweiPublishCredential(name='$name')"
    }
}
