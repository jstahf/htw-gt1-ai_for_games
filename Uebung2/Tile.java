package s0566861;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Tile {

    private Rectangle2D rect;
    private Point2D middle;
    private double height;
    private double width;
    int tileIndex;

    private Tile parent = null;
    private boolean freespace = false;

    private double distanceToStart;
    private double estimatedDistance;


    public Tile(Rectangle2D rect, int widthFraction) {
        this.rect = rect;
        this.middle = new Point2D.Double(rect.getCenterX(), rect.getCenterY());
        this.height = rect.getHeight();
        this.width = rect.getWidth();
        this.tileIndex = (int) (middle.getX()/width) + (int) (middle.getY()/height) * widthFraction;
    }

    public void wipeData() {
        this.parent = null;
        this.estimatedDistance = 1000000;
        this.distanceToStart = 0;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Tile)) return false;
        if(middle.getY() == ((Tile) o).getMiddle().getY() && middle.getX() == ((Tile) o).getMiddle().getX()) return true;
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(tileIndex);
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setFreespace(boolean freespace) {
        this.freespace = freespace;
    }

    public boolean isFreespace() {
        return freespace;
    }

    public void setParent(Tile parent) {
        this.parent = parent;
    }

    public Tile getParent() {
        return parent;
    }

    public void setDistanceToStart(double distanceToStart) {
        this.distanceToStart = distanceToStart;
    }

    public double getDistanceToStart() {
        return distanceToStart;
    }

    public void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public double getEstimatedDistance() {
        return estimatedDistance;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Point2D getMiddle() {
        return middle;
    }

    public Rectangle2D getRect() {
        return rect;
    }

}
