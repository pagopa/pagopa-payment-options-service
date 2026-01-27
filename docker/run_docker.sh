#!/bin/bash

if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE_CMD="docker compose"
else
    DOCKER_COMPOSE_CMD="docker-compose"
fi

ENV=$1

if [ -z "$ENV" ]
then
  ENV="local"
  echo "No environment specified: local is used."
fi

if [ "$ENV" = "local" ]; then
  image="service-local:latest"
  ENV="dev"
else
  if [ ! -f "../helm/values-$ENV.yaml" ]; then
    echo "Error: File ../helm/values-$ENV.yaml not found."
    exit 1
  fi
  repository=$(yq -o=json -r '."microservice-chart".image.repository' ../helm/values-$ENV.yaml)
  image="${repository}:latest"
fi
export image=${image}

rm -f .env
touch .env

echo "Generating .env file from values-$ENV.yaml..."

yq -o=json -r '."microservice-chart".envConfig' ../helm/values-$ENV.yaml | \
jq -r 'to_entries[] | "\(.key)=\(.value)"' >> .env

keyvault=$(yq -o=json -r '."microservice-chart".keyvault.name' ../helm/values-$ENV.yaml)

yq -o=json -r '."microservice-chart".envSecret' ../helm/values-$ENV.yaml | \
jq -r 'to_entries[] | "\(.key)=\(.value)"' | \
while IFS='=' read -r env_key secret_name; do

  secret_name=$(echo "$secret_name" | tr -d '\r')

  if [ -n "$secret_name" ]; then
      response=$(az keyvault secret show --vault-name "$keyvault" --name "$secret_name" 2>/dev/null)

      if [ -n "$response" ]; then
          value=$(echo "$response" | jq -r '.value')
          echo "$env_key=$value" >> .env
      else
          echo "WARNING: Secret '$secret_name' not found in KeyVault '$keyvault'"
      fi
  fi
done

echo "Starting Docker Compose using command: $DOCKER_COMPOSE_CMD"

$DOCKER_COMPOSE_CMD up -d --remove-orphans --force-recreate --build

printf 'Waiting for the service'
attempt_counter=0
max_attempts=50

until [ "$(curl -s -w '%{http_code}' -o /dev/null "http://localhost:8080/q/health/live")" -eq 200 ]; do
    if [ ${attempt_counter} -eq ${max_attempts} ];then
      echo ""
      echo "Max attempts reached. Service failed to start."
      $DOCKER_COMPOSE_CMD logs
      exit 1
    fi

    printf '.'
    attempt_counter=$((attempt_counter+1))
    sleep 5
done

echo ''
echo 'Service Started'