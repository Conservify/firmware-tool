DOCKER_IMAGE_VERSION=latest
DOCKER_IMAGE_NAME=conservify/firmwaretool
DOCKER_IMAGE_TAGNAME=$(DOCKER_IMAGE_NAME):$(DOCKER_IMAGE_VERSION)

default: build

build:
	(cd .. && gradle build)

	cp ../firmware-tool-cli/build/distributions/*.tar .

	docker build -t $(DOCKER_IMAGE_TAGNAME) .

push:
	docker push $(DOCKER_IMAGE_NAME)

test:
	docker run --rm $(DOCKER_IMAGE_TAGNAME) /bin/echo "Success."

run:
	docker run --rm $(DOCKER_IMAGE_TAGNAME) 
