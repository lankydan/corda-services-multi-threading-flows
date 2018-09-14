package com.lankydanblog.tutorial.flows

import com.lankydanblog.tutorial.services.MessageService
import com.lankydanblog.tutorial.states.MessageState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.util.jar.JarInputStream
import kotlin.test.assertEquals

class ReplyToMessagesFlowTest {

    private lateinit var mockNetwork: MockNetwork
    private lateinit var partyA: StartedMockNode
    private lateinit var partyB: StartedMockNode
    private lateinit var notaryNode: MockNetworkNotarySpec

    @Before
    fun setup() {
        notaryNode = MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB"))
        mockNetwork = MockNetwork(
            listOf(
                "com.lankydanblog"
            ),
            notarySpecs = listOf(notaryNode),
            threadPerNode = true,
            networkSendManuallyPumped = false
        )
        partyA =
                mockNetwork.createNode(MockNodeParameters(legalName = CordaX500Name("PartyA", "Berlin", "DE")))

        partyB =
                mockNetwork.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB", "Berlin", "DE")))
        mockNetwork.startNodes()
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }

    @Test
    fun `Flow runs without errors`() {
        val future1 = partyA.startFlow(
            SendNewMessageFlow(
                MessageState(
                    contents = "hi",
                    recipient = partyB.info.singleIdentity(),
                    sender = partyA.info.singleIdentity(),
                    linearId = UniqueIdentifier()
                )
            )
        )
        println("done: ${future1.get()}")
        val future2 = partyA.startFlow(
            SendNewMessageFlow(
                MessageState(
                    contents = "hey",
                    recipient = partyB.info.singleIdentity(),
                    sender = partyA.info.singleIdentity(),
                    linearId = UniqueIdentifier()
                )
            )
        )
        println("done: ${future2.get()}")

//        val future3 = partyB.startFlow(
//            ReplyToMessagesAsyncFlow()
//        )
//        println("done: ${future3.get()}")

        val result = partyB.services.cordaService(MessageService::class.java).replyAll()
        println("replied: $result")

//        val future3 = partyB.startFlow(
//            ReplyToMessagesBrokenAsyncFlow()
//        )
//        println("done: ${future3.get()}")
    }
}