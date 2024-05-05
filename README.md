## CMPS 405 Operating Systems
## Project Phase 2

## Summary
In this phase, we used the concepts of Sockets, Multithreading, and Synchronization, to create a simple yet interactive game of "Two Thirds".
The game was made using Kotlin, which runs on the Java Virtual Machine (JVM), making development easier and faster.

## Team Members

| Member | Student ID |
| :----- | ---------: |
| Abdollah Kandrani | 202104093 |
| Mohamed Salih | 202104778 |
| Ibrahim Alsalimy | 202104709 |
| Abdelrazzaq Alsiddiq | 202004464 |

## Challenges
1. One of the Main challenges we faced was TCP Packet Overloading, as Packet Delivery was inconsistent due to differing network connectivity on different devices, To address this issue, we integrated delays and pauses into the code to ensure reliable packet delivery.

2. Syncing multiple UI Packets to their Respective UI elements, this is a challenge as doing this normally results in issues and the game seemingly working or not working randomly, to solve this issue we used Kotlin channels, which provided a multi-threaded solution, ensuring smooth communication between UI packets and their corresponding UI elements.

## Issues
1. Unstable GUI, likely due to us deciding to use LittleKT, a tool used to design and build Game Engines

## Contributions

| Member | Contribution |
| :----- | ---------: |
| Abdollah | 25% |
| Mohamed | 25% |
| Ibrahim | 25% |
| Abdelrazzaq | 25% |

## References
https://kotlinlang.org/docs/channels.html
