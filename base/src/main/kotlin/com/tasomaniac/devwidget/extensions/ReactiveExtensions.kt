@file:Suppress("NOTHING_TO_INLINE")

package com.tasomaniac.devwidget.extensions

import io.reactivex.Single

inline fun <T> Single<List<T>>.flatten() = flattenAsObservable { it }

inline fun Single<Boolean>.onlyTrue() = filter { it }
