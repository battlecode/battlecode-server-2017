package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

import battlecode.world.signal.ConvexHullSignal;

import static battlecode.common.GameConstants.BUILDING_LINK_DIST;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

public final class ScoreCalculator {

	static final class XComparator implements Comparator<MapLocation> {

		public int compare(MapLocation a, MapLocation b) {
			int dx = a.getX()-b.getX();
			if(dx!=0) return dx;
			else return a.getY()-b.getY();
		}

		public boolean equals(Object o) {
			return o instanceof XComparator;
		}
	
	}

	static final class AngleComparator implements Comparator<MapLocation> {

		int refX;
		int refY;

		public AngleComparator(MapLocation l) {
			refX = l.getX();
			refY = l.getY();
		}

		public int compare(MapLocation a, MapLocation b) {
			int comp = (a.getX() - refX)*(b.getY() - refY) - (b.getX() - refX)*(a.getY() - refY);
			if(comp!=0) return comp;
			// break ties with reverse X comparator
			comp = b.getX()-a.getX();
			if(comp!=0) return comp;
			return b.getY()-a.getY();
		}

		public int compareAngleOnly(MapLocation a, MapLocation b) {
			return (a.getX() - refX)*(b.getY() - refY) - (b.getX() - refX)*(a.getY() - refY);
		}

		public boolean equals(Object o) {
			if(o instanceof AngleComparator) {
				AngleComparator ac = (AngleComparator)o;
				return refX==ac.refX&&refY==ac.refY;
			}
			else
				return false;
		}
	
	}

	static final class UnionFindNode {

		static ArrayList<UnionFindNode> stack = new ArrayList<UnionFindNode>();

		UnionFindNode parent;
		public UnionFindNode next;
		UnionFindNode last;
		int rank;
		int size;
		public MapLocation loc;

		public boolean isHead() {
			return parent == this;
		}

		public UnionFindNode(MapLocation l) {
			loc = l;
			reset();
		}

		public void reset() {
			parent = this;
			last = this;
			next = null;
			size = 1;
			rank = 0;
		}

		public UnionFindNode find() {
			stack.clear();
			UnionFindNode n = this;
			while(n.parent!=n) {
				stack.add(n);
				n=n.parent;
			}
			for(UnionFindNode n2: stack) {
				n2.parent = n;
			}
			return n;
		}
	
		// returns the node that used to be a head,
		// but isn't any more
		public UnionFindNode union(UnionFindNode n) {
			UnionFindNode findMe = find();
			UnionFindNode findN = n.find();
			if(findMe.rank>findN.rank) {
				findN.parent = findMe;
				findMe.last.next = findN;
				findMe.last = findN.last;
				findMe.size += findN.size;
				return findN;
			}
			else if(findN.rank>findMe.rank) {
				findMe.parent = findN;
				findN.last.next = findMe;
				findN.last = findMe.last;
				findN.size += findMe.size;
				return findMe;
			}
			else if(findN!=findMe) {
				findN.parent = findMe;
				findMe.rank++;
				findMe.last.next = findN;
				findMe.last = findN.last;
				findMe.size += findN.size;
				return findN;
			}
			else
				return null;
		}

		public UnionFindNode [] children() {
			int i=-1;
			UnionFindNode node = find();
			UnionFindNode [] ans = new UnionFindNode [node.size];
			do {
				ans[++i]=node;
			} while((node=node.next)!=null);
			return ans;
		}

		public MapLocation [] elements() {
			int i=-1;
			UnionFindNode node = find();
			MapLocation [] ans = new MapLocation [node.size];
			do {
				ans[++i]=node.loc;
			} while((node=node.next)!=null);
			return ans;
		}
	}

	static public final int BUILDING_LINK_DIST_SQ = BUILDING_LINK_DIST * BUILDING_LINK_DIST;

	TreeSet<MapLocation> allPoints;
	HashMap<MapLocation,UnionFindNode> nodes;
	HashMap<UnionFindNode,MapLocation []> hulls;
	double score;
	static final XComparator xComparator = new XComparator();
	LinkedList<TreeSet<MapLocation>> groups;
	//HashMap<MapLocation,Iterator<LinkedList<TreeSet<MapLocation>>>> groups;

	public ScoreCalculator() {
		allPoints = new TreeSet<MapLocation>(xComparator);
		nodes = new HashMap<MapLocation,UnionFindNode>();
		hulls = new HashMap<UnionFindNode,MapLocation []>();
	}

	public double getScore() {
		return score;
	}

	public void recomputeScore() {
		int tmpScore = 0;
		int i;
		for(MapLocation [] l : hulls.values()) {
			for(i=l.length-1;i>0;i--) {
				tmpScore+=l[i].getX()*l[i-1].getY()-l[i-1].getX()*l[i].getY();
			}
			tmpScore+=l[0].getX()*l[l.length-1].getY()-l[l.length-1].getX()*l[0].getY();
			//if(tmpScore<0) System.out.println("Negative area!");
		}
		score = tmpScore * GameConstants.POINTS_PER_AREA_FACTOR;
		//System.out.println("Score is "+score);
	}

	public MapLocation [][] hullArray() {
		Collection<MapLocation []> allHulls = hulls.values();
		MapLocation [][] hullArray = new MapLocation [hulls.size()][];
		return hulls.values().toArray(hullArray);
	}

	public void add(MapLocation loc) {
		//System.out.println("adding "+loc);
		UnionFindNode node = new UnionFindNode(loc);
		UnionFindNode n;
		nodes.put(loc,node);
		Set<MapLocation> xStrip = allPoints.subSet(new MapLocation(loc.getX()-BUILDING_LINK_DIST,loc.getY()),true, new MapLocation(loc.getX()+BUILDING_LINK_DIST,loc.getY()),true);
		for(MapLocation l: xStrip) {
			if(loc.distanceSquaredTo(l)<=BUILDING_LINK_DIST_SQ) {
				n=nodes.get(l);
				n=node.union(n);
				if(n!=null) hulls.remove(n);
			}
		}
		allPoints.add(loc);
		MapLocation [] ml = node.elements();
		MapLocation [] ch = convexHull(ml);
		//System.out.println("The ch is :");
		//for(MapLocation l: ch) {
		//	System.out.println(l.toString());
		//}
		hulls.put(node.find(),ch);
		recomputeScore();
	}

	public void remove(MapLocation loc) {
		allPoints.remove(loc);
		UnionFindNode removed = nodes.remove(loc);
		UnionFindNode head = removed.find();
		hulls.remove(head);
		UnionFindNode [] brokenNodes = head.children();
		for(UnionFindNode n : brokenNodes) {
			n.reset();
		}
		for(UnionFindNode n : brokenNodes) {
			if(n==removed) continue;
			else {
				for(MapLocation l : allPoints.subSet(new MapLocation(n.loc.getX()-BUILDING_LINK_DIST,n.loc.getY()),n.loc)) {
					if(n.loc.distanceSquaredTo(l)<=BUILDING_LINK_DIST_SQ) {
						n.union(nodes.get(l));
					}
				}
			}
		}
		for(UnionFindNode n : brokenNodes) {
			if(n==removed) continue;
			if(n.isHead())
				hulls.put(n,convexHull(n.elements()));
		}
		recomputeScore();
	}

	/*
	 * Computes the convex hull using a Graham scan
	 */
	public static MapLocation [] convexHull(MapLocation[] locs) {
		//System.out.println("Number of points: "+locs.length);
		if(locs.length<=2) {
			return locs;
		}
		MapLocation leftmost=locs[0];
		MapLocation last;
		int i, i2, j, lefti=0;
		for(i=locs.length-1;i>0;i--) {
			if(xComparator.compare(locs[i],leftmost)<0) {
				leftmost=locs[i];
				lefti=i;
			}
		}
		locs[lefti]=locs[0];
		locs[0]=leftmost;
		AngleComparator angleComp = new AngleComparator(leftmost);
		Arrays.sort(locs,1,locs.length,angleComp);
		j=2;
		for(i=2;i<locs.length;i++) {
			if(angleComp.compareAngleOnly(locs[i],locs[j])==0) continue;
			do {
				if((locs[j].getX()-locs[i].getX())*(locs[j-1].getY()-locs[i].getY())-(locs[j-1].getX()-locs[i].getX())*(locs[j].getY()-locs[i].getY())>=0) break;
			} while(--j>0);
			locs[++j]=locs[i];
		}
		if(j==locs.length-1)
			return locs;
		else
			return Arrays.copyOf(locs,j+1);
	}

}