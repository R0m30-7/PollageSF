package application.Utils;

public class Point {
    private double x, y;
    private Point position;
    
    public Point(double x, double y){
        this.x = x;
        this.y = y;
        this.position = new Point(x, y);
    }

    public Point add(double dx, double dy){
        return new Point(this.x + dx, this.y + dy);
    }
    
    public void setPosition(Point newPoint){
        this.position = newPoint;
    }
    
    public void setPosition(double x, double y){
        this.position = new Point(x, y);
    }

    public double getX(){
        return this.position.x;
    }

    public double getY(){
        return this.position.y;
    }
}
