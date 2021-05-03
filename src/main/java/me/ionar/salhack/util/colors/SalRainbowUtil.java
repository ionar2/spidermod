package me.ionar.salhack.util.colors;

import me.ionar.salhack.util.Timer;

import java.util.ArrayList;

/// Object for rainbow handling
public class SalRainbowUtil
{
    public SalRainbowUtil(int p_Timer)
    {
        m_Timer = p_Timer;
        
        /// Populate the RainbowArrayList
        for (int l_I = 0; l_I < 360; l_I++)
        {
            RainbowArrayList.add(ColorUtil.GetRainbowColor(l_I, 90.0f, 50.0f, 1.0f).getRGB());
            CurrentRainbowIndexes.add(l_I);
        }
    }
    
    private ArrayList<Integer> CurrentRainbowIndexes = new ArrayList<Integer>();
    private ArrayList<Integer> RainbowArrayList = new ArrayList<Integer>();
    private Timer RainbowSpeed = new Timer();
    
    private int m_Timer;
    
    public int GetRainbowColorAt(int p_Index)
    {
        if (p_Index > CurrentRainbowIndexes.size() - 1)
            p_Index = CurrentRainbowIndexes.size() - 1;

        return RainbowArrayList.get(CurrentRainbowIndexes.get(p_Index));
    }
    
    public void SetTimer(int p_NewTimer)
    {
        m_Timer = p_NewTimer;
    }

    /// Call this function in your render/update function.
    public void OnRender()
    {
        if (RainbowSpeed.passed(m_Timer))
        {
            RainbowSpeed.reset();
            MoveListToNextColor();
        }
    }

    private void MoveListToNextColor()
    {
        if (CurrentRainbowIndexes.isEmpty())
            return;

        CurrentRainbowIndexes.remove(CurrentRainbowIndexes.get(0));

        int l_Index = CurrentRainbowIndexes.get(CurrentRainbowIndexes.size() - 1) + 1;

        if (l_Index >= RainbowArrayList.size() - 1)
            l_Index = 0;

        CurrentRainbowIndexes.add(l_Index);
    }

    public int getRainbowColorNumber(int l_I) {
        l_I += 20;
        if (l_I >= 355) {
            l_I = 0;
        }
        return l_I;
    }
    
}
