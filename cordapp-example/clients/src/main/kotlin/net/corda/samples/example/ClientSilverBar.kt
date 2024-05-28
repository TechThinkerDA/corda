package net.corda.samples.example

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import net.corda.samples.example.flows.IssueSilverBarFlow
import net.corda.samples.example.states.SilverBarState
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    require(args.size == 3) { "Usage: Client <node address> <rpc username> <rpc password>" }
    val nodeAddress = NetworkHostAndPort.parse(args[0])
    val rpcUsername = args[1]
    val rpcPassword = args[2]

    val client = CordaRPCClient(nodeAddress)
    val clientConnection = client.start(rpcUsername, rpcPassword)
    val proxy = clientConnection.proxy

    val logger = LoggerFactory.getLogger("ClientSilverBar")

    try {
        // Získanie účastníkov
        val owner = proxy.wellKnownPartyFromX500Name(CordaX500Name("PartyB", "London", "GB"))
                ?: throw IllegalArgumentException("PartyB not found")
        val issuer = proxy.nodeInfo().legalIdentities.first()

        // Spustenie flowu na vytvorenie SilverBar
        val flowHandle = proxy.startFlowDynamic(IssueSilverBarFlow::class.java, 1.0, owner)
        val result = flowHandle.returnValue.getOrThrow()

        println("Transaction id: ${result.id}")
        logger.info("Transaction id: ${result.id}")

        // Kontrola stavu SilverBar v trezore
        val silverBars = proxy.vaultQueryBy<SilverBarState>().states
        println("Silver Bars in vault:")
        silverBars.forEach { println(it.state.data) }

    } catch (e: Exception) {
        logger.error("Error: ", e)
    } finally {
        clientConnection.close()
    }
}
