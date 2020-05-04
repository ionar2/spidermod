package me.ionar.salhack.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.util.Timer;

public class NotificationManager
{
    public final List<Notification> Notifications = new CopyOnWriteArrayList<>();
    
    public void AddNotification(String p_Title, String p_Description)
    {
        Notifications.add(new Notification(p_Title, p_Description));
    }
    
    public static NotificationManager Get()
    {
        return SalHack.GetNotificationManager();
    }
    
    public class Notification
    {
        public Notification(String p_Title, String p_Description)
        {
            Title = p_Title;
            Description = p_Description;
            DecayTime = 2500;
            
            timer.reset();
            DecayTimer.reset();
        }
        
        private String Title;
        private String Description;
        private Timer timer = new Timer();
        private Timer DecayTimer = new Timer();
        private int DecayTime;
        
        private int X;
        private int Y;
        
        public void OnRender()
        {
            if (timer.passed(DecayTime-500))
                --Y;
        }
        
        public boolean IsDecayed()
        {
            return DecayTimer.passed(DecayTime);
        }

        /**
         * @return the description
         */
        public String GetDescription()
        {
            return Description;
        }

        /**
         * @return the title
         */
        public String GetTitle()
        {
            return Title;
        }

        /**
         * @return the x
         */
        public int GetX()
        {
            return X;
        }

        /**
         * @param x the x to set
         */
        public void SetX(int x)
        {
            X = x;
        }

        /**
         * @return the y
         */
        public int GetY()
        {
            return Y;
        }

        /**
         * @param y the y to set
         */
        public void SetY(int y)
        {
            Y = y;
        }
    }
}
