#!/bin/bash

endpoint=${AWS_DYNAMO_URL:-http://localhost:8000}


dynamoReady=false
until "$dynamoReady"; do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$endpoint")
  if [[ "$code" == "000" ]]; then
    echo "dynamoDb is unavailable - sleeping"
    sleep 1
  else
    dynamoReady=true
  fi;
done

AWS_ACCESS_KEY_ID=1 AWS_SECRET_ACCESS_KEY=2 aws dynamodb create-table --table-name carAdverts --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 --endpoint-url "$endpoint" --region us-east-1

unzip target/universal/car-adverts-assignment-1.0
car-adverts-assignment-1.0/bin/car-adverts-assignment