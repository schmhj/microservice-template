# Variables
APP_NAME := microservice
VERSION  := 0.0.1-SNAPSHOT
IMAGE_NAME := $(APP_NAME):$(VERSION)

.PHONY: help build-image build-dockerfile clean test run-compose

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

clean: ## Clean the gradle build directory
	./gradlew clean

test: ## Run ArchUnit and Domain tests
	./gradlew test

build-image: ## Build Docker image using Cloud Native Buildpacks (Recommended)
	./gradlew bootBuildImage --imageName=$(IMAGE_NAME)

build-dockerfile: ## Build Docker image using the traditional Dockerfile
	./gradlew bootJar
	docker build -t $(IMAGE_NAME) .

run-compose: ## Build and start the entire stack (App, DB, Kafka)
	docker-compose up --build

stop-compose: ## Stop all services
	docker-compose down