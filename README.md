# Fluxo 2.0 - Backend API 🚀

Este é o Core da plataforma Fluxo 2.0, responsável pelo gerenciamento de fluxos, autenticação e regras de negócio. 

---

## 🛠️ Tecnologias Utilizadas

- **Java 21 (OpenJDK)**: Versão mais recente para aproveitar os recursos modernos da JVM.
- **Spring Boot 3.x**: Framework principal para criação de microserviços.
- **Spring Data JPA & Hibernate**: Camada de persistência utilizando a estratégia **Code-First**.
- **PostgreSQL 15**: Banco de dados relacional robusto.
- **Docker & Docker Compose**: Gerenciamento de infraestrutura local "em um clique".
- **Maven**: Gerenciador de dependências e automação de build.

## ⚙️ Pré-requisitos para o Desenvolvimento

Para garantir que o projeto rode na sua máquina sem erros, você precisa de:
1. **JDK 21** instalado e configurado nas variáveis de ambiente.
2. **Docker Desktop** instalado e rodando.
3. **Git** para controle de versão.
4. **IntelliJ IDEA** (recomendada) ou **VS Code** com extensões Java.

## 🚀 Como Rodar o Projeto

Siga este passo a passo para subir o ambiente local:

### 1. Preparar o Banco de Dados (Docker)
Na raiz do projeto, execute o comando para subir o container do banco:
```bash
docker-compose up -d
```
Isso criará um banco PostgreSQL na porta 5432 com os dados persistidos no volume postgres_data.

### 3. Execução da API
Abra o projeto na sua IDE e execute a classe FluxoBackendApplication.java.
- **Porta da API**: 8081 (Alterada da 8080 padrão para evitar conflitos de sistema).
- **URL Base**: http://localhost:8081

> **Nota:** O Hibernate está configurado com ddl-auto=update, o que significa que as tabelas serão criadas automaticamente no banco assim que a aplicação subir.


