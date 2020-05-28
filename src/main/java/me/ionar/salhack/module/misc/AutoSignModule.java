package me.ionar.salhack.module.misc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.util.text.TextComponentString;

public class AutoSignModule extends Module
{
    public static final Value<String> Line1 = new Value<>("Line1", new String[] {""}, "The first sign line", "SalHack");
    public static final Value<String> Line2 = new Value<>("Line2", new String[] {""}, "The second sign line", "on");
    public static final Value<String> Line3 = new Value<>("Line3", new String[] {""}, "The third sign line", "top!");
    public static final Value<String> Line4 = new Value<>("Line4", new String[] {""}, "The fourth sign line", "~currentdate~");
    public static final Value<Boolean> AutoComplete = new Value<Boolean>("AutoComplete", new String[] {"AC"}, "Automatially completes the sign for you", true);
    
    public AutoSignModule()
    {
        super("AutoSign", new String[] {"AutoSignz"}, "Automatically writes texts on signs for you", "NONE", -1, ModuleType.MISC);
    }

    private Timer _completeTimer = new Timer();
    private boolean _wasInSign = false;

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen == null)
            return;
        
        if (!(mc.currentScreen instanceof GuiEditSign))
        {
            _wasInSign = false;
            return;
        }
        
        if (!_wasInSign)
        {
            _completeTimer.reset();
            _wasInSign = true;
        }
        
        GuiEditSign sign = (GuiEditSign)mc.currentScreen;
        
        if (_completeTimer.passed(3000))
        {
            // complete transaction
            sign.tileSign.markDirty();
            mc.displayGuiScreen(null);
            return;
        }
        
        sign.tileSign.signText[0] = new TextComponentString(genLine(Line1.getValue()));
        sign.tileSign.signText[1] = new TextComponentString(genLine(Line2.getValue()));
        sign.tileSign.signText[2] = new TextComponentString(genLine(Line3.getValue()));
        sign.tileSign.signText[3] = new TextComponentString(genLine(Line4.getValue()));
    });
    
    private String genLine(String val)
    {
        if (val.equals("~currentdate~"))
        {
            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return date.format(formatter);
        }
        else if (val.contains("~playername~"))
        {
            val = val.replace("~playername~", mc.player.getName());
        }
        
        return val;
    }
}
