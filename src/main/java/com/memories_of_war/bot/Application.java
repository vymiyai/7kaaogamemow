package com.memories_of_war.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class Application {

    // Logger.
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static IDiscordClient DISCORD_CLIENT;

    @Autowired
    private void setBasicCommandHandler(CommandHandler bch) {
        basicCommandHandler = bch;
    }

    @Value("${discord.CLIENT_TOKEN}")
    private void setClientToken(String clientToken) {
        CLIENT_TOKEN = clientToken;
    }

    // as of 23.06.2017, moving the autowired annotation from the setter to this
    // property fucks up everything.
    private static CommandHandler basicCommandHandler;
    // Discord token for bot. Same case as the CommandHandler.
    private static String CLIENT_TOKEN;

    /**
     * Main method run statically.
     *
     * @param args
     */
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

        // get token as environment variable.
        final String token = CLIENT_TOKEN;

        try {
            DISCORD_CLIENT = new ClientBuilder().withToken(token).withRecommendedShardCount().build();

            // Register a listener via the EventSubscriber annotation which allows for organization and delegation of events
            DISCORD_CLIENT.getDispatcher().registerListener(basicCommandHandler);

            // Only login after all events are registered otherwise some may be missed.
            DISCORD_CLIENT.login();
        } catch (Exception e) {
            // do nothing.
            log.warn("WARNING - Discord4J :" + e.getMessage());
        }
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println();
        };
    }

    @Bean
    public CommandLineRunner demo() {
        return (args) -> {
            log.info("Command Line Runner is running.");
        };
    }

}
