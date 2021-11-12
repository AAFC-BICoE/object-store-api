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

#RUN apk add --no-cache imagemagick bash pngcrush optipng=0.7.7-r0
#CMD ["/bin/bash"]
ENTRYPOINT ["tail", "-f", "/dev/null"]
