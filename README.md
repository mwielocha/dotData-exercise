# dotData’s Model Factory

To test:

`sbt test`

To run:

`sbt run`

# Query analisys

Both summary and status queries should perform at a constant complexity.

Because of underlying `PriorityQueue` submitting a job may in worst case scenario result in log(n) complexity relative to the number of pending jobs.
