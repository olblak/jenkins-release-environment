CHART_REPO := http://jenkins-x-chartmuseum:8080
DIR := "env"
NAMESPACE := "jx"
OS := $(shell uname)

build: clean
	rm -rf requirements.lock
	helm version
	helm init
	helm repo add releases ${CHART_REPO}
	helm repo add jenkins-x http://chartmuseum.build.cd.jenkins-x.io
	helm dependency build ${DIR}
	helm lint ${DIR}

dependency:
	helm dependency update env

install: dependency
	helm secrets upgrade ${NAMESPACE} ${DIR} -f ${DIR}/secrets.yaml --install --namespace ${NAMESPACE} --debug

delete:
	helm delete --purge ${NAMESPACE}  --namespace ${NAMESPACE}

clean:

init:
	jx install --verbose=true --provider=aks --no-default-environments=true --tls-acme=true --http=false

import:
	jx import -j Jenkinsfile.release --no-draft=true --url https://github.com/olblak/jenkins.git --verbose=true

helm_plugins:
	helm plugin install https://github.com/futuresimple/helm-secrets

generate_myvalues.yaml:
	sops -d myvalues.release.yaml > myvalues.yaml

edit_secrets:
	sops myvalues.yaml.enc
