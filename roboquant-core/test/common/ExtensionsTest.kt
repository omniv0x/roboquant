/*
 * Copyright 2021 Neural Layer
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

import org.junit.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExtensionsTest {

    @Test
    fun testDouble() {
        val a = doubleArrayOf(1.0, 2.0, 3.0, 2.0)
        assertEquals(1.5, a.kurtosis())
        assertEquals(2.0, a.mean())
        assertEquals(0.816496580927726, a.std())
        assertEquals(0.0, a.skewness())
        assertEquals(0.6666666666666666, a.variance())

        assertEquals(1.0, (a / 2.0).mean())


    }

    @Test
    fun returns() {
        val a = doubleArrayOf(10.0, 20.0, 10.0)
        assertEquals(2, a.returns().size)
        assertEquals(1.0, a.returns()[0])
        assertEquals(-0.5, a.returns()[1])
    }


    @Test
    fun testRounding() {
        val small = 0.00000000000001
        assertEquals(0.0, small.zeroOrMore)
        assertTrue(small.iszero)
        assertFalse(small.nonzero)
    }

    @Test
    fun numbers() {
        val x = ZonedDateTime.now()
        val y = x + 1.days + 2.months + 1.years + 2.weeks + 100.millis + 10.seconds + 30.minutes + 1.hours
        assertTrue(y > x)
    }



    @Test
    fun testAccess() {
        val l = listOf("1", 1, true, "more", 2 , 3)
        val sub = l[1..2]
        assertEquals(1, sub[0])
        assertEquals(2, sub.size)
    }

    @Test
    fun testCurrencyPairs() {
        val s = "ABCDEF"
        val t = "ABC_DEF"
        assertEquals(s.toCurrencyPair(), t.toCurrencyPair())
        assertNull("dummy".toCurrencyPair())
    }


}