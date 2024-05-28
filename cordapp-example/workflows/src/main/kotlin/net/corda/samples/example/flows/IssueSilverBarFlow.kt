package net.corda.samples.example.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.example.contracts.SilverBarContract
import net.corda.samples.example.states.SilverBarState

@InitiatingFlow
@StartableByRPC
class IssueSilverBarFlow(private val weight: Double, private val owner: Party) : FlowLogic<SignedTransaction>() {

    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new SilverBar.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        val issuer = ourIdentity
        val silverBarState = SilverBarState(weight, owner, issuer)
        val txCommand = Command(SilverBarContract.Commands.Issue(), silverBarState.participants.map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(silverBarState, SilverBarContract.ID)
                .addCommand(txCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = GATHERING_SIGS
        val otherPartySessions = (silverBarState.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions))

        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions))
    }
}

@InitiatedBy(IssueSilverBarFlow::class)
class IssueSilverBarFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // Implement any additional transaction checks here
            }
        }

        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, txId))
    }
}
