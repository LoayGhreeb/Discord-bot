package bot.commands.userCommands;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UserInfoCommand implements Command {
    private final CodeforcesAPIImpl codeforcesAPI;

    public UserInfoCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String username = Objects.requireNonNull(event.getOption("username")).getAsString();

        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getUserInfo(username);
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve user info: " + e.getMessage(), e);
            }
        }).thenAccept(embedBuilder ->
                hook.sendMessageEmbeds(embedBuilder.build()).queue()
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}