package io.github.animalat.bettersnow.events;


import io.github.animalat.bettersnow.BetterSnow;
import io.github.animalat.bettersnow.SnowLayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.SplittableRandom;

@Mod.EventBusSubscriber(modid = BetterSnow.MOD_ID)
public class BackgroundBlockProcessor {
    private static SplittableRandom randomVals = new SplittableRandom();

    private static ServerLevel getOverworld() {
        // Get Minecraft server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            return server.overworld();
        }

        return null;
    }

    private static void processChunk(LevelChunk chunk, Level world) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getMinBlockX();
        int chunkStartZ = chunkPos.getMinBlockZ();

        final int chunkSize = 16;
        for (int x = chunkStartX; x < chunkStartX + chunkSize; ++x) {
            for (int z = chunkStartZ; z < chunkStartZ + chunkSize; ++z) {
                SnowLayerHandler.placeSnowBlock(world, x, z, randomVals.nextInt(16));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        ServerLevel overworld = getOverworld();
        if (overworld == null) {
            return;
        }

        for (Player player : overworld.players()) {
            if (!(player instanceof ServerPlayer)) {
                continue;
            }

            Level world = player.level();
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
            int viewDistance = ((ServerPlayer) player).server.getPlayerList().getViewDistance();

            for (int chunkX = playerChunkPos.x - viewDistance; chunkX <= playerChunkPos.x + viewDistance; ++chunkX) {
                for (int chunkZ = playerChunkPos.z - viewDistance; chunkZ <= playerChunkPos.z + viewDistance; ++chunkZ) {
                    LevelChunk chunk = world.getChunkSource().getChunk(chunkX, chunkZ, false);
                    if (chunk == null) {
                        continue;
                    }

                    processChunk(chunk, world);
                }
            }
        }
    }
}
