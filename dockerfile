# Estágio de Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 1. Copia apenas o que é necessário para baixar as dependências
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# 2. BAIXA AS DEPENDÊNCIAS (Essa camada fica salva no cache!)
# Se o pom.xml não mudar, o Docker pula essa parte nas próximas vezes
RUN ./mvnw dependency:go-offline -B

# 3. Agora sim copia o código fonte e compila
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081

# O Adaptador se torna o porteiro principal do contêiner
ENTRYPOINT ["/opt/extensions/lambda-adapter"]

# E ele executa o seu Spring Boot como processo filho
CMD ["java", "-Dserver.port=8081", "-jar", "app.jar"]