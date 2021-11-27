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

package org.roboquant.jupyter

import org.roboquant.brokers.Trade
import org.roboquant.common.Asset

/**
 * Trade chart plots the [trades] that have been generated during a run per Asset. By default, the realized pnl of the
 * trades will be plotted but this is configurable
 *
 */
class TradeChartByAsset(
    private val trades: Collection<Trade>,
    private val aspect: String = "pnl"
) : Chart() {


    init {
        val validAspects = listOf("pnl", "fee", "amount", "quantity")
        require(aspect in validAspects) { "Unsupported aspect $aspect, valid values are $validAspects" }
    }

    private fun getTooltip(trade: Trade): String {
        val c = trade.asset.currency
        val pnl = c.toBigDecimal(trade.pnl)
        val amount = c.toBigDecimal(trade.totalAmount)
        val fee = c.toBigDecimal(trade.fee)
        return "asset: ${trade.asset} <br> time: ${trade.time} <br> qty: ${trade.quantity} <br> fee: $fee <br> pnl: $pnl <br> amount: $amount <br> order: ${trade.orderId}"
    }

    private fun toSeriesData(assets: List<Asset>): List<List<Any>> {
        val d = mutableListOf<List<Any>>()
        for (trade in trades) {
            with(trade) {
                val c = asset.currency
                val value = when (aspect) {
                    "pnl" -> c.toBigDecimal(pnl)
                    "fee" -> c.toBigDecimal(fee)
                    "amount" -> c.toBigDecimal(totalAmount)
                    "quantity" -> quantity.toBigDecimal()
                    else -> throw Exception("Unsupported aspect $aspect")
                }

                val y = assets.indexOf(asset)

                val tooltip = getTooltip(this)
                d.add(listOf(time, y, value, tooltip))
            }
        }

        return d
    }

    override fun renderOption(): String {
        val gson = gsonBuilder.create()

        val assets = trades.map { it.asset }.distinct().sortedBy { it.symbol }
        val d = toSeriesData(assets)
        val data = gson.toJson(d)
        val series = """
            {
                type: 'scatter',
                symbolSize: 10,
                data : $data
            }
        """


        val yAxisData = gson.toJson(assets.map { it.symbol })

        return """
            {
                xAxis: {
                    type: 'time',
                    scale: true
                },
                title: {
                    text: 'Trade Chart $aspect'
                },
                yAxis: {
                    type: 'category',
                    data: $yAxisData,
                    axisLine: {
                      show: false
                    }
                },
                tooltip: {
                     formatter: function (params) {
                        return params.value[3];
                     }
                },
                toolbox: {
                    feature: {
                        dataZoom: { },
                        dataView: {readOnly: true},
                        restore: {},
                        saveAsImage: {}
                    }
                },
                ${renderDataZoom()},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }
}