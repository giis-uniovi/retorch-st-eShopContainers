[![Build Status](https://github.com/giis-uniovi/retorch-st-eShopContainers/actions/workflows/build.yml/badge.svg)](https://github.com/giis-uniovi/retorch-st-eShopContainers/actions)

# RETORCH eShopContainers End-to-End Test Suite

This repository contains a detached fork of [eShopContainers](https://github.com/erjain/eShopOnContainers)  and an
End-to-End Test suite that are used as demonstrator of the [RETORCH Framework](https://github.com/giis-uniovi/retorch).
eShopContainers is a sample reference application, powered by Microsoft, based into a simplified microsservice
architecture
using Docker containers. See more details in the [forked version](https://github.com/erjain/eShopOnContainers)
or [official repository](https://github.com/dotnet-architecture/eShopOnContainers)

## Deployment instructions

The net core containers would get in trouble due to the number of threads and inotify instances of most of the common
Linux SO.
To avoid this problem, it's highly recommended to increase the number of users and instances, e.g. modifying the
`/etc/sysctl.conf` including:

```bash
fs.inotify.max_user_instances=2048
fs.inotify.max_user_watches=1048576
```