resource "bitbucketserver_project" "apis" {
  key         = "API"
  name        = "APIs"
  description = "The project containing the API repositories"
}

resource "bitbucketserver_project" "products" {
  key         = "PRDCT"
  name        = "Products"
  description = "The project containing the product repositories"
}

locals {
  api_repos = {
    "api-index" = {
      name        = "api_index",
      description = "The API index"
    }
    "stock-api" = {
      name        = "stock-api",
      description = "The stock API"
    },
    "order-api" = {
      name        = "order-api",
      description = "The order API"
    }
  }
  product_repos = {
    "product-index" = {
      name        = "product_index",
      description = "The product index"
    },
    "ordering-and-fulfillment-product" = {
      name        = "ordering-and-fulfillment-product",
      description = "The ordering and fulfillment product"
    },
  }
}

resource "bitbucketserver_repository" "api_repsitories" {
  for_each = local.api_repos
  project  = bitbucketserver_project.apis.id
  name     = each.value.name
}

resource "bitbucketserver_repository_webhook" "api_repositories_webhooks" {
  for_each    = local.api_repos
  project     = bitbucketserver_project.apis.key
  repository  = bitbucketserver_repository.api_repsitories[each.key].slug
  name        = "push-notification"
  webhook_url = "https://www.google.com/"
  secret      = "abc"
  events      = ["repo:refs_changed"]
  active      = true
}

resource "bitbucketserver_repository" "product_repositories" {
  for_each = local.product_repos
  project = bitbucketserver_project.products.id
  name    = each.value.name
}

resource "bitbucketserver_repository_webhook" "product_repositories_webhooks" {
  for_each    = local.product_repos
  project     = bitbucketserver_project.products.key
  repository  = bitbucketserver_repository.product_repositories[each.key].slug
  name        = "push-notification"
  webhook_url = "https://www.google.com/"
  secret      = "abc"
  events      = ["repo:refs_changed"]
  active      = true
}

output "api_repository_urls" {
  value = {
    for repo in bitbucketserver_repository.api_repsitories : repo.name => repo.clone_https
  }
}

output "product_repository_urls" {
  value = {
    for repo in bitbucketserver_repository.product_repositories : repo.name => repo.clone_https
  }
}
