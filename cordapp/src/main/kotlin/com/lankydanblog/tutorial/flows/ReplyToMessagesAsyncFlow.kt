package com.lankydanblog.tutorial.flows

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import com.lankydanblog.tutorial.services.MessageRepository
import com.lankydanblog.tutorial.services.MessageService
import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.internal.FlowAsyncOperation
import net.corda.core.internal.FlowIORequest
import net.corda.core.internal.FlowStateMachine
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.internal.executeAsync
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.transactions.SignedTransaction
import java.util.concurrent.CompletableFuture

@InitiatingFlow
@StartableByRPC
class ReplyToMessagesAsyncFlow : FlowLogic<List<SignedTransaction>>() {
//    class ReplyToMessagesAsyncFlow : FlowLogic<List<CompletableFuture<SignedTransaction>>>() {
    @Suspendable
    override fun call(): List<SignedTransaction> = serviceHub.cordaService(MessageService::class.java).replyAll()

//        @Suspendable
//    override fun call(): List<SignedTransaction> {
//        serviceHub.cordaService(MessageService::class.java).replyAll()
//        return emptyList()
//    }

//    @Suspendable
//    override fun call(): List<CompletableFuture<SignedTransaction>> {
////        return serviceHub.cordaService(MessageService::class.java).replyAll().map { it.join() }
//        val result = serviceHub.cordaService(MessageService::class.java).replyAll()
////        Fiber.currentFiber() as FlowStateMachine<*>
//        logger.error("Reached the end of ReplyToMessagesAsyncFlow")
//        return result
////        return emptyList()
//    }

//    @Suspendable
//    override fun call(): List<CompletableFuture<SignedTransaction>> {
////        return serviceHub.cordaService(MessageService::class.java).replyAll().map { it.join() }
//        val result = serviceHub.cordaService(MessageService::class.java).replyAll()
////        (Fiber.currentFiber() as FlowStateMachine<*>).suspend(FlowIORequest.ExecuteAsyncOperation, maySkipCheckpoint)
////        executeAsync()
//        logger.error("Reached the end of ReplyToMessagesAsyncFlow")
//        return result
////        return emptyList()
//    }

//    @Suspendable
//    override fun call(): List<SignedTransaction> {
////        val result = serviceHub.cordaService(MessageService::class.java).replyAll()
////        (Fiber.currentFiber() as FlowStateMachine<*>).suspend(FlowIORequest.ExecuteAsyncOperation, maySkipCheckpoint)
//        logger.error("Starting ReplyToMessagesAsyncFlow")
//        val result = messages().map {executeAsync(ReplyOperation(serviceHub.cordaService(MessageService::class.java), it))}
//        logger.error("Reached the end of ReplyToMessagesAsyncFlow")
//        return result
////        return emptyList()
//    }

//    @Suspendable
//    override fun call(): List<CompletableFuture<SignedTransaction>> = serviceHub.cordaService(MessageService::class.java).replyAll()

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

class ReplyOperation(val messageService: MessageService, val message: StateAndRef<MessageState>) : FlowAsyncOperation<SignedTransaction> {
    override fun execute(): CordaFuture<SignedTransaction> {
        return messageService.replyInNewFlow(message).returnValue
    }
}