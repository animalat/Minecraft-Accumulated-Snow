package io.github.animalat.accumulatedsnow;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.animalat.accumulatedsnow.events.BackgroundBlockProcessor;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.checkerframework.checker.units.qual.C;

import java.util.SplittableRandom;

public class SnowLayerHandler {
    private static SplittableRandom placeBlockChance = new SplittableRandom();

    public static void placeSnowBlock(Level world, int x, int z, int randomValue) {
        final int doProbability = 0;
        if (randomValue != doProbability) {
            return;
        }

        --x;    // realign coordinate
                // don't need one for z due to way that coords are handled

        // >> 4 is divide by 16 (the chunk location)
        ChunkAccess chunk = world.getChunk(x >> 4, z >> 4);

        // & 15 is to get the block in that chunk
        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);

        // This is the block we will be placing
        BlockPos surfacePos = new BlockPos(x, surfaceY, z);

        Biome worldBiome = world.getBiome(surfacePos).get();

        final MutableBoolean isWinter = new MutableBoolean(false);
        if (ModList.get().isLoaded("sereneseasons")) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }

            CommandSourceStack commandSource = new CommandSourceStack(
                    new CommandSource() {
                        @Override
                        public void sendSystemMessage(Component message) {
                            String messageText = message.getString();
                            if (messageText.contains("Winter")) {
                                isWinter.setValue(true);
                            }
                        }

                        @Override
                        public boolean acceptsSuccess() {
                            return true;
                        }

                        @Override
                        public boolean acceptsFailure() {
                            return true;
                        }

                        @Override
                        public boolean shouldInformAdmins() {
                            return false;
                        }
                    },
                    new Vec3(0, 0, 0),
                    Vec2.ZERO,
                    server.overworld(),
                    4,
                    "CustomCommandSource",
                    Component.literal("CustomCommandSource"),
                    server,
                    null
            );

            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();

            try {
                dispatcher.execute("season get", commandSource);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }

        final double freezingTemp = 0.15;
        boolean isSnowing = worldBiome.getModifiedClimateSettings().hasPrecipitation() &&
                world.isRaining() &&
                (worldBiome.getModifiedClimateSettings().temperature() <= freezingTemp ||
                        isWinter.getValue());

        if (!isSnowing) {
            return;
        }

        BlockState blockState = world.getBlockState(surfacePos);

        if (!(blockState.is(Blocks.SNOW))) {
            return;
        }

        int layers = blockState.getValue(SnowLayerBlock.LAYERS);
        // only want case where layers are (near) full or nonexistent
        final int maxLayers = 7;
        final int minLayers = 1;
        if (layers >= maxLayers || layers < minLayers) {
            return;
        }

        // Check surrounding blocks (on same Y position) to do a
        //  weighted probability of placing
        BlockPos surroundingBlocks[] = {
                new BlockPos(x + 1, surfaceY, z),
                new BlockPos(x - 1, surfaceY, z),
                new BlockPos(x, surfaceY, z + 1),
                new BlockPos(x, surfaceY, z - 1),
                new BlockPos(x + 1, surfaceY, z + 1),
                new BlockPos(x - 1, surfaceY, z + 1),
                new BlockPos(x + 1, surfaceY, z - 1),
                new BlockPos(x - 1, surfaceY, z - 1)
        };
        int numSurroundingSnow = 0;
        for (int i = 0; i < surroundingBlocks.length; ++i) {
            BlockState curBlockState = world.getBlockState((surroundingBlocks[i]));
            if (!(curBlockState.is(Blocks.SNOW))) {
                continue;
            }

            // increase probability if surrounding snow blocks are taller
            if (curBlockState.getValue(SnowLayerBlock.LAYERS) >= layers) {
                ++numSurroundingSnow;
            }
        }
        int placeBlockProb = placeBlockChance.nextInt((9 - numSurroundingSnow));
        if (placeBlockProb != 0) {
            return;
        }

        // place the snow block
        ++layers;
        world.setBlockAndUpdate(surfacePos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
    }
}
