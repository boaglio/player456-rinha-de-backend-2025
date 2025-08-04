# mvn clean package

# docker build -t player456  .

#docker tag player456 boaglio/player456

docker tag player456:1.0.0-SNAPSHOT boaglio/player456:v4

docker push boaglio/player456:v4
#docker push boaglio/player456:latest
