#!/bin/bash

AWS_ACCESS_KEY_ID=1 AWS_SECRET_ACCESS_KEY=2 aws dynamodb create-table --table-name carAdverts --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 --endpoint-url http://localhost:8000 --region us-east-1
