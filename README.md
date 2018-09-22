# Asynchronous Flow invocations with a Corda Service

This project focuses initiating a new Flow from inside a Corda Service. The blog post written with this code contains extra information that the code does not contain. The post can be found [here](https://lankydanblog.com/2018/09/22/asynchronous-flow-invocations-with-corda-services/)

# Structure:

* app: Spring code
* cordapp: Corda application code
* contracts-and-states: Contracts and states

# Pre-requisites:

See https://docs.corda.net/getting-set-up.html.

# Usage

## Running the nodes:

Run the following gradle task to build the Corda nodes

* Windows: `gradlew.bat deployNodes`
* Unix: `./gradlew deployNodes`

Once built, go to the `build/nodes` directory and run `./runnodes` 

## Running the webservers:

Once the nodes are running, you can start the node webserver from the command line:

* Windows: `gradlew.bat runPartyAServer`
* Unix: `./gradlew runPartyAServer`

Both approaches use environment variables to set:

* `server.port`, which defines the HTTP port the webserver listens on
* `config.rpc.*`, which define the RPC settings the webserver uses to connect to the node

## Interacting with the nodes:

Send a message via a post request

    `localhost:10011/messages`

with a body like
```json
{
    "recipient": "O=PartyB,L=London,C=GB",
    "contents": "this is a temporary message"
}
```

To reply use post request:

    `localhost:10012/replyAll`