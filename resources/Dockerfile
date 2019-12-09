FROM python:3.6-alpine

# Install new packages
RUN apk add --update build-base python-dev py-pip jpeg-dev zlib-dev libffi-dev openssl-dev git openssh-client sshpass

# Upgrade pip
RUN pip install --upgrade pip

# Change LIBRARY_PATH environment variable because of error in building zlib
ENV LIBRARY_PATH=/lib:/usr/lib

# Set Workdir
WORKDIR /ansible

# Define volumes
VOLUME [ "/ansible" ]

# Install ansible dependencies
RUN pip install boto botocore boto3

# Install ansible
RUN pip install ansible==2.9

# Copia chaves necessarias para clonar
COPY github_rsa /home/.ssh/

# Corrige as permissões
RUN chmod 700 /home/.ssh && chmod 600 /home/.ssh/github_rsa

# Adiciona chave na keychain
RUN ssh-add -k /home/.ssh/github_rsa

# Clona repositório do playbook de deploy na AWS
RUN git clone git@github.com:Schaidhauer/aws-ansible.git /ansible

# Define arquivo de hosts conhecidos para o SSH
RUN 'echo "UserKnownHostsFile /ansible/ssh/known_hosts" >> /etc/ssh/ssh_config'
