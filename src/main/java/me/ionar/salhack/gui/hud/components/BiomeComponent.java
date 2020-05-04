package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class BiomeComponent extends HudComponentItem
{
    public BiomeComponent()
    {
        super("Biome", 2, 95);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final BlockPos pos = mc.player.getPosition();
        final Chunk chunk = mc.world.getChunk(pos);
        final Biome biome = chunk.getBiome(pos, mc.world.getBiomeProvider());

        SetWidth(RenderUtil.getStringWidth(biome.getBiomeName()));
        SetHeight(RenderUtil.getStringHeight(biome.getBiomeName()));

        RenderUtil.drawStringWithShadow(biome.getBiomeName(), GetX(), GetY(), -1);
    }
}
