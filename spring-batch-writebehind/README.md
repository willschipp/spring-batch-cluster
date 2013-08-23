Spring Batch Writebehind

A pattern using Spring Integration to allow Spring Batch to run the MapJobRepository, 
whilst ensuring write-behind persistence to an RDBMS service.

Components;

- write-behind components to listen to Instance and Execution object changes and publish to the RDBMS
- load component to load a MapJobRepository with values for previous executions from the RDBMS
