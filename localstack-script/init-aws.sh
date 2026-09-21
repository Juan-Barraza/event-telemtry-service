#!/bin/sh

echo #=== Init DynamoDb Table in LocalStack ===#

awslocal dynamodb create-table \
    --table-name telemetry_events \
    --attribute-definitions \
        AttributeName=deviceId,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=deviceId,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --region us-east-1

echo #=== telemetry_events table created successfully  ===#
