event = {
  "Records": [
    {
      "eventName": "INSERT",
      "dynamodb": {
        "NewImage": {
          "codigo_pedido_credito": {
            "S": "123456"
          },
          "chave_ordenacao": {
            "S": "#PEDIDO"
          },
          "data_pedido": {
            "S": "2023-04-15"
          },
          "valor_pedido": {
            "N": "1000.00"
          },
          "status_pedido": {
            "S": "APROVADO"
          }
        }
      }
    },
    {
      "eventName": "MODIFY",
      "dynamodb": {
        "NewImage": {
          "codigo_pedido_credito": {
            "S": "789012"
          },
          "chave_ordenacao": {
            "S": "#PEDIDO"
          },
          "data_pedido": {
            "S": "2023-04-16"
          },
          "valor_pedido": {
            "N": "2000.00"
          },
          "status_pedido": {
            "S": "REJEITADO"
          },
          "parecerOrigemPedido": {
            "S": "Renda insuficiente"
          }
        }
      }
    }
  ]
}
