package com.lankydanblog.tutorial.server.rpc

import com.lankydanblog.tutorial.server.NodeRPCConnection
import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import org.springframework.stereotype.Repository

@Repository
class MessageRepository(rpc: NodeRPCConnection) {

    private val proxy = rpc.proxy

    fun findAll(pageSpec: PageSpecification): Vault.Page<MessageState> =
            proxy.vaultQueryBy(QueryCriteria.LinearStateQueryCriteria(), pageSpec)
}