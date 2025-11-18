FROM openjdk:17-jdk-slim

# Instalar herramientas necesarias incluyendo nmap
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        git \
        maven \
        nmap \
        curl \
        wget \
        net-tools \
        iputils-ping \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Crear el usuario vscode
RUN useradd -ms /bin/bash vscode

USER vscode
