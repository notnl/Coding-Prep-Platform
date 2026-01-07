#!/bin/bash


cd ../ && ./gradlew build

cd judge0-v1.13.1/

cp -fr ../build/libs/* ./SpringBoot/ 
cp -fr ../application.properties ./SpringBoot/
cp -fr ../.env ./SpringBoot/

docker compose build --no-cache
docker compose up -d



