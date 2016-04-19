# Rate Limit Service

This codebase contains a service for keeping track of per user limits for outgoing requests based on the 
given set of rules. The service requires Redis to be up and running on the same host with itself.

## To Start the Service


```
$ cd rate-limiting-proxy
```

```
$ sbt run
```

# Description

The service is intended for a distributed environments, where it can be run as a microservice. It can be invoked by any
other service to report all requests and reliably tell whether a limit has been reached by a particular user. Prior to 
invoking verify limit request please make sure you've set a limits policy.

To **report a request**:

*PUT /[userKey]*

```
$ curl --request PUT -i 'http://localhost:7575/userA'

  HTTP/1.1 200 OK
  Content-Type: text/plain; charset=UTF-8
  Date: Mon, 18 Apr 2016 05:25:27 UTC
  Content-Length: 0

```

To **set a limit**:

*PUT /policy/[userKey]/[requests]/per/[seconds]*

```
$ curl --request PUT -H 'text/plain' -i 'http://localhost:7575/policy/userA/10/per/3600' -d 'Hourly limit'

  HTTP/1.1 200 OK
  Content-Type: text/plain; charset=UTF-8
  Date: Mon, 18 Apr 2016 05:31:14 UTC
  Content-Length: 0

```

To **delete a limit**:

*DELETE /policy/[userKey]/[requests]/per/[seconds]*

```
$ curl --request DELETE -i 'http://localhost:7575/policy/userA/10/per/3600'

  HTTP/1.1 200 OK
  Content-Type: text/plain; charset=UTF-8
  Date: Mon, 18 Apr 2016 05:31:14 UTC
  Content-Length: 0

```

To **get user's limits**:

*GET /policy/[userKey]*

```
$ curl --request GET -i 'http://localhost:7575/policy/userA'

  HTTP/1.1 200 OK
  Content-Type: text/plain; charset=UTF-8
  Date: Mon, 18 Apr 2016 05:34:06 UTC
  Content-Length: 50
  
  Hourly limit: [10] requests per [3600] sec

```

To **verify whether limits have been all used by a user**:

*GET /[user_id]*

```
$ curl --request GET -i 'http://localhost:7575/userA'

HTTP/1.1 409 Conflict
Content-Type: text/plain; charset=UTF-8
Date: Mon, 18 Apr 2016 05:59:23 UTC
Content-Length: 52

Number of maximum requests reached in [Hourly limit]
```

# Internals

The service relies on Redis to keep its state and can be scaled out horizontally.