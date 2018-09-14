package com.lankydanblog.tutorial.flows

import co.paralleluniverse.fibers.Suspendable
import com.lankydanblog.tutorial.services.MessageService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import java.util.concurrent.CompletableFuture

@InitiatingFlow
@StartableByRPC
class ReplyToMessagesAsyncFlow : FlowLogic<List<SignedTransaction>>() {
//    class ReplyToMessagesAsyncFlow : FlowLogic<List<CompletableFuture<SignedTransaction>>>() {
//    @Suspendable
//    override fun call(): List<SignedTransaction> = serviceHub.cordaService(MessageService::class.java).replyAll()

    @Suspendable
    override fun call(): List<SignedTransaction> {
        return serviceHub.cordaService(MessageService::class.java).replyAll().map { it.join() }
//        return emptyList()
    }

//    @Suspendable
//    override fun call(): List<CompletableFuture<SignedTransaction>> = serviceHub.cordaService(MessageService::class.java).replyAll()
}