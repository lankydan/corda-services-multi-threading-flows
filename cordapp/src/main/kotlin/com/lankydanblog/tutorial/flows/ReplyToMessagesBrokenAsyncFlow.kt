package com.lankydanblog.tutorial.flows

import co.paralleluniverse.fibers.Suspendable
import com.lankydanblog.tutorial.services.MessageRepository
import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.transactions.SignedTransaction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@InitiatingFlow
@StartableByRPC
class ReplyToMessagesBrokenAsyncFlow : FlowLogic<List<SignedTransaction>>() {

    private companion object {
        private val executor: Executor = Executors.newFixedThreadPool(8)!!
    }

    @Suspendable
    override fun call(): List<SignedTransaction> {
        messages().map {
            executor.execute {
                reply(it)
            }
        }
        return emptyList()
    }

//    @Suspendable
//    override fun call(): List<SignedTransaction> {
//        return messages().map { CompletableFuture.supplyAsync { reply(it) }.join() }
//    }

    private fun messages() =
        repository().findAll(PageSpecification(1, 100))
            .states
            .filter { it.state.data.recipient == ourIdentity }

    private fun repository() = serviceHub.cordaService(MessageRepository::class.java)

    @Suspendable
    private fun reply(message: StateAndRef<MessageState>) = subFlow(SendMessageFlow(response(message), message))

    private fun response(message: StateAndRef<MessageState>): MessageState {
        val state = message.state.data
        return state.copy(
            contents = "Thanks for your message: ${state.contents}",
            recipient = state.sender,
            sender = state.recipient
        )
    }
}