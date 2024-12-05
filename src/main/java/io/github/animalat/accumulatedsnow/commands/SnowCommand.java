package io.github.animalat.accumulatedsnow.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.animalat.accumulatedsnow.SnowLayerHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class SnowCommand {
    public SnowCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("snow").executes((context) -> {
            CommandSourceStack source = context.getSource();
            Player player = source.getPlayerOrException();

            double x = player.getX();
            double z = player.getZ();

            final int testSeed = 0;

            SnowLayerHandler.placeSnowBlock(source.getLevel(), (int) x, (int) z, testSeed);
            return 0;
        }));
    }
}
