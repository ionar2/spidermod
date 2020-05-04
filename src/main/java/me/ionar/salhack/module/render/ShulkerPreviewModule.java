package me.ionar.salhack.module.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.input.Mouse;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.EventRenderTooltip;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

public final class ShulkerPreviewModule extends Module
{
    public final Value<Boolean> middleClick = new Value("MiddleClick", new String[]
    { "MC", "Mid" }, "Allows you to middle click shulkers and view their contents.", true);
    public final Value<Boolean> EnderChest = new Value("EnderChest", new String[]
    { "EC", "Ender" }, "Previews your enderchest (Requires you to oepn your ender chest first", true);
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "Server Mode" }, "Server mode, like 2b2t has custom preview settings", Modes.DropPacket);

    public enum Modes
    {
        Normal, DropPacket, Inventory
    }

    private boolean clicked;

    public ShulkerPreviewModule()
    {
        super("ShulkerPreview", new String[]
        { "SPreview", "ShulkerView" }, "Hover over a shulker box to the items inside.", "NONE", 0xDB3C24, ModuleType.RENDER);
    }

    private final ArrayList<ItemStack> EnderChestItems = new ArrayList<ItemStack>();
    private final HashMap<String, List<ItemStack>> SavedShulkerItems = new HashMap<String, List<ItemStack>>();
    private int EnderChestWindowId = -1;
    private int ShulkerWindowId = -1;
    private Timer timer = new Timer();
    private String LastWindowTitle = "";

    public NBTTagCompound getShulkerNBT(ItemStack stack)
    {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey("BlockEntityTag", 10))
        {
            NBTTagCompound tags = compound.getCompoundTag("BlockEntityTag");
            if (tags.hasKey("Items", 9))
                return tags;
        }

        return null;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
    }
    
    @Override
    public String getMetaData()
    {
        return Mode.getValue().toString();   
    }

    @EventHandler
    private Listener<EntityJoinWorldEvent> OnEntityJoinWorld = new Listener<>(p_Event ->
    {
        if (Mode.getValue() != Modes.DropPacket)
            return;
        
        if (p_Event.getEntity() == null || !(p_Event.getEntity() instanceof EntityItem))
            return;
        
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                EntityItem l_Item = (EntityItem)p_Event.getEntity();
                
                if (!(l_Item.getItem().getItem() instanceof ItemShulkerBox))
                    return;
                
                ItemStack shulker = l_Item.getItem();
                
                NBTTagCompound shulkerNBT = getShulkerNBT(shulker);
                
                if (shulkerNBT != null)
                {
                    TileEntityShulkerBox fakeShulker = new TileEntityShulkerBox();
                    fakeShulker.loadFromNbt(shulkerNBT);
                    String customName = shulker.getDisplayName();
                    /*
                    boolean hasCustomName = false;
                    if (shulkerNBT.hasKey("CustomName", 8))
                    {
                        customName = shulkerNBT.getString("CustomName");
                        hasCustomName = true;
                    }*/

                    ArrayList<ItemStack> l_Items = new ArrayList<ItemStack>();
                    
                    int l_SlotsNotEmpty = 0;
                    
                    for (int i = 0; i < 27; ++i)
                    {
                        l_Items.add(fakeShulker.getStackInSlot(i));
                        
                        if (fakeShulker.getStackInSlot(i) != ItemStack.EMPTY)
                            ++l_SlotsNotEmpty;
                    }
                    
                    if (SavedShulkerItems.containsKey(customName))
                        SavedShulkerItems.remove(customName);
                    else
                        SalHack.SendMessage("New shulker found with name " + customName + " it contains " + l_SlotsNotEmpty + " slots NOT empty");
                    
                    SavedShulkerItems.put(customName, l_Items);
                }
            }
        }, 5000);
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketWindowItems)
        {
            final SPacketWindowItems l_Packet = (SPacketWindowItems) p_Event.getPacket();

            if (l_Packet.getWindowId() == EnderChestWindowId)
            {
                EnderChestItems.clear();

                for (int i = 0; i < l_Packet.getItemStacks().size(); ++i)
                {
                    ItemStack itemStack = l_Packet.getItemStacks().get(i);
                    if (itemStack == null)
                        continue;

                    if (i > 26)
                        break;

                    EnderChestItems.add(itemStack);
                }
            }
            else if (l_Packet.getWindowId() == ShulkerWindowId)
            {
                if (SavedShulkerItems.containsKey(LastWindowTitle))
                    SavedShulkerItems.remove(LastWindowTitle);
                
                ArrayList<ItemStack> l_List = new ArrayList<ItemStack>();

                for (int i = 0; i < l_Packet.getItemStacks().size(); ++i)
                {
                    ItemStack itemStack = l_Packet.getItemStacks().get(i);
                    if (itemStack == null)
                        continue;

                    if (i > 26)
                        break;

                    l_List.add(itemStack);
                }
                
                SavedShulkerItems.put(LastWindowTitle, l_List);
            }
        }
        else if (p_Event.getPacket() instanceof SPacketOpenWindow)
        {
            final SPacketOpenWindow l_Packet = (SPacketOpenWindow) p_Event.getPacket();

            if (l_Packet.getWindowTitle().getFormattedText().startsWith("Ender"))
            {
                EnderChestWindowId = l_Packet.getWindowId();
                return;
            }
            else
            {
                ShulkerWindowId = l_Packet.getWindowId();
                LastWindowTitle = l_Packet.getWindowTitle().getUnformattedText();
                return;
            }
        }
        else if (p_Event.getPacket() instanceof SPacketSetSlot)
        {
            final SPacketSetSlot l_Packet = (SPacketSetSlot) p_Event.getPacket();

            if (l_Packet.getWindowId() == EnderChestWindowId)
            {
                // salhack.INSTANCE.logChat("Ender Chest updated, reopen to update preview.");
            }
        }
    });

    @EventHandler
    private Listener<EventRenderTooltip> OnRenderTooltip = new Listener<>(p_Event ->
    {
        if (p_Event.getItemStack() == null)
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        if (p_Event.getItemStack().getDisplayName().equals("Ender Chest"))
        {
            // store mouse/event coords
            int x = p_Event.getX();
            int y = p_Event.getY();

            // translate to mouse x, y
            GlStateManager.translate(x + 10, y - 5, 0);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            // background
            RenderUtil.drawRect(-3, -mc.fontRenderer.FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x99101010);
            RenderUtil.drawRect(-2, -mc.fontRenderer.FONT_HEIGHT - 3, 9 * 16 + 2, 3 * 16 + 2, 0xFF202020);
            RenderUtil.drawRect(0, 0, 9 * 16, 3 * 16, 0xFF101010);

            // text
            RenderUtil.drawStringWithShadow("SalHack EnderChest Viewer", 0, -mc.fontRenderer.FONT_HEIGHT - 1,
                    0xFFFFFFFF);

            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 150.0F;
            RenderHelper.enableGUIStandardItemLighting();

            for (int i = 0; i < EnderChestItems.size(); ++i)
            {
                ItemStack itemStack = EnderChestItems.get(i);
                if (itemStack == null)
                    continue;

                // salhack.INSTANCE.logChat("Item: " + itemStack.getDisplayName());

                int offsetX = (i % 9) * 16;
                int offsetY = (i / 9) * 16;
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
            }

            p_Event.cancel();

            RenderHelper.disableStandardItemLighting();
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableLighting();

            // reverse the translate
            GlStateManager.translate(-(x + 10), -(y - 5), 0);

            if (this.middleClick.getValue())
            {
                if (Mouse.isButtonDown(2))
                {
                    if (!this.clicked)
                    {
                        InventoryBasic l_Inventory = new InventoryBasic("SalHack EnderChest Viewer", true, 27);

                        for (int i = 0; i < EnderChestItems.size(); ++i)
                        {
                            ItemStack itemStack = EnderChestItems.get(i);
                            if (itemStack == null)
                                continue;

                            l_Inventory.addItem(itemStack);
                        }

                        mc.displayGuiScreen(new GuiChest(mc.player.inventory, l_Inventory));
                    }
                    this.clicked = true;
                }
                else
                {
                    this.clicked = false;
                }
            }
        }
        else if (p_Event.getItemStack().getItem() instanceof ItemShulkerBox)
        {
            if (Mode.getValue() == Modes.Normal)
                RenderLegacyShulkerPreview(p_Event);
            else if (Mode.getValue() == Modes.DropPacket || Mode.getValue() == Modes.Inventory)
                Render2b2tShulkerPreview(p_Event);
        }
    });
    
    public void RenderLegacyShulkerPreview(EventRenderTooltip event)
    {
        ItemStack shulker = event.getItemStack();
        NBTTagCompound tagCompound = shulker.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10))
        {
            NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
            if (blockEntityTag.hasKey("Items", 9))
            {
                event.cancel();

                NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist); // load the itemstacks from the tag to
                                                                           // the list

                // store mouse/event coords
                int x = event.getX();
                int y = event.getY();

                // translate to mouse x, y
                GlStateManager.translate(x + 10, y - 5, 0);

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                // background
                RenderUtil.drawRect(-3, -mc.fontRenderer.FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x99101010);
                RenderUtil.drawRect(-2, -mc.fontRenderer.FONT_HEIGHT - 3, 9 * 16 + 2, 3 * 16 + 2, 0xFF202020);
                RenderUtil.drawRect(0, 0, 9 * 16, 3 * 16, 0xFF101010);

                // text
                RenderUtil.drawStringWithShadow(shulker.getDisplayName(), 0, -mc.fontRenderer.FONT_HEIGHT - 1,
                        0xFFFFFFFF);

                GlStateManager.enableDepth();
                mc.getRenderItem().zLevel = 150.0F;
                RenderHelper.enableGUIStandardItemLighting();

                // loop through items in shulker inventory
                for (int i = 0; i < nonnulllist.size(); i++)
                {
                    ItemStack itemStack = nonnulllist.get(i);
                    int offsetX = (i % 9) * 16;
                    int offsetY = (i / 9) * 16;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
                }

                RenderHelper.disableStandardItemLighting();
                mc.getRenderItem().zLevel = 0.0F;
                GlStateManager.enableLighting();

                // reverse the translate
                GlStateManager.translate(-(x + 10), -(y - 5), 0);
            }
        }

        if (this.middleClick.getValue())
        {
            if (Mouse.isButtonDown(2))
            {
                if (!this.clicked)
                {
                    final BlockShulkerBox shulkerBox = (BlockShulkerBox) Block.getBlockFromItem(shulker.getItem());
                    if (shulkerBox != null)
                    {
                        final NBTTagCompound tag = shulker.getTagCompound();
                        if (tag != null && tag.hasKey("BlockEntityTag", 10))
                        {
                            final NBTTagCompound entityTag = tag.getCompoundTag("BlockEntityTag");

                            final TileEntityShulkerBox te = new TileEntityShulkerBox();
                            te.setWorld(mc.world);
                            te.readFromNBT(entityTag);
                            mc.displayGuiScreen(new GuiShulkerBox(mc.player.inventory, te));
                        }
                    }
                }
                this.clicked = true;
            }
            else
            {
                this.clicked = false;
            }
        }
    }

    public void Render2b2tShulkerPreview(EventRenderTooltip event)
    {
        if (!SavedShulkerItems.containsKey(event.getItemStack().getDisplayName()))
            return;
        
        final List<ItemStack> l_Items = SavedShulkerItems.get(event.getItemStack().getDisplayName());
            
        // store mouse/event coords
        int x = event.getX();
        int y = event.getY();

        // translate to mouse x, ye
        GlStateManager.translate(x + 10, y - 5, 0);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        // background
   //     RenderUtil.drawRect(-3, -mc.fontRenderer.FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x99101010);
        RenderUtil.drawRect(-2, -10 - 3, 9 * 16 + 2, 3 * 16 + 2, 0xFF202020);
        RenderUtil.drawRect(0, 0, 9 * 16, 3 * 16, 0xFF101010);

        // text
        RenderUtil.drawStringWithShadow(event.getItemStack().getDisplayName(), 0, -12,
                0xFFFFFFFF);

        GlStateManager.enableDepth();
        mc.getRenderItem().zLevel = 150.0F;
        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < l_Items.size(); ++i)
        {
            ItemStack itemStack = l_Items.get(i);
            if (itemStack == null)
                continue;

            // salhack.INSTANCE.logChat("Item: " + itemStack.getDisplayName());

            int offsetX = (i % 9) * 16;
            int offsetY = (i / 9) * 16;
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
        }

        event.cancel();

        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.enableLighting();

        // reverse the translate
        GlStateManager.translate(-(x + 10), -(y - 5), 0);

        if (this.middleClick.getValue())
        {
            if (Mouse.isButtonDown(2))
            {
                if (!this.clicked)
                {
                    InventoryBasic l_Inventory = new InventoryBasic(event.getItemStack().getDisplayName(), true, 27);

                    for (int i = 0; i < l_Items.size(); ++i)
                    {
                        ItemStack itemStack = l_Items.get(i);
                        if (itemStack == null)
                            continue;

                        l_Inventory.addItem(itemStack);
                    }

                    mc.displayGuiScreen(new GuiChest(mc.player.inventory, l_Inventory));
                }
                this.clicked = true;
            }
            else
            {
                this.clicked = false;
            }
        }
    }
}
