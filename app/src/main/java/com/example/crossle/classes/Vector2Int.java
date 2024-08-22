package com.example.crossle.classes;

public class Vector2Int {
    private int x;
    private int y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int(String s) {
        String[] parts = s.split(":");
        this.x = Integer.parseInt(parts[0]);
        this.y = Integer.parseInt(parts[1]);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void add(Vector2Int other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void subtract(Vector2Int other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public void multiply(Vector2Int other) {
        this.x *= other.x;
        this.y *= other.y;
    }

    public void divide(Vector2Int other) {
        this.x /= other.x;
        this.y /= other.y;
    }

    public static Vector2Int ZERO() {
        return new Vector2Int(0, 0);
    }

    public static Vector2Int ONE() {
        return new Vector2Int(1, 1);
    }

    public static Vector2Int UP() {
        return new Vector2Int(0, 1);
    }

    public static Vector2Int DOWN() {
        return new Vector2Int(0, -1);
    }

    public static Vector2Int LEFT() {
        return new Vector2Int(-1, 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Vector2Int other = (Vector2Int) obj;

        if (this.x != other.x || this.y != other.y) {
            return false;
        }

        return true;
    }

}
