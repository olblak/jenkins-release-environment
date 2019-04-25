# Dockerfile for adding plugins to Jenkins X 
FROM jenkinsxio/jenkinsx:latest

ARG VERSION

LABEL \
  jenkins_version=${VERSION}\
  maintainer="olblak<me@olblak.com>"

USER root

ENV JENKINS_VERSION=${VERSION:-latest}

RUN \
  env && \
  echo $VERSION > /usr/share/jenkins/VERSION.txt && \ 
  cat  /usr/share/jenkins/VERSION.txt && \
  rm /usr/share/jenkins/jenkins.war && \
  curl -SL http://mirrors.jenkins.io/war-stable/$VERSION/jenkins.war > /usr/share/jenkins/jenkins.war
USER jenkins

# Install Jenkins Plugin
COPY plugins.txt /usr/share/jenkins/ref/release-plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/release-plugins.txt

# Disable for testing purpose
# Groovy Init script
#COPY lockbox.groovy  /usr/share/jenkins/ref/init.groovy.d


USER jenkins
