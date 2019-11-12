/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-2019 Datadog, Inc.
 */

package com.datadog.android.log.internal.file

import com.datadog.android.log.internal.LogReader
import java.io.File
import java.io.FileFilter

internal class LogFileReader(private val rootDirectory: File) : LogReader {

    // region LogReader

    override fun readNextLog(): String? {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun readNextBatch(): List<String> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    // endregion
}
