# About
This returns a list of transactions that are prioritized based on its maximum amount while taking into consideration the 
time it takes to send the transaction over the network. The algorithm used for prioritization is heavily borrowed from 
the `Knapsack problem` which uses dynamic programming and back tracking to optimally select items that will yield the max
value given a weight constraint. There are two parts to the algorithm - one is to compute the max amount that can
be attained given the constraint - and another is to get the participating transactions that contributed to the max amount
using back tracking. The performance of this algorithm is `O(N*W)` where N is the number of transactions and W is the
duration constraint. A brute force solution for this would have taken `O(2^n)`.

# Test Results
**50ms**
* Amount processed for target time=`50ms` countryCode=`us` amount=`4139.43` (took `50ms`)
* Amount processed for target time=`50ms` countryCode=`all` amount=`4139.43` (took `50ms`)

**60ms**
* Amount processed for target time=`60ms` countryCode=`us` amount=`1915.37` (took `20ms`)
* Amount processed for target time=`60ms` countryCode=`all` amount=`4675.71` (took `60ms`)

**90ms**
* Amount processed for target time=`90ms` countryCode=`us` amount=`3474.55` (took `40ms`)
* Amount processed for target time=`90ms` countryCode=`all` amount=`6972.29` (took `90ms`)

**1000ms**
* Amount processed for target time=`1000ms` countryCode=`us` amount=`5763.62` (took `90ms`)
* Amount processed for target time=`1000ms` countryCode=`all` amount=`35471.81` (took `1000ms`)

# Rooms for improvement
* There's a variant of the knapsack problem that utilizes less auxiliary space. I did not spend time learning
about it as it seems to be a more complicated solution especially when backtracking the transactions involved.
* `W` is duration, can be a very big number, and is usually denoted by long . However, the size of arrays in Java are
maxed at `int` so there are some limitations around this (you'll see casting from `long` to `int` which sucks).

# Notes
* The logic is implemented in the class called `TransactionProcessor`
* The unit test is in the class called `TransactionProcessorTest`

# References
* https://medium.com/@fabianterh/how-to-solve-the-knapsack-problem-with-dynamic-programming-eb88c706d3cf
* https://www.youtube.com/watch?v=8LusJS5-AGo
