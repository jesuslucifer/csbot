package com.example.csbot;

import com.example.csbot.parser.Bo3Client;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CsbotApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context =
				SpringApplication.run(CsbotApplication.class, args);

		Bo3Client bo3Client = new Bo3Client();
		JsonNode jsonNode = bo3Client.getLiveMatches();
		System.out.println(jsonNode.toString());
	}

}
