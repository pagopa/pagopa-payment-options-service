# Integration Tests

👀 Integration tests are in `integration-test/src/` folder. See there for more information.

## Precondition

In order to correctly execute the integration test some configuration must be done.

[👀 More details here](https://pagopa.atlassian.net/wiki/spaces/IQCGJ/pages/2025357476/Integration+test+Opzioni+di+Pagamento)

## How run on Docker 🐳

To run the integration tests on docker, you can run from this directory the script:

``` shell
sh ./run_integration_test.sh <local|dev|uat|prod> <sub-key>
```

---
💻 If you want to test your local branch,

``` shell
sh ./run_integration_test.sh local SUBSCRIPTION-KEY
```