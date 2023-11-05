package com.svcsolicitarcredito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories; // Importar a anotação EnableDynamoDBRepositories do Spring Data DynamoDB
import org.springframework.context.annotation.ComponentScan;

// Definir a classe principal da aplicação com as anotações @SpringBootApplication e @EnableDynamoDBRepositories
@SpringBootApplication
@ComponentScan(basePackages = { "com.svcsolicitarcredito" })
public class Application {

    // Definir o método main que inicia a aplicação usando o SpringApplication.run
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}