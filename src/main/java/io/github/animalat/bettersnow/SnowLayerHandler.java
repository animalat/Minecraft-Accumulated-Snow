package io.github.animalat.bettersnow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;

public class SnowLayerHandler {
    public static void placeSnowBlock(Level world, int x, int z) {
        --x;    // realign coordinate
                // don't need one for z due to way that coords are handled

        // >> 4 is divide by 16 (the chunk location)
        ChunkAccess chunk = world.getChunk(x >> 4, z >> 4);

        // & 15 is to get the block in that chunk
        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);

        BlockPos surfacePos = new BlockPos(x, surfaceY, z);

        Biome worldBiome = world.getBiome(surfacePos).get();

        boolean isSnowing = worldBiome.getModifiedClimateSettings().hasPrecipitation() &&
                            worldBiome.getModifiedClimateSettings().temperature() <= 0.15;

        if (!isSnowing) {
            return;
        }

        BlockState blockState = world.getBlockState(surfacePos);

        if (blockState.is(Blocks.SNOW)) {
            int layers = blockState.getValue(SnowLayerBlock.LAYERS);
            // only want case where layers are (near) full or nonexistent
            if (layers >= 7 || layers < 1) {
                return;
            }

            ++layers;
            world.setBlockAndUpdate(surfacePos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
        }
    }
}
