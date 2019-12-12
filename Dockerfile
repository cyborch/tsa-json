FROM openjdk:13-alpine as develop

RUN apk update

WORKDIR /opt/tsa
COPY ./ ./
RUN ./gradlew --no-daemon build

# Install OpenSSL
RUN apk add openssl

WORKDIR /opt/tsa
COPY ./ ./

# Create keys
RUN ./bin/create_tsa_certs

# Build project
RUN apk add zip
RUN ./gradlew --no-daemon jar
WORKDIR /opt/tsa/build/distributions
RUN unzip tsa-json-1.0-SNAPSHOT

WORKDIR /opt/tsa
CMD ["/opt/tsa/gradlew", "run"]

FROM openjdk:13-alpine as prod

WORKDIR /opt/tsa
COPY --from=develop /var/lib/tsa /var/lib/tsa
COPY --from=develop /opt/tsa/build/distributions/tsa-json-1.0-SNAPSHOT/* /opt/tsa/
RUN mkdir -p lib
RUN mv *.jar lib
RUN mkdir -p bin
RUN mv tsa-json* bin

RUN apk update

# Install ntp and configure for North America
RUN apk add chrony
RUN echo "server 0.fedora.pool.ntp.org iburst" > /etc/chrony.conf
RUN echo "server 1.fedora.pool.ntp.org iburst" >> /etc/chrony.conf
RUN echo "server 2.fedora.pool.ntp.org iburst" >> /etc/chrony.conf
RUN echo "server 3.fedora.pool.ntp.org iburst" >> /etc/chrony.conf

# Add entrypoint
WORKDIR /opt/tsa
COPY ./bin/entrypoint.sh ./bin/entrypoint.sh

EXPOSE 7000

ENTRYPOINT "/opt/tsa/bin/entrypoint.sh"
