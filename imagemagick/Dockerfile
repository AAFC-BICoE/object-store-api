FROM aafcbicoe/object-store-api:1.36

USER root
RUN apt-get update && apt-get install -y --no-install-recommends git ca-certificates build-essential curl
RUN git clone --depth=1 https://github.com/SoftCreatR/imei.git /tmp/imei

RUN bash /tmp/imei/imei.sh

RUN magick -version

RUN ldconfig
