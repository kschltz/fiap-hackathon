FROM clojure:temurin-22-tools-deps-bullseye-slim AS builder
ENV CLOJURE_VERSION=1.11.1.1182
RUN mkdir -p /build
WORKDIR /build
COPY ./ /build/

RUN  clojure -T:build uber :project server


FROM eclipse-temurin:17-alpine AS runner
RUN addgroup -S server && adduser -S server -G server
RUN mkdir -p /service && chown -R server. /service
USER server

RUN mkdir -p /service
WORKDIR /service
ENV HTTP_PORT=8080
EXPOSE 8080
COPY --from=builder /build/target/server-1.0.4-standalone.jar /service/server.jar
ENTRYPOINT ["java", "-jar", "/service/server.jar"]
