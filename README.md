# HACKATHON - FIAP SOAT 

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


### Licença

Este projeto está licenciado sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.