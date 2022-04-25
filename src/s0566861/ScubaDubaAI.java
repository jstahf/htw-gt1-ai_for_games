package s0566861;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

public class ScubaDubaAI extends AI {

    private Point2D target;
    private Point2D nextPearl;
    private int score = 0;
    ArrayList<Point> pearls = new ArrayList<>(Arrays.asList(info.getScene().getPearl()));


    public ScubaDubaAI(Info info) {
        super(info);

        enlistForTournament(566861);
    }

    @Override
    public String getName() {
        return "ScubaDuba";
    }

    @Override
    public Color getPrimaryColor() {
        return Color.black;
    }

    @Override
    public Color getSecondaryColor() {
        return Color.MAGENTA;
    }

    @Override
    public PlayerAction update() {

        if(score < info.getScore()) {

            Point scoredPearl = pearls.get(0);

            for(Point pearl : pearls) {
                if(pearl.distance(new Point2D.Double(info.getX(), info.getY())) < scoredPearl.distance(new Point2D.Double(info.getX(), info.getY()))) {
                    scoredPearl = pearl;
                }
            }

            pearls.remove(scoredPearl);
            score++;
        }

        this.nextPearl = getNextPearl();

        target = nextPearl;


        boolean clockwise = false;

        if(getTargetDirection()-getPearlDirection() < 0) {
            clockwise = true;
        }

        //System.out.println(clockwise + "" + getTargetDirection());
        target = avoidObstacles(target, clockwise);

        System.out.println(clockwise);
        return new DivingAction(info.getMaxAcceleration(), getTargetDirection());
    }

    private float getTargetDirection() {

        double xVec = target.getX() - info.getX();
        double yVec = target.getY() - info.getY();

        double direct =  -Math.atan2(yVec, xVec);


        return (float) direct;
    }

    private float getPearlDirection() {
        double xVec = nextPearl.getX() - info.getX();
        double yVec = nextPearl.getY() - info.getY();

        double direct =  -Math.atan2(yVec, xVec);

        return (float) direct;
    }

    private Point2D avoidObstacles(Point2D target, boolean clockwise) {

        Line2D lineToTarget = new Line2D.Double(info.getX(), info.getY(), target.getX(), target.getY());

        Path2D[] obstacles = info.getScene().getObstacles();

        for(Path2D obstacle: obstacles) {

            Point lastPoint = null;
            for(PathIterator pi = obstacle.getPathIterator(null); !pi.isDone(); pi.next()) {

                double[] coordinates = new double[6];
                pi.currentSegment(coordinates);

                if(lastPoint == null) {
                    lastPoint = new Point((int) coordinates[0], (int) coordinates[1]);
                }

                Line2D segment = new Line2D.Double(lastPoint.getX(), lastPoint. getY(), coordinates[0],coordinates[1]);

                if(segment.intersectsLine(lineToTarget) && coordinates[0] != 0 && coordinates[1]!= 0) {
                    //System.out.println("intersect detected");

                    int winding;
                    if(clockwise) {
                        winding = -1;
                    } else {
                        winding = 1;
                    }

                    double xVec = target.getX() - info.getX();
                    double yVec = target.getY() - info.getY();

                    double newX = xVec * Math.cos(winding*0.0174) - yVec * Math.sin(winding*0.0174);
                    double newY = xVec * Math.sin(winding*0.0174) + yVec * Math.cos(winding*0.0174);

                    Point2D newTarget = new Point2D.Double(newX + info.getX(), newY + info.getY());

                    return avoidObstacles(newTarget, clockwise);
                }


                lastPoint.setLocation((int) coordinates[0], (int) coordinates[1]);
            }
        }
        return target;
    }

    private Point getNextPearl() {

        Point nextPearl = pearls.get(0); // random pearl for start reference

        for(Point pearl : pearls) {
            if(pearl.distance(new Point2D.Double(info.getX(), info.getY())) < 20) {
                return pearl;
            }
            if (pearl.x < nextPearl.x) {
                nextPearl = pearl;
            }
        }
        return nextPearl;
    }
}
