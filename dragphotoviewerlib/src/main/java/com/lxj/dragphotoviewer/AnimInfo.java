package com.lxj.dragphotoviewer;

public class AnimInfo {
    public int x, y;
    public int width, height;
    public AnimInfo(){}
    public AnimInfo(int x, int y,
                    int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "AnimInfo{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
