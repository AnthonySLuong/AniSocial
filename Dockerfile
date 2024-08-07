FROM eclipse-temurin:21
LABEL authors="anthonyluong"

WORKDIR /app
COPY build/libs/AniSocial-*-all.jar /app/AniSocial.jar
CMD ["java", "-jar", "/app/AniSocial.jar"]