package org.example;

import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.Objects;

/**
 * Represents a node for the pathfinding algorithm.
 */
public class Node implements Comparable<Node> {

    public LngLat lngLat;
    public double angle;
    public String orderNo;
    double startCost;
    double endCost;
    public Node parent;

    /**
     * Constructs a Node with geographical coordinates, angle, and order number.
     *
     * @param lngLat  The longitude and latitude of the node.
     * @param angle   The angle associated with the node.
     * @param orderNo The order number associated with the node.
     */
    public Node(LngLat lngLat, double angle, String orderNo) {
        this.lngLat = lngLat;
        this.angle = angle;
        this.orderNo = orderNo;
    }

    public double lng() { return this.lngLat.lng(); }

    public double angle() { return this.angle; }

    public double lat() { return this.lngLat.lat(); }

    public LngLat lngLat() { return this.lngLat; }

    /**
     * Compares this node with another node for order.
     * Ordering is based on the sum of start and end costs.
     *
     * @param other The other node to compare to.
     * @return A negative integer, zero, or a positive integer as this node is less than,
     *         equal to, or greater than the specified node.
     */
    @Override
    public int compareTo(Node other) {
        double thisTotal = this.startCost + this.endCost;
        double otherTotal = other.startCost + other.endCost;

        return Double.compare(thisTotal, otherTotal);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o The reference object with which to compare.
     * @return true if this object is the same as the o argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Double.compare(node.lngLat.lng(), lngLat.lng()) == 0 &&
                Double.compare(node.lngLat.lat(), lngLat.lat()) == 0 &&
                Double.compare(node.startCost, startCost) == 0 &&
                Double.compare(node.endCost, endCost) == 0;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(lngLat.lng(), lngLat.lat(), startCost, endCost);
    }
}