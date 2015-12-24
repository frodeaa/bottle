FROM frolvlad/alpine-oraclejdk8

ADD build/install .

WORKDIR bottle

EXPOSE 9000
ENTRYPOINT ["java"]
CMD ["-cp", "lib/*", "github.frodeaa.bottle.App"]