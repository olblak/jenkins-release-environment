
JENKINS_VERSION=$(shell ./utils/getLatestStableVersion.py)
IMAGE="olblak/jenkins"
TAG="$(JENKINS_VERSION)-release"

show: 
	echo $(JENKINS_VERSION)

build: 
	docker build --no-cache -t $(IMAGE):$(TAG) --build-arg VERSION=$(JENKINS_VERSION) .

run: 
	docker run -i -t $(IMAGE):$(TAG)

shell: 
	docker run --entrypoint /bin/bash -i -t $(IMAGE):$(TAG)

publish: 
	docker push $(IMAGE):$(TAG)
