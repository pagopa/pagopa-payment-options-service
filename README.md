# pagoPA Payment Options Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-payment-options-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-payment-options-service)

Expose APIs that will be used by IO to retrieve the PDF receipts

---

## Summary 📖

- [Api Documentation 📖](#api-documentation-)
- [Technology Stack 📚](#technology-stack-)
- [Start Project Locally 🚀](#start-project-locally-)
    * [Run locally with Docker](#run-locally-with-docker)
        + [Prerequisites](#prerequisites)
        + [Run docker container](#run-docker-container)
    * [Running the application in dev mode](#running-the-application-in-dev-mode)
- [Develop Locally 💻](#develop-locally-)
    * [Prerequisites](#prerequisites)
    * [Testing 🧪](#testing-)
        + [Unit test](#unit-test)
        + [Integration test [WIP]](#integration-test-wip)
        + [Performance test [WIP]](#performance-test-wip)
- [Contributors 👥](#contributors-)
    * [Maintainers](#maintainers)

---

## Api Documentation 📖

See
the [OpenApi 3 here](https://editor-next.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-payment-options-service/main/openapi/openapi.json)

In local env typing following url on browser for ui interface:

```http://localhost:8080/q/swagger-ui```

or that for `yaml` version ```http://localhost:8080/q/openapi```

or that for `json` version ```http://localhost:8080/q/openapi?format=json```

---

## Technology Stack 📚

- Java 17 Runtime Environment GraalVM CE
- [Quarkus](https://quarkus.io/)
- quarkus-resteasy-reactive
- quarkus-smallrye-health
- quarkus-smallrye-openapi
- quarkus-resteasy-reactive-jackson
- reactor-netty
- lombok (provided)

---

## Start Project Locally 🚀

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only
> at http://localhost:8080/q/dev/.

### Run locally with Docker

#### Prerequisites

- docker

#### Set environment variables

`docker build -t pagopa-payments-option-service .`

`cp .env.example .env`

and replace in `.env` with correct values

#### Run docker container

then type :

`docker run -p 8080:8080 --env-file=./.env pagopa-payment-options-service`

## Develop Locally 💻

### Prerequisites

- git
- maven (v3.9.3)
- jdk-17

### Testing 🧪

#### Unit test

Typing `mvn clean verify`

#### Integration test [WIP]

- Run the application
- Install dependencies: `yarn install`
- Run the test: `yarn test`

#### Performance test [WIP]

---

## Contributors 👥

Made with ❤️ by PagoPa S.p.A.

### Maintainers

See `CODEOWNERS` file
