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
        val logger = loggerFor<MessageService>()
        // open source works with the executor service but not completable futures
        val executor: Executor = Executors.newFixedThreadPool(8)!!
    }

    fun replyInNewFlow(message: StateAndRef<MessageState>) = serviceHub.startFlow(SendMessageFlow(response(message), message))

    fun replyAll() {
        messages().map {
            executor.execute {
                reply(it)
            }
        }
    }
//
//    fun replyAll(): List<CompletableFuture<SignedTransaction>> =
//        messages().map { reply(it).returnValue.toCompletableFuture() }

//    fun replyAll(): List<SignedTransaction> =
//        messages().map { reply(it).returnValue.toCompletableFuture() }.map { it.join() }

//    fun replyAll(): List<SignedTransaction> {
//        messages().map {
//            executor.execute {
//                reply(it)
//            }
//        }
//        return emptyList()
//    }

    // might need to do in a executor to get it all to run
    // this code will have the same problem, as the flow worker is still blocking due to the while loop
    // while it waits for all the message flow workers it kicked off
    // therefore there is no way to wait for results within the service
//    fun replyAll(): List<SignedTransaction> {
//        val messages = messages()
//        val transactions = mutableListOf<SignedTransaction>()
//        messages.map {
//            executor.execute {
//                transactions + reply(it).returnValue.getOrThrow()
//            }
//        }
//        while (transactions.size < messages.size) {
//        }
//        return transactions
//    }

    private fun messages() =
            repository().findAll(PageSpecification(1, 100))
                    .states
                    .filter { it.state.data.recipient == serviceHub.myInfo.legalIdentities.first() }.also { logger.info(it.toString()) }

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

    fun sleep() {
        Thread.sleep(10000)
    }
}