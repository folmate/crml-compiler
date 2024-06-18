# RDP development environemnt for CRML compiler

## Build
### Windows Command Prompt
```console
docker build -t crml-dev --progress=plain .
```

## Run container
### Windows Command Prompt
Set `CRML_HOME` to the `crml-compiler` directory.

Then start the docker container.
```console
docker run -d --name=crml-dev ^
    --security-opt seccomp=unconfined ^
    -p 4000:3389 ^
    --shm-size="1gb" ^
    -v %CRML_HOME%:/config/crml-compiler ^
    crml-dev 
```

## Git integration
You can set ssh keys for the container by putting them into the .ssh directory before build.

It is also possible to set up *Deploy keys* to allow pushing to a single repository.
