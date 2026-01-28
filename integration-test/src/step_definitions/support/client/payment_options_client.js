const { get, post, del } = require("../utility/axios_common");
const fs = require('fs/promises');
const path = require('path');

const PAY_OPT_HOST = process.env.PAY_OPT_HOST;
const GPD_V3_HOST = process.env.GPD_V3_HOST;
const API_TIMEOUT = process.env.API_TIMEOUT;
const GPD_SUBKEY = process.env.GPD_SUBKEY;
const SUBKEY = process.env.SUBKEY;

async function loadDebtPosition(filename, idOrg) {
    const basePath = path.join(process.cwd(), 'config', 'gpd-data');
    const fullPath = path.join(basePath, filename + '.json');
    let fileContent = await fs.readFile(fullPath, 'utf8');
    fileContent = fileContent.replaceAll("XXXXXXXXXXX", idOrg);
    let payload = JSON.parse(fileContent);

    let iupdToDelete = payload.iupd;
    if (iupdToDelete) {
        await deleteDebtPosition(idOrg, iupdToDelete);
    }

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowISO = tomorrow.toISOString();

    if (payload.paymentOption) {
        payload.paymentOption.forEach(option => {
            if (option.installments) {
                option.installments.forEach(installment => {
                    installment.dueDate = tomorrowISO;
                });
            }
            if (option.retentionDate) {
                const futureRetention = new Date();
                futureRetention.setDate(futureRetention.getDate() + 2);
                option.retentionDate = futureRetention.toISOString();
            }
        });
    }
    const resp = await createDebtPosition(idOrg, payload);
    return (resp.status === 201 || resp.status === 409);
}

async function deleteDebtPosition(org, iupd) {
    const url = GPD_V3_HOST + `/organizations/${org}/debtpositions/${iupd}`;

    return await del(url, {
            timeout: API_TIMEOUT,
            headers: {
                "Ocp-Apim-Subscription-Key": GPD_SUBKEY,
                "Content-Type": "application/json"
            }
        });
}

async function createDebtPosition(org, jsonBody) {
    return await post(GPD_V3_HOST +
        `/organizations/` + org + `/debtpositions?toPublish=true`,
        jsonBody,
        {
            timeout: API_TIMEOUT,
            headers: {
                "Ocp-Apim-Subscription-Key": GPD_SUBKEY,
                "Content-Type": "application/json"
            }
        });
}

async function getPaymentOptions(taxCode, noticeNumber, idPsp) {

    let params = {};

    if (idPsp) {
        params.idPsp = idPsp;
        params.idBrokerPsp = idPsp;
    }

    return await get(PAY_OPT_HOST +
        `/payment-options/organizations/` + taxCode + `/notices/` + noticeNumber, {
        timeout: API_TIMEOUT,
        params,
        headers: {
            "Ocp-Apim-Subscription-Key": SUBKEY,
            "Content-Type": "application/json"
        }
    });
}

module.exports = {
    loadDebtPosition,
    createDebtPosition,
    getPaymentOptions
}