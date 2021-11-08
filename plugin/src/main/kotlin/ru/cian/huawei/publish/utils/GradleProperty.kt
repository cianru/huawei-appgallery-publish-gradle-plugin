package ru.cian.huawei.publish.utils

import kotlin.reflect.KProperty
import org.gradle.api.Project

internal class GradleProperty<T, V>(
    project: Project,
    type: Class<V>
) {
    private val property = project.objects.property(type)

    operator fun getValue(thisRef: T, property: KProperty<*>): V {
        return this.property.orNull ?: throw IllegalArgumentException(
            "You've forgot mention '$property' among plugin required extension params"
        )
    }

    operator fun setValue(thisRef: T, property: KProperty<*>, value: V) =
        this.property.set(value)
}
