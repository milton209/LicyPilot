package com.licypilot.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // Tenta carregar da pasta atual
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        // Se nÃ£o achou o token, tenta na pasta pai (raiz do monorepo)
        if (dotenv.get("GITHUB_TOKEN") == null) {
            dotenv = Dotenv.configure().directory("..").ignoreIfMissing().load();
        }
        
        final Dotenv finalDotenv = dotenv;
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(BackendApplication.class, args);
    }
}
