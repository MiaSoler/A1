package com.mireia.steering;

//"All steering behaviour calculations are implemented using LibGDX's Vector2D class to ensure efficient vector math operations."

public class Vector2D{
    public double posX, posY;

    public Vector2D(double posX, double posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public Vector2D add (Vector2D vector) {
        return new Vector2D(posX + vector.posX, posY + vector.posY);
    }
    public Vector2D subtract(Vector2D vector) {
        return new Vector2D(posX - vector.posX, posY - vector.posY);
    }

    public Vector2D multiply(double s) {
        return new Vector2D(posX * s, posY * s);
    }

    public double length() {
        return Math.sqrt(posX * posX + posY * posY);
    }

    public Vector2D normalize() {
        double len = length();
        return len > 0 ? new Vector2D(posX / len, posY / len) : new Vector2D(0, 0);
    }
    
}
