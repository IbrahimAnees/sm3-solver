# Scheduling McQueen 3 (SM3) Task Scheduling Program

Algorithmic solution to a job-scheduling problem.

## Environment

The `scheduler.jar` file located in the project root

## Arguments/Inputs

This task scheduler takes in a digraph (in .dot file form) as the primary input. This digraph represents a series of tasks to schedule (nodes), as well as the communication cost between the tasks (edges).

| Arguments   | Description                                                                   |
| ----------- | ----------------------------------------------------------------------------- |
| _INPUT.dot_ | A dot file which is representative of a digraph                               |
| _P_         | Number of processors in which to schedule the input graph on                  |
| -p N        | Where N is the number of cores to process the algorithm in parallel           |
| -v          | Option to display real time visualization of the scheduling search            |
| -o FILENAME | Where FILENAME is a specified file name in which to store the output schedule |

_Arguments in italics are compulsory. Other arguments are not_

## Output

An optimal schedule in which the tasks in the digraph can be scheduled accordding to the tasks and their respective relations with other graphs.

## Example usages:

`java -jar scheduler.jar inputGraph.dot 2 -v -p 4`

## Team

| Name          | UPI     | GitHub Username                                                                         |
| ------------- | ------- | --------------------------------------------------------------------------------------- |
| Ibrahim Anees | iane056 | [iane056](https://github.com/iane056) / [IbrahimAnees](https://github.com/IbrahimAnees) |
| Joshua Feng   | jfen445 | [jfen445](https://github.com/jfen445)                                                   |
| Eric Heo      | sheo291 | [sheo291](https://github.com/sheo291) / [ericheose](https://github.com/ericheose)       |
| Matthew Lai   | mlai962 | [mlai962](https://github.com/mlai962)                                                   |
| Bill Wong     | bwon783 | [bwon783](https://github.com/bwon783)                                                   |
| Kevin Wang    | kwan772 | [kwan772](https://github.com/kwan772)                                                   |
