FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} backend_app.jar
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

CMD ["sh", "docker-entrypoint.sh"]