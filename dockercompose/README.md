# Docker compose Alien4Cloud

## Configure ssl/https

In case of existing X.509 certificate for the service:

* Certificate file plus any intermediate CA chain: <a4c_host_chain>.crt
* Key file: <a4c_host>.key

The `docker-compose.yml` use the `.env` file containing the necessary variables to instantiate the container a4c.
Ensure to set the variable
`A4C_ADMIN_PASSWORD` in the `.env`, you can add more envirnment variables according to the documentation.

The following variables should also be set, they are taken from the registration of the alien4cloud
client in the IAM OpenIDConnect management service:

* A4C_SPRING_OIDC_CLIENT_ID
* A4C_SPRING_OIDC_CLIENT_SECRET

## Instantiate the container

After cloning this repo, change to the directory with the docker-compose.yml:

```
cd alien4cloud-deep/dockercompose/
```

Modify the file `.env`, and run:

```
docker-compose up -d
```

Check the status with

```
docker-compose ps
```
