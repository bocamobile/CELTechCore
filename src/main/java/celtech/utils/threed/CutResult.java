/*
 * Copyright 2015 CEL UK
 */
package celtech.utils.threed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.shape.TriangleMesh;


/**
 *
 * @author tony
 */
class CutResult
{

    /**
     * The child mesh that was created by the split.
     */
    TriangleMesh childMesh;
    /**
     * The indices of the vertices of the child mesh, in sequence, that form the perimeter of the
     * new open face that needs to be triangulated. Some loops (list of points) may be holes inside
     * other loops.
     */
    List<PolygonIndices> loopsOfVerticesOnOpenFace;

    public CutResult(TriangleMesh childMesh, List<PolygonIndices> vertexsOnOpenFace)
    {
        this.childMesh = childMesh;
        this.loopsOfVerticesOnOpenFace = vertexsOnOpenFace;
    }

    /**
     * Identify which of the loops in loopsOfVerticesOnOpenFace are internal to other loops. There
     * should not be any overlapping loops. Each LoopSet has one outer loop and zero or more inner
     * loops.
     *
     * Find sets of polygons that are nested inside each other. Sort each set by area giving a list
     * of nested polygons. The first in the list is the outer polygon.
     */
    public Set<LoopSet> identifyOuterLoopsAndInnerLoops()
    {
        Set<LoopSet> loopSets = new HashSet<>();
        Set<Set<PolygonIndices>> nestedPolygonsSet = getNestedPolygonSets();
        for (Set<PolygonIndices> nestedPolygons : nestedPolygonsSet)
        {
            List<PolygonIndices> nestedPolygonsList = sortByArea(nestedPolygons);
           PolygonIndices outerPolygon = nestedPolygonsList.get(0);
            // inner polygons is remaining polygons after removing outer polygon
            nestedPolygonsList.remove(0);
            loopSets.add(new LoopSet(outerPolygon, nestedPolygonsList));
        }
        return loopSets;
    }

    /**
     * Organise the loops from loopsOfVerticesOnOpenFace into sets. Each set contains polygons that
     * are nested inside each other. There is no need to sort the nested polygons according to which
     * is nested inside which.
     */
    Set<Set<PolygonIndices>> getNestedPolygonSets()
    {
        Set<Set<PolygonIndices>> nestedPolygonSets = new HashSet<>();
        for (PolygonIndices polygon : loopsOfVerticesOnOpenFace)
        {
            Set<PolygonIndices> containingPolygonSet
                = polygonSetInsideOrContainingPolygon(nestedPolygonSets, polygon);
            if (containingPolygonSet != null)
            {
                containingPolygonSet.add(polygon);
            } else
            {
                Set<PolygonIndices> newPolygonSet = new HashSet<>();
                newPolygonSet.add(polygon);
                nestedPolygonSets.add(newPolygonSet);
            }
        }
        return nestedPolygonSets;
    }

    /**
     * Return the set of polygons that either contains the given polygon or is contained by it. If
     * no polygon set contains/is inside the polygon then return null.
     */
    private Set<PolygonIndices> polygonSetInsideOrContainingPolygon(
        Set<Set<PolygonIndices>> nestedPolygonSets, PolygonIndices polygon)
    {
        for (Set<PolygonIndices> polygonSet : nestedPolygonSets)
        {
            // we need only consider 1 polygon from the set
            PolygonIndices innerPolygon = polygonSet.iterator().next();
            if (contains(innerPolygon, polygon) || contains(polygon, innerPolygon))
            {
                return polygonSet;
            }
        }
        return null;
    }

    /**
     * Sort the given list of polygons by area, largest first.
     */
    private List<PolygonIndices> sortByArea(Set<PolygonIndices> nestedPolygons)
    {
        List<PolygonIndices> sortedNestedPolygons = new ArrayList<>(nestedPolygons);
        Collections.sort(sortedNestedPolygons, new Comparator<PolygonIndices>()
        {

            @Override
            public int compare(PolygonIndices o1, PolygonIndices o2)
            {
                double a1 = getPolygonArea(o1);
                double a2 = getPolygonArea(o2);
                if (a1 > a2) {
                    return 1;
                } else if (a1 == a2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return sortedNestedPolygons;
    }
    
    private Point getPointAt(PolygonIndices loop, int index) {
        double x = childMesh.getPoints().get(loop.get(index) * 3);
        double y = childMesh.getPoints().get(loop.get(index) * 3 + 2);
        return new Point(x, y);
    }

    public boolean contains(PolygonIndices outerPolygon, PolygonIndices innerPolygon)
    {
        Point point = getPointAt(innerPolygon, 0);
        return contains(point, outerPolygon);
    }

    /**
     * Return if the given loop contains the given point. see
     * http://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon
     */
    public boolean contains(Point test, PolygonIndices loop)
    {
        Point[] points = new Point[loop.size()];
        for (int k = 0; k < points.length; k++)
        {
            points[k] = getPointAt(loop, k);
        }
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++)
        {
            if ((points[i].y > test.y) != (points[j].y > test.y) && (test.x < (points[j].x
                - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x))
            {
                result = !result;
            }
        }
        return result;
    }

    double getPolygonArea(PolygonIndices loop)
    {
        int N = loop.size();
        Point[] polygon = new Point[N];
        for (int k = 0; k < polygon.length; k++)
        {
            polygon[k] = getPointAt(loop, k);
        }
        
        int i;
        int j;
        double area = 0;
        for (i = 0; i < N; i++)
        {
            j = (i + 1) % N;
            area += polygon[i].x * polygon[j].y;
            area -= polygon[i].y * polygon[j].x;
        }
        area /= 2;
        return area < 0 ? -area : area;
    }

}