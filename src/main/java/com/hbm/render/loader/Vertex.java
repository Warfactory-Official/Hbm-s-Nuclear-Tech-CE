package com.hbm.render.loader;

public class Vertex
{
    public float x, y, z;
    public int color = 0xFFFFFFFF;

    public Vertex(float x, float y)
    {
        this(x, y, 0F);
    }

    public Vertex(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
