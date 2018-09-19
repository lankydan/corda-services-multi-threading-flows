package com.lankydanblog.tutorial.flows

import co.paralleluniverse.fibers.Suspendable
import com.lankydanblog.tutorial.services.MessageService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class ReplyToMessagesAsyncFlow : FlowLogic<Unit>() {
    @Suspendable
    override fun call(): Unit = serviceHub.cordaService(MessageService::class.java).replyAll()
}