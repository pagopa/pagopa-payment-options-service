const {get} = require("../utility/axios_common");

const PAY_OPT_HOST = process.env.PAY_OPT_HOST;
const API_TIMEOUT = process.env.API_TIMEOUT;

async function getPaymentOptions(taxCode, noticeNumber, idPsp) {

    if (dueDate) {
        params.idPsp = idPsp;
    }

    const data = await get(PAY_OPT_HOST +
     `/payment-options/organizations/{taxCode}/notices/{noticeNumber}`, {
        timeout: API_TIMEOUT,
        params,
        headers: {
            "Ocp-Apim-Subscription-Key": process.env.SUBKEY,
            "Content-Type": "application/json"
        }
    });
    return data;
}

module.exports = {
    getPaymentOptions
}