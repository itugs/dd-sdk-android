/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.rum.internal.domain

import com.datadog.android.core.internal.constraints.DataConstraints
import com.datadog.android.core.internal.utils.toJsonArray
import com.datadog.android.log.internal.user.UserInfo
import com.datadog.android.rum.internal.domain.event.RumEvent
import com.datadog.android.rum.internal.domain.event.RumEventSerializer
import com.datadog.android.rum.internal.domain.model.ActionEvent
import com.datadog.android.rum.internal.domain.model.ErrorEvent
import com.datadog.android.rum.internal.domain.model.ResourceEvent
import com.datadog.android.rum.internal.domain.model.ViewEvent
import com.datadog.android.utils.forge.Configurator
import com.datadog.tools.unit.assertj.JsonObjectAssert.Companion.assertThat
import com.datadog.tools.unit.extensions.ApiLevelExtension
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import java.util.Date
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class),
    ExtendWith(ApiLevelExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(Configurator::class)
internal class RumEventSerializerTest {

    lateinit var testedSerializer: RumEventSerializer

    @BeforeEach
    fun `set up`() {
        testedSerializer = RumEventSerializer()
    }

    @Test
    fun `𝕄 serialize RUM event 𝕎 serialize() with ResourceEvent`(
        @Forgery fakeEvent: RumEvent,
        @Forgery event: ResourceEvent
    ) {
        val rumEvent = fakeEvent.copy(event = event)

        val serialized = testedSerializer.serialize(rumEvent)

        val jsonObject = JsonParser.parseString(serialized).asJsonObject
        assertSerializedJsonMatchesInputEvent(jsonObject, rumEvent)
        assertThat(jsonObject)
            .hasField("type", "resource")
            .hasField("date", event.date)
            .hasField("resource") {
                hasField("type", event.resource.type.name.toLowerCase())
                hasField("url", event.resource.url)
                hasField("duration", event.resource.duration)
                hasNullableField("method", event.resource.method?.name)
                hasNullableField("status_code", event.resource.statusCode)
                hasNullableField("size", event.resource.size)
                // TODO timing ?
            }
            .hasField("application") {
                hasField("id", event.application.id)
            }
            .hasField("session") {
                hasField("id", event.session.id)
                hasField("type", event.session.type.name.toLowerCase())
            }
            .hasField("view") {
                hasField("id", event.view.id)
                hasField("url", event.view.url)
            }
            .hasField("usr") {
                hasNullableField("id", event.usr?.id)
                hasNullableField("name", event.usr?.name)
                hasNullableField("email", event.usr?.email)
            }
            .hasField("_dd") {
                hasField("format_version", 2L)
            }
    }

    @Test
    fun `𝕄 serialize RUM event 𝕎 serialize() with ActionEvent`(
        @Forgery fakeEvent: RumEvent,
        @Forgery event: ActionEvent
    ) {
        val rumEvent = fakeEvent.copy(event = event)

        val serialized = testedSerializer.serialize(rumEvent)

        val jsonObject = JsonParser.parseString(serialized).asJsonObject
        assertSerializedJsonMatchesInputEvent(jsonObject, rumEvent)
        assertThat(jsonObject)
            .hasField("type", "action")
            .hasField("date", event.date)
            .hasField("action") {
                hasField("type", event.action.type.name.toLowerCase())
                hasNullableField("id", event.action.id)
                event.action.target?.let {
                    hasField("target") {
                        hasField("name", it.name)
                    }
                }
                event.action.resource?.let {
                    hasField("resource") {
                        hasField("count", it.count)
                    }
                }
                event.action.error?.let {
                    hasField("error") {
                        hasField("count", it.count)
                    }
                }
                event.action.longTask?.let {
                    hasField("long_task") {
                        hasField("count", it.count)
                    }
                }
                hasNullableField("loading_time", event.action.loadingTime)
            }
            .hasField("application") {
                hasField("id", event.application.id)
            }
            .hasField("session") {
                hasField("id", event.session.id)
                hasField("type", event.session.type.name.toLowerCase())
            }
            .hasField("view") {
                hasField("id", event.view.id)
                hasField("url", event.view.url)
            }
            .hasField("usr") {
                hasNullableField("id", event.usr?.id)
                hasNullableField("name", event.usr?.name)
                hasNullableField("email", event.usr?.email)
            }
            .hasField("_dd") {
                hasField("format_version", 2L)
            }
    }

    @Test
    fun `𝕄 serialize RUM event 𝕎 serialize() with ViewEvent`(
        @Forgery fakeEvent: RumEvent,
        @Forgery event: ViewEvent
    ) {
        val rumEvent = fakeEvent.copy(event = event)

        val serialized = testedSerializer.serialize(rumEvent)

        val jsonObject = JsonParser.parseString(serialized).asJsonObject
        assertSerializedJsonMatchesInputEvent(jsonObject, rumEvent)
        assertThat(jsonObject)
            .hasField("type", "view")
            .hasField("date", event.date)
            .hasField("application") {
                hasField("id", event.application.id)
            }
            .hasField("session") {
                hasField("id", event.session.id)
                hasField("type", event.session.type.name.toLowerCase())
            }
            .hasField("view") {
                hasField("id", event.view.id)
                hasField("url", event.view.url)
                hasField("time_spent", event.view.timeSpent)
                hasField("action") {
                    hasField("count", event.view.action.count)
                }
                hasField("resource") {
                    hasField("count", event.view.resource.count)
                }
                hasField("error") {
                    hasField("count", event.view.error.count)
                }
                event.view.longTask?.let {
                    hasField("long_task") {
                        hasField("count", it.count)
                    }
                }
            }
            .hasField("usr") {
                hasNullableField("id", event.usr?.id)
                hasNullableField("name", event.usr?.name)
                hasNullableField("email", event.usr?.email)
            }
            .hasField("_dd") {
                hasField("format_version", 2L)
            }
    }

    @Test
    fun `𝕄 serialize RUM event 𝕎 serialize() with ErrorEvent`(
        @Forgery fakeEvent: RumEvent,
        @Forgery event: ErrorEvent
    ) {
        val rumEvent = fakeEvent.copy(event = event)

        val serialized = testedSerializer.serialize(rumEvent)

        val jsonObject = JsonParser.parseString(serialized).asJsonObject
        assertSerializedJsonMatchesInputEvent(jsonObject, rumEvent)
        assertThat(jsonObject)
            .hasField("type", "error")
            .hasField("date", event.date)
            .hasField("error") {
                hasField("message", event.error.message)
                hasField("source", event.error.source.name.toLowerCase())
                hasNullableField("stack", event.error.stack)
                event.error.resource?.let {
                    hasField("resource") {
                        hasField("method", it.method.name.toUpperCase())
                        hasField("status_code", it.statusCode)
                        hasField("url", it.url)
                    }
                }
            }
            .hasField("application") {
                hasField("id", event.application.id)
            }
            .hasField("session") {
                hasField("id", event.session.id)
                hasField("type", event.session.type.name.toLowerCase())
            }
            .hasField("view") {
                hasField("id", event.view.id)
                hasField("url", event.view.url)
            }
            .hasField("usr") {
                hasNullableField("id", event.usr?.id)
                hasNullableField("name", event.usr?.name)
                hasNullableField("email", event.usr?.email)
            }
            .hasField("_dd") {
                hasField("format_version", 2L)
            }
    }

    @Test
    fun `𝕄 serialize RUM event 𝕎 serialize() with unknown event`(
        @Forgery fakeEvent: RumEvent,
        @Forgery unknownEvent: UserInfo
    ) {
        val rumEvent = fakeEvent.copy(event = unknownEvent)

        val serialized = testedSerializer.serialize(rumEvent)

        val jsonObject = JsonParser.parseString(serialized).asJsonObject
        assertSerializedJsonMatchesInputEvent(jsonObject, rumEvent)
        assertThat(jsonObject)
            .doesNotHaveField("type")
            .doesNotHaveField("date")
            .doesNotHaveField("error")
            .doesNotHaveField("action")
            .doesNotHaveField("resource")
            .doesNotHaveField("application")
            .doesNotHaveField("session")
            .doesNotHaveField("view")
            .doesNotHaveField("usr")
            .doesNotHaveField("_dd")
    }

    @Test
    fun `𝕄 keep known custom attributes as is 𝕎 serialize()`(
        @Forgery fakeEvent: RumEvent,
        forge: Forge
    ) {
        val key = forge.anElementFrom(RumEventSerializer.knownAttributes)
        val value = forge.anAlphabeticalString()
        val event = fakeEvent.copy(globalAttributes = mapOf(key to value))

        val serialized = testedSerializer.serialize(event)

        val jsonObject = JsonParser.parseString(serialized).asJsonObject

        assertThat(jsonObject)
            .hasField(key, value)
    }

    @Test
    fun `M serialize W serialize() with custom timing`(
        forge: Forge
    ) {
        // GIVEN
        val fakeCustomTimings = forge.aMap { forge.anAlphabeticalString() to forge.aLong() }
        val fakeEvent: RumEvent =
            forge.getForgery(RumEvent::class.java).copy(customTimings = fakeCustomTimings)

        // WHEN
        val serialized = testedSerializer.serialize(fakeEvent)
        val jsonObject = JsonParser.parseString(serialized).asJsonObject

        // THEN
        fakeCustomTimings
            .filter { it.key.isNotBlank() }
            .forEach {
                val keyName = "${RumEventSerializer.VIEW_CUSTOM_TIMINGS_ATTRIBUTE_PREFIX}.${it.key}"
                assertThat(jsonObject).hasField(keyName, it.value)
            }
    }

    @Test
    fun `M not add custom timings group at all W serialize() with custom timings null`(
        @Forgery fakeEvent: RumEvent
    ) {
        // WHEN
        val serialized = testedSerializer.serialize(fakeEvent)
        val jsonObject = JsonParser.parseString(serialized).asJsonObject

        // THEN
        assertThat(jsonObject)
            .doesNotHaveField(RumEventSerializer.VIEW_CUSTOM_TIMINGS_ATTRIBUTE_PREFIX)
    }

    @Test
    fun `M sanitise the custom attributes keys W level deeper than 9`(forge: Forge) {
        // GIVEN
        val fakeBadKey =
            forge.aList(size = 10) { forge.anAlphabeticalString() }.joinToString(".")
        val lastIndexOf = fakeBadKey.lastIndexOf('.')
        val expectedSanitisedKey =
            fakeBadKey.replaceRange(lastIndexOf..lastIndexOf, "_")
        val fakeAttributeValue = forge.anAlphabeticalString()
        val fakeEvent: RumEvent = forge.getForgery(RumEvent::class.java).copy(
            globalAttributes = mapOf(
                fakeBadKey to fakeAttributeValue
            )
        )

        // WHEN
        val serializedEvent = testedSerializer.serialize(fakeEvent)
        val jsonObject = JsonParser.parseString(serializedEvent).asJsonObject

        // THEN
        assertThat(jsonObject)
            .hasField(
                "${RumEventSerializer.GLOBAL_ATTRIBUTE_PREFIX}.$expectedSanitisedKey",
                fakeAttributeValue
            )
        assertThat(jsonObject)
            .doesNotHaveField("${RumEventSerializer.GLOBAL_ATTRIBUTE_PREFIX}.$fakeBadKey")
    }

    @Test
    fun `M sanitise the user extra info keys W level deeper than 8`(forge: Forge) {
        // GIVEN
        val fakeBadKey =
            forge.aList(size = 9) { forge.anAlphabeticalString() }.joinToString(".")
        val lastIndexOf = fakeBadKey.lastIndexOf('.')
        val expectedSanitisedKey =
            fakeBadKey.replaceRange(lastIndexOf..lastIndexOf, "_")
        val fakeAttributeValue = forge.anAlphabeticalString()
        val fakeEvent: RumEvent = forge.getForgery(RumEvent::class.java).copy(
            userExtraAttributes = mapOf(
                fakeBadKey to fakeAttributeValue
            )
        )

        // WHEN
        val serializedEvent = testedSerializer.serialize(fakeEvent)
        val jsonObject = JsonParser.parseString(serializedEvent).asJsonObject

        // THEN
        assertThat(jsonObject)
            .hasField(
                "${RumEventSerializer.USER_ATTRIBUTE_PREFIX}.$expectedSanitisedKey",
                fakeAttributeValue
            )
        assertThat(jsonObject)
            .doesNotHaveField("${RumEventSerializer.USER_ATTRIBUTE_PREFIX}.$fakeBadKey")
    }

    @Test
    fun `M sanitise the custom timings keys W level deeper than 8`(forge: Forge) {
        // GIVEN
        val fakeBadKey =
            forge.aList(size = 9) { forge.anAlphabeticalString() }.joinToString(".")
        val lastIndexOf = fakeBadKey.lastIndexOf('.')
        val expectedSanitisedKey =
            fakeBadKey.replaceRange(lastIndexOf..lastIndexOf, "_")
        val fakeTimingValue = forge.aLong(min = 1)
        val fakeEvent: RumEvent = forge.getForgery(RumEvent::class.java).copy(
            customTimings = mapOf(
                fakeBadKey to fakeTimingValue
            )
        )

        // WHEN
        val serializedEvent = testedSerializer.serialize(fakeEvent)
        val jsonObject = JsonParser.parseString(serializedEvent).asJsonObject

        // THEN
        assertThat(jsonObject)
            .hasField(
                "${RumEventSerializer.VIEW_CUSTOM_TIMINGS_ATTRIBUTE_PREFIX}.$expectedSanitisedKey",
                fakeTimingValue
            )
        assertThat(jsonObject)
            .doesNotHaveField(
                "${RumEventSerializer.VIEW_CUSTOM_TIMINGS_ATTRIBUTE_PREFIX}.$fakeBadKey"
            )
    }

    @Test
    fun `M use the attributes group verbose name W validateAttributes { user extra info }`(
        @Forgery fakeEvent: RumEvent
    ) {

        // GIVEN
        val mockedDataConstrains: DataConstraints = mock()
        testedSerializer = RumEventSerializer(mockedDataConstrains)

        // WHEN
        testedSerializer.serialize(fakeEvent)

        // THEN
        verify(mockedDataConstrains).validateAttributes(
            fakeEvent.userExtraAttributes,
            RumEventSerializer.USER_ATTRIBUTE_PREFIX,
            RumEventSerializer.USER_EXTRA_GROUP_VERBOSE_NAME
        )
    }

    @Test
    fun `M use the attributes group verbose name W validateAttributes { custom timings }`(
        forge: Forge
    ) {

        // GIVEN
        val mockedDataConstrains: DataConstraints = mock()
        testedSerializer = RumEventSerializer(mockedDataConstrains)
        val fakeCustomTimings = forge.aMap { forge.anAlphabeticalString() to forge.aLong(min = 1) }
        val fakeEvent: RumEvent = forge.getForgery(RumEvent::class.java).copy(
            customTimings = fakeCustomTimings
        )

        // WHEN
        testedSerializer.serialize(fakeEvent)

        // THEN
        verify(mockedDataConstrains).validateAttributes(
            fakeCustomTimings,
            RumEventSerializer.VIEW_CUSTOM_TIMINGS_ATTRIBUTE_PREFIX,
            RumEventSerializer.CUSTOM_TIMINGS_GROUP_VERBOSE_NAME
        )
    }

    // region Internal

    private fun assertSerializedJsonMatchesInputEvent(
        jsonObject: JsonObject,
        event: RumEvent
    ) {
        assertJsonContainsCustomAttributes(jsonObject, event)
    }

    private fun assertJsonContainsCustomAttributes(
        jsonObject: JsonObject,
        event: RumEvent
    ) {
        event.globalAttributes
            .filter { it.key.isNotBlank() }
            .forEach {
                val value = it.value
                val keyName = "${RumEventSerializer.GLOBAL_ATTRIBUTE_PREFIX}.${it.key}"
                when (value) {
                    null -> assertThat(jsonObject).hasNullField(keyName)
                    is Boolean -> assertThat(jsonObject).hasField(keyName, value)
                    is Int -> assertThat(jsonObject).hasField(keyName, value)
                    is Long -> assertThat(jsonObject).hasField(keyName, value)
                    is Float -> assertThat(jsonObject).hasField(keyName, value)
                    is Double -> assertThat(jsonObject).hasField(keyName, value)
                    is String -> assertThat(jsonObject).hasField(keyName, value)
                    is Date -> assertThat(jsonObject).hasField(keyName, value.time)
                    is JsonObject -> assertThat(jsonObject).hasField(keyName, value)
                    is JsonArray -> assertThat(jsonObject).hasField(keyName, value)
                    is Iterable<*> -> assertThat(jsonObject).hasField(keyName, value.toJsonArray())
                    else -> assertThat(jsonObject).hasField(keyName, value.toString())
                }
            }
        event.userExtraAttributes
            .filter { it.key.isNotBlank() }
            .forEach {
                val value = it.value
                val keyName = "${RumEventSerializer.USER_ATTRIBUTE_PREFIX}.${it.key}"
                when (value) {
                    null -> assertThat(jsonObject).hasNullField(keyName)
                    is Boolean -> assertThat(jsonObject).hasField(keyName, value)
                    is Int -> assertThat(jsonObject).hasField(keyName, value)
                    is Long -> assertThat(jsonObject).hasField(keyName, value)
                    is Float -> assertThat(jsonObject).hasField(keyName, value)
                    is Double -> assertThat(jsonObject).hasField(keyName, value)
                    is String -> assertThat(jsonObject).hasField(keyName, value)
                    is Date -> assertThat(jsonObject).hasField(keyName, value.time)
                    is JsonObject -> assertThat(jsonObject).hasField(keyName, value)
                    is JsonArray -> assertThat(jsonObject).hasField(keyName, value)
                    is Iterable<*> -> assertThat(jsonObject).hasField(keyName, value.toJsonArray())
                    else -> assertThat(jsonObject).hasField(keyName, value.toString())
                }
            }
    }

    // endregion
}
