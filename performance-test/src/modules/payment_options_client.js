import http from 'k6/http';

const subKey = `${__ENV.OCP_APIM_SUBSCRIPTION_KEY}`;

export function getToService(url, params) {

  let headers = {
    'Ocp-Apim-Subscription-Key': subKey,
    "Content-Type": "application/json"
  };

  return http.get(url, { headers, responseType: "text", params });
}