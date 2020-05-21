package me.ionar.salhack.util;

// Translation from https://en.wikipedia.org/wiki/Hilbert_curve

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HilbertCurve
{
    public static class Point
    {
        public int x;
        public int y;

        public Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public String toString()
        {
            return "(" + x + ", " + y + ")";
        }

        // rotate/flip a quadrant appropriately
        public void rot(int n, boolean rx, boolean ry)
        {
            if (!ry)
            {
                if (rx)
                {
                    x = (n - 1) - x;
                    y = (n - 1) - y;
                }

                // Swap x and y
                int t = x;
                x = y;
                y = t;
            }
        }

        public int calcD(int n)
        {
            boolean rx, ry;
            int d = 0;
            for (int s = n >>> 1; s > 0; s >>>= 1)
            {
                rx = ((x & s) != 0);
                ry = ((y & s) != 0);
                d += s * s * ((rx ? 3 : 0) ^ (ry ? 1 : 0));
                rot(s, rx, ry);
            }

            return d;
        }

    }

    public static Point fromD(int n, int d)
    {
        Point p = new Point(0, 0);
        boolean rx, ry;
        int t = d;
        for (int s = 1; s < n; s <<= 1)
        {
            rx = ((t & 2) != 0);
            ry = (((t ^ (rx ? 1 : 0)) & 1) != 0);
            p.rot(s, rx, ry);
            p.x += (rx ? s : 0);
            p.y += (ry ? s : 0);
            t >>>= 2;
        }
        return p;
    }

    public static List<Point> getPointsForCurve(int n)
    {
        List<Point> points = new ArrayList<Point>();
        for (int d = 0; d < (n * n); d++)
        {
            Point p = fromD(n, d);
            points.add(p);
        }

        return points;
    }
}
