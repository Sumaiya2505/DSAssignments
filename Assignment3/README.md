PAXOS CONSENSUS ALGORITHM 

OVERVIEW:
    
  This assignment is a proof of concept of the Paxos consensus algorithm implemented in Java with an easy implementation configuration. The Paxos protocol is used for obtaining consensus in distributed systems in which more than one node should choose the same value while some nodes may fail or suffer from delays.

FEATURES:

Distributed Councillors: Councillors actually emulate distributed nodes that contribute to the decision making process.

Proposers: Proposers start a Paxos protocol to put forth values that are to be agreed upon.

Three-Phase Paxos Protocol:

Prepare Phase: Delegates then assure other councils not to be given lower proposal number whether in every session or in general.

Accept Phase: Councillors approve applications if those proposals correspond to promises.

Decide Phase: The members guaranteed to uphold a value once it is agreed on by all other councillors.

Realistic Simulation: The councillors have unpredictable latencies, link strengths and availability to approximate realistic distributed architectures.

HOW IT WORKS?

Proposers offer a value to be set.

Councillors (nodes):Vow to consider and agree to a their proposal if it has not already agreed to another more highly-numbered one.

If the councillors support the proposal, adopted the proposal and stick to it.

Agreement occurs when most of the councillors choose a single value in a voting process.



