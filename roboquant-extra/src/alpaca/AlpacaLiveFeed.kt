package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.marketdata.realtime.bar.BarMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.realtime.quote.QuoteMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.realtime.trade.TradeMessage
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataListener
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.time.Instant

/**
 * Alpaca feed allows you to subscribe to live market data from Alpaca. Alpaca needs a key and secret in order to access
 * their API.
 *
 * You can provide these to the constructor or set them as environment variables ("APCA_API_KEY_ID", "APCA_API_SECRET_KEY").
 *
 * @constructor
 *
 * @param apiKey
 * @param apiSecret
 */
class AlpacaLiveFeed(apiKey: String? = null, apiSecret: String? = null, accountType :AccountType = AccountType.PAPER, dataType:DataType = DataType.IEX, autoConnect: Boolean = true) : LiveFeed() {

    private val alpacaAPI: AlpacaAPI = AlpacaConnection.getAPI(apiKey, apiSecret, accountType, dataType)
    private val assetsMap = mutableMapOf<String, Asset>()

    val assets
        get() = assetsMap.values.toSortedSet()

    val logger = Logging.getLogger("AlpacaFeed")
    private val listener = createListener()

    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI)
    }

    init {
        if (autoConnect) connect()
        alpacaAPI.marketDataStreaming().setListener(listener)
    }


    /**
     * Start listening for market data
     *
     */
    fun connect() {
        alpacaAPI.marketDataStreaming().connect()
    }

    /**
     * Stop listening for market data
     *
     */
    fun disconnect() {
        try {
            alpacaAPI.marketDataStreaming().disconnect()
        } catch (e: Exception) {}
    }


    fun subscribe(assets: Collection<Asset>) {
        subscribe(*assets.toTypedArray())
    }

    fun subscribe(vararg assets: Asset) {
        for (asset in assets) {
            require(asset.type == AssetType.STOCK) { "Only stocks supported, received ${asset.type}" }
            require(asset.currencyCode == "USD") { "Only USD currency supported, received ${asset.currencyCode}" }
        }
        if (assets.isEmpty()) {
            alpacaAPI.marketDataStreaming().subscribe(null, null, listOf("*"))
            logger.info("Subscribing to all assets")
        } else {
            val symbols = assets.map { it.symbol }
            alpacaAPI.marketDataStreaming().subscribe(null, null, symbols)
            logger.info("Subscribing to ${assets.size} assets")
        }
    }


    private fun createListener(): MarketDataListener {
        return MarketDataListener { streamMessageType, msg ->
            val action: PriceAction = when (msg) {
                is TradeMessage -> TradePrice(assetsMap[msg.symbol]!!, msg.price, msg.size.toDouble())
                is QuoteMessage -> PriceQuote(
                    assetsMap[msg.symbol]!!,
                    msg.askPrice,
                    msg.askSize.toDouble(),
                    msg.bidPrice,
                    msg.bidSize.toDouble()
                )
                is BarMessage -> PriceBar(
                    assetsMap[msg.symbol]!!,
                    msg.open.toFloat(),
                    msg.high.toFloat(),
                    msg.low.toFloat(),
                    msg.close.toFloat(),
                    msg.volume.toFloat()
                )
                else -> {
                    throw Exception("Unexpected type $streamMessageType")
                }
            }
            val now = Instant.now()
            val event = Event(listOf(action), now)
            channel?.offer(event)
        }
    }

}

