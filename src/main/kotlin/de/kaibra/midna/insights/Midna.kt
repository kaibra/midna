package de.kaibra.midna.insights

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

class InsightAlreadyGathered() : Exception("The insight was already gathered")

data class Insight(val type: Int)

@Component
class Midna {

    private var insightGathering: Job? = null
    private val insightProducerByName: MutableMap<String, Function0<ReceiveChannel<Insight>>> = mutableMapOf()

    init {
        this.registerInsightProducer("Insight A") {
            produce {
                delay(4000)
                send(Insight((Math.random() * 100).toInt()))
            }
        }

        this.registerInsightProducer("Insight B") {
            produce {
                delay(1000)
                send(Insight((Math.random() * 100).toInt()))
            }
        }

        this.registerInsightProducer("Insight C") {
            produce {
                delay(500)
                throw kotlin.RuntimeException("DAMN")
            }
        }

        this.wakeUpMidna()
    }

    fun registerInsightProducer(insightName: String,
                                       insightProducer: Function0<ReceiveChannel<Insight>>) {
        this.insightProducerByName.put(insightName, insightProducer)
    }

    private fun wakeUpMidna() {
        this.insightGathering = launch {
            while (true) {
                delay(1000)
                gatherAllInsights()
            }
        }
    }

    private suspend fun gatherAllInsights() {
        insightProducerByName.forEach {
            try {
                val result = checkResult(it.value)
                println("${it.key}: ${result.type}")
            } catch (e: TimeoutCancellationException) {
                println("${it.key}: timeout")
            } catch (t: Throwable) {
                println("${it.key}: error")
            }
        }
    }

    private suspend fun checkResult(checkFn: Function0<ReceiveChannel<Insight>>): Insight {
        val channel = checkFn.invoke()
        try {
            return withTimeout(2000) {
                channel.receive()
            }
        } finally {
            channel.cancel(InsightAlreadyGathered())
        }
    }

    @PreDestroy
    private fun shutdown() {
        this.insightGathering?.cancel()
    }

}