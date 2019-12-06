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
ARG ANSIBLE_VERSION=2.9

RUN pip install ansible==$ANSIBLE_VERSION

# Clona repositório do playbook de deploy na AWS
RUN git clone git@github.com:Schaidhauer/aws-ansible.git /ansible

# Define arquivo de hosts conhecidos para o SSH
RUN 'echo "UserKnownHostsFile /ansible/ssh/known_hosts" >> /etc/ssh/ssh_config'
