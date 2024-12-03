package io.github.animalat.bettersnow.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.animalat.bettersnow.SnowLayerHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.logging.Logger;

public class SnowCommand {
    public SnowCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("snow").executes((context) -> {
            CommandSourceStack source = context.getSource();
            Player player = source.getPlayerOrException();

            double x = player.getX();
            double z = player.getZ();

            SnowLayerHandler.placeSnowBlock(source.getLevel(), (int) x, (int) z);
            return 0;
        }));
    }
}
