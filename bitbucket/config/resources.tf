resource "bitbucketserver_project" "apis" {
    key         = "API"
    name        = "APIs"
    description = "The project containing the API repositories"
}

resource "bitbucketserver_project" "products" {
    key = "PRDCT"
    name = "Products"
    description = "The project containing the product repositories"
}

resource "bitbucketserver_repository" "api_repsitories" {
    for_each = {
        "stock-api" = {
            name = "stock-api",
            description = "The stock API"
        },
        "order-api" = {
            name = "order-api",
            description = "The order API"
        }
    }
    project = bitbucketserver_project.apis.id
    name = each.key
}

resource "bitbucketserver_repository" "product_repositories" {
    for_each = {
        "ordering-and-fulfillment-product" = {
            name = "ordering-and-fulfillment-product",
            description = "The ordering and fulfillment product"
        },
    }
    project = bitbucketserver_project.products.id
    name = each.key
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