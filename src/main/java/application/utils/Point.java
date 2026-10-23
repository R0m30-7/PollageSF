package application.utils;

public class Point {
    private double x, y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public Point add(double dx, double dy){
        return new Point(this.x + dx, this.y + dy);
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }
    
    public void setPosition(Point newPoint){
        this.x = newPoint.getX();
        this.y = newPoint.getY();
    }
    
    public void setPosition(double x, double y){
        this.x = x;
        this.y = y;
    }
}
