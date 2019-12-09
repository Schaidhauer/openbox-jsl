############################################################
# Este Dockerfile foi escrito em multiplos estágios porque #
# a chave privada de acesso para clonagem do repositorio é #
# passada via CLI --build-arg ao construir a imagem.       #
# A imagem deve sempre ser construida com o parâmetro --rm #
# para que os containers intermediários sejam removidos    #
############################################################

######## ESTÁGIO INICIAL ########
# instala os pacotes necessários
# para clonar o repositório privado
FROM python:3.6-alpine AS intermediate

RUN apk add --update git openssh-client sshpass

# Copia chaves necessarias para clonar
ARG SSH_PRIVATE_KEY
RUN mkdir /root/.ssh/
RUN echo "${SSH_PRIVATE_KEY}" >  /root/.ssh/github_rsa

# Corrige as permissões
RUN chmod 700 /root/.ssh && chmod 600 /root/.ssh/github_rsa

# Adiciona chave na keychain
#RUN eval "$(ssh-agent -s)"
#RUN ssh-add -k /root/.ssh/github_rsai
RUN echo "Host *
  HostName github.com
  IdentityFile /root/.ssh/github_rsa
  User git
  StrictHostKeyChecking no" > /etc/ssh/ssh_config

# Clona repositório do playbook de deploy na AWS
RUN git clone git@github.com:Schaidhauer/aws-ansible.git /root/ansible/


######## ESTÁGIO FINAL ########
# Configura o ambiente para executar o Ansible
FROM python:3.6-alpine

# Instala dependencias
RUN apk add --update build-base python-dev py-pip jpeg-dev zlib-dev libffi-dev openssl-dev openssh-client sshpass

# Atualiza pip
RUN pip install --upgrade pip

# Altera LIBRARY_PATH devido a um erro ao buildar zlib
ENV LIBRARY_PATH=/lib:/usr/lib

# Define novo diretorio de trabalho
WORKDIR /ansible

# Define volumes
VOLUME [ "/ansible" ]

# Copia conteudo do container intermediario (clone do repositorio) para o volume
COPY --from=intermediate /root/ansible/* /ansible/

# Instala dependencias do Ansible
RUN pip install boto botocore boto3

# Instala o Ansible
RUN pip install ansible==2.9

# Define arquivo de hosts conhecidos para o SSH
RUN 'echo "UserKnownHostsFile /ansible/ssh/known_hosts" >> /etc/ssh/ssh_config'
