package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HoleInfoComponent extends HudComponentItem
{
    public HoleInfoComponent()
    {
        super("HoleInfo", 2, 170);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        SetWidth(100);
        SetHeight(20);     
        
        String l_Addon = "None";

        final Vec3d l_PlayerPos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos l_BlockPos = new BlockPos(l_PlayerPos.x, l_PlayerPos.y, l_PlayerPos.z);

        BlockPos[] l_Positions = {l_BlockPos.north(), l_BlockPos.south(), l_BlockPos.east(), l_BlockPos.west()/*, l_BlockPos.down()*/}; /// todo check down ?
        
        int l_Counter = 0;
        boolean l_AllBedrock = true;
        
        for (BlockPos l_Pos : l_Positions)
        {
            Block l_Block = mc.world.getBlockState(l_Pos).getBlock();
            
            if (l_Block == Blocks.AIR)
                break;
            
            if (l_Block != Blocks.BEDROCK)
                l_AllBedrock = false;
            
            if (l_Block == Blocks.OBSIDIAN || l_Block == Blocks.BEDROCK)
                ++l_Counter;
        }
        
        if (l_Counter == 4) /// 5 if down
        {
            if (l_AllBedrock)
                l_Addon = ChatFormatting.GREEN + "Safe";
            else
                l_Addon = ChatFormatting.YELLOW + "Unsafe";
        }
        
        RenderUtil.drawStringWithShadow(String.format("Hole: %s", l_Addon), GetX(), GetY(), 0xFFFFFF);
    }

}
