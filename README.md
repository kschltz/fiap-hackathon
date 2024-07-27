# HACKATHON - FIAP SOAT
[![Coverage Status](https://coveralls.io/repos/github/kschltz/fiap-hackathon/badge.svg)](https://coveralls.io/github/kschltz/fiap-hackathon)

<!--toc:start-->

- [HACKATHON - FIAP SOAT](#hackathon-fiap-soat)
  - [Introdução](#introdução)
  - [Sistema de Telemedicina Health&Med](#sistema-de-telemedicina-healthmed)
    - [Funcionalidades](#funcionalidades)
    - [Contextos Delimitados](#contextos-delimitados)
    - [Requisitos Não Funcionais](#requisitos-não-funcionais)
  - [Arquitetura cloud](#arquitetura-cloud)
  - [Rodando o Projeto](#rodando-o-projeto) - [Rodando pelo docker](#rodando-pelo-docker)
  <!--toc:end-->


[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/7462440-a3586d04-59d2-49cf-b4e0-bb909e3cf1d7?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D7462440-a3586d04-59d2-49cf-b4e0-bb909e3cf1d7%26entityType%3Dcollection%26workspaceId%3D89237b62-8986-4c78-81a4-725c13c2db8e#?env%5B%5Blocal%5D%20hackathon%5D=W3sia2V5IjoiaG9zdCIsInZhbHVlIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiZW5hYmxlZCI6dHJ1ZSwidHlwZSI6ImRlZmF1bHQiLCJzZXNzaW9uVmFsdWUiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzZXNzaW9uSW5kZXgiOjB9LHsia2V5IjoidG9rZW4iLCJ2YWx1ZSI6IiIsImVuYWJsZWQiOnRydWUsInR5cGUiOiJkZWZhdWx0Iiwic2Vzc2lvblZhbHVlIjoiQmVhcmVyLi4uIiwic2Vzc2lvbkluZGV4IjoxfSx7ImtleSI6ImVzcGVjaWFsaWRhZGUiLCJ2YWx1ZSI6Im9mdGFsbW9sb2dpYSIsImVuYWJsZWQiOnRydWUsInR5cGUiOiJkZWZhdWx0Iiwic2Vzc2lvblZhbHVlIjoib2Z0YWxtb2xvZ2lhIiwic2Vzc2lvbkluZGV4IjoyfV0=)

## Introdução

O Domain-Driven Design (DDD) é uma abordagem para o desenvolvimento de software que prioriza a modelagem de um domínio complexo, a fim de refletir um entendimento profundo do problema a ser resolvido. Para uma compreensão mais aprofundada do DDD e como ele foi aplicado neste projeto, consulte o arquivo da [documentação DDD](doc/ddd.md).

## Sistema de Telemedicina Health&Med

A Health&Med é uma startup inovadora no setor de saúde que está desenvolvendo um novo sistema para revolucionar a telemedicina. O sistema permitirá o agendamento e a realização de consultas online, além de oferecer um prontuário eletrônico para armazenamento e compartilhamento de documentos médicos.

### Funcionalidades

O sistema possui as seguintes funcionalidades principais:

- Autenticação de usuários (médicos e pacientes)
- Cadastro e edição de horários disponíveis para consultas por médicos
- Aceite ou recusa de consultas médicas por médicos
- Busca por médicos por pacientes
- Agendamento e cancelamento de consultas por pacientes
- Acesso, upload e compartilhamento de arquivos no prontuário eletrônico por pacientes

### Contextos Delimitados

O sistema é dividido em três contextos delimitados principais:

- Autenticação: Responsável por autenticar médicos e pacientes no sistema
- Agendamento de Consultas: Responsável por gerenciar o agendamento e cancelamento de consultas
- Prontuário Eletrônico: Responsável por gerenciar o acesso, upload e compartilhamento de arquivos no prontuário eletrônico

Para mais detalhes sobre o modelo de domínio, os eventos de domínio e os contextos delimitados, consulte o arquivo [doc/ddd.md](doc/ddd.md).

### Requisitos Não Funcionais

O sistema deve estar disponível 24/7, ser capaz de lidar com alta demanda e seguir as melhores práticas de segurança da informação, especialmente no que diz respeito à proteção dos dados sensíveis dos pacientes.

## Arquitetura cloud

Para uma visão detalhada da infraestrutura e desenho da aplicação você pode consultar o [arquivo da arquitetura](doc/cloud_infra.md).

## Rodando o Projeto

Para desenvolvimento local, garanta que você já possua o clojure instalado na sua máquina. Caso não tenha, você pode seguir as instruções de instalação no [site oficial](https://clojure.org/guides/getting_started).

Após a instalação do clojure, você pode rodar o projeto com o seguinte comando:

```bash
clj -A:dev:test
```

Após rodar o comando acima, você inicializa o ambiente com:

```clojure
(reset-all)
```

#### Rodando pelo docker

Copie o env de exemplo e adapte para seu ambiente:

```bash
cp .env.example .env
```

Construa o docker da aplicação:

```bash
docker compose up -d --build
```

Isso irá criar um container para o postgres e para o app e irá rodar em `localhost:8080`



### Licença

Este projeto está licenciado sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.
