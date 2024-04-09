FROM mcr.microsoft.com/devcontainers/base:bullseye
RUN sudo apt update
RUN sudo apt install libsecret-1-0 -y iputils-ping
COPY jenkins-jack-1.2.1.vsix /
RUN mkdir -p /home/vscode/.ssh && chown -R vscode:vscode /home/vscode/
RUN git config --global credential.helper store