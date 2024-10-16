terraform {
  required_providers {
    digitalocean = {
      source = "digitalocean/digitalocean"
      version = "~> 2.0"
    }
  }
}

variable "do_token" {
    type = string
}
variable "pvt_key" {
    type = string

}

provider "digitalocean" {
  token = var.do_token
}

data "digitalocean_ssh_key" "digitalocean" {
  name = "digitalocean"
}