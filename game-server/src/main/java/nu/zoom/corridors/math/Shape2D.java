/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

import java.util.ArrayList;

public class Shape2D {

    private final ArrayList<Tuple2f> vertices = new ArrayList<>();

    public Shape2D addVertex(final Tuple2f vertex) {

        if (vertex == null) {
            throw new IllegalArgumentException("Vertex may not be null");
        }
        this.vertices.add(vertex);
        return this;
    }

    public Tuple2f[] toArray() {

        return this.vertices.toArray(new Tuple2f[this.vertices.size()]);
    }
}
