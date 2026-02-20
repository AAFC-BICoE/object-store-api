FROM aafcbicoe/object-store-api:1.31

USER root
RUN apt-get update && apt-get install -y --no-install-recommends git ca-certificates build-essential curl
RUN git clone --depth=1 https://github.com/SoftCreatR/imei.git /tmp/imei

RUN bash /tmp/imei/imei.sh -i -y

RUN /usr/local/bin/magick -version

RUN ldconfig
