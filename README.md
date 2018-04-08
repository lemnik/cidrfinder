# CIDR Finder
A simple tool to help find available subnet / CIDR spaces in existing networks. This
web application allows you to enter your existing network constraints, and the size of the
subnets you need to allocate. It will then figure out suitable subnet CIDRs for each of
your planned subets.

This is especially useful if you have cloud hosting (AWS, GCloud, etc.) and your VPC
already has several (possibly baddly planned) subnets allocated.

# Tech Stack

This application is build with Kotlin and React both of which I really like, but this
was the first time I've used both together.
