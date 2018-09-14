How can I make my Flows faster? There's a good chance you have thought about this before if you have been working with Corda for a while. You can make reasonable tweaks to eak out performance improvements by changing a few things: transaction size, optimising queries and reducing the amount of network hops required throughout the Flow's execution. There is one other possibility that probably also crossed your mind at some point. Multi-threading. 

If you tried this you have probably faced a similar exception to the one I got. Furthermore, as of now, Corda does not support threading within Flows. But, it can still be done. We just need to be clever about it. Thats where multi-threading within Corda Services comes in. They can be called within Flows but are not prisoners to the strict rules that Flows put on them, since an executing Flow will not suspend or checkpoint from within a service.

In this post I will focus on multi-threading the starting of Flows from within a Service. There are other area's that threading can be used within Corda, but this is the most interesting area to make use of them. On the other hand, starting Flows from a Service is also filled with a few gotchas. These need to be accounted for and traversed around. Otherwise, you are going to wake up one day and wonder why everything has stopped for no apparent reason. Luckily for you, I am here to help. For me, well, I had to face this problem head on.

For reference, I will be using Open Source `3.2` and Enterprise `3.1` Corda distributions for this post.

### Scenario

Let's start with outlining the scenario that we will be using for this post.

- PartyA sends PartyB some messages over time. Each message comes from a single Flow.
- PartyB responds to all messages sent to them. Each message comes from a single Flow, but they want a single place to execute the process.

A series of Flows can be quickly put together to satisfy this requirement. Doing this sequentially should prove absolutely zero problems (after we have fixed all the stupid mistakes we all make).

Although this scenario is a poor case for needing performance, it is a simple one to process for how we can run this asynchronously. 

### Solutions

In my opinion, there are two solutions here. I will go through both of them in this post.
- Start multiple Flows from the RPC level.
- Start a single Flow that triggers subsequent Flows.

As you will see throughout this post, the first option of starting from the RPC level comes with the least problems. The main downside of doing it here, in my opinion, is that your application code that lives outside of the Corda node now contains logic that should be inside the node. This decreases reusability, unless you are already being smart with how you structure your code. Maybe this won't be a problem for you, but maybe it will...

Furthermore, using RPC might not even be possible from the point you want to trigger the asynchronous starting of Flows. These are all areas that need to be considered.

The benefit of a single Flow that triggers this actually the solution to the problem I proposed with RPC. The code now lives inside a CorDapp and therefore a node. Anyone that has this CorDapp can now execute this Flow and doesn't need to worry about writing their own application code to cater for the scenario.

### RPC version

Have I ran into the queue issue? Am I not starting my flows correctly?
- Yes it is the queue issue
- I need to start in a new thread (e.g. executor service)
- Futures still hold the flow worker since join was being used
- Futures are not safe, they do work in enterprise, but leads to the possibility of flow worker deadlock
- Only fully correct solution is to start on new threads and not depend on the result of the thread (i.e. dont use futures)
- This limits the functionality quite heavily