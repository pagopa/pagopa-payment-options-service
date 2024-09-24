locals {
  apim_payment_options = {
    // GPD Payments Pull
    display_name          = "Payments Options"
    description           = "API for Payments Options"
    path                  = "/payment-options/service"
    subscription_required = true
    service_url           = null
  }
  host     = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname = var.hostname
}

##############
## Api Vers ##
##############

resource "azurerm_api_management_api_version_set" "api_payment_options_service" {

  name                = format("%s-gpd-payment-options-service-api", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_payment_options.display_name
  versioning_scheme   = "Segment"
}

##############
## OpenApi  ##
##############

module "apim_payment_options_api_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v6.4.1"

  name                  = format("%s-payment-options-service-api", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.gpd_payments_pull_product_id]
  subscription_required = local.api_payment_options_service.subscription_required
  version_set_id        = azurerm_api_management_api_version_set.api_payment_options_service.id
  api_version           = "v1"

  description  = local.apim_payment_options.description
  display_name = local.apim_payment_options.display_name
  path         = local.apim_payment_options.path
  protocols    = ["https"]
  service_url  = local.apim_payment_options.service_url

  content_format = "openapi"
  content_value  = file("../openapi/openapi.json")

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = local.hostname
  })

}
