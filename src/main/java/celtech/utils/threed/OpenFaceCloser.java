/*
 * Copyright 2015 CEL UK
 */
package celtech.utils.threed;

import static celtech.utils.threed.MeshCutter.makePoint3D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;


/**
 *
 * @author tony
 */
public class OpenFaceCloser
{
    
    /**
     * Take the given mesh and vertices of the open face, close the face and add the new face to the
     * mesh and return it.
     */
    static TriangleMesh closeOpenFace(CutResult cutResult, double cutHeight,
        MeshCutter.BedToLocalConverter bedToLocalConverter)
    {
        TriangleMesh mesh = cutResult.childMesh;
        Set<LoopSet> loopSets = cutResult.identifyOuterLoopsAndInnerLoops();
        for (LoopSet loopSet : loopSets)
        {
            try
            {
                List<Integer> vertices = loopSet.outerLoop;
                Polygon outerPolygon = makePolygon(vertices, mesh, bedToLocalConverter);
                for (List<Integer> innerVertices : loopSet.innerLoops)
                {
                    Polygon innerPolygon = makePolygon(innerVertices, mesh, bedToLocalConverter);
                    outerPolygon.addHole(innerPolygon);
                }

                Poly2Tri.triangulate(outerPolygon);
                addTriangulatedFacesToMesh(mesh, outerPolygon, vertices, cutHeight, bedToLocalConverter);
            } catch (Exception ex)
            {
                System.out.println("unable to close loop: " + loopSet);
            }
        }

        return mesh;
    }

    /**
     * Make a Polygon for the given vertices. 3D points should be in bed coordinates so that
     * only X and Z are required (Y being a constant at the cut height in bed coordinates).
     */
    private static Polygon makePolygon(List<Integer> vertices, TriangleMesh mesh,
        MeshCutter.BedToLocalConverter bedToLocalConverter)
    {
        List<PolygonPoint> points = new ArrayList<>();
        for (Integer vertexIndex : vertices)
        {
            Point3D pointInBed = bedToLocalConverter.localToBed(makePoint3D(mesh, vertexIndex));
            if (pointInBed.getX() > 1e5 || 
                pointInBed.getX() < -1e5||
                pointInBed.getZ() > 1e5 ||
                pointInBed.getZ() < -1e5)
            {
                throw new RuntimeException("invalid point calculated");
            }
            points.add(new PolygonPoint(
                pointInBed.getX(),
                pointInBed.getZ()));
        }
        Polygon outerPolygon = new Polygon(points);
        return outerPolygon;
    }

    /**
     * For each triangle in the polygon add a face to the mesh. If any point in any triangle is not
     * one of the outerVertices then also add that point to the mesh.
     */
    private static void addTriangulatedFacesToMesh(TriangleMesh mesh, Polygon outerPolygon,
        List<Integer> outerVertices, double cutHeight, MeshCutter.BedToLocalConverter bedToLocalConverter)
    {
        // vertexToVertex allows us to identify equal vertices (but different instances) and then
        // get the definitive instance of that vertex, to avoid superfluous vertices in the mesh.
        Map<Vertex, Vertex> vertexToVertex = new HashMap<>();
        // first add already existing vertices for outer perimeter of polygon to vertexToVertex
        for (Integer vertexIndex : outerVertices)
        {
            Point3D point = makePoint3D(mesh, vertexIndex);
            Point3D pointInBed = bedToLocalConverter.localToBed(point);
            Vertex vertex = new Vertex(vertexIndex,
                (float) pointInBed.getX(), (float) pointInBed.getY(), (float) pointInBed.getZ());
            vertexToVertex.put(vertex, vertex);
        }

        for (DelaunayTriangle triangle : outerPolygon.getTriangles())
        {
            TriangulationPoint[] points = triangle.points;
            Vertex vertex0 = getOrMakeVertexForPoint(mesh, points[0], vertexToVertex, cutHeight, bedToLocalConverter);
            Vertex vertex1 = getOrMakeVertexForPoint(mesh, points[1], vertexToVertex, cutHeight, bedToLocalConverter);
            Vertex vertex2 = getOrMakeVertexForPoint(mesh, points[2], vertexToVertex, cutHeight, bedToLocalConverter);
            makeFace(mesh, vertex0.meshVertexIndex, vertex1.meshVertexIndex, vertex2.meshVertexIndex);
        }
    }

    private static void makeFace(TriangleMesh mesh, int meshVertexIndex0, int meshVertexIndex1,
        int meshVertexIndex2)
    {
        int[] vertices = new int[6];
        vertices[0] = meshVertexIndex0;
        vertices[2] = meshVertexIndex1;
        vertices[4] = meshVertexIndex2;
        mesh.getFaces().addAll(vertices);
    }

    /**
     * Make a Vertex for the point, in bed coordinates (so that equality comparisons are
     * all in bed coordinates), and add the points to the mesh in local coordinates.
     */
    private static Vertex getOrMakeVertexForPoint(TriangleMesh mesh, TriangulationPoint point,
        Map<Vertex, Vertex> vertexToVertex, double cutHeight, MeshCutter.BedToLocalConverter bedToLocalConverter)
    {
        Point3D pointInBed = new Point3D(point.getX(), cutHeight, point.getY());
        Vertex vertex = new Vertex((float) pointInBed.getX(), (float) pointInBed.getY(), (float) pointInBed.getZ());
        
        Point3D pointInLocal = bedToLocalConverter.bedToLocal(pointInBed);
        if (!vertexToVertex.containsKey(vertex))
        {
            mesh.getPoints().addAll((float) pointInLocal.getX(), (float) pointInLocal.getY(), (float) pointInLocal.getZ());
            int vertexIndex = mesh.getPoints().size() / 3 - 1;
            vertex.meshVertexIndex = vertexIndex;
            vertexToVertex.put(vertex, vertex);
            return vertex;
        } else
        {
            return vertexToVertex.get(vertex);
        }

    }

    
}

