# [Hill Climbing Algorithm Cloud Computing Solver](https://fenix.tecnico.ulisboa.pt/disciplinas/AVExe/2018-2019/2-semestre)

## Project Overview
The HillClimbing@Cloud project for the Cloud Computing and Virtualization course at MEIC / METI - IST - ULisboa endeavors to architect and construct an elastic cluster of web servers. Its core function involves utilizing hill-climbing algorithms to ascertain peak values on height-maps, addressing incoming web requests with precision and agility. The project's central tenets revolve around scalability, performance enhancement, and adept management of task intricacies.

## Implementation Details
This endeavor encompasses a suite of system components, including web servers, a load balancer, an auto-scaler, and a metrics storage system. Integral to the project's success is the meticulous instrumentation of code for real-time performance metric collection. Moreover, students are tasked with devising algorithms for load balancing and auto-scaling, each meticulously designed to optimize system throughput and resource allocation. The final submission should showcase seamless integration between web server instrumentation and the metrics storage system, alongside the implementation of efficient auto-scaling and load balancing algorithms, leveraging comprehensive metrics data analysis.

### Initial Notes
The primary endeavor of this project was to craft a distributed system adept at processing graph coordinates to deduce heights utilizing a spectrum of algorithms including **_Astar_**, **_BFS_**, and **_DFS_**. The project achieved a commendable grade of 17.8/20 following rigorous evaluation by Professor Luís Veiga and Nuno Anselmo.

### Modules
- The **_./Enunciado_** directory houses the project's statement.
- The **_Auto Scaler_** module orchestrates dynamic provisioning and termination of Web Servers.
- The **_Load Balancer_** acts as the system's gateway, proficiently distributing incoming requests among Web Servers.
- **_Web Servers_** dutifully process requests, furnish results to the Load Balancer, and persist pertinent metrics in DynamoDB.
- The **_Load Balancer_** aggregates results, facilitating their prompt return to the client.
- System configurations are meticulously housed in the **_rc.local_** file.

## Group 11
- 82069 - José Brás - MEIC-T (Final grade: 16/20) [GitHub](https://github.com/ist182069)
- 84609 - Marco Coelho - MEIC-T (Final grade: 16/20) [GitHub](https://github.com/OCoise)
- 84897 - Dário Sá - MEIC-T (Final grade: 15/20) [GitHub](https://github.com/dariosa)
