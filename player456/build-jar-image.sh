# mvn clean package

docker build -t curva-de-rio  .

docker tag curva-de-rio boaglio/curva-de-rio

docker push boaglio/curva-de-rio:latest

