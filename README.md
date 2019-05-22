# [ZIO][zio] workshop

Welcome to gentle introduction to Effect systems using [ZIO][zio]. Goal of this
exercise is to show you how to use [ZIO][zio] to solve real world problems.
Hopefully, it will show you that by using ZIO you will get to functional
implementation that is simple to reason about.

The intended way to go through this exercise is to start implementing
[modules][modules]. Each module contains basic description of the task and some
hints on how to go about implementing them. It also contains dummy implementation
(`PipelineStage.delegate`) which you are supposed to replace with your own
implementation.

Modules are ordered in order from easiest to hardest. This is of course subjective
but we tried our best.

You can start by taking a look at `_00accesslog` for reference implementation of
module. It contains code that logs all successful or failed requests. 

Reference implementations will be revealed few days after the workshop to allow
people to experiment at home.

## Requirements
Unfortunately, we are not Windows developers so if you are using Windows machine,
you are on your own. If you are using Linux or MacOS, you need following: 

- `curl` installed (if you want to use tools provided by us)
- [sbt][sbt-site] installed 
- Docker and docker-compose installed
- IDE / editor that supports SBT / Scala projects

## Running the project
First you need to bring up postgres
```shell 
docker-compose up # brings up the postgres
```

Starting proxy from terminal:
```shell 
./run-proxy.sh
```

Running from SBT shell: 
```
runMain com.avast.zioworkshop.zioworkshop.Main
```


You can also run the app from IDE, i'm sure you know how to do that ;)

## Useful resources:
- [zio docs][ziodocs] - zio docs and getting started
- [zio api docs][zioapidocs] - scaladoc for zio api
- [httpstatus][httpstatus] - simple service that always return http status based on
  path - for example `curl http://httpstat.us/500` returns status code 500, same
  goes for all status codes

# Tools
## Curl with proxy
You can run `./curl-proxy` in this directory that has all the same params as `curl` but
sends data via this proxy.

Example: `./curl-proxy -X POST http://google.com`

Note: **It won't work with HTTPS.**

## Throttling test
Run `./throttling-test.sh` in order to verify the throttling you implemented is working
properly. You should expect a lot of `Too Many Requests` and few successful requests

## Postgres admin
Part of the docker-compose is adminer. To access it, go to [localhost:9000][adminer]
and fill in:
 - server: `db`
 - username: `proxy`
 - password: `password`
 - database: `proxydb`

[ziodocs]: https://scalaz.github.io/scalaz-zio/getting_started.html 
[zioapidocs]: https://javadoc.io/doc/org.scalaz/scalaz-zio_2.12/1.0-RC4
[zio]: https://scalaz.github.io/scalaz-zio/
[modules]: /src/main/scala/com/avast/zioworkshop/zioworkshop/modules
[httpstatus]: http://httpstat.us/    
[sbt-site]: https://www.scala-sbt.org/
[adminer]: http://localhost:9000
