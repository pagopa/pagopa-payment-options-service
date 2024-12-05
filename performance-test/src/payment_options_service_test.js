import { getToService } from "./modules/payment_options_client.js";
import { SharedArray } from 'k6/data';
import { check } from 'k6';

const varsArray = new SharedArray('vars', function () {
    return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
export const ENV_VARS = varsArray[0];

export const options = JSON.parse(open(__ENV.TEST_TYPE)); // Needed to enable the test-types

const paymentOptionsServiceURIBasePath = `${ENV_VARS.paymentOptionsServiceURIBasePath}`;
const noticeType = `${__ENV.NOTICE_TYPE}`;

const ORGANIZATIONAL_FISCAL_CODE = "77777777777";

const SINGLE_OPT_NOTICE_NUMBER = "311111111111111111";
const SINGLE_AND_MANY_OPT_NOTICE_NUMBER = "311111111111111112";
const SINGLE_AND_MULTI_OPT_NOTICE_NUMBER = "311111111111111116";
const SINGLE_AND_CO_OPT_NOTICE_NUMBER = "311111111112222225";

const VALID_PSP = "99999000001";

const getSelectedNoticeNumbers = () => {
    const noticeTypeAll = noticeType === "undefined" || noticeType === "all";

    const selectedNotices = [];
    if (noticeType === "single_opt" || noticeTypeAll) {
        selectedNotices.push(SINGLE_OPT_NOTICE_NUMBER);
    }
    if (noticeType === "single_and_many_opt" || noticeTypeAll) {
        selectedNotices.push(SINGLE_AND_MANY_OPT_NOTICE_NUMBER);
    }
    if (noticeType === "single_and_multy_opt" || noticeTypeAll) {
        selectedNotices.push(SINGLE_AND_MULTI_OPT_NOTICE_NUMBER);
    }
    if (noticeType === "single_and_co_opt" || noticeTypeAll) {
        selectedNotices.push(SINGLE_AND_CO_OPT_NOTICE_NUMBER);
    }
    console.log(`Selected the following notices: ${JSON.stringify(selectedNotices)}`);
    return selectedNotices;
}

export default function () {
    const selectedNotices = getSelectedNoticeNumbers();
    for (let i = 0; i < selectedNotices.length; i++) {
        const el = selectedNotices[i];
        let response = getToService(`${paymentOptionsServiceURIBasePath}/payment-options/organizations/${ORGANIZATIONAL_FISCAL_CODE}/notices/${el}`, { idPsp: VALID_PSP });
        console.info(`Payment Options Service getPaymentOptions with notice number ${el} call, Status ${response.status}`);

        let responseBody = JSON.parse(response.body);

        if(response.status !== 200){
            console.info(`Payment Options Service getPaymentOptions responded with error: ${responseBody.message}`);
        }

        check(response, {
            'Payment Options Service getPaymentOptions status is 200': () => response.status === 200,
            'Payment Options Service getPaymentOptions body has list of payment options': () =>
                Boolean(responseBody && responseBody.paymentOptions && responseBody.paymentOptions.length)
        });
    }
}