package bot.commands.compareUsers;

import java.util.concurrent.CompletableFuture;

import bot.api.CodeforcesApiCaller;
import bot.commands.Command;
import bot.infrastructure.CodeforcesAPIImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class CompareProblemRatingsCommand implements Command {

    private final CodeforcesAPIImpl codeforcesAPI;

    public CompareProblemRatingsCommand() {
        this.codeforcesAPI = new CodeforcesAPIImpl(new CodeforcesApiCaller());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Defer the reply to avoid the 3-second timeout
        event.deferReply().queue();

        // Get the interaction hook for later use
        InteractionHook hook = event.getHook();

        CompletableFuture.supplyAsync(() -> {
            try {
                return codeforcesAPI.getFinishedContests();
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve problem ratings: " + e.getMessage(), e);
            }
        }).thenAccept(embedBuilder ->
                hook.sendMessageEmbeds(embedBuilder.build()).queue()
        ).exceptionally(throwable -> {
            hook.sendMessage("Error: " + throwable.getCause().getMessage()).queue();
            return null;
        });
    }
}