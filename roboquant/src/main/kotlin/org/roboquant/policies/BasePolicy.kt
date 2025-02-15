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

package org.roboquant.policies

import kotlin.collections.set

/**
 * Contains a number of utility methods that are useful when implementing a new policy.
 *
 * For example, how to deal with conflicting signals or how to handle amounts in a multi-currency environment. It
 * also contains a simple method to record metrics.
 *
 * @property recording should metrics be recorded, default is false
 * @constructor Create empty Base policy
 */
abstract class BasePolicy(private val prefix: String = "policy.", var recording: Boolean = false) : Policy {

    private val metrics = mutableMapOf<String, Double>()

    /**
     * Record a metric
     *
     * @param key The name of the metric
     * @param value The value of the metric
     */
    protected fun record(key: String, value: Number) {
        if (!recording) return
        metrics["$prefix$key"] = value.toDouble()
    }

    /**
     * Return any recorded metrics
     */
    override fun getMetrics(): Map<String, Double> {
        val result = metrics.toMap()
        metrics.clear()
        return result
    }


    /**
     * Reset the state, including any recorded metrics.
     */
    override fun reset() {
        metrics.clear()
    }


}