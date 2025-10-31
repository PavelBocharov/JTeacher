FROM eclipse-temurin:25-jre-alpine

WORKDIR /opt/app

COPY target/jteach*.jar /opt/app/jteach.jar

ENV BOT_TOKEN=1111111111:222_333333333_444444444444444444-99
ENV START_IMG=/opt/app/jteach/start.jpg
ENV BASE_IMG=/opt/app/jteach/base.jpg

CMD java -jar /opt/app/jteach.jar --botToken=${BOT_TOKEN} --dir=/opt/app/jteach --startImage=${START_IMG} --baseImage=${BASE_IMG}