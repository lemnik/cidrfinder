# Subnet CIDR Finder
A simple tool to help find available subnet / CIDR spaces in existing networks. This
web application allows you to enter your existing network constraints, and the size of the
subnets you need to allocate. It will then figure out suitable subnet CIDRs for each of
your planned subets.

This is especially useful if you have cloud hosting (AWS, GCloud, etc.) and your VPC
already has several (possibly badly planned) subnets allocated.

You just need to enter your available network space, any eixsting subnets, and how much
address space you need for new subnets... the app finds the available space (if there is any)
and gives you the CIDR blocks for your new subnets.

# Tech Stack

This application is built with Kotlin and React both of which I really like, but this
was the first time I've used both together. The application includes a self-contained
implementation of the Materal Design components I needed, sitting on top of the
[Material Design Lite](https://getmdl.io) CSS and JavaScript (delivered over CDN).

## Warning

I was experimenting with this codebase, so it might have some strange things that should
not be repeated. It was an experiment to see where Kotlin might bring something extra
to the ReactJS ecosystem (besides it's language syntax and core API).

Strange things you'll find in this codebase include

 - Extension functions of props and state
 - Imperative statements like `if` and `forEach` in component rendering
 - The `noinline` keyword on various lambdas

# Sources

Some bits of code are copied directly from projects elsewhere, some of these have been
modified to suit my needs here.

 - The `MDLComponent` class is inspired by ReactKT: https://github.com/ylemoigne/ReactKT
 - The `Netmask` class was mostly ported from the Node-Netmask project: https://github.com/rs/node-netmask/