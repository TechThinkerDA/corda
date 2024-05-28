package net.corda.samples.example.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import net.corda.samples.example.states.SilverBarState
import java.util.function.Predicate

class SilverBarContract : Contract {
    companion object {
        const val ID = "net.corda.samples.example.contracts.SilverBarContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.findCommand(SilverBarContract.Commands.Issue::class.java, Predicate { true })
        requireThat {
            "No inputs should be consumed when issuing a silver bar." using (tx.inputStates.isEmpty())
            "Only one output state should be created when issuing a silver bar." using (tx.outputStates.size == 1)
            val outputState = tx.outputStates.single() as SilverBarState
            "The weight of the silver bar must be positive." using (outputState.weight > 0)
        }
    }

    interface Commands : CommandData {
        class Issue : Commands
    }
}
