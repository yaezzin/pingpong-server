terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

provider "ncloud" {
  access_key  = var.NCP_ACCESS_KEY
  secret_key  = var.NCP_SECRET_KEY
  region      = var.region
  site        = var.site
  support_vpc = var.support_vpc
}

locals {
  name = "k8s-pingpong"
  env  = "local"
}

resource "ncloud_login_key" "loginkey" {
  key_name = "login-key-${local.name}"
}

module "network" {
  source = "../modules/network"

  local_name     = local.name
  region         = var.region
  site           = var.site
  support_vpc    = var.support_vpc
  NCP_ACCESS_KEY = var.NCP_ACCESS_KEY
  NCP_SECRET_KEY = var.NCP_SECRET_KEY
  env            = local.env
}

resource "ncloud_nks_cluster" "cluster" {
  cluster_type         = "SVR.VNKS.STAND.C002.M008.NET.SSD.B050.G002"
  k8s_version          = data.ncloud_nks_versions.version.versions.0.value
  login_key_name       = ncloud_login_key.loginkey.key_name
  name                 = "cluster-${local.name}"
  lb_private_subnet_no = module.network.subnet_lb_id
  kube_network_plugin  = "cilium"
  subnet_no_list       = [module.network.subnet_id]
  public_network       = true
  vpc_no               = module.network.vpc_id
  zone                 = "KR-1"

  log {
    audit = true
  }
}


resource "ncloud_nks_node_pool" "node_pool" {
  cluster_uuid   = ncloud_nks_cluster.cluster.uuid
  node_pool_name = "node-pool-${local.name}"
  node_count     = 1
  product_code   = data.ncloud_server_product.product.product_code

  autoscale {
    enabled = true
    min     = 1
    max     = 2
  }

  lifecycle {
    ignore_changes = [node_count, subnet_no_list]
  }
}