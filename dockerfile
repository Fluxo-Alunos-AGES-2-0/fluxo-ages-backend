# Estágio de Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 1. Copia apenas o que é necessário para baixar as dependências
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# 2. BAIXA AS DEPENDÊNCIAS
RUN ./mvnw dependency:go-offline -B

# 3. Agora sim copia o código fonte e compila
COPY src src
RUN ./mvnw clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV PORT=8081
# Habilita o Init Assíncrono para anular o timeout de 10s da AWS
ENV AWS_LWA_ASYNC_INIT=true 

# Copia o adaptador para a pasta mágica de extensões da AWS 
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.9.1 /lambda-adapter /opt/extensions/lambda-adapter

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081

# Mantém as flags de otimização extremas para forçar o boot em ~6s
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-noverify", "-Dspring.main.lazy-initialization=true", "-Dserver.port=8081", "-jar", "app.jar"]