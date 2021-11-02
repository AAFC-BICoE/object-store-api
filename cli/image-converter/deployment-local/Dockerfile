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
FROM alpine
# make sure we are up to date
RUN apk update && apk upgrade
RUN adduser -S -D -H -h /app appuser
USER appuser
COPY --from=builder /go/src/converter/main /app/
COPY --from=builder /go/src/converter/converter_config.yml /app/
# For now copy the cover report to release container
COPY --from=builder /tmp/cover.html /tmp/
WORKDIR /app
# folder for media files
RUN mkdir /tmp/data-test
# folder for test SQLite DB
RUN mkdir /tmp/db-test
# folder for test logs
RUN mkdir /tmp/log-test
# timzones
ENV ZONEINFO /tmp/zoneinfo.zip

CMD ["./main", "/app/converter_config.yml"]
