package com.tasomaniac.devwidget.configure

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.then
import com.tasomaniac.devwidget.data.Filter
import com.tasomaniac.devwidget.data.FilterDao
import com.tasomaniac.devwidget.data.updater.PackageResolver
import com.tasomaniac.devwidget.test.given
import com.tasomaniac.devwidget.test.willReturn
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.mockito.BDDMockito

@RunWith(Parameterized::class)
class PackageMatcherModelTest(
    private val expectedPackageMatchers: List<String>,
    private val givenPackages: List<String>
) {

    private val packageResolver = mock<PackageResolver> {
        given { allApplications() } willReturn givenPackages
    }
    private val filterDao = mock<FilterDao> {
        given { findFiltersByWidgetId(APP_WIDGET_ID) } willReturn Flowable.just(emptyList())
        given { insertFilter(any()) } willReturn Completable.complete()
    }

    private val model = PackageMatcherModel(packageResolver, filterDao, mock(), APP_WIDGET_ID)

    @Test
    fun `should find expected packageMatchers`() {
        model.findPossiblePackageMatchers()
            .test()
            .assertValue(expectedPackageMatchers)
    }

    @Test
    fun `should find possible packageMatchers with persisted filtered out`() {
        val persisted = listOf("com.*")
        BDDMockito.given(filterDao.findFiltersByWidgetId(APP_WIDGET_ID))
            .willReturn(Flowable.just(persisted))

        val expected = expectedPackageMatchers - persisted

        model.findPossiblePackageMatchers()
            .test()
            .assertValue(expected)
    }

    @Test
    fun `should emit persisted packageMatchers`() {
        BDDMockito.given(filterDao.findFiltersByWidgetId(APP_WIDGET_ID))
            .willReturn(Flowable.just(SOME_PACKAGE_MATCHERS))

        model.findAvailablePackageMatchers()
            .test()
            .assertValue(SOME_PACKAGE_MATCHERS)
    }

    @Test
    fun `should insert packageMatchers`() {
        model.insertPackageMatcher("com.tasomaniac.*")
            .test()

        val expected = Filter("com.tasomaniac.*", APP_WIDGET_ID)
        then(filterDao).should().insertFilter(expected)
    }

    companion object {

        @JvmStatic
        @Parameters(name = "expectedPackageMatchers: {0} given: {1}")
        fun data() = arrayOf(
            arrayOf(
                listOf("com.*", "com.tasomaniac.*", "com.tasomaniac.devwidget"),
                listOf("com.tasomaniac.devwidget")
            ),
            arrayOf(
                listOf(
                    "com.*", "com.tasomaniac.*", "com.tasomaniac.devwidget",
                    "de.*", "de.is24.*", "de.is24.android"
                ),
                listOf("com.tasomaniac.devwidget", "de.is24.android")
            ),
            arrayOf(
                listOf(
                    "com.*", "com.tasomaniac.*",
                    "com.tasomaniac.devwidget",
                    "com.tasomaniac.openwith"
                ),
                listOf("com.tasomaniac.devwidget", "com.tasomaniac.openwith")
            ),
            arrayOf(
                listOf("somePackage.*", "somePackage"),
                listOf("somePackage")
            )
        )

        private val SOME_PACKAGE_MATCHERS = listOf("com.*")
        private const val APP_WIDGET_ID = 1
    }
}
