/*
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package celtech.utils.threed.importers.obj;

import celtech.appManager.Project;
import celtech.configuration.PrintBed;
import celtech.coreUI.visualisation.ApplicationMaterials;
import celtech.coreUI.visualisation.metaparts.IntegerArrayList;
import celtech.coreUI.visualisation.metaparts.FloatArrayList;
import celtech.coreUI.visualisation.metaparts.ModelLoadResult;
import celtech.modelcontrol.ModelContainer;
import celtech.services.modelLoader.ModelLoaderTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 * Obj file reader
 */
public class ObjImporter
{

    private static final Stenographer steno = StenographerFactory.getStenographer(ObjImporter.class.getName());

    private ModelLoaderTask parentTask = null;

    private int vertexIndex(int vertexIndex)
    {
        if (vertexIndex < 0)
        {
            return vertexIndex + verticesFromFile.size() / 3;
        } else
        {
            return vertexIndex - 1;
        }
    }

    private static boolean debug = false;
    private static float scale = 1;
    private static boolean flatXZ = false;

    /**
     *
     * @return
     */
    public Set<String> getMeshes()
    {
        return meshes.keySet();
    }

    private final Map<String, TriangleMesh> meshes = new HashMap<>();
    private final Map<String, Integer> materialsForObjects = new HashMap<>();
    private final Map<String, Integer> materialNameAgainstIndex = new HashMap<>();
    private String objFileUrl;

    /**
     *
     * @param parentTask
     * @param modelFileToLoad
     * @param targetProjectTab
     * @return
     */
    public ModelLoadResult loadFile(ModelLoaderTask parentTask, String modelFileToLoad,
            Project targetProject)
    {
        this.parentTask = parentTask;
        this.objFileUrl = modelFileToLoad;

        ModelLoadResult modelLoadResult = null;

        try
        {
            File modelFile = new File(objFileUrl);
            URL fileURL = new URL(objFileUrl);
            String filePath = modelFile.getParent();

            read(fileURL.openStream(), filePath);

            ArrayList<MeshView> meshes = new ArrayList<>();
            ArrayList<Integer> extruderAssociations = new ArrayList<>();

            //We will take the 
            for (String key : getMeshes())
            {
                meshes.add(buildMeshView(key));

                int materialNumber = materialsForObjects.get(key);
                extruderAssociations.add(materialNumber);
            }

            ModelContainer modelContainer = new ModelContainer(modelFile, meshes, extruderAssociations);
            boolean modelIsTooLarge = PrintBed.isBiggerThanPrintVolume(modelContainer.getOriginalModelBounds());

            modelLoadResult = new ModelLoadResult(modelIsTooLarge, modelFileToLoad, modelFile.getName(), targetProject, modelContainer);
        } catch (IOException ex)
        {
            steno.error("Exception whilst reading obj file " + modelFileToLoad + ":" + ex);
        }

        if (parentTask
                != null && parentTask.isCancelled())
        {
            modelLoadResult = null;
        }
        return modelLoadResult;
    }

    /**
     *
     * @return
     */
    public TriangleMesh getMesh()
    {
        return meshes.values().iterator().next();
    }

    /**
     *
     * @param key
     * @return
     */
    public TriangleMesh getMesh(String key)
    {
        return meshes.get(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public MeshView buildMeshView(String key)
    {
        MeshView meshView = new MeshView();
        meshView.setId(key);
        meshView.setMaterial(ApplicationMaterials.getDefaultModelMaterial());
        meshView.setMesh(meshes.get(key));
        meshView.setCullFace(CullFace.BACK);
        return meshView;
    }

    /**
     *
     * @param debug
     */
    public static void setDebug(boolean debug)
    {
        ObjImporter.debug = debug;
    }

    /**
     *
     * @param scale
     */
    public static void setScale(float scale)
    {
        ObjImporter.scale = scale;
    }

    private FloatArrayList verticesFromFile = new FloatArrayList();
    private IntegerArrayList facesFromFile = new IntegerArrayList();
    private int materialNumber = -1;
    private int facesStart = 0;

    private void read(InputStream inputStream, final String filePath) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String key = "default";
        boolean pendingObject = false;

        while ((line = br.readLine()) != null)
        {
            try
            {
                if (line.startsWith("v "))
                {
                    String[] split = line.substring(2).trim().split(" +");
                    float x = Float.parseFloat(split[0]) * scale;
                    float y = Float.parseFloat(split[1]) * scale;
                    float z = Float.parseFloat(split[2]) * scale;

                    //                log("x = " + x + ", y = " + y + ", z = " + z);
                    verticesFromFile.add(x);
                    verticesFromFile.add(-z);
                    verticesFromFile.add(y);
                } else if (line.startsWith("vt "))
                {
                    //Ignore vertex textures for the moment
//                    String[] split = line.substring(3).trim().split(" +");
//                    float u = Float.parseFloat(split[0]);
//                    float v = Float.parseFloat(split[1]);
//
                    //                log("u = " + u + ", v = " + v);
                } else if (line.startsWith("f "))
                {
                    //Faces
                    pendingObject = true;
                    String[] split = line.substring(2).trim().split(" +");
                    int[][] data = new int[split.length][];
                    boolean uvProvided = true;
                    boolean normalProvided = true;
                    for (int i = 0; i < split.length; i++)
                    {
                        String[] split2 = split[i].split("/");
                        if (split2.length < 2)
                        {
                            uvProvided = false;
                        }
                        if (split2.length < 3)
                        {
                            normalProvided = false;
                        }
                        data[i] = new int[split2.length];
                        for (int j = 0; j < split2.length; j++)
                        {
                            if (split2[j].length() == 0)
                            {
                                data[i][j] = 0;
                                if (j == 1)
                                {
                                    uvProvided = false;
                                }
                                if (j == 2)
                                {
                                    normalProvided = false;
                                }
                            } else
                            {
                                data[i][j] = Integer.parseInt(split2[j]);
                            }
                        }
                    }
                    int v1 = vertexIndex(data[0][0]);
                    int uv1 = -1;
                    int n1 = -1;
                    for (int i = 1; i < data.length - 1; i++)
                    {
                        int v2 = vertexIndex(data[i][0]);
                        int v3 = vertexIndex(data[i + 1][0]);
                        int uv2 = -1;
                        int uv3 = -1;
                        int n2 = -1;
                        int n3 = -1;

                        //                    log("v1 = " + v1 + ", v2 = " + v2 + ", v3 = " + v3);
                        //                    log("uv1 = " + uv1 + ", uv2 = " + uv2 + ", uv3 = " + uv3);
                        facesFromFile.add(v1);
                        facesFromFile.add(uv1);
                        facesFromFile.add(v2);
                        facesFromFile.add(uv2);
                        facesFromFile.add(v3);
                        facesFromFile.add(uv3);
                    }
                } else if (line.startsWith("mtllib "))
                {
                    // setting materials lib - just take the first one for the moment
                    String materialFilename = line.substring("mtllib ".length()).trim();
                    MtlReader mtlReader = new MtlReader(materialFilename, filePath);

                    Map<String, Material> mtlMap = mtlReader.getMaterials();

                    int materialCount = 0;
                    for (Entry<String, Material> entry : mtlMap.entrySet())
                    {
                        materialNameAgainstIndex.put(entry.getKey(), materialCount);
                        materialCount++;
                    }
                } else if (line.startsWith("usemtl "))
                {
                    // setting new material for next mesh
                    String materialName = line.substring("usemtl ".length());

                    materialNumber = materialNameAgainstIndex.get(materialName);
                } else if (line.isEmpty() || line.startsWith("#"))
                {
                    // comments and empty lines are ignored
                } else if (line.startsWith("vn "))
                {
                    //Ignore vertex normals
//                    String[] split = line.substring(2).trim().split(" +");
//                    float x = Float.parseFloat(split[0]);
//                    float y = Float.parseFloat(split[1]);
//                    float z = Float.parseFloat(split[2]);
                } else if (line.startsWith("o"))
                {
                    if (pendingObject)
                    {
                        addMesh(key);
                        pendingObject = false;
                    }
                    //This is an object definition
                    String objectName = line.substring(1).trim();
                    steno.debug("Got object name " + objectName);
                    key = objectName;
                } else if (line.startsWith("g"))
                {
                    if (pendingObject)
                    {
                        addMesh(key);
                        pendingObject = false;
                    }
                    //This is an object group definition
                    String objectName = line.substring(1).trim();
                    steno.debug("Got object name " + objectName);
                    key = objectName;
                } else
                {
                    steno.debug("line skipped: " + line);

                }
            } catch (Exception ex)
            {
                Logger.getLogger(MtlReader.class
                        .getName()).log(Level.SEVERE, "Failed to parse line:" + line, ex);
            }
        }

        if (pendingObject)
        {
            addMesh(key);
        }
    }

    private void addMesh(final String key)
    {
        Map<Integer, Integer> vertexMap = new HashMap<>(verticesFromFile.size() / 2);
        FloatArrayList newVertices = new FloatArrayList(verticesFromFile.size() / 2);
        IntegerArrayList newFaces = new IntegerArrayList();

        for (int i = facesStart; i < facesFromFile.size(); i += 2)
        {
            int vi = facesFromFile.get(i);
            Integer nvi = vertexMap.get(vi);
            if (nvi == null)
            {
                nvi = newVertices.size() / 3;
                vertexMap.put(vi, nvi);
                newVertices.add(verticesFromFile.get(vi * 3));
                newVertices.add(verticesFromFile.get(vi * 3 + 1));
                newVertices.add(verticesFromFile.get(vi * 3 + 2));
            }
            newFaces.add(nvi);
            newFaces.add(0);
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(newVertices.toFloatArray());
        FloatArrayList texCoords = new FloatArrayList();
        texCoords.add(0f);
        texCoords.add(0f);
        mesh.getTexCoords().addAll(texCoords.toFloatArray());
        mesh.getFaces().setAll(newFaces.toIntArray());

        int[] smoothingGroups = new int[newFaces.size() / 6];
        for (int i = 0; i < smoothingGroups.length; i++)
        {
            smoothingGroups[i] = 0;
        }
        mesh.getFaceSmoothingGroups().addAll(smoothingGroups);

        meshes.put(key, mesh);
        materialsForObjects.put(key, materialNumber);

//        steno.info(
//                "Added mesh '" + key + "' of " + mesh.getPoints().size() / TriangleMesh.NUM_COMPONENTS_PER_POINT + " vertexes, "
//                + mesh.getTexCoords().size() / TriangleMesh.NUM_COMPONENTS_PER_TEXCOORD + " uvs, "
//                + mesh.getFaces().size() / TriangleMesh.NUM_COMPONENTS_PER_FACE +" faces, "
//                + mesh.getFaceSmoothingGroups().size() + " smoothing groups.");
        steno.debug(
                "Loaded object mesh " + (newVertices.size() / 3.) + " vertices, "
                + (newFaces.size() / 6.) + " faces, "
                + smoothingGroups.length + " smoothing groups.");

        facesStart = facesFromFile.size();
    }

    /**
     *
     * @param flatXZ
     */
    public static void setFlatXZ(boolean flatXZ)
    {
        ObjImporter.flatXZ = flatXZ;
    }
}
