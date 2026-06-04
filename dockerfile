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

# ==========================================
# Estágio de Execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV PORT=8081

# Usamos a versão 0.9.1 mais atualizada, mas colada na RAIZ para evitar o erro de colisão
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.9.1 /lambda-adapter /lambda-adapter

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081

# O adaptador ATUALIZADO assume o controlo como porteiro principal
ENTRYPOINT ["/lambda-adapter"]

# E executa o seu Spring Boot
CMD ["java", "-Dserver.port=8081", "-jar", "app.jar"]