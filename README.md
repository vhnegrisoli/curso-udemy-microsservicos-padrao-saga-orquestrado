# Projeto: Curso Udemy - Arquitetura de Microsserviços: Padrão Saga Orquestrado

Repositório contendo o projeto desenvolvido do curso Arquitetura de Microsserviços: Padrão Saga Orquestrado, ministrado por mim para a plataforma Udemy.

## Tecnologias

* **Java 17**
* **Spring Boot 3**
* **Apache Kafka**
* **API REST**
* **PostgreSQL**
* **MongoDB**
* **Docker**
* **docker-compose**

# Ferramentas utilizadas

* **IntelliJ IDEA Community Edition**
* **Docker**
* **Gradle**

# Arquitetura Proposta

No curso, desenvolveremos a seguinte aquitetura:

![Arquitetura](https://github.com/vhnegrisoli/curso-udemy-microsservicos-padrao-saga-orquestrado/blob/main/Conte%C3%BAdos/Arquitetura%20Proposta.png)

Em nossa arquitetura, teremos 5 serviços:

* **Order-Service**: microsserviço responsável apenas por gerar um pedido inicial, e receber uma notificação. Aqui que teremos endpoints REST para inciar o processo e recuperar os dados dos eventos. O banco de dados utilizado será o MongoDB.
* **Orchestrator-Service**: microsserviço responsável por orquestrar todo o fluxo de execução da Saga, ele que saberá qual microsserviço foi executado e em qual estado, e para qual será o próximo microsserviço a ser enviado, este microsserviço também irá salvar o processo dos eventos. O banco de dados utilizado será o MongoDB.
* **Product-Validation-Service**: microsserviço responsável por validar se o produto informado no pedido existe e está válido. Este microsserviço guardará a validação de um produto para o ID de um pedido. O banco de dados utilizado será o PostgreSQL.
* **Payment-Service**: microsserviço responsável por realizar um pagamento com base nos valores unitários e quantidades informadas no pedido. Este microsserviço guardará a informação de pagamento de um pedido. O banco de dados utilizado será o PostgreSQL.
* **Inventory-Service**: microsserviço responsável por realizar a baixa do estoque dos produtos de um pedido. Este microsserviço guardará a informação da baixa de um produto para o ID de um pedido. O banco de dados utilizado será o PostgreSQL.

Todos os serviços da arquitetura irão subir através do arquivo **docker-compose.yml**.

## Execução do projeto

Para rodar as aplicações, será necessário ter instalado:

* **Docker**
* **Java 17**
* **Gradle 7.6 ou superior**

### Executando os serviços de bancos de dados e Message Broker

Para que seja possível executar os serviços de bancos de dados e Message Broker, como MongoDB, PostgreSQL e Apache Kafka, basta ir no diretório raiz do repositório, onde encontra-se o arquivo `docker-compose.yml` e executar o comando:

`docker-compose up --build -d`

Para parar todos os containers, basta rodar:

`docker-compose down` 

Ou então:

`docker stop ($docker ps -aq)`
`docker container prune -f`

### Executando os projetos

Antes da execução do projeto, realize o `build` da aplicação indo no diretório raiz e executando o comando:

`gradle build -x test`

Para executar os projetos com Gradle, basta entrar no diretório raiz de cada projeto, e executar o comando:

`gradle bootRun` 

Ou então, entrar no diretório: `build/libs` e executar o comando:

`java -jar nome_do_jar.jar`

### Acessando a aplicação

Para acessar as aplicações e realizar um pedido, basta acessar a URL:

http://localhost:3000/swagger-ui.html

Você chegará nesta página:

![Swagger](https://github.com/vhnegrisoli/curso-udemy-microsservicos-padrao-saga-orquestrado/blob/main/Conte%C3%BAdos/Documentacao.png)
