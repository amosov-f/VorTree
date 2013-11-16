import java.util.List;

public class Rectangle {
    private Point minVertex;
    private Point maxVertex;

    public Rectangle(final List<Point> points) {
        minVertex = points.get(0);
        maxVertex = points.get(0);
        for (Point p : points) {
            minVertex = minVertex.min(p);
            maxVertex = maxVertex.max(p);
        }
    }

    public double getX() {
        return minVertex.get(0);
    }

    public double getY() {
        return minVertex.get(1);
    }

    public double getWidth() {
        return (maxVertex.get(0) - minVertex.get(0));
    }

    public double getHeight() {
        return (maxVertex.get(1) - minVertex.get(1));
    }

    public Point getMinVertex() {
        return minVertex;
    }

    public Point getMaxVertex() {
        return maxVertex;
    }

    @Override
    public String toString() {
        return "Rectangle(" +
                "minVertex = " + minVertex +
                ", maxVertex = " + maxVertex +
                ')';
    }
}
