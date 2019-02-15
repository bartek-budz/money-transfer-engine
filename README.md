## Exercise

Design and implement a RESTful API (including data model and the backing implementation)
for money transfers between accounts.

### Explicit requirements:
1. You can use Java, Scala or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but don't forget about
requirement #2 – keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require
a pre-installed container/server).
7. Demonstrate with tests that the API works as expected.

### Implicit requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.

# Solution overview

## Features

### Functional

* account creation with initial balance
* transfer between accounts
* checking accoutn balance
* checking account statement

### Non-functional

* high performance, thread safe concurrent processing
* although database is in-memory, data are presistent

## Implementation details

### Notable framweorks

* [Airomem](https://github.com/airomem/airomem) for persistence
* [Ratpack](https://ratpack.io) for web server instantiation

### Assumptions made

* all accounts have the same currency - an exchange service would be needed to implement transfer beetween various currencies
* only internal transfers (all accounts are in the same bank) - for simplification
* no debits (account balance can't be negative) - easy to implement - there could be a property of the Account entity defining how much an account can have debit - just no time to do that

### Known limitations

* no authorization
* no transfer history archiving, no pagination
* no API versioning
* no protection against duplicated transfers

### If I had more time I would do...

* proper performance tests
* more sophisticated http status codes

## How to...

Build:
```
gradlew build
```

Run:
```
gradlew run
```

## End Points

```
POST /services/account/create
GET /services/account/balance/:accountId
POST /services/transfer/make
GET /services/transfer/statement/:accountId
```
