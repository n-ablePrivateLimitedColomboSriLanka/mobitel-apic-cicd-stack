FROM docker:latest as docker-client

FROM jenkins/jenkins:lts-jdk17
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false,-Dpermissive-script-security.enabled=true
ENV CASC_JENKINS_CONFIG /usr/share/jenkins/ref/casc_configs/casc.yaml
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --latest true --plugin-file /usr/share/jenkins/ref/plugins.txt
COPY --from=docker-client /usr/local/bin/docker /usr/local/bin/docker
COPY casc.yaml /usr/share/jenkins/ref/casc_configs/casc.yaml
COPY initial-jobs-apic.groovy /usr/share/jenkins/ref/initial-jobs/initial-jobs-apic.groovy 
