# Distributed Locking with Apache ZooKeeper
This repository contains a **distributed system** that implements **distributed locking** using Apache ZooKeeper.

**Objective:** Ensure mutual exclusion among multiple application instances, with stable behavior in the presence of edge cases such as session expiration, process failures, retries, and network partitions.

## Mutual Exclusion guarantee
The implementation relies on ephemeral sequential nodes in ZooKeeper:
- Ordering: Each process creates an ephemeral sequential node. The process holding the smallest sequence number is granted the lock.
- Automatic release: If a process crashes or its ZooKeeper session closes, its ephemeral node is automatically removed, ensuring the lock is released without leaks.
- Notification mechanism: Watches notify waiting processes when their predecessor node is deleted, so the next in line can acquire the lock.
- Fairness: Lock acquisition follows a FIFO queue order based on the sequence numbers.

## Requirements
- Java 17+
- Maven 3.9+ 
- ZooKeeper 3.8+
