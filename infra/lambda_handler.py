import datetime
import os
from typing import Union
from boto3.dynamodb.conditions import Key, Attr 
from boto3 import Session
from opensearchpy import AWSV4SignerAuth, OpenSearch, RequestsHttpConnection
from opensearchpy.connection.http_requests import RequestsHttpConnection  
  

# Criar um objeto OpenSearch com as credenciais do IAM
host = os.environ['os_host'] # O endereço do seu domínio opensearch
port = os.environ['os_port'] # A porta do seu domínio opensearch 
service = 'es' # O nome do serviço opensearch
credentials = Session().get_credentials()

auth = AWSV4SignerAuth(credentials, 'sa-east-1')
os_query = OpenSearch(
    hosts = [{'host': host, 'port': port}],
    http_auth = auth,
    use_ssl = False,
    verify_certs = False,
    connection_class = RequestsHttpConnection
)


def unmarshal_dynamodb_json(node):
    data = dict({})
    data['M'] = node
    return _unmarshal_value(data)


def _unmarshal_value(node):
    if type(node) is not dict:
        return node

    for key, value in node.items():
        # S – String - return string
        # N – Number - return int or float (if includes '.')
        # B – Binary - not handled
        # BOOL – Boolean - return Bool
        # NULL – Null - return None
        # M – Map - return a dict
        # L – List - return a list
        # SS – String Set - not handled
        # NN – Number Set - not handled
        # BB – Binary Set - not handled
        key = key.lower()
        if key == 'bool':
            return value
        if key == 'null':
            return None
        if key == 's':
            return value
        if key == 'n':
            if '.' in str(value):
                return float(value)
            return int(value)
        if key in ['m', 'l']:
            if key == 'm':
                data = {}
                for key1, value1 in value.items():
                    if key1.lower() == 'l':
                        data = [_unmarshal_value(n) for n in value1]
                    else:
                        if type(value1) is not dict:
                            return _unmarshal_value(value)
                        data[key1] = _unmarshal_value(value1)
                return data
            data = []
            for item in value:
                data.append(_unmarshal_value(item))
            return data
         
         
# Definir o método lambda handler
def lambda_handler(event, context):
    print(event)
    # Iterar sobre os registros do evento do DynamoDB
    for record in event['Records']:
        # Verificar se o tipo de evento é INSERT ou MODIFY
        if record['eventName'] in ['INSERT', 'MODIFY']:
            # Obter o item modificado do stream
            item = record['dynamodb']['NewImage']
            # Converter o item em um dicionário Python
            doc_body = unmarshal_dynamodb_json(item)
            # Obter o código do pedido como id do documento
            doc_id = doc_body['codigo_pedido_credito']
            if 'parecerOrigemPedido' in doc_body:
                del doc_body['parecerOrigemPedido'] 
            # Inserir o documento no opensearch usando o objeto OpenSearch
            print(doc_body)
            if '#PEDIDO' in doc_body['chave_ordenacao']:
                result = os_query.index(index='pedidocredito',   id=doc_id, body=doc_body,refresh = True) 
                # Imprimir o resultado da inserção
                print(result)
