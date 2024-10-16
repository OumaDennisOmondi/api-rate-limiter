resource "digitalocean_droplet" "irembo-dp" {
    image = "ubuntu-20-04-x64"
    name = "irembo-dp"
    region = "nyc3"
    size = "s-1vcpu-1gb"
    ssh_keys = [
      data.digitalocean_ssh_key.digitalocean.id
    ]

      
  connection {
    host = self.ipv4_address
    user = "root"
    type = "ssh"
    private_key = file(var.pvt_key)
    timeout = "2m"
  }
  
  provisioner "remote-exec" {
    inline = [
      # update
      "sudo apt update"
    ]
  }

}