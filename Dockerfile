FROM eclipse-temurin:21
LABEL authors="anthonyluong"
WORKDIR /anisocial
COPY build/libs/AniSocial-*-all.jar ./AniSocial.jar
CMD ["java", "-jar", "AniSocial.jar"]