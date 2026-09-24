#!/bin/sh

echo #=== Init DynamoDb Table in LocalStack ===#

awslocal dynamodb create-table \
    --table-name telemetry_events \
    --attribute-definitions \
        AttributeName=accountId,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=accountId,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --region us-east-1 ||true

echo #=== telemetry_events table created successfully  ===#
