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

PROJECT STRUCTURE:

Paxos:
The main class to initialize councillors and simulate the Paxos protocol.

Councillor:
Represents a councillor participating in the protocol.
Handles incoming requests for the Prepare, Accept, and Decide phases.

Proposer:
Initiates the voting process and coordinates with councillors to reach consensus.

REQUIREMENTS:

PREREQUISITES:

1.JAVA
  VERSION:java 22.0.2 2024-07-16

![Screenshot (138)(1).png](..%2F..%2FAppData%2FLocal%2FTemp%2Fkappframework-gqZoRo%2FScreenshot%20%28138%29%281%29.png)
