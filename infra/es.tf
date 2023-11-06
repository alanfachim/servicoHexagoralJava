# Criar um cluster elasticsearch na AWS
module "elasticsearch" {
  source  = "clouddrove/elasticsearch/aws"
  version = "1.0.1"

  name        = "es"
  environment = "test"
  domain_name = "meu-cluster"

  # Configurações do IAM
  enable_iam_service_linked_role = true
  iam_actions = ["es:ESHttpGet", "es:ESHttpPut", "es:ESHttpPost"]

  # Configurações da rede
  vpc_enabled = true
  security_group_ids = [module.security_group.security_group_ids]
  subnet_ids = tolist(module.public_subnets.public_subnet_id)
  availability_zone_count = length(module.public_subnets.public_subnet_id)
  zone_awareness_enabled = true

  # Configurações do ES
  elasticsearch_version = "7.8"
  instance_type = "c5.large.elasticsearch"
  instance_count = 2

  # Configurações do volume
  volume_size = 30
  volume_type = "gp2"

  # Configurações do DNS
  dns_enabled = false
  es_hostname = "es"
  kibana_hostname = "kibana"
  dns_zone_id = false
  advanced_options = {
    "rest.action.multi.allow_explicit_index" = "true"
  }

  # Configurações do Cognito
  cognito_enabled = false
  user_pool_id = ""
  identity_pool_id = ""

  # Configurações dos logs
  log_publishing_index_enabled = true
  log_publishing_search_enabled = true
  log_publishing_application_enabled = true
  log_publishing_audit_enabled = false
}

# Criar uma função lambda para pausar e retomar o cluster elasticsearch
resource "aws_lambda_function" "pause_resume_es" {
  function_name = "pause_resume_es"
  handler       = "lambda_function.lambda_handler"
  role          = aws_iam_role.lambda_role.arn
  runtime       = "python3.8"

  # O código abaixo é um exemplo simples de como pausar e retomar o cluster elasticsearch usando a API do boto3
  # Você pode modificar o código de acordo com as suas necessidades
  # Fonte: 4
  code = <<EOF
import boto3
import os

# Criar um cliente para o serviço elasticsearch
client = boto3.client('es')

# Obter o nome do domínio elasticsearch a partir de uma variável de ambiente
domain_name = os.environ['ES_DOMAIN_NAME']

# Definir uma função para pausar o cluster elasticsearch
def pause_es():
  # Atualizar a configuração do domínio elasticsearch para desabilitar o processamento de cluster
  response = client.update_elasticsearch_domain_config(
    DomainName=domain_name,
    ElasticsearchClusterConfig={
      'InstanceCount': 0
    }
  )
  # Retornar o status da resposta
  return response['ResponseMetadata']['HTTPStatusCode']

# Definir uma função para retomar o cluster elasticsearch
def resume_es():
  # Obter o número de instâncias desejado a partir de uma variável de ambiente
  instance_count = int(os.environ['ES_INSTANCE_COUNT'])
  # Atualizar a configuração do domínio elasticsearch para habilitar o processamento de cluster
  response = client.update_elasticsearch_domain_config(
    DomainName=domain_name,
    ElasticsearchClusterConfig={
      'InstanceCount': instance_count
    }
  )
  # Retornar o status da resposta
  return response['ResponseMetadata']['HTTPStatusCode']

# Definir uma função principal para lidar com o evento lambda
def lambda_handler(event, context):
  # Obter o tipo de evento (pause ou resume) a partir do evento lambda
  event_type = event['type']
  # Verificar se o tipo de evento é válido
  if event_type not in ['pause', 'resume']:
    # Retornar um erro se o tipo de evento não for válido
    return {
      'statusCode': 400,
      'body': 'Invalid event type'
    }
  # Chamar a função correspondente ao tipo de evento
  if event_type == 'pause':
    status = pause_es()
  elif event_type == 'resume':
    status = resume_es()
  # Retornar o status da operação
  return {
    'statusCode': status,
    'body': f'{event_type} operation completed'
  }
EOF

  # Definir as variáveis de ambiente para o nome do domínio elasticsearch e o número de instâncias
  environment {
    variables = {
      ES_DOMAIN_NAME   = module.elasticsearch.domain_name
      ES_INSTANCE_COUNT = module.elasticsearch.instance_count
    }
  }

  # Definir o tempo limite da função lambda em segundos
  timeout = 30
}

# Criar uma função lambda para pausar e retomar o cluster elasticsearch
resource "aws_iam_role" "lambda_role" {
  name = "lambda_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

# Anexar uma política ao papel lambda para permitir que ele acesse o serviço elasticsearch
resource "aws_iam_role_policy_attachment" "lambda_es_policy_attachment" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonESFullAccess"
}

# Criar um evento do CloudWatch para acionar a função lambda para pausar o cluster elasticsearch às 18:00
resource "aws_cloudwatch_event_rule" "pause_es_rule" {
  name                = "pause_es_rule"
  description         = "Pause elasticsearch cluster at 18:00"
  schedule_expression = "cron(0 18 * * ? *)"
}

# Criar um alvo do CloudWatch para a função lambda para pausar o cluster elasticsearch
resource "aws_cloudwatch_event_target" "pause_es_target" {
  rule      = aws_cloudwatch_event_rule.pause_es_rule.name
  target_id = "pause_es_target"
  arn       = aws_lambda_function.pause_resume_es.arn

  # Passar o tipo de evento (pause) como um parâmetro para a função lambda
  input = <<EOF
{
  "type": "pause"
}
EOF
}

# Criar uma permissão para permitir que o evento do CloudWatch invoque a função lambda para pausar o cluster elasticsearch
resource "aws_lambda_permission" "pause_es_permission" {
  statement_id  = "pause_es_permission"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.pause_resume_es.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.pause_es_rule.arn
}

# Criar um evento do CloudWatch para acionar a função lambda para retomar o cluster elasticsearch às 09:00
resource "aws_cloudwatch_event_rule" "resume_es_rule" {
  name                = "resume_es_rule"
  description         = "Resume elasticsearch cluster at 09:00"
  schedule_expression = "cron(0 9 * * ? *)"
}

# Criar um alvo do CloudWatch para a função lambda para retomar o cluster elasticsearch
resource "aws_cloudwatch_event_target" "resume_es_target" {
  rule      = aws_cloudwatch_event_rule.resume_es_rule.name
  target_id = "resume_es_target"
  arn       = aws_lambda_function.pause_resume_es.arn

  # Passar o tipo de evento (resume) como um parâmetro para a função lambda
  input = <<EOF
{
  "type": "resume"
}
EOF
}

# Criar uma permissão para permitir que o evento do CloudWatch invoque a função lambda para retomar o cluster elasticsearch
resource "aws_lambda_permission" "resume_es_permission" {
  statement_id  = "resume_es_permission"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.pause_resume_es.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.resume_es_rule.arn
}
