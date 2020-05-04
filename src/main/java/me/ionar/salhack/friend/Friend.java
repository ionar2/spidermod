package me.ionar.salhack.friend;

public class Friend
{
    public Friend(String p_Name, String p_Alias, String p_Cape)
    {
        Name = p_Name;
        Alias = p_Alias;
        Cape = p_Cape;
    }
    
    private String Name;
    private String Alias;
    private String Cape;
    
    public void SetAlias(String p_Alias)
    {
        Alias = p_Alias;
    }
    
    public void SetCape(String p_Cape)
    {
        Cape = p_Cape;
    }
    
    public String GetName()
    {
        return Name;
    }
    
    public String GetAlias()
    {
        return Alias;
    }
    
    public String GetCape()
    {
        return Cape;
    }
}
