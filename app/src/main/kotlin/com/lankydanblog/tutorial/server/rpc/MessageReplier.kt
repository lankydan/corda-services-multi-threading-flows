package com.lankydanblog.tutorial.server.rpc

import com.lankydanblog.tutorial.flows.SendMessageFlow
import com.lankydanblog.tutorial.server.NodeRPCConnection
import com.lankydanblog.tutorial.services.MessageService
import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.transactions.SignedTransaction
import org.springframework.stereotype.Component

@Component
class MessageReplier(rpc: NodeRPCConnection, private val repository: MessageRepository) {

    private val proxy = rpc.proxy

    fun replyAll(): List<SignedTransaction> =
            messages().map { reply(it).returnValue.toCompletableFuture() }.map { it.join() }

    private fun messages() =
            repository.findAll(PageSpecification(1, 100))
                    .states
                    .filter { it.state.data.recipient == proxy.nodeInfo().legalIdentities.first() }

    private fun reply(message: StateAndRef<MessageState>) =
            proxy.startFlow(::SendMessageFlow, response(message), message)

    private fun response(message: StateAndRef<MessageState>): MessageState {
        val state = message.state.data
        return state.copy(
                contents = "Thanks for your message: ${state.contents}",
                recipient = state.sender,
                sender = state.recipient
        )
    }
}