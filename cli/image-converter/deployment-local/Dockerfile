# - - - BUILD - - - #
FROM golang:alpine as builder
# Install alpine dependencies
RUN apk add --update --no-cache git build-base
# make sure we are up to date
RUN apk update && apk upgrade
# Preps
# making source folder for a build
RUN mkdir -p /go/src/converter
# copy assets
COPY converter /go/src/converter
# define working directory
WORKDIR /go/src/converter
# go vendors mode
RUN go mod vendor
# pulling all vendors/dependencies
RUN go get -v -d ./...

# - - - TESTS - - - #
# Running tests
RUN go test ./... -v -coverprofile cover.out
# Output html coverage report to /tmp/cover.html
RUN go tool cover -html=cover.out -o /tmp/cover.html
# When close to prod
# RUN go tool cover -func cover.out | grep total | awk '{print substr($3, 1, length($3)-1)}'
# And we need to post using curl to one of the services
# Building Artifact
ENV GOOS=linux
ENV GOARCH=amd64
RUN go build -tags netgo -ldflags '-extldflags "-static"' -o main .

# - - - LOCAL - - - #
# Pushing Artifact to local
FROM alpine:3.10
RUN apk update

ADD ./deployment-local/dcraw.c /opt/dcraw/src/

RUN apk upgrade && \
    apk update && \
    apk add --no-cache musl-dev \
            gcc \
            libjpeg-turbo-dev \
            lcms2-dev \
            jasper-dev; \
    mkdir -p /opt/dcraw/bin; \
    gcc -o /opt/dcraw/bin/dcraw -O4 /opt/dcraw/src/dcraw.c -lm -ljasper -ljpeg -llcms2

ENV PATH="/opt/dcraw/bin:${PATH}"
RUN apk --update add imagemagick
RUN apk update
RUN apk upgrade
RUN apk add bash
# make sure we are up to date
RUN apk update && apk upgrade
RUN mkdir /app
COPY --from=builder /go/src/converter/main /app/
COPY --from=builder /go/src/converter/converter_config.yml /app/
# For now copy the cover report to release container
COPY --from=builder /tmp/cover.html /tmp/

ADD ./deployment-local/magic.sh /app/
# make it executable
RUN chmod 755 /app/magic.sh

WORKDIR /app
# folder for test logs
RUN mkdir /tmp/log-test

CMD ["./main", "/app/converter_config.yml"]
