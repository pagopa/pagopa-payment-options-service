locals {
  product = "${var.prefix}-${var.env_short}"

  apim = {
    name                   = "${local.product}-apim"
    rg                     = "${local.product}-api-rg"
    payments_options_product_id = "pagopa_payment_options"
    nodo_auth_product_id        = "nodo-auth"
  }
}

