# Notes on 7 concurrency model in 7 weeks
// This notes is for vietnamruby #hardcore learning group

## Author
- Paul Butcher - https://paulbutcher.com/
- Consultant
- He was Chief Software Architect of SwiftKey,
- Chief Technical Officer of Texperts and
- Chief Technical Officer of Smartner.


## Why
- Economic advantage
- Responsive

  Main job and background job model
- Resilient

  One thing crashes doesn't crash the whole world.
- Distributed - Multicore to multi machines

## When
- Rebuild the current single thread system to multiple thread.
- Building new high scale for current big system

## Terms
```
Concurrency is about dealing with lots of things at once.
Parallelism is about doing lots of things at once.
```

# Correctness

## Problem
- Race condition
- Memory Visibility
- Deadlock
- LiveLock
- Error handling inside blocking area.

## Solution
- Modeling ( at programming language level )
  - STM
  - Communicate by messaging

- Lock
  - Implicit lock with `synchronized` method in Java
  - Explicit lock with `ReentranceLock`
- Atomic datastructure.
- CopyOnWrite

## Notice
- Fairness
- Timeout
- Error Handling

## Implementation
- Be aware of alien method inside blocking method ( implicit or explicit ), it might cause deadlock.


# Performance

## Mistakes
- No free lunch
  - Throwing more cores and threads doesn't mean speed will increase.
- Small workload might not show the improvement, you need to use the big enough workload.

## Problem
- Resource Contention
- CPU cache missing

## Solution
- Parallelize as much as possible.
- Batching to leverage CPU cache.

## More
- Understand memory model of your language, VM design ( JIT ) really helps analyzing and design high performance system.
- Really understand your hardware so you can archive the best performance.

## Related
- Java memory model ? Language specific
  JIT language tends to reorder instruction in order to optimize running time, we need to notice this as well.


# Functional Programming
- Avoid hiding mutable state
  In OOP, we hide state as properties, attributes inside object. Having shared state you need to use Locks to protect it, it gets complicated over time. In functional programming, they try to avoid sharing state as much as possible with the idea of Immutable Datastructure.

## Why
- Mutable state causes contention and make it hard to parallelize operation.

  ```
  public​ ​int​ sum(​int​​[]​ numbers) {
  ​int​ accumulator = 0; // shared memory
  ​for​ (​int​ n: numbers)
    accumulator += n;
 ​return​ accumulator;
  }
  ```
- Hidden mutable state makes it harder to parallelize operation.

  ```
  // Imperative
  class​ DateParser {
  ​private​ ​final​ ​DateFormat​ format = ​new​ ​SimpleDateFormat​(​"yyyy-MM-dd"​); // hidden state
  ​public​ ​Date​ parse(​String​ s) ​throws​ ParseException {
    ​return​ format.parse(s);
  }
  }
  ```

## Solution
- Immutable with Persistent data structure
- Mutable with Atom ( based on Atomic variable in Java ) and STM

## Correctness
- Immutable datastructure > parallelize would be easier without worrying about contention of shared memory.

## Performance
- Batching - same idea like OOP, how does it improve the performance is more related to hardware architecture. We will talk more about this later.

## Examples
- We use CLojure for the demonstration since it's dynamic and concurrency is built and designed elegantly.

```
;; Just copy this whole block and paste into repl
(defn sum [xs] (reduce + xs))
(sum [1 2 3 4 5])

;; parallel version
(require '[clojure.core.reducers :as r])
(defn parallel-sum [xs] (r/fold + xs))
(parallel-sum [1 2 3 4 5])
```

## More
- [Clojure] Trying out clojure quickly

```
brew install leiningen
lein new clj-test; cd clj-test;
lein repl
```
- Clojure: `clojure.core.reducers` is a lib designed to make parallelism easier and faster. You can learn more through Rich Hickey talk
  https://vimeo.com/45561411

# Concurrency Models in functional programming
- Atom: It makes sure the variable's value correct at the moment in time. It is implemented with a high performance operation CAS at hardware level, and it's suitable for simple variable.

```
(def a (atom 0))
(swap! a inc) ;; increase a by 1
(swap! a dec) ;; decrease a by 1
@a ;; deref to get the value
;; internal compare-and-set!
```

- Agent: Same as atom, but it does change the value of single variable in async manner. By changing a variable we just send an operation to it.

```
(def ag (agent 0))
(send ag inc)
(send ag dec)
@ag ;; deref to get the value
```

- Ref  : Ref is one of the unique concurrency model of Clojure. By leveraging persistent datastructure ( you can think of it like database in memory ), it can do ACID operation like most of database does, without D because it doesn't support persisting to disk. Because of this, we can do transaction on multiple values easily.

```

(​def​ attempts (​atom​ 0))
​ 
(​def​ transfers (​agent​ 0))
 
(​defn​ transfer [from to amount]
  (​dosync
    (​swap!​ attempts ​inc​) // Side-effect in transaction ​-​ DON'T DO THIS
    (​send​ transfers ​inc​)
    (​alter​ from ​-​ amount)
    (​alter​ to ​+​ amount)))
(​def​ checking (​ref​ 10000))
(​def​ savings (​ref​ 20000))
(​defn​ stress-thread [from to iterations amount]
  (Thread. #(​dotimes​ [_ iterations] (transfer from to amount))))

(​defn​ main []
  (​println​ ​"Before: Checking ="​ @checking ​" Savings ="​ @savings)
  (​let​ [t1 (stress-thread checking savings 100 100)
        t2 (stress-thread savings checking 200 100)]
    (​.​start t1)
    (​.​start t2)
    (​.​join t1)
    (​.​join t2))
  (​await​ transfers) ;;
  (​println​ ​"Attempts: "​ @attempts)
  (​println​ ​"Transfers: "​ @transfers)
  (​println​ ​"After: Checking ="​ @checking ​" Savings ="​ @savings))
```

There are utils included in these concurrency model including: error-handler, validation, watcher.

- Actor - ready for distributed programming
Actor is even more OOP than OOP itself.

An actor is just like an object in OOP, it does have internal state. But it's completely isolated, it only communicate with each other through messaging, and it does not share anything. Because of this feature, it's very scalable.

Since one of the most famous language that implements actor is Erlang, we will try to use a modern look of Erlang which is Elixir for the example ( In JVM, you can use Akka lib ).

```
defmodule​ Counter ​do
  ​def​ start(count) ​do
    spawn(__MODULE__, :loop, [count])
  ​end

  ​def​ next(counter) ​do
    send(counter, {:next})
  ​end
​ 
  ​def​ loop(count) ​do
    ​receive​ ​do
      {:next} ->
        IO.puts(​"Current count: #{count}"​)
        loop(count + 1)
    ​end
  ​end
​end

# Usage
counter = Counter.start(42)
Counter.next(counter)
# Current count: 42
```

# Applying Concurrency
- To find out which one is better or worse, I have modeled dining philosophers problem in different modeling including locking, STM, atom ( pretty much implicit lock ) and actor. What makes this problem interesting is we have a coordinated async problem, how ? 5 chopsticks, 5 people, at the same time we can have at most 2 eating (async) and each must have 2 chopsticks to dine ( coordinated ). So one can thinking about locking the whole table then each can dine, it's correct but it's not the way we do concurrency.
- After observing the implementation, I conculde that Clojure has a very clear way on how to do concurrency, they have full utils to do async and coordinated concurrency. Both clojure and erlang implementations are lockless which is nice. Other implementation, you cannot avoid lock and in the case of coordinated concurrency you need to pay attention to global fixed order and the order on how you unlock your lock.

# Tooling
- Manage Thread pool :
  Why: The work load you need to handle comes faster than they can be handled, then the whole server can be hung cause the app will use all available threads. So we have Executor to manage those thread, they know when to intansiate new thread and when to not.

## More on tooling
- Fork/Join vs ExecutorService

// TODO: add more examples
// How to write more about modeling current software around: Identity ( Global state changing over time ) and MapReduce ( action to produce something ). Most of the works in concurrency are about these two.
// This note will be edited and changed over the time I read the book, so it might not correct, any feedback is appreciated.

