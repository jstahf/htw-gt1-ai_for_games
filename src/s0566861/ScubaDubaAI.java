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

    private ArrayList<Point> pearls; // holds all remaining pearls

    private Point2D target; // current target
    private Point2D nextPearl; // next pearl targeted
    private final boolean leftFirst;

    private final int sceneWidth = info.getScene().getWidth();
    private final int sceneHeight = info.getScene().getHeight();
    private final int widthFraction = 400;
    private final int heightFraction;
    private int tileWidth;
    private int tileHeight;

    private Tile[] tiles;

    private int score = 0;

    private Point2D playerPos; // last player position

    private List<Tile> playerPath;

    private int frameCount = 0;
    private boolean aboutToBreath = false;
    private boolean breathed = false;
    private boolean abovePearl = true;



    public ScubaDubaAI(Info info) {
        super(info);

        this.pearls = new ArrayList<>(Arrays.asList(info.getScene().getPearl()));
        this.playerPos = new Point2D.Double(info.getX(), info.getY());


        this.heightFraction = this.widthFraction * this.sceneHeight / this.sceneWidth;

        this.tiles = createTiles();

        int meanPearlX = 0;

        for(Point2D pearl : pearls) {
            meanPearlX += pearl.getX()/pearls.size();
        }

        this.leftFirst = (meanPearlX>playerPos.getX()) ? true : false;

        this.nextPearl = getNextPearl(); // get next target pearl
        this.target = this.nextPearl;

        this.playerPath = findPath(playerPos, target);
        checkOxygen(playerPath);

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

        gfx.setStroke(new BasicStroke(1f));

        if(playerPath!=null) {
            for (Tile t : playerPath) {
                gfx.drawRect((int) t.getMiddle().getX(), (int) t.getMiddle().getY(), tileWidth, tileHeight);
            }
        }
    }


    @Override
    public PlayerAction update() {

        playerPos = new Point2D.Double(info.getX(), info.getY()); // save player position

        if(aboutToBreath && info.getAir() == info.getMaxAir()) {
            breathed = true;
            System.out.println("breathed");
            aboutToBreath = false;
        }

        if (score < info.getScore() || breathed) { // if scored

            if(!breathed) {
                removePearl();
                score++; // increase score
            }
            for(Tile t : playerPath) {
                t.wipeData();
            }
            this.nextPearl = getNextPearl(); // get next target pearl
            this.playerPath = findPath(playerPos, nextPearl);
            breathed = false;
            checkOxygen(playerPath);
        }

        if(!abovePearl && Math.abs(nextPearl.getX()-playerPos.getX()) <10) {
            abovePearl = true;
            this.nextPearl = getNextPearl(); // get next target pearl
            this.playerPath = findPath(playerPos, nextPearl);
        }

/*
        if(frameCount>20) {
            //System.out.println(info.getAir());
            frameCount = 0;
        }
        frameCount++;*/

        //this.nextPearl = getNextPearl(); // get next target pearl
        this.target = nextPearl;

        DivingAction action = dive();

        return action;
    }

    private void checkOxygen(List<Tile> pp) {
        Point2D breathingPoint = new Point2D.Double(nextPearl.getX(), 45);



        int pathLength = pp.size() * tileHeight + findPath(this.nextPearl, breathingPoint).size()*tileHeight;

        if(pearls.size()<5) {
            pathLength = getAllDistance();
        }

        System.out.println("PathL: " + pathLength + " Duration: " + pathLength / info.getMaxVelocity() + " Air: " + info.getAir());

        if(pathLength / info.getMaxVelocity() > info.getAir() && info.getAir() != info.getMaxAir()) {

            this.nextPearl = new Point2D.Double(playerPos.getX(), 45);
            this.playerPath = findPath(playerPos, nextPearl);
            System.out.println("going up");
            aboutToBreath = true;

        } else if (info.getAir() == info.getMaxAir()) {

            this.nextPearl = new Point2D.Double(nextPearl.getX(), 45);
            this.playerPath = findPath(playerPos, nextPearl);
            System.out.println("going above");
            abovePearl = false;
        }
    }

    private int getAllDistance() {
        ArrayList<Point> remainPearls = (ArrayList<Point>) pearls.clone();

        remainPearls.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if(leftFirst){
                    if(o1.getX() < o2.getX()) return 0;
                } else {
                    if(o1.getX() > o2.getX()) return 0;
                }
                return 1;

            }
        });

        int allPath = 0;

        for(int i = 1; i<remainPearls.size(); i++) {
            allPath += findPath(remainPearls.get(i-1), remainPearls.get(i)).size()*tileHeight;
        }

        return allPath;
    }

    /**
     *  Checks for obstacles and returns a new diving call
     * @return DivingAction with max speed and direction to current target
     */
    private DivingAction dive() {

        target = avoidObstacles(target, new ArrayList<>(playerPath));

        return new DivingAction(info.getMaxAcceleration(), getTargetDirection(target)); // dive;
    }

    /**
     * Checks if obstacles are in the way. If so the furthest element of path gets selected as new target.
     *
     * @param target current target
     * @param path current path as Tile ArrayList
     * @return returns an target free of obstacles
     */
    private Point2D avoidObstacles(Point2D target, ArrayList<Tile> path) {
        Line2D lineToTarget = new Line2D.Double(info.getX(), info.getY(), target.getX(), target.getY()); // line to target

        for(Tile t : tiles) {
            if(t.isFreespace()) continue;
            if(t.getRect().intersectsLine(lineToTarget)) {
                Point2D newTarget = path.get(path.size()-1).getMiddle();
                path.remove(path.get(path.size()-1));

                return avoidObstacles(newTarget, path);
            }
        }
        return  target;
    }


    /**
     * - Placeholder method -
     * @param newTarget Target as Graphic Point
     * @return ArrayList<Tile> with index 0 as start and list.length-1 as goal
     */
    private List<Tile> findPath(Point2D start, Point2D newTarget) {

        List<Tile> newPath;

        newPath = findNode(start, newTarget);

        return newPath;
    }

    /**
     * Basic A* Algorithm
     * @param startPos Graphic Point as Start
     * @param endPos Graphic Point as goal
     * @return ArrayList<Tile> with index 0 as start and list.length-1 as goal
     */
    private List<Tile> findNode(Point2D startPos, Point2D endPos) {


        Tile start = tiles[(int) (startPos.getX() / tileWidth) + (int) (startPos.getY() / tileHeight) * widthFraction];
        Tile end = tiles[(int) (endPos.getX() / tileWidth) + (int) (endPos.getY() / tileHeight) * widthFraction];

        HashSet<Tile> closed = new HashSet<>();
        Queue<Tile> open = new PriorityQueue<>(heightFraction * widthFraction, (o1, o2) -> {
            if (o1.getEstimatedDistance() > o2.getEstimatedDistance()) return 1;
            return 0;
        });

        start.setDistanceToStart(0.0);
        open.add(start);

        Tile current;

        while (!open.isEmpty()) {

            current = open.remove();
            if (current.getMiddle().distance(end.getMiddle()) < 10) return reconstructPath(start, current);

            for (Tile neighbour : getNeighbors(current)) {

                if (closed.contains(neighbour) || open.contains(neighbour)) continue;

                neighbour.setParent(current);

                double distanceToStart = current.getDistanceToStart() + neighbour.getMiddle().distance(current.getMiddle());
                double totalDistance = distanceToStart + neighbour.getMiddle().distance(end.getMiddle());

                neighbour.setDistanceToStart(distanceToStart);
                neighbour.setEstimatedDistance(totalDistance);

                open.add(neighbour);
            }
            closed.add(current);
        }

        // UNREACHABLE! -> Switch to next Pearl instead
        pearls.remove(nextPearl);
        nextPearl = getNextPearl();
        return findNode(startPos, nextPearl);
    }

    /**
     *  Recursively reconstructs the path from goal to start and returns it as ArrayList<Tile>
     * @param start starting Tile
     * @param goal finish Tile
     * @return ArrayList<Tile> with index 0 as start and list.length-1 as goal
     */
    private List<Tile> reconstructPath(Tile start, Tile goal) {
        // construct output list
        List<Tile> path = new ArrayList<>();
        Tile currNode = goal;
        while (!currNode.equals(start)) {
            path.add(0, currNode);
            currNode = currNode.getParent();
        }
        path.add(0, start);
        return path;
    }

    /**
     *  Returns all 4 neighbours with distance = 1 tile
     * @param current current tile
     * @return neighbours as HashSet<Tile>
     */
    private Set<Tile> getNeighbors(Tile current) {
        Set<Tile> neighbours = new HashSet<>();

        int tileIndex = current.getTileIndex();

        if (tileIndex - 1 >= 0 && tiles[tileIndex - 1].isFreespace()) {
            neighbours.add(tiles[tileIndex - 1]);
        }
        if (tileIndex + 1 < tiles.length && tiles[tileIndex + 1].isFreespace()) {
            neighbours.add(tiles[tileIndex + 1]);
        }
        if (tileIndex - widthFraction >= 0 && tiles[tileIndex - widthFraction].isFreespace()) {
            neighbours.add(tiles[tileIndex - widthFraction]);
        }
        if (tileIndex + widthFraction < tiles.length && tiles[tileIndex + widthFraction].isFreespace()) {
            neighbours.add(tiles[tileIndex + widthFraction]);
        }

        return neighbours;
    }

    /** creates a 2D tilemap stored in a 1D-Tile Array. Index from upper left to bottom right corner.
     *
     * @return Tiles in order: x+widthFraction*y
     */
    private Tile[] createTiles() {

        Tile[] tileMap = new Tile[widthFraction * heightFraction];

        tileWidth = info.getScene().getWidth() / widthFraction;
        tileHeight = info.getScene().getHeight() / heightFraction;

        Path2D[] obstacles = info.getScene().getObstacles(); // get all obstacles from scene

        for (int i = 0; i < widthFraction; i++) {
            for (int j = 0; j < heightFraction; j++) {

                int index = j * widthFraction + i;
                tileMap[index] = new Tile(new Rectangle2D.Double(i * sceneWidth / widthFraction, j * sceneHeight / heightFraction, tileWidth, tileHeight), widthFraction);

                for (Path2D obstacle : obstacles) {

                    if (obstacle.intersects(tileMap[index].getRect())) {
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

    /**
     * @return returns next pearl to aim at. usually left to right, close proximity has priority
     */
    private Point getNextPearl() {

        Point nextPearl = pearls.get(0); // random pearl for start reference

        if (pearls.size() == 1) return nextPearl;
        for (Point pearl : pearls) {

            if (pearl.distance(playerPos) < 50) return pearl;

            if(leftFirst) {
                if (pearl.x < nextPearl.x) {
                    nextPearl = pearl;
                }
            } else {
                if (pearl.x > nextPearl.x) {
                    nextPearl = pearl;
                }
            }
        }
        return nextPearl;
    }

    private void removePearl() {
        Point scoredPearl = pearls.get(0);

        for (Point pearl : pearls) { // iterate all pearls
            if (pearl.distance(new Point2D.Double(info.getX(), info.getY())) < scoredPearl.distance(new Point2D.Double(info.getX(), info.getY()))) { // if distance is smaller than previous pearl
                scoredPearl = pearl; // take this pearl
            }
        }

        pearls.remove(scoredPearl); // remove it
    }

    /**
     * @return returns current player direction as radians
     */
    private float getTargetDirection(Point2D target) {

        double xVec = target.getX() - info.getX();
        double yVec = target.getY() - info.getY();

        double direct = -Math.atan2(yVec, xVec);

        if (direct < 0) direct += 2 * Math.PI;

        return (float) direct;
    }
}
