package s0566861;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class ScubaDubaAI extends AI {

    private Point2D target; // current target
    private Point2D nextPearl; // next pearl targeted

    ArrayList<Point> pearls; // holds all remaining pearls



    private int sceneWidth = info.getScene().getWidth();
    private int sceneHeight = info.getScene().getHeight();
    private int widthFraction = 200;
    private int heightFraction;
    private int tileWidth;
    private int tileHeight;

    private Tile[] tiles;
    private Boolean[] checkedTiles;

    private int score = 0;

    private Point2D playerPos; // last player position

    private List<Tile> playerPath;



    private boolean stuck = false; // hasnt moved / is stuck
    private float stuckLimit = 0; // counter for stuckness measurement
    private boolean clockwise = false; // how to turn around obstacles



    public ScubaDubaAI(Info info) {
        super(info);

        pearls  = new ArrayList<>(Arrays.asList(info.getScene().getPearl()));
        playerPos = new Point2D.Double(info.getX(), info.getY());


        heightFraction = widthFraction*sceneHeight/sceneWidth;

        tiles = createTiles();

        playerPath = findPath();
        System.out.println(playerPath);

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
    public void drawDebugStuff(Graphics2D gfx) {
        gfx.setColor(Color.red);
        gfx.drawLine((int) playerPos.getX(), (int) playerPos.getY(), (int) target.getX(), (int) target.getY());


        for(int i = 0; i<tiles.length; i++) {

            gfx.setColor(Color.red);
            gfx.setStroke(new BasicStroke(1f));

            for(Tile t : playerPath) {
                gfx.drawOval((int) t.getMiddle().getX(), (int) t.getMiddle().getY(), 10, 10);
            }
            gfx.setStroke(new BasicStroke(0.2f));
            if(tiles[i].isFreespace()){
                gfx.setColor(Color.green);

            } else {
                gfx.setColor(Color.yellow);
            }



            gfx.draw(tiles[i].getRect());
            gfx.drawOval((int) tiles[i].getMiddle().getX(), (int) tiles[i].getMiddle().getY(), 2, 2);
        }
    }

    @Override
    public PlayerAction update() {

        DivingAction action = oldBehaviour();
        return action;
    }

    private Tile[] createTiles() {

        Tile[] tileMap = new Tile[widthFraction*heightFraction];

        tileWidth = info.getScene().getWidth()/widthFraction;
        tileHeight = info.getScene().getHeight()/heightFraction;

        Path2D[] obstacles = info.getScene().getObstacles(); // get all obstacles from scene

        for(int i = 0; i<widthFraction; i++) {
            for(int j = 0; j<heightFraction; j++) {

                int index = j*widthFraction+i;
                tileMap[index] = new Tile(new Rectangle2D.Double(i*sceneWidth/widthFraction, j*sceneHeight/heightFraction, tileWidth, tileHeight), widthFraction);

                for(Path2D obstacle : obstacles) {

                    if(obstacle.intersects(tileMap[index].getRect())) {
                        tileMap[index].setFreespace(false);
                        break;
                    } else {
                        tileMap[index].setFreespace(true);
                    }
                }
            }
        }

        return tileMap;
    }

    private List<Tile> findPath() {

        List<Tile> newPath;

        Point2D start = playerPos;

        newPath = findNode(start, pearls.get(0));

        newPath = flattenPath(newPath);

        return newPath;
    }

    private List<Tile> flattenPath(List<Tile> newPath) {

    }

    private List<Tile> findNode(Point2D startPos, Point2D endPos) {


        Tile start = tiles[(int) (startPos.getX()/tileWidth) + (int) (startPos.getY()/tileHeight) * widthFraction];
        Tile end = tiles[(int) (endPos.getX()/tileWidth) + (int) (endPos.getY()/tileHeight) * widthFraction];

        HashSet<Tile> closed = new HashSet<>();
        Queue<Tile> open = new PriorityQueue<>(heightFraction * widthFraction, (o1, o2) -> {
            if(o1.getEstimatedDistance() > o2.getEstimatedDistance()) return 1;
            return 0;
        });

        start.setDistanceToStart(0.0);
        open.add(start);

        Tile current;

        while (!open.isEmpty()) {

            current = open.remove();
            if (current.getMiddle().distance(end.getMiddle())<10) return reconstructPath(start, current);

            for(Tile neighbour : getNeighbors(current)) {

                if(closed.contains(neighbour) || open.contains(neighbour)) continue;

                neighbour.setParent(current);

                double distanceToStart = current.getDistanceToStart() + neighbour.getMiddle().distance(current.getMiddle());
                double totalDistance = distanceToStart + neighbour.getMiddle().distance(end.getMiddle());

                neighbour.setDistanceToStart(distanceToStart);
                neighbour.setEstimatedDistance(totalDistance);

                open.add(neighbour);
            }
            closed.add(current);
        }

        return null;
    }

    private List<Tile> reconstructPath(Tile start, Tile goal) {
        // construct output list
        LinkedList<Tile> path = new LinkedList<>();
        Tile currNode = goal;
        while(!currNode.equals(start)){
            path.addFirst(currNode);
            currNode = currNode.getParent();
        }
        path.addFirst(start);
        return path;
    }

    private Set<Tile> getNeighbors(Tile current) {
        Set<Tile> neighbours = new HashSet<>();

        int tileIndex = current.getTileIndex();

        if(tileIndex-1 >= 0 && tiles[tileIndex-1].isFreespace())  {
            neighbours.add(tiles[tileIndex-1]);
        }
        if(tileIndex+1 < tiles.length && tiles[tileIndex+1].isFreespace()) {
            neighbours.add(tiles[tileIndex+1]);
        }
        if(tileIndex-widthFraction >= 0 && tiles[tileIndex-widthFraction].isFreespace()) {
            neighbours.add(tiles[tileIndex-widthFraction]);
        }
        if(tileIndex+widthFraction < tiles.length && tiles[tileIndex+widthFraction].isFreespace()) {
            neighbours.add(tiles[tileIndex+widthFraction]);
        }

        return neighbours;
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
     * Old behaviour from Tournament 1
     * @return Divingaction based on old behaviour
     */
    private DivingAction oldBehaviour() {

        if(playerPos.distance(new Point2D.Double(info.getX(), info.getY())) < 0.4 && stuckLimit<150) { // if diver hasn't moved much
            stuckLimit++; // count towards stuck
        } else {
            stuckLimit -= 1.5; // calm down, everything ok
            if(stuckLimit<0) stuckLimit = 0; // no negatives
        }


        if(stuck && stuckLimit <=75) stuckLimit = 15;
        stuck =  (stuckLimit>50) ? true : false; // if too few moves for too long -> stuck



        playerPos = new Point2D.Double(info.getX(), info.getY()); // save player position


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

        return target; // return new, obstacle-free target point
    }
}

