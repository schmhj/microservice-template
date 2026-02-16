FROM eclipse-temurin:25-jdk-jammy

# Build argument for project/username (defaults to "microservice-template")
ARG PROJECT_USER=microservice-template

# Create the user based on PROJECT_USER argument
RUN groupadd --gid 1000 ${PROJECT_USER} && useradd --uid 1000 --gid ${PROJECT_USER} --shell /bin/bash --create-home ${PROJECT_USER}

# Install sudo for project user
RUN apt-get update && apt-get install -y sudo curl wget && rm -rf /var/lib/apt/lists/*

# Allow project user to use sudo without password
RUN usermod -aG sudo ${PROJECT_USER} && echo "${PROJECT_USER} ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/${PROJECT_USER}

# Create gradle user directory with proper permissions
RUN mkdir -p /home/${PROJECT_USER}/.gradle && chown -R ${PROJECT_USER}:${PROJECT_USER} /home/${PROJECT_USER}/.gradle

RUN apt-get update && apt-get install -y git-lfs

# Set default user
USER ${PROJECT_USER}
