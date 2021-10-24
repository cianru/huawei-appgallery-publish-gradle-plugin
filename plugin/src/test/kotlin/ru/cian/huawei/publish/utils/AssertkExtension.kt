package ru.cian.huawei.publish.utils

import assertk.Assert
import assertk.all
import assertk.assertions.hasClass
import assertk.assertions.isFailure
import assertk.assertions.support.expected
import assertk.assertions.support.show
import kotlin.reflect.KClass

/**
 * Asserts the collection have any item with type.
 */
fun <T : Collection<*>> Assert<T>.haveAny(kclass: KClass<*>) = given { actual ->
    if (actual.any { kclass.qualifiedName == it!!.javaClass.name }) return
    expected("to any item with class ${show(kclass)}")
}

/**
 * Asserts the collection have nono item with type.
 */
fun <T : Collection<*>> Assert<T>.haveNone(kclass: KClass<*>) = given { actual ->
    if (actual.none { kclass.qualifiedName == it!!.javaClass.name }) return
    expected("to none item with class ${show(kclass)}")
}

/**
 * Asserts for catching of Exception. If you want check that calls doesn't meet
 * any exception use `isSuccess()` method instead of.
 */
fun <T> Assert<assertk.Result<T>>.hasException(kclass: KClass<*>) {
    this.isFailure().all(fun Assert<Throwable>.() {
        hasClass(kclass)
    })
}
