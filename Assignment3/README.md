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
  VERSION:  java 22.0.2 2024-07-16 
  COMMAND: java --version
  ![screenshot.png](images%2Fscreenshot.png)

HOW TO RUN?

1. Clone or download the repository.
2. Compile the Java files 

       javac Paxos.java
3. Run the Paxos class:
        
       java Paxos
4. The output will display the progress of the Paxos protocol, including councillors' behaviors and consensus outcomes.

SIMULATION DETAILS

Number of Councillors

Total Councillors: 9

Majority Required: (TOTAL_COUNCILLORS / 2) + 1 = 5

Simulation Steps

Proposers (e.g., M1, M8) initiate proposals.

Each councillor responds based on its state (connected or offline).

Proposers collect promises, move to accept phase, and eventually decide on the value if a majority of councillors agree.

CODE WALKTHROUGH:

Paxos Class

Instantiates councillors with a use of a thread pool.

Continues multiple proposers to mimic several simultaneous propounded.

Closes the thread pool after the execution of the thread pool.

Councillor Class

Acts as a server for accepting request from proposers.

Handles:

Prepare Phase: Sends a promise if there is no other proposition that can be forwarded.

Accept Phase: Takes proposals with the promise as ideas.

Decide Phase: Commits to the proposal.

Imitates delays and disconnections depending on which role it has.

Proposer Class

Initiates the Paxos protocol in three phases:

Sends Prepare requests to all councillors.

Resends Accept requests to councillors who promised.

Posts Decide messages to those councillors who agreed.

OUTPUT:

 ![screenshot1.png](images%2Fscreenshot1.png)

 ![screenshot2.png](images%2Fscreenshot2.png)
 
TESTING:

PRE-REQUISITES:

JUNIT VERSION : 4.13.2

HAMCREST-CORE VERSION:1.3



COMMAND TO COMPILE TEST FILE:

        javac -cp ".;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar" PaxosTest.java 

COMMAND TO RUN TEST FILE:

        java -cp ".;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore PaxosTest

  ![screenshot3.png](images%2Fscreenshot3.png)

  ![screenshot4.png](images%2Fscreenshot4.png)
  
