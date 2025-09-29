const assert = require('assert');
const {defineParameterType, Given, When, Then, After, AfterAll} = require('@cucumber/cucumber');
const {getPaymentOptions} = require("./support/client/payment_options_client");

const idOrg = process.env.VALID_ORGANIZATIONAL_FISCAL_CODE;
const missingOrg = process.env.MISSING_ORGANIZATIONAL_FISCAL_CODE;
const singleOptNoticeNumber = process.env.SINGLE_OPT_NOTICE_NUMBER;
const singleOptAndSingleMultiNoticeNumber = process.env.SINGLE_AND_MANY_OPT_NOTICE_NUMBER;
const singleOptAndManyMultiNoticeNumber = process.env.SINGLE_AND_MULTI_OPT_NOTICE_NUMBER;
const coOptNoticeNumber = process.env.SINGLE_AND_CO_OPT_NOTICE_NUMBER;
const invalidStationNoticeNumber = process.env.INVALID_NOTICE_NUMBER;
const disabledStationNoticeNumber = process.env.DISABLED_STATION_NOTICE_NUMBER;
const disabledOdpStationNoticeNumber = process.env.DISABLED_STATION_ODP_NOTICE_NUMBER;
const missingStationNoticeNumber = process.env.MISSING_STATION_NOTICE_NUMBER;
const errorPagamentoSconosciutoNoticeNumber = process.env.ERROR_PAGAMENTO_SCONOSCIUTO_NOTICE_NUMBER;
const errorStazioneIntErrataNoticeNumber = process.env.ERROR_STAZIONE_INT_ERRATA_NOTICE_NUMBER;
const invalidResponseErrorCodeNoticeNumber = process.env.INVALID_RESPONSE_ERROR_CODE_NOTICE_NUMBER;
const invalidResponseStatusCodeNoticeNumber = process.env.INVALID_RESPONSE_STATUS_CODE_NOTICE_NUMBER;
const invalidPsp = process.env.UNAUTHORIZED_PSP;
const validPsp = process.env.VALID_PSP;


const paymentOptions = [];

defineParameterType({
    name: "boolean",
    regexp: /true|false/,
    transformer: (s) => s === "true"
});


// After each Scenario
AfterAll(async function () {

    this.paymentOptions = [];
    this.response = null;

});

When('an Http GET request is sent to recover payment options for taxCode {string} with noticeNumber {string} and idPsp {string}',
    async function (taxCode, noticeNumber, idPsp) {

        var selectedTaxCode;
        var selectedNoticeNumber;
        var selectedPsp;

        switch(taxCode) {
          case "missing":
            selectedTaxCode = missingOrg;
            break;
          default:
            selectedTaxCode = idOrg;
        }

        switch(noticeNumber) {
          case "invalid":
            selectedNoticeNumber = invalidStationNoticeNumber;
            break;
          case "disabledOdp":
            selectedNoticeNumber = disabledOdpStationNoticeNumber;
            break;
          case "missing":
            selectedNoticeNumber = missingStationNoticeNumber;
            break;
          case "single":
            selectedNoticeNumber = singleOptNoticeNumber;
            break;
          case "singleAndMultiple":
            selectedNoticeNumber = singleOptAndSingleMultiNoticeNumber;
            break;
          case "singleAndManyMultiples":
            selectedNoticeNumber = singleOptAndManyMultiNoticeNumber;
            break;
          case "co-oblidged":
            selectedNoticeNumber = coOptNoticeNumber;
            break;
          case "errorStazioneIntErrata":
            selectedNoticeNumber = errorStazioneIntErrataNoticeNumber;
            break;
          case "disabled":
            selectedNoticeNumber = disabledStationNoticeNumber;
            break;
          case "errorPagamentoSconosciuto":
            selectedNoticeNumber = errorPagamentoSconosciutoNoticeNumber;
            break;
          case "invalidResponseErrorCode":
            selectedNoticeNumber = invalidResponseErrorCodeNoticeNumber;
            break;
          case "invalidResponseStatusCode":
            selectedNoticeNumber = invalidResponseStatusCodeNoticeNumber;
            break;
        }

        switch (idPsp) {
          case "invalid":
            selectedPsp = invalidPsp;
            break;
          case "valid":
            selectedPsp = validPsp;
        }

        this.response = await getPaymentOptions(
        selectedTaxCode, selectedNoticeNumber, selectedPsp);

   });

Then('payments options has size {int}', function (expectedSize) {
    assert.strictEqual(this.response?.data?.paymentOptions.length, expectedSize);
});

Then('payments option n {int} has {int} installments', function (i, expectedSize) {
//    console.log(this.response?.data?.paymentOptions);
    assert.strictEqual(this.response?.data?.paymentOptions[i - 1].installments.length, expectedSize);
});

Then('response has a {int} Http status', function (expectedStatus) {
    assert.strictEqual(this.response.status, expectedStatus);
});


Then('response has an http status that is either {int} or {int}', function (missingStatus, unauthorizedStatus) {
    assert.strictEqual(true, this.response.status == missingStatus || this.response.status == unauthorizedStatus);
});


Then('has error code {string}', function (expectedCode) {
    assert.strictEqual(this.response?.data?.appErrorCode, expectedCode);
});

Then('error message start with {string}', function (expectedStartErrorMessage) {
  console.log(this.response.data);
  const re = new RegExp('^' + expectedStartErrorMessage);
  console.log(re);
  assert.match(this.response?.data?.errorMessage, re);
})
