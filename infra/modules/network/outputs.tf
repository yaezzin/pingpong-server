output "vpc_id" {
  value = ncloud_vpc.vpc.id
}

output "subnet_id" {
  value = ncloud_subnet.subnet.id
}

output "subnet_lb_id" {
  value = ncloud_subnet.subnet_lb.id
}