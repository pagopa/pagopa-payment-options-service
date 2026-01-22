const { get, post } = require("../utility/axios_common");
const fs = require('fs/promises');
const path = require('path');

const PAY_OPT_HOST = process.env.PAY_OPT_HOST;
const GPD_V3_HOST = process.env.GPD_V3_HOST;
const API_TIMEOUT = process.env.API_TIMEOUT;
const GPD_SUBKEY = process.env.GPD_SUBKEY;

async function loadDebtPosition(filename, idOrg) {
    const basePath = path.join(process.cwd(), 'config', 'gpd-data');
    const fullPath = path.join(basePath, filename + '.json');
    let fileContent = await fs.readFile(fullPath, 'utf8');
    fileContent = fileContent.replaceAll("XXXXXXXXXXX", idOrg)
    const resp = await createDebtPosition(idOrg, JSON.parse(fileContent));
    if (resp.status !== 201 && resp.status !== 409) {
        console.error(`Failed to load debt position ${filename}. Status: ${resp.status}, Data: ${JSON.stringify(resp.data)}`);
    }
    return (resp.status === 201 || resp.status === 409)
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
            "Ocp-Apim-Subscription-Key": process.env.SUBKEY,
            "Content-Type": "application/json"
        }
    });
}

module.exports = {
    loadDebtPosition,
    createDebtPosition,
    getPaymentOptions
}
