/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.loggers

import org.roboquant.common.Lifecycle
import org.roboquant.common.TimeSeries
import java.time.Instant


/**
 * Interface that a metrics logger will need to implement. It is called by Roboquant after metrics have been calculated
 * to store and/or log them.
 *
 * A metrics logger also extends the [Lifecycle] interface.
 */
interface MetricsLogger : Lifecycle {

    /**
     * Log the [results] of the metric calculations. Also [time] is provided about when these results where captured and
     * [run] to provide the run name.
     *
     * This method is invoked once at the end of each step within a run with all the metrics that where captured during
     * that step. It should be noted that the provided results can be empty.
     */
    fun log(results: Map<String, Double>, time: Instant, run: String)

    /**
     * Get all the logged entries for a specific metric. The result is a Map with the key the run and the value the
     * metrics.
     *
     * This is optional to implement for a MetricsLogger since not all logger stores the metrics. Use [metricNames]
     * to see which metrics are available.
     */
    fun getMetric(name: String): Map<String, TimeSeries> = emptyMap()

    /**
     * The list of metric names that are available and can be retrieved with the [getMetric].
     */
    val metricNames: List<String>
        get() = emptyList()

}

/**
 * Get the metrics for the run that starts the earliest in time
 */
fun Map<String, TimeSeries>.earliestRun() : TimeSeries = values.minBy { it.timeline.first() }


/**
 * Get the metrics for the run that starts the earliest in time
 */
fun Map<String, TimeSeries>.latestRun() : TimeSeries = values.maxBy { it.timeline.last() }


