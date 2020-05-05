package ru.cian.huawei.publish.utils

import kotlin.reflect.KProperty
import org.gradle.api.Project

internal class GradleProperty<T, V>(
    project: Project,
    type: Class<V>,
    default: V? = null
) {
    val property = project.objects.property(type).apply {
        set(default)
    }

    operator fun getValue(thisRef: T, property: KProperty<*>): V =
        this.property.get()

    operator fun setValue(thisRef: T, property: KProperty<*>, value: V) =
        this.property.set(value)
}

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal class GradleIntProperty<T>(
    project: Project,
    default: Int? = null
) {
    val property = project.objects.property(Integer::class.java).apply {
        set(default as? Integer)
    }

    operator fun getValue(thisRef: T, property: KProperty<*>): Int =
            this.property.get().toInt()

    operator fun setValue(thisRef: T, property: KProperty<*>, value: Int) =
            this.property.set(value as? Integer)
}
