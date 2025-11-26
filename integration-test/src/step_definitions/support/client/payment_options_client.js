const {get, post} = require("../utility/axios_common");
const fs = require('fs/promises');
const path = require('path');

const PAY_OPT_HOST = process.env.PAY_OPT_HOST;
const GPD_V3_HOST = process.env.GPD_V3_HOST;
const API_TIMEOUT = process.env.API_TIMEOUT;
const GPD_SUBKEY = process.env.GPD_SUBKEY;

async function loadDebtPosition(filename, idOrg) {
    const basePath = path.join(process.cwd(), 'config', 'gpd-data');
    const fullPath = path.join(basePath, filename + '.json');
    fileContent = await fs.readFile(fullPath, 'utf8');
    fileContent = fileContent.replaceAll("XXXXXXXXXXX", idOrg)
    resp = await createDebtPosition(idOrg, JSON.parse(fileContent));
    return (resp.status === 201 || resp.status === 409)
}

async function createDebtPosition(org, jsonBody) {
    const data = await post(GPD_V3_HOST +
        `/organizations/` + org + `/debtpositions?toPublish=true`,
        jsonBody,
        {
            timeout: API_TIMEOUT,
            headers: {
                "Ocp-Apim-Subscription-Key": GPD_SUBKEY,
                "Content-Type": "application/json"
            }
    });

    return data;
}



async function getPaymentOptions(taxCode, noticeNumber, idPsp) {

    let params = {};

    if (idPsp) {
        params.idPsp = idPsp;
        params.idBrokerPsp = idPsp;
    }

    const data = await get(PAY_OPT_HOST +
     `/payment-options/organizations/`+taxCode+`/notices/`+noticeNumber, {
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
    loadDebtPosition,
    createDebtPosition,
    getPaymentOptions
}
