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

package org.roboquant.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.math.absoluteValue
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TimeframeTest {

    @Test
    fun split() {
        val tf = Timeframe.fromYears(1980, 1999)
        val subFrames = tf.split(2.years)
        assertEquals(10, subFrames.size)
        assertEquals(tf.start, subFrames.first().start)
        assertEquals(tf.end, subFrames.last().end)

        val tf2 = Timeframe.past(24.hours)
        val subFrames2 = tf2.split(30.minutes)
        assertEquals(48, subFrames2.size)
    }

    @Test
    fun sample() {
        val tf = Timeframe.fromYears(1980, 1999)
        val subFrames = tf.sample(2.months, 100)
        assertEquals(100, subFrames.size)
        assertTrue(subFrames.all { it.start >= tf.start })
        assertTrue(subFrames.all { it.end <= tf.end })
        assertTrue(subFrames.all { it.end == it.start + 2.months })
    }


    @Test
    fun empty() {
        val empty = Timeframe.EMPTY
        assertTrue(empty.isEmpty())

        val now = Instant.now()
        val empty2 = Timeframe(now, now)
        assertTrue(empty2.isEmpty())

        val empty3 = Timeframe(now, now + 1.millis)
        assertFalse(empty3.isEmpty())
    }

    @Test
    fun extend() {
        val tf = Timeframe.fromYears(1990, 2000)
        val tf2 = tf.extend(2.years, 5.years)
        assertTrue(tf.start in tf2)
        assertTrue(tf.end in tf2)
        assertTrue(tf.start - 1.years in tf2)
        assertTrue(tf.end + 4.years in tf2)
        assertFalse(tf.start - 3.years in tf2)
        assertFalse(tf.end + 6.years in tf2)
    }

    @Test
    fun parse() {
        val tf = Timeframe.parse("2019-01-01T00:00:00Z", "2020-01-01T00:00:00Z")
        assertEquals(tf, Timeframe.parse("2019", "2020"))
        assertEquals(tf, Timeframe.parse("2019-01", "2020-01"))
        assertEquals(tf, Timeframe.parse("2019-01-01", "2020-01-01"))
        assertEquals(tf, Timeframe.parse("2019-01-01T00:00:00", "2020-01-01T00:00:00"))
        assertEquals(tf, Timeframe.parse("2019-01-01T00:00:00Z", "2020-01-01T00:00:00Z"))
    }

    @Test
    fun constants() {
        val tf2 = Timeframe.INFINITE
        assertEquals(Timeframe.INFINITE, tf2)
        assertTrue(tf2.isInfinite())

        val zoneId = ZoneId.of("America/New_York")

        assertTrue(Timeframe.blackMonday1987.isSingleDay(zoneId))
        assertFalse(Timeframe.coronaCrash2020.isSingleDay(zoneId))
        assertTrue(Timeframe.flashCrash2010.isSingleDay(zoneId))
        assertFalse(Timeframe.financialCrisis2008.isSingleDay(zoneId))
        assertFalse(Timeframe.tenYearBullMarket2009.isSingleDay(zoneId))
    }

    @Test
    fun print() {
        val tf2 = Timeframe.INFINITE

        val s2 = tf2.toString()
        assertTrue(s2.isNotBlank())

        val s3 = tf2.toPrettyString()
        assertTrue(s3.isNotBlank())
    }

    @Test
    fun creation() {
        val tf = Timeframe.next(1.minutes)
        assertEquals(60, tf.end.epochSecond - tf.start.epochSecond)

        val tf2 = Timeframe.past(2.years)
        assertEquals(tf2.start, (tf2 - 2.years).end)

        assertThrows<IllegalArgumentException> {
            Timeframe.fromYears(1800, 2000)
        }
    }


    private fun Double.roughly(other: Double) = (this - other).absoluteValue < (0.01 + this.absoluteValue / 100.0)

    @Test
    fun annualize() {
        val tf = Timeframe.fromYears(2019, 2020)
        val x = tf.annualize(0.1)
        assertTrue(x.roughly(0.1))

        val tf2 = Timeframe.fromYears(2019, 2021)
        val y = tf2.annualize(0.1)
        assertTrue(y.roughly(0.05))
    }

    @Test
    fun contains() {
        val tf = Timeframe.fromYears(2019, 2020)
        assertFalse(tf.end in tf)
        assertTrue(tf.end in tf.toInclusive())
    }

    @Test
    fun plusMinus() {
        val tf = Timeframe.fromYears(2019, 2020)
        val tf2 = tf + 2.years - 2.years
        assertEquals(tf, tf2)
    }

    @Test
    fun toTimeline() {
        val tf = Timeframe.parse("2020-01-01T18:00:00Z", "2021-12-31T18:00:00Z")
        val timeline = tf.toTimeline(1.days)
        assertEquals(Instant.parse("2020-01-01T18:00:00Z"), timeline.first())
        assertTrue(Instant.parse("2021-12-31T18:00:00Z") > timeline.last())
        assertTrue(timeline.size > 200)
    }


    @Test
    fun testTrainTestSplit() {
        val tf = Timeframe.fromYears(2010, 2020)
        val (a, b) = tf.splitTrainTest(0.5)
        assertTrue(a.duration - b.duration < Duration.ofDays(2))

        val (c, d) = tf.splitTrainTest(5.years)
        assertTrue(c.duration - d.duration < Duration.ofDays(2))

        assertThrows<java.lang.IllegalArgumentException> {
            tf.splitTrainTest(11.years)
        }

        assertThrows<java.lang.IllegalArgumentException> {
            tf.splitTrainTest(1.2)
        }

    }

}