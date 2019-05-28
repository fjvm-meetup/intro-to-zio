build-lists: true
theme: Next, 1

# Taming side-effects with ZIO

---
# About us

---

# Tomáš Heřman
- `@tomasherman`
- `mailto: tomas.herman@gmail.com`

# Honza Strnad
- `@hanny_strnad`
- `mailto: hanny.strnad@gmail.com`

^ members of FF
^ educating avast about FP ways
^ pushing FP in Avast forward
^ Honza ZIO contributor

[.build-lists: false]
---

# Why are we here

---

## Why are we here

- How to solve real problems in functional way?
- Target audience
- Constraints of this workshop
- About the project

^ 
Above all, we wanted to show how to solve real problem - global mutable state, working with nasty api in pure way
For total beginners, no category theory BS
We wanted to keep things as simple as possible
Proxy, it does throttling, caching, timeouts etc ... runs on http4s but abstracted away



---

# Introduction

--- 

## What is ZIO
Library for programs that are:

- fast
- asynchronous
- pure

---

## What is ZIO
Library for programs that are:

- fast
- asynchronous
- pure

<br/>

> (TLDR: better, lazy `Future[A]`)

[.build-lists: false]

--- 

## Why `lazy` is important

---

## What is an effect?

In Functional Programming:

> replacing function call with function output must not change program

---

## Example

[.code-highlight: none]
[.code-highlight: 1]
[.code-highlight: 3-5]
[.code-highlight: 1-9]
[.code-highlight: 11]

``` scala
def strLen(str: String): Int = { println(str); str.length }

@ val r: Int = strLen("hi mom!")
hi mom
res0: Int = 6

// are these programs same?
// val result = 6
// val result = strLen("hi mom")

// this is called referential transparency (but who cares)
```

---

## So how do you do ...

- Logging
- Network operations
- DB access
- File system operations
- Caching
- Global state
- ...

---

## The BIG idea

- your programs should not do effects directly
- they should describe which effects should happen and how
- then you let the interpreter to execute the description later

<br/>

- sometimes called *Effect system*
- `ZIO` is Effect System, `Future` is async result of running computation

---

## Additional ZIO features

- Error management
- Streaming
- Resource handling
- Dependency injection
- Concurrency primitives (Semaphore, Ref ...)
- Blocking API interoperability
- STM (Software transactional memory)
- ...

^ not just effect system

---

## Scope

- ZIO as effect system
- Dependency injection (just a tiny bit)
- Some concurrency primitives
- Some error management
- Retries, recovers etc.

---

## ZIO

```scala
trait ZIO[R, E, O]
```
- **R** is for envi**R**onment
- **E** is for **E**rror
- **O** is for **O**utput

> Describes a computation that, given some environment of type **R** returns either an error of type **E** or value of type **O**

---

## Less generic ZIO datatypes
[.code-highlight: none]
[.code-highlight: 1]
[.code-highlight: 3]
[.code-highlight: 5]
[.code-highlight: 3]

```scala

type IO[E, O] = ZIO[Any, E, O]

type Task[O] = ZIO[Any, Throwable, O]

type UIO[O] = ZIO[Any, Nothing, O]
```
---

## ZIO Task
```scala
ZIO[Any, Throwable, O]
```

- like lazy `Future[O]`
- or even better, like `() => Future[O]`
- except:
  - no Exceptions in pure code (no `throw`, just `Task.fail`)
  - no implicit passing of `ExecutionContext`

---

# Quick intro to ZIO envi*R*onment

- value that is passed through computation
- can be accessed at any point
- must be provided at some point before running the computation
- intended for DI, but useful for other stuff
- `Any` => computation doesn't require anything to run
- `.provide(a: A)` moves `ZIO[A, _, _]` to `ZIO[Any, _, _]`

---

# ZIO DI Example

```scala
val printTime: ZIO[Console with Clock, Throwable, Unit] = ???

import com.avast.zioworkshop.zioworkshop.utils.Common
// ^--- object Common extends Console with Clock with ...

val printTime2: Task[Unit] = printTime.provide(Common)
```


---

### Basic Task API

| Future | Task |
| ------ | ---- |
| `Future.success` | `Task.succeed` |
| `Future.failed` | `Task.fail`| 
| `Future.apply` | `Task.effect` | 
| `Future.apply` | `Task.effectTotal` | 
| `Future.{map, flatMap}` | `Task.{map, flatMap}` | 

---

## Example (revisited)

``` scala
def strLen(str: String): Task[Int] = 
    Task
      .effect(println(str))
      .map((_: Unit) => str.length)
```

---

## Basic concurrency primitives

- `scalaz.zio.Ref`: shared, concurrent, mutable variable
- `scalaz.zio.Semaphore`: you know, a classic semaphore
- `scalaz.zio.Queue`: concurrent asynchronous queue
- `scalaz.zio.FiberLocal` / `scalaz.zio.FiberRef`: `ThreadLocal` counterpart

---

### Ref

Shared mutable reference.

[.code-highlight: all]

[.code-highlight: 2]
[.code-highlight: 3]
[.code-highlight: 4]

[.code-highlight: all]

``` scala
for {
    ref   <- Ref.make(10)
    _     <- ref.set(20)
    value <- ref.get
} yield value

```

---

### Fork & join

... how to achieve concurrency

``` scala
for {
    fiber1 <- Task.effect(println(10)).fork
    fiber2 <- Task.effect(println(20)).fork
    _      <- fiber1.join
    _      <- fiber2.join
} yield ()
```

---

# Notes for the exercises

- see `_00accesslog` as an example
- use `com.avast.zioworkshop.zioworkshop.utils.Common` to inject dependencies
- use `import Common.HttpDsl._` to construct HttpResponses
- use `scalaz.zio.Ref` for global mutable state (e.g. throttling)
- go through modules and fill in `PipelineStage` implementations

---


 