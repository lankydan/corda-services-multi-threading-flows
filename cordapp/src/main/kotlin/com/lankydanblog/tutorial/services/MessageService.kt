package com.lankydanblog.tutorial.services

import com.lankydanblog.tutorial.flows.SendMessageFlow
import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@CordaService
class MessageService(private val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private companion object {
        val executor: Executor = Executors.newFixedThreadPool(8)!!
    }

    fun replyAll() {
        messages().map {
            executor.execute {
                reply(it)
            }
        }
    }

    private fun messages() =
            repository().findAll(PageSpecification(1, 100))
                    .states
                    .filter { it.state.data.recipient == serviceHub.myInfo.legalIdentities.first() }

    private fun repository() = serviceHub.cordaService(MessageRepository::class.java)

    private fun reply(message: StateAndRef<MessageState>) =
            serviceHub.startFlow(SendMessageFlow(response(message), message))

    private fun response(message: StateAndRef<MessageState>): MessageState {
        val state = message.state.data
        return state.copy(
                contents = "Thanks for your message: ${state.contents}",
                recipient = state.sender,
                sender = state.recipient
        )
    }
}