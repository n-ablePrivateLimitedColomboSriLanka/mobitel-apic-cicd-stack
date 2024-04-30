FROM mcr.microsoft.com/devcontainers/base:bullseye
RUN sudo apt update
RUN sudo apt install libsecret-1-0 -y iputils-ping vim
COPY jenkins-jack-1.2.1.vsix /
COPY apic /usr/local/bin/apic
RUN mkdir -p /home/vscode/.ssh && chown -R vscode:vscode /home/vscode/
RUN git config --global credential.helper store
RUN git clone --depth 1 https://github.com/git/git.git /tmp/git
RUN cd /tmp/git/contrib/subtree && sudo make install
RUN wget https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/bin/yq && \
    chmod +x /usr/bin/yq