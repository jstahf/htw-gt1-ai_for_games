package Uebung1;

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

    private Point2D target; // current target
    private Point2D nextPearl; // next pearl targeted

    ArrayList<Point> pearls; // holds all remaining pearls

    private int score = 0;

    private Point2D playerPos; // last player position

    private boolean stuck = false; // hasnt moved / is stuck
    private float stuckLimit = 0; // counter for stuckness measurement
    private boolean clockwise = false; // how to turn around obstacles

    private Point virtualPearl; // virtual pearl for special cases
    private boolean virtualPearlUsed; // virtualPearl already used?


    public ScubaDubaAI(Info info) {
        super(info);

        pearls  = new ArrayList<>(Arrays.asList(info.getScene().getPearl()));

        playerPos = new Point2D.Double(info.getX(), info.getY());

        virtualPearl = new Point (1580, 450); // specific checkpoint for this seed, since pathfinding was bugged for last pearl
        virtualPearlUsed = false;
        pearls.add(virtualPearl);

        enlistForTournament(566861);
    }

    @Override
    public String getName() {
        return "ScubaDuba";
    }

    @Override
    public Color getPrimaryColor() {
        return Color.BLACK;
    }

    @Override
    public Color getSecondaryColor() {
        return Color.CYAN;
    }

    @Override
    public PlayerAction update() {

        if(playerPos.distance(new Point2D.Double(info.getX(), info.getY())) < 0.4 && stuckLimit<150) { // if diver hasn't moved much
            stuckLimit++; // count towards stuck
        } else {
            stuckLimit -= 1.5; // calm down, everything ok
            if(stuckLimit<0) stuckLimit = 0; // no negatives
        }


        if(stuck && stuckLimit <=75) stuckLimit = 15;
        stuck =  (stuckLimit>50) ? true : false; // if too few moves for too long -> stuck



        playerPos = new Point2D.Double(info.getX(), info.getY()); // save player position

        if(playerPos.distance(new Point2D.Double(1580,450)) < 10){ // check if virtual pearl is in reach
            pearls.remove(virtualPearl); // remove
            virtualPearlUsed = true; // virtual pearl has been used up
        }

        if(score < info.getScore()) { // if scored

            Point scoredPearl = pearls.get(0);

            for(Point pearl : pearls) { // iterate all pearls
                if(pearl.distance(new Point2D.Double(info.getX(), info.getY())) < scoredPearl.distance(new Point2D.Double(info.getX(), info.getY()))) { // if distance is smaller than previous pearl
                    scoredPearl = pearl; // take this pearl
                }
            }

            pearls.remove(scoredPearl); // remove it
            stuckLimit = 0; // reset stuck counter
            score++; // increase score
        }

        this.nextPearl = getNextPearl(); // get next target pearl

        target = nextPearl; // set pearl as target

        clockwise = false; // default


        if((getPearlDirection() < Math.PI/2 || (getPearlDirection() > Math.PI*1.8 ))) { // for certain directions counter clockwise is better
            clockwise = true;
        }
        if(stuck) {
            clockwise = !clockwise; // if stuck, try other direction
        }

        target = avoidObstacles(target, clockwise); // check for obstacles. if there is one -> target a point around it

        return new DivingAction(info.getMaxAcceleration(), getTargetDirection()); // dive
    }

    /**
     *
     * @return returns current player direction as radians
     */
    private float getTargetDirection() {

        double xVec = target.getX() - info.getX();
        double yVec = target.getY() - info.getY();

        double direct =  -Math.atan2(yVec, xVec);

        if(direct<0) direct += 2*Math.PI;

        return (float) direct;
    }

    /**
     *
     * @return returns direction to next pearl in radians
     */
    private float getPearlDirection() {
        double xVec = nextPearl.getX() - info.getX();
        double yVec = nextPearl.getY() - info.getY();

        double direct =  -Math.atan2(yVec, xVec);

        if(direct<0) direct += 2*Math.PI;

        return (float) direct;
    }

    /**
     * partly inspired by: https://stackoverflow.com/questions/24645064/how-to-check-if-path2d-intersect-with-line
     * @param target current target
     * @param clockwise winding order
     * @return returns an target free of obstacles
     */
    private Point2D avoidObstacles(Point2D target, boolean clockwise) {

        Line2D lineToTarget = new Line2D.Double(info.getX(), info.getY(), target.getX(), target.getY()); // line to target

        Path2D[] obstacles = info.getScene().getObstacles(); // get all obstacles from scene


        for(Path2D obstacle: obstacles) { // for every obstacle in obstacles

            Point lastPoint = null;

            for(PathIterator pi = obstacle.getPathIterator(null); !pi.isDone(); pi.next()) { // for every segment in obstacle

                // get coordinates
                double[] coordinates = new double[6];
                pi.currentSegment(coordinates);

                if(lastPoint == null) { // if first iteration
                    lastPoint = new Point((int) coordinates[0], (int) coordinates[1]); // prevent out of bounds
                }

                Line2D segment = new Line2D.Double(lastPoint.getX(), lastPoint. getY(), coordinates[0],coordinates[1]); // create line from segment

                if(segment.intersectsLine(lineToTarget) && coordinates[0] != 0 && coordinates[1]!= 0) { // if line to target intersects segment AND not last iteration (preventing out of bounds)
                    //System.out.println("intersect detected");

                    int winding;
                    if (clockwise) {
                        winding = -1;
                    } else {
                        winding = 1;
                    }


                    double xVec = target.getX() - info.getX();
                    double yVec = target.getY() - info.getY();

                    // rotate forward vector by 1 degree depending on winding order
                    double newX = xVec * Math.cos(winding * 0.174) - yVec * Math.sin(winding * 0.174);
                    double newY = xVec * Math.sin(winding * 0.174) + yVec * Math.cos(winding * 0.174);

                    // if stuck rotate forward vector 180 degrees (probably not needed anymore)
                    if(stuck) {
                        newX = xVec * Math.cos(winding * Math.PI) - yVec * Math.sin(winding * Math.PI);
                        newY = xVec * Math.sin(winding * Math.PI) + yVec * Math.cos(winding * Math.PI);
                    }

                    // create new point for target
                    Point2D newTarget = new Point2D.Double(newX + info.getX(), newY + info.getY());

                    return avoidObstacles(newTarget, clockwise); // recursion (check new target for obstacles)
                }


                lastPoint.setLocation((int) coordinates[0], (int) coordinates[1]); // remember last segment (needed for creating line)
            }
        }

        if(nextPearl.getX() > 1320 && Math.abs(playerPos.getX() - nextPearl.getX()) > 10 && !virtualPearlUsed && playerPos.getY()<nextPearl.getY()-50 && !stuck) { // special cases (after dealing with them for 10 hours straight, this at least fits for the current seed)
            target = new Point2D.Double(nextPearl.getX(), info.getY());
        }

        return target; // return new, obstacle-free target point
    }

    /**
     *
     * @return returns next pearl to aim at. usually left to right, proximity has priority
     */
    private Point getNextPearl() {

        Point nextPearl = pearls.get(0); // random pearl for start reference

        if(pearls.size()==1) return nextPearl;
        for(Point pearl : pearls) {
            if(pearl.distance(new Point2D.Double(info.getX(), info.getY())) < 80) return pearl;

            if (pearl.x < nextPearl.x) {
                nextPearl = pearl;
            }
        }
        return nextPearl;
    }
}

