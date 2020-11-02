/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.core.internal.data.batching

import com.datadog.android.core.internal.data.batching.processors.DataProcessor
import com.datadog.android.core.internal.data.privacy.ConsentProvider
import com.datadog.android.core.internal.data.privacy.ConsentProviderCallback
import com.datadog.android.privacy.Consent

internal class DefaultConsentAwareDataHandler<T : Any>(

    private val consentProvider: ConsentProvider,
    private val processorsFactory: DataProcessorFactory<T>,
    private val migratorsFactory: MigratorFactory
) : ConsentAwareDataHandler<T>, ConsentProviderCallback {

    private var processor: DataProcessor<T>

    init {
        consentProvider.registerCallback(this)
        processor = resolveProcessor(null, consentProvider.getConsent())
    }

    // region ConsentAwareDataHandler

    @Synchronized
    override fun consume(event: T) {
        processor.consume(event)
    }

    @Synchronized
    override fun onConsentUpdated(previousConsent: Consent, newConsent: Consent) {
        processor = resolveProcessor(previousConsent, newConsent)
    }

    // endregion

    // region Internal

    private fun resolveProcessor(
        prevConsentFlag: Consent?,
        newConsentFlag: Consent
    ): DataProcessor<T> {
        val migrator = migratorsFactory.resolveMigrator(prevConsentFlag, newConsentFlag)
        migrator.migrateData()
        return processorsFactory.resolveProcessor(newConsentFlag)
    }

    // endregion
}