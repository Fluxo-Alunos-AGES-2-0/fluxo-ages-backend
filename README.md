# 🤝 Guia de Contribuição - Fluxo 2.0

Bem-vindo ao repositório do Fluxo 2.0! Para mantermos o código limpo, organizado e o histórico de versionamento compreensível para todos, estabelecemos algumas regras de ouro. 

Por favor, leia com atenção antes de abrir seu primeiro Pull Request (PR).

---

## 🌍 1. Idioma Oficial: Inglês
Como padrão da indústria, **todo o código gerado neste projeto deve ser em inglês**.
Isso inclui:
- Nomes de variáveis, classes, métodos e pacotes.
- Comentários no código.
---

## 🌿 2. Padrão de Nomenclatura de Branches
**Pegue o nome do Linar**
Na sua task no linear você consegue copiar o nome da branch que deverá ser usado, use este nome.

**Como criar sua branch:**
```bash
git checkout dev
git pull origin dev
git checkout -b feat/sua-nova-feature
```

---

## 💬 3. Conventional Commits (Padrão de Commits)
Nossas mensagens de commit seguem o padrão [Conventional Commits](https://www.conventionalcommits.org/). Isso nos ajuda a entender o histórico do projeto sem precisar ler o código.

**Formato exigido:**
`<tipo>(escopo opcional): <descrição no imperativo, em letras minúsculas>`

### 📚 Dicionário de Commits:
| Tipo | Quando usar? | Exemplo |
| :--- | :--- | :--- |
| **`feat`** | Nova funcionalidade para o usuário ou sistema. | `feat(billing): add tax calculation logic` |
| **`fix`** | Correção de um bug ou erro. | `fix(auth): resolve token expiration issue` |
| **`refactor`** | Mudança no código que não adiciona feature nem corrige bug. | `refactor: simplify transaction mapping` |
| **`docs`** | Mudanças apenas na documentação (README, etc). | `docs: update setup instructions` |
| **`chore`** | Atualizações de ferramentas, dependências ou configurações. | `chore: update spring boot to v3.2.4` |
| **`test`** | Adição ou correção de testes automatizados. | `test(user): add unit tests for user service` |

*Dica: Pense na descrição do commit completando a frase: "Se aplicado, este commit irá... [sua mensagem]".*

---

## 🚀 4. Fluxo de Trabalho (O Caminho do Código)
1. Certifique-se de que sua branch está atualizada com a `dev`.
2. Faça commits pequenos e descritivos seguindo o padrão acima.
3. Suba sua branch para o repositório (`git push origin nome-da-branch`).
4. Abra um Pull Request (PR) apontando para a branch `dev`.
5. Solicite a revisão de código (Code Review) do Arquiteto ou de outro colega.
6. Após a aprovação, o código será mesclado (merged).


## 🚀 Como Rodar o Projeto

Temos duas formas de rodar o ambiente local. Escolha a que melhor se adapta à sua necessidade no momento:

### Opção A: Modo Desenvolvimento (Recomendado para codar) 💻
Neste modo, rodamos **apenas o Banco de Dados no Docker** e a API roda livremente na sua máquina (via IDE ou Terminal). Isso evita conflitos de porta e permite que você coloque *breakpoints* para debugar o código.

**1. Preparar o Banco de Dados (Docker)**
Na raiz do projeto, execute o comando abaixo para subir **exclusivamente** o container do banco:
```bash
docker-compose up -d db
```
*(Isso criará um banco PostgreSQL na porta 5432 com os dados persistidos no volume `postgres_data`).*

**2. Execução da API**
Abra o projeto na sua IDE e dê o "Play" na classe `FluxoBackendApplication.java`.
*(Alternativa via terminal: rode `./mvnw spring-boot:run`)*

---

### Opção B: Modo Full Docker (Para testar o build final) 🐳
Se você quiser rodar a aplicação inteira (Banco + API) encapsulada no Docker, exatamente como seria em produção:

Na raiz do projeto, execute:
```bash
docker-compose up --build
```
> ⚠️ **Atenção:** Neste modo, **NÃO** tente rodar a API pela sua IDE ao mesmo tempo. A porta `8081` já estará ocupada pelo container do Docker!

---

### 📌 Informações Importantes do Ambiente
- **Porta da API**: `8081` (Alterada da 8080 padrão para evitar conflitos do Windows/Mac).
- **URL Base**: `http://localhost:8081`

> **Nota:** O Hibernate está configurado com `ddl-auto=update`. Isso significa que as tabelas serão criadas e atualizadas automaticamente no banco de dados assim que a aplicação subir, espelhando as suas classes `@Entity`.

### 🛑 Como parar o projeto e limpar a memória
Quando terminar o dia de trabalho, sempre desligue os containers rodando:
```bash
docker-compose down
```
