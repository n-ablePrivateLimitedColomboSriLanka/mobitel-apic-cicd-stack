terraform {
  required_version = ">= 1.7.1"

  required_providers {
    bitbucketserver = {
      source  = "gavinbunney/bitbucketserver"
    }
  }
}