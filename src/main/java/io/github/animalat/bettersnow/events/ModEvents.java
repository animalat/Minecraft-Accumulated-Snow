package io.github.animalat.bettersnow.events;

import io.github.animalat.bettersnow.BetterSnow;
import io.github.animalat.bettersnow.commands.SnowCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = BetterSnow.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SnowCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
