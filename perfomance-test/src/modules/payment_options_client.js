import http from 'k6/http';

const subKey = `${__ENV.OCP_APIM_SUBSCRIPTION_KEY}`;

// TODO
export function fetchPaymentOptions(url, fiscalCode) {

  return http.get(url, { headers, responseType: "text"});
}