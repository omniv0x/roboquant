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

@file:Suppress("WildcardImport")

package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.SignalType
import org.roboquant.strategies.Strategy

/**
 * This strategy that makes it easy to implement different types strategies based on technical analysis indicators from
 * the TaLib library.
 *
 * This strategy requires [PriceBar] data and common use cases are candlestick patterns and moving average strategies.
 *
 * @property block the logic that will generate a signal
 */
class TaLibSignalStrategy(
    private var block: TaLib.(asset: Asset, series: PriceBarSerie) -> Signal?
) : Strategy {

    private val buffers = mutableMapOf<Asset, PriceBarSerie>()

    /**
     * The underlying [TaLib] instance that is used when executing this strategy.
     */
    val taLib = TaLib()

    /**
     * Some default TA strategies that are based off TaLibSignalStrategy
     */
    companion object {

        /**
         * Breakout strategy that supports different entry and exit periods
         */
        fun breakout(entryPeriod: Int = 100, exitPeriod: Int = 50): TaLibSignalStrategy {
            return TaLibSignalStrategy { asset, series ->
                when {
                    recordHigh(series.high, entryPeriod) -> Signal(asset, Rating.BUY, SignalType.BOTH)
                    recordLow(series.low, entryPeriod) -> Signal(asset, Rating.SELL, SignalType.BOTH)
                    recordLow(series.low, exitPeriod) -> Signal(asset, Rating.SELL, SignalType.EXIT)
                    recordHigh(series.high, exitPeriod) -> Signal(asset, Rating.BUY, SignalType.EXIT)
                    else -> null
                }
            }

        }

        /**
         * MACD strategy
         */
        fun macd(): TaLibSignalStrategy {

            val strategy = TaLibSignalStrategy { asset, prices ->
                val (_, _, diff) = macd(prices, 12, 26, 9)
                val (_, _, diff2) = macd(prices, 12, 26, 9, 1)
                when {
                    diff < 0.0 && diff2 >= 0.0 -> Signal(asset, Rating.BUY)
                    diff > 0.0 && diff2 <= 0.0 -> Signal(asset, Rating.SELL)
                    else -> null
                }
            }

            return strategy
        }

    }

    /**
     * Based on a [event], return zero or more signals. Typically, they are for the assets in the event,
     * but this is not a strict requirement.
     *
     * @see Strategy.generate
     *
     */
    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for (priceAction in event.prices.values.filterIsInstance<PriceBar>()) {
            val asset = priceAction.asset
            val buffer = buffers.getOrPut(asset) { PriceBarSerie(1) }
            if (buffer.add(priceAction)) {
                try {
                    val signal = block.invoke(taLib, asset, buffer)
                    signals.addNotNull(signal)
                } catch (ex: InsufficientData) {
                    buffer.increaseCapacity(ex.minSize)
                }
            }
        }
        return signals
    }

    /**
     * Reset all the state
     */
    override fun reset() {
        buffers.clear()
    }

    /**
     * @suppress
     */
    override fun toString() = "TaLibSignalStrategy"

}

