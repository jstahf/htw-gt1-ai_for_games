package s0575695;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class BestAi extends AI {

	final int AVOID_TIME = 120;
	int currPearlIndex;
	int currScore;
	int avoidTime = 0;
	int offsetAngle = 0;
	Point center;
	Point currPearl;
	ArrayList<Point> remainingPearls;
	Vector currDirection;
	float currentAngle;
	int passedTime =0;
	List<Point2D> currPath;
	ArrayList<Point2D> reflexCorners;
	Point2D currTarget;
	GraphNode currNode;
	Graph graphy;
	List<Point2D> aStarPath;
	Point2D startPos;
	public BestAi(Info info) {
		super(info);
		currPearlIndex = 0;
		currScore = 0;
		constructGraph();
		remainingPearls = new ArrayList<>();
		for(Point point : info.getScene().getPearl())remainingPearls.add(point);
		currPearl = getClosestPoint(new Point(0, info.getScene().getHeight() /2));
		currDirection = new Vector(0, 0); 

		
		enlistForTournament(575695);
		//Point2D test = reflexCorners.remove(0);
		GraphNode testa = new GraphNode(new Float(info.getX(), info.getY()), reflexCorners);
		currNode = testa;
		//Graph graph = new Graph(reflexCorners);
		//Point2D point = testa.edges.get(4);
		Point2D currPos = new Float(info.getX(), info.getY());
		for(int i = 0; i < reflexCorners.size(); i++) {
			for(int f = i; f < reflexCorners.size(); f++) {
				if(reflexCorners.get(i).distance(reflexCorners.get(f)) < 15) reflexCorners.remove(f);
			}
		}
		
		System.out.println(reflexCorners.size());
		//currTarget =new Point((int)point.getX(), (int) point.getY());
		for(Point2D point1 : info.getScene().getPearl())reflexCorners.add(point1);
		Graph graph = new Graph(reflexCorners);
		GraphNode node = new GraphNode(currPos, reflexCorners);
		Area obstacleArea = new Area();
		for(Path2D path : info.getScene().getObstacles()) 
			obstacleArea.add(new Area(path.createTransformedShape(new AffineTransform())));
	
		graphy = graph;
		node.addOneWayTransitions(graphy.nodes, obstacleArea);
		List<Point2D> path = graph.constructPathAStar(node, currPearl);
		
		startPos = currPos;
		if(path != null) {
			System.out.println(path.size());
			aStarPath = path;
			currTarget = path.get(path.size() - 1);
			
		}else {
			currTarget = currPearl;
		}
	
		
	}
	
	@Override
	public String getName() {
		return "A* Pathfinding";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.RED;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.BLACK;
	}
	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		if(aStarPath != null) {
			gfx.setColor(Color.red);
			for(Point2D pointy : aStarPath) {
				gfx.drawOval((int)pointy.getX(), (int)pointy.getY(), 5, 5);
			}
		}else {
			//System.out.println("no path right now");
		}
		gfx.setColor(Color.green);
		for(Point2D point : reflexCorners) {
			gfx.drawOval((int)point.getX(), (int)point.getY(), 5, 5);
		}
		GraphNode base = graphy.nodes.get(33);
	
		//gfx.drawOval((int)base.point.getX(), (int)base.point.getY(), 5, 5);
		gfx.setColor(Color.red);
		for(GraphNode to: base.transitions) {
			//gfx.drawLine((int) base.point.getX(), (int) base.point.getY(),(int)  to.point.getX(),(int)  to.point.getY());
		}
		//Point2D postion = currNode.point;
		//GraphNode test = graphy.getGraphNode(currPearl);
		//Point2D testPos = test.point;
		//for(Point2D edge : test.edges) {
		//	gfx.drawLine((int)testPos.getX(), (int)testPos.getY(), (int)edge.getX(), (int)edge.getY());
		//}
		
	}
	@Override
	public PlayerAction update() {
		
		//Point[] pearls = info.getScene().getPearl();
		Point position = new Point((int)info.getX(),(int) info.getY());
		if(info.getScore() > currScore) {
			removePearl(position);
			currScore = info.getScore();
			currPearl = getClosestPoint(position);
			
			Point2D currPos = new Float(info.getX(), info.getY());
			GraphNode node = new GraphNode(currPos, reflexCorners);
			Area obstacleArea = new Area();
			for(Path2D path : info.getScene().getObstacles()) 
				obstacleArea.add(new Area(path.createTransformedShape(new AffineTransform())));
		
			node.addOneWayTransitions(graphy.nodes, obstacleArea);
			List<Point2D> path = graphy.constructPathAStar(node, currPearl);
			if(path != null) {
				//System.out.println(path.size());
				aStarPath = path;
				currTarget = path.get(path.size() - 1);
				
			}else {
				System.out.println("did not find path");
				return new DivingAction(info.getMaxAcceleration(), (float)Math.random());
			};
			
		}
		if(position.distance(currTarget) < 2) {
			if(aStarPath.size() != 1) {
				aStarPath.remove(aStarPath.size() - 1);
				currTarget = aStarPath.get(aStarPath.size()-1);
			}else {
				currTarget = currPearl;
			}
		}
		//if(rayCast2(position, seek(currPearl).normalize(), currPearl)) {
			//Vector direction = seek(currPearl);
			//float angle = (float) Math.atan2(direction.y, direction.x);
			//return new DivingAction(info.getMaxAcceleration(), -angle);
		//}
		Vector direction = seek(currTarget);
		direction.normalize();
		
		float angle = (float) Math.atan2(direction.y, direction.x);
		currentAngle = angle;
		return new DivingAction(info.getMaxAcceleration(), -angle);
		
		
	}
	private Vector seek(Point2D target) {
		return new Vector(target.getX() - info.getX(), target.getY() - info.getY());
	}
	private Point flee(Point target) {
		return new Point((int) -(target.x - info.getX()), (int) (target.y - info.getY()));
	}
	private float getLen(Point point) {
		return (float) Math.sqrt(Math.pow(point.getX(), 2) + Math.pow(point.getY(), 2));
	}
	private void removePearl(Point playerPos) {
		Point ClosestPearl = getClosestPoint(playerPos);
		remainingPearls.remove(ClosestPearl);
	}
	private Point getClosestPoint(Point playerPos) {
		Point ClosestPearl = remainingPearls.get(0);
		for(Point pearl: remainingPearls) {
			if(playerPos.distance(pearl) < playerPos.distance(ClosestPearl))ClosestPearl = pearl;
		}
		return ClosestPearl;
	}
	private Point normalize(Point toNormalize) {
		float len = getLen(toNormalize);
		return new Point((int)( toNormalize.getX() / len), (int) (toNormalize.getY() / len));
	}
	private int rayCast(Point origin, double directionX, double directionY) {
		for (int i = 0; i < 500; i++) {
			for(Path2D path : info.getScene().getObstacles()) {
				if(path.contains(origin.x + i * directionX, origin.y + i * directionY )) return i;
			}
		}
		return 5;
	}
	private boolean rayCast2(Point origin, Vector direction, Point target) {
		Line2D pathToTarget = new Line2D.Double(origin.x, origin.y, target.x, target.y);
		
		
	
		for(Path2D path : info.getScene().getObstacles()) {
			Point2D lastPoint = null;
			for(PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
				double[] coordinates = new double[6];
                pi.currentSegment(coordinates);
                if(lastPoint == null) {
                    lastPoint = new Point((int) coordinates[0], (int) coordinates[1]);
                }
                Line2D segment = new Line2D.Double(lastPoint.getX(), lastPoint. getY(), coordinates[0],coordinates[1]);
                lastPoint.setLocation((int) coordinates[0], (int) coordinates[1]);
                if(coordinates[0] != 0 && coordinates[1] != 0 && pathToTarget.intersectsLine(segment)) {
                	return false;
                }
		}
		}
		return true;
	}

	private void constructGraph() {
		Path2D[] obstacles = info.getScene().getObstacles();
		Area[] areas = new Area[obstacles.length];
		ArrayList<Point2D> reflexCorners = new ArrayList<>();
		for(int i = 0; i < obstacles.length; i++)areas[i] = new Area(obstacles[i]);
		int amountOfPoints = 0;
		for(Path2D obstacle : obstacles) {
			PathIterator it = obstacle.getPathIterator(null);
			Point2D prevPoint = new Double(0, 0);
			Point2D currPoint = new Double(0,0);
			Point2D nextPoint = new Double(0,0);
			for(; !it.isDone(); it.next()) {
				float[] coord = new float[2];

				it.currentSegment(coord);
				nextPoint = new Float(coord[0], coord[1]);
				if(isReflexCorner(currPoint, prevPoint, nextPoint)) {
					Vector normal1  =new Vector(-(currPoint.getY() - prevPoint.getY()),currPoint.getX() - prevPoint.getX()).normalize();
					Vector normal2  =new Vector(-(nextPoint.getY() - currPoint.getY()),nextPoint.getX() - currPoint.getX()).normalize();
					Vector toAdd = normal1.add(normal2).scale(0.5f).normalize().scale(25);
					Point2D movedPoint = new Double(currPoint.getX() - toAdd.x, currPoint.getY() - toAdd.y);
					reflexCorners.add(movedPoint);
				}
				prevPoint = currPoint;
				currPoint = nextPoint;
				amountOfPoints++;
			}
		}
		System.out.println(amountOfPoints);
		System.out.println(reflexCorners.size());
		this.reflexCorners = reflexCorners;
	}
	private boolean isReflexCorner(Point2D toTest, Point2D prev, Point2D next) {
		Vector aTob = new Vector((float) (toTest.getX() - prev.getX()), (float) (toTest.getY() - prev.getY()));
		Vector aToC = new Vector((float) (next.getX() - toTest.getX()), (float) (next.getY() - toTest.getY()));
		//	Vector aToC = new Vector((float) (next.getX() - prev.getX()), (float) (next.getY() - prev.getY())); 
		aTob = new Vector(-aTob.y, aToC.x);
		return aToC.dotProdut(aTob) < 0;
	}
	
	private class Vector{
		public float x;
		public float y;
		@Override
		public String toString() {
			return "Vector [x=" + x + ", y=" + y + "]";
		}
		public Vector(float x, float y) {
			this.x = x;
			this.y = y;
		}
		public Vector(double x, double y) {
			this.x = (float)x;
			this.y = (float)y;
		}
		public Vector add(Vector toAdd) {
			this.x += toAdd.x;
			this.y += toAdd.y;
			return this;
		}
		public float getLen() {
			return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		}
		public double getX() {
			return x;
		}
		public double getY() {
			return y;
		}
		public Vector normalize() {
			float len = getLen();
			x /= len;
			y /= len;
			return this;
		}
		public Vector scale(float scalar) {
			return new Vector(x* scalar,y * scalar);
		}
		public float dotProdut(Vector toMulitply) {
			return this.x * toMulitply.x + this.y * toMulitply.y; 
		}
		
	}

	private class Graph {
		public ArrayList<GraphNode> nodes;
		public Graph(ArrayList<Point2D> points){
			Area obstacleArea = new Area();
			for(Path2D pathing : info.getScene().getObstacles()) 
				obstacleArea.add(new Area(pathing.createTransformedShape(new AffineTransform())));
			nodes = new ArrayList<>();
			
			for (int i = 0; i < points.size() ; i++) {
				Point2D curr = points.get(i);
				GraphNode toAdd = new GraphNode(curr, points);
				//for(GraphNode node : nodes) {
					//if(node.edges.contains(toAdd.point))toAdd.edges.add(node.point);
				//}
				nodes.add(toAdd);
				
			}
			 ArrayList<GraphNode> clone = (ArrayList<GraphNode>) nodes.clone();
			for(GraphNode node : nodes) {
				//clone.remove(node);
				node.addTransitions(nodes, obstacleArea);
				//System.out.println(node.transitions.size());
				
			}
			
	
		}
		public List<Point2D> constructPathAStar(GraphNode start, Point2D target) {
			PointComperator comp = new PointComperator(target);
			ArrayList<GraphNode> frontier = new ArrayList<>();
			HashMap<GraphNode, GraphNode> cameFrom = new HashMap<>();
			HashMap<Point2D, Integer> HighestCost = new HashMap<>();
			for(GraphNode node : nodes) {
				if(node.transitions.size() < 3)System.out.println(node.transitions.size());
			}
			GraphNode nearestNode = nodes.get(0);
			for(GraphNode node : nodes) {
				if(node.point.distance(start.point) < nearestNode.point.distance(start.point))nearestNode = node;
			}
			//start = nearestNode;
			frontier.add(start);
			cameFrom.put(start, null);
			start.lowestCost = -1;
			HighestCost.put(start.point, 0);
			while(frontier.size() != 0) {
				GraphNode curr = frontier.remove(0);
				if(curr.point.distance(target) < 15) {
				
					for(GraphNode node : nodes) {
						node.lowestCost = 0;
					}
					List<Point2D> path = new ArrayList<>();
					GraphNode point = curr;
					path.add(point.point);
					while(cameFrom.get(point) != null) {
						point = cameFrom.get(point);
						path.add(point.point);
					}
					return path;
					
				}
				for(GraphNode next : curr.transitions) {
					//GraphNode nuxt = getGraphNode(next);
					if(next != null) {
						int cost =(int) next.point.distance(curr.point) + curr.lowestCost;
						if(!frontier.contains(next) && !cameFrom.containsKey(next))frontier.add(next);
						if(cost < next.lowestCost || next.lowestCost == 0) {
							next.lowestCost = cost;
							cameFrom.put(next, curr );
						}
						
						
					}else {
						System.out.println("could not find point");
					}
					
				}
				frontier.sort(comp);
				
			}
			System.out.println("did not find path");
			for(GraphNode node : nodes) {
				node.lowestCost = 0;
			}
		
			return null;
			
		}
		public GraphNode getGraphNode(Point2D point) {
			for(GraphNode node : nodes) if(node.point.equals(point))return node;
			return null;
		}
		private void addPoint(Point2D point) {
			Area obstacleArea = new Area();// new Area[info.getScene().getObstacles().length];
			for(Path2D path : info.getScene().getObstacles()) 
				obstacleArea.add(new Area(path.createTransformedShape(new AffineTransform())));
			
			for(GraphNode node : nodes) {
				
			}
		}
	}
	private class GraphNode {
		public Point2D point;
		public ArrayList<Point2D> edges;
		public ArrayList<GraphNode> transitions;
		int lowestCost;
		public GraphNode(Point2D point, ArrayList<Point2D> points) {
			edges = new ArrayList<>();
			transitions = new ArrayList<>();
			this.point = point;
//			Area obstacleArea = new Area();// new Area[info.getScene().getObstacles().length];
//			for(Path2D path : info.getScene().getObstacles()) 
//				obstacleArea.add(new Area(path.createTransformedShape(new AffineTransform())));
//			
//			for(Point2D currPoint : points) {	
//				Vector normal = new Vector(-(currPoint.getY() - point.getY()),currPoint.getX() - point.getX()).normalize();
//				Path2D path = new Path2D.Double();
//				path.moveTo(point.getX(), point.getY());
//				path.lineTo(currPoint.getX(), currPoint.getY());
//				path.lineTo(currPoint.getX() + normal.getX(), currPoint.getY() + normal.getY());
//				path.lineTo(point.getX() + normal.getX(), point.getY() + normal.getY());
//				path.closePath();
//				Area test = new Area(path);
//				test.intersect(obstacleArea);
//				if(test.isEmpty()) edges.add(currPoint);
//				
//				
//				
//			}
		}
		public void addOneWayTransitions(ArrayList<GraphNode> nodes, Area obstacleArea) {
			for(GraphNode node : nodes) {
				Point2D currPoint = node.point;
				Vector normal = new Vector(-(currPoint.getY() - point.getY()),currPoint.getX() - point.getX()).normalize().scale(6f);
				Path2D path = new Path2D.Double();
				path.moveTo(point.getX() - normal.getX(), point.getY() - normal.getY());
				path.lineTo(currPoint.getX() - normal.getX(), currPoint.getY() - normal.getY());
				path.lineTo(currPoint.getX() + normal.getX(), currPoint.getY() + normal.getY());
				path.lineTo(point.getX() + normal.getX(), point.getY() + normal.getY());
				path.closePath();
				Area test = new Area(path);
				test.intersect(obstacleArea);
			
				if(test.isEmpty()) {
					if(transitions.add(node));
				
				}
			}
		}
		public void addTransitions(ArrayList<GraphNode> nodes, Area obstacleArea) {
			for(GraphNode node : nodes) {
				Point2D currPoint = node.point;
				Vector normal = new Vector(-(currPoint.getY() - point.getY()),currPoint.getX() - point.getX()).normalize().scale(6f);
				Path2D path = new Path2D.Double();
				path.moveTo(point.getX() - normal.getX(), point.getY() - normal.getY());
				path.lineTo(currPoint.getX() - normal.getX(), currPoint.getY() - normal.getY());
				path.lineTo(currPoint.getX() + normal.getX(), currPoint.getY() + normal.getY());
				path.lineTo(point.getX() + normal.getX(), point.getY() + normal.getY());
				path.closePath();
				Area test = new Area(path);
				test.intersect(obstacleArea);
			
				if(test.isEmpty()) {
					if(transitions.add(node));
					node.transitions.add(this);
				}
			}
		}
	}
	private class PointComperator implements Comparator<GraphNode>{
		
		private Point2D target;
		public PointComperator(Point2D target) {
			super();
			this.target = target;
			
		}
		@Override
		public int compare(GraphNode o1, GraphNode o2) {
			return  ((int)o1.point.distance(target) + o1.lowestCost)  - ((int)o2.point.distance(target) + o2.lowestCost);
		}
		
	}
}
