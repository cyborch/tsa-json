FROM ubuntu:bionic

RUN apt update

RUN apt install -y git curl gcc make openssl openjdk-11-jdk zip

# Install Rust
WORKDIR /opt/rust
RUN curl -f -L https://static.rust-lang.org/rustup.sh -O
RUN chmod +x rustup.sh
RUN ./rustup.sh -y
ENV PATH="/root/.cargo/bin:${PATH}"

# Install RoughEnough
WORKDIR /opt
RUN git clone -b 1.1.7 https://github.com/int08h/roughenough.git
WORKDIR /opt/roughenough
RUN cargo build --release --features default

WORKDIR /opt/tsa
COPY ./ ./

# Create keys
RUN ./bin/create_tsa_certs

# Install TSA
RUN ./gradlew --no-daemon build jar
RUN find /opt/tsa
RUN unzip /opt/tsa/build/distributions/tsa-json-1.0-SNAPSHOT
RUN mkdir lib && mv tsa-json-1.0-SNAPSHOT/lib/* ./lib/ && mv tsa-json-1.0-SNAPSHOT/bin/* ./bin/

EXPOSE 7000

ENTRYPOINT "/opt/tsa/bin/entrypoint.sh"

