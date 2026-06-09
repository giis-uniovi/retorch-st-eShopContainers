[![Build Status](https://github.com/giis-uniovi/retorch-st-eShopContainers/actions/workflows/build.yml/badge.svg)](https://github.com/giis-uniovi/retorch-st-eShopContainers/actions)

# RETORCH eShopContainers End-to-End Test Suite

This repository contains a detached fork of [eShopContainers](https://github.com/erjain/eShopOnContainers)  and an
End-to-End Test suite that are used as demonstrator of the [RETORCH Framework](https://github.com/giis-uniovi/retorch).
eShopContainers is a sample reference application, powered by Microsoft, based into a simplified microsservice
architecture
using Docker containers. See more details in the [forked version](https://github.com/erjain/eShopOnContainers)
or [official repository](https://github.com/dotnet-architecture/eShopOnContainers)

## SUT Deployment

### Local deployment

The SUT provided in the `📁 /sut` directory is prepared to be deployed using Docker (`>~v26.0.0`) and docker-compose(
`v2.26.1>~`). To deploy the SUT we dispose of two scripts in the root of the repository: `📜 redeploy-local.ps1` for
Windows users and `📜 redeploy-local.sh` for Linux users.

These scripts made the necessary configurations and calls the docker compose command with the environment files and
variables to succeed in the deployment. The script can be configured to rebuild (or not) the Docker images, in order to 
do that specify the argument `-NoBuild`:

```bash
.\redeploy-local.ps1 -NoBuild
```

To tear-down the SUT, the following command is required (and also prompt in the terminal):

```bash
docker compose -f sut\src\docker-compose.yml `
    -f sut\src\docker-compose.local-override.yml `
    --env-file .retorch\envfiles\local.env `
    -p local down --volumes
```

### Jenkins CI deployment

The repository contains a `🚀Jenkinsfile` and the necessary scripts in the  `📁 /.retorch` folder to execute the test suite in
CI. In addition, to the requirements of the [RETORCH tool](https://github.com/giis-uniovi/retorch) for this concrete demonstrator the user can get in trouble
due to the number of instances required.
To avoid this problem,  Linux users are highly  encouraged to increase the number of users and instances in their system,
e.g. modifying the `/etc/sysctl.conf` including:

```bash
fs.inotify.max_user_instances=2048
fs.inotify.max_user_watches=1048576
```