package me.ionar.salhack.module.schematica;

import com.github.lunatrius.schematica.client.printer.SchematicPrinter;

import me.ionar.salhack.module.Module;

public class PrinterModule extends Module
{

    public PrinterModule()
    {
        super("Printer", new String[] {"SchematicaPrinter"}, "Integration of Schematica's printer", "NONE", -1, ModuleType.SCHEMATICA);
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public String getMetaData()
    {
        return SchematicPrinter.INSTANCE.IsStationary() ? "Stationary" : "Printing";
    }
    
    @Override
    public void toggle()
    {
        super.toggle();
        SchematicPrinter.INSTANCE.setPrinting(this.isEnabled());
    }

}
