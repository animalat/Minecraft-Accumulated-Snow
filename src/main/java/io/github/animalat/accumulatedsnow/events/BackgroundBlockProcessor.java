package io.github.animalat.accumulatedsnow.events;


import io.github.animalat.accumulatedsnow.AccumulatedSnow;
import io.github.animalat.accumulatedsnow.SnowLayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.SplittableRandom;

@Mod.EventBusSubscriber(modid = AccumulatedSnow.MOD_ID)
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

    public static void printMessage(Player player, double num) {
        player.sendSystemMessage(Component.literal(String.valueOf(num)));
    }

    private static void processChunk(LevelChunk chunk, Level world) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getMinBlockX();
        int chunkStartZ = chunkPos.getMinBlockZ();

        final int chunkSize = 16;

        // there is a 1/20 chance for each chunk process for a snow layer to
        //  be put on some random block in that chunk.

        int randX = chunkStartX + randomVals.nextInt(chunkSize);
        int randZ = chunkStartZ + randomVals.nextInt(chunkSize);

        SnowLayerHandler.placeSnowBlock(world, randX, randZ, randomVals.nextInt(16));
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

            // go through each chunk loaded by the player
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
