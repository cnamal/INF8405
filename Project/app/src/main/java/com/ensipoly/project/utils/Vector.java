package com.ensipoly.project.utils;

/**
 * Created by Adrien on 23/04/2017.
 */

public class Vector {

    public double x,y,z;

    public Vector(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(Vector copy){
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
    }

    public static double dot(Vector a, Vector b){
        return a.x*b.x + a.y*b.y + a.z*b.z;
    }

    public static double norm(Vector v){
        return Math.sqrt(Vector.dot(v,v));
    }

    public Vector add(Vector v){
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vector mult(double d){
        this.x *= d;
        this.y *= d;
        this.z *= d;
        return this;
    }

    public Vector normalize(){
        double norm = Vector.norm(this);
        if(norm == 0)
            return this;
        this.x /= norm;
        this.y /= norm;
        this.z /= norm;
        return this;
    }

    public static Vector sum(Vector[] list){
        Vector res = new Vector(0,0,0);
        for (Vector v: list) {
            res.add(v);
        }
        return res;
    }
}
