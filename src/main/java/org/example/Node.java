package org.example;

import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.Objects;

public class Node implements Comparable<Node> {

    public LngLat lngLat;
    public double angle;
    public double lng;
    public double lat;
    public String orderNo;
    public double startCost;
    public double endCost;
    public Node parent;

    public Node(LngLat lngLat, double angle, String orderNo) {
        this.lngLat = lngLat;
        this.lng = lngLat.lng();
        this.lat = lngLat.lat();
        this.angle = angle;
        this.orderNo = orderNo;
    }

    public double lng() { return this.lngLat.lng(); }

    public double angle() { return this.angle; }

    public double lat() { return this.lngLat.lat(); }

    public LngLat lngLat() { return this.lngLat;}

    @Override
    public int compareTo(Node other) {
        double thisTotal = this.startCost + this.endCost;
        double otherTotal = other.startCost + other.endCost;

        return Double.compare(thisTotal, otherTotal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Double.compare(node.lat(), lat) == 0 &&
                Double.compare(node.lng(), lng) == 0 &&
                Double.compare(node.startCost, startCost) == 0 &&
                Double.compare(node.endCost, endCost) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng, startCost, endCost);
    }
}