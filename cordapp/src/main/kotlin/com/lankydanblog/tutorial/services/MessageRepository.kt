package com.lankydanblog.tutorial.services

import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor

@CordaService
class MessageRepository(private val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private companion object {
        val log = loggerFor<MessageRepository>()
    }

    init {
        log.info("I am alive!")
    }

    fun findAll(pageSpec: PageSpecification): Vault.Page<MessageState> =
        serviceHub.vaultService.queryBy(QueryCriteria.LinearStateQueryCriteria(), pageSpec)
}