import http from 'k6/http';
const subKey = `${__ENV.OCP_APIM_SUBSCRIPTION_KEY}`;

export function getToService(endpoint, params) {
  let url = endpoint;
  let headers = {
    'Ocp-Apim-Subscription-Key': subKey,
    "Content-Type": "application/json"
  };

  const queryParams = params ? Object.entries(params) : [];
  if (queryParams && queryParams.length) {
    queryParams.forEach((el, index) => {
      url = url.concat(index === 0 ? "?" : "&", el[0],  "=", el[1]);
    });
  }

  console.info(`Calling url ${url}`);
  return http.get(url, { headers, responseType: "text" });
}