FROM ubuntu:latest
LABEL authors="tanaka"

ENTRYPOINT ["top", "-b"]