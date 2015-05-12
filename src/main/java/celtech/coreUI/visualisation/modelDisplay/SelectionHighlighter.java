package celtech.coreUI.visualisation.modelDisplay;

import celtech.configuration.ApplicationConfiguration;
import celtech.coreUI.visualisation.ShapeProvider;
import celtech.coreUI.visualisation.Xform;
import celtech.modelcontrol.ModelContainer;
import celtech.utils.Math.MathUtils;
import javafx.event.ActionEvent;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author Ian Hudson @ Liberty Systems Limited
 */
public class SelectionHighlighter extends Group implements ShapeProvider.ShapeChangeListener
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        SelectionHighlighter.class.getName());
    public static final String idString = "selectionHighlighter";

    private final PhongMaterial greenMaterial = new PhongMaterial(Color.LIMEGREEN);

    private Xform selectionBoxBackLeftTop = null;
    private Xform selectionBoxBackRightTop = null;
    private Xform selectionBoxFrontLeftTop = null;
    private Xform selectionBoxFrontRightTop = null;
    private Xform selectionBoxBackLeftBottom = null;
    private Xform selectionBoxBackRightBottom = null;
    private Xform selectionBoxFrontLeftBottom = null;
    private Xform selectionBoxFrontRightBottom = null;

    private final double cornerBracketLength = 5;

    //Leave out for 1.01.05
//    private final ScaleControls scaleControls;
    
    /**
     *
     * @param modelContainer
     */
    public SelectionHighlighter(final ModelContainer modelContainer)
    {
        this.setId(idString);
        Image illuminationMap = new Image(SelectionHighlighter.class.getResource(ApplicationConfiguration.imageResourcePath + "greenIlluminationMap.png").toExternalForm());
        greenMaterial.setSelfIlluminationMap(illuminationMap);
        buildSelectionBox();

//        scaleControls = new ScaleControls(this);
        modelContainer.addShapeChangeListener(this);
    }

    private void buildSelectionBox()
    {
        selectionBoxBackLeftBottom = generateSelectionCornerGroup(0, 90, 0);

        selectionBoxBackRightBottom = generateSelectionCornerGroup(0, -180, 0);

        selectionBoxBackLeftTop = generateSelectionCornerGroup(180, 0, 0);

        selectionBoxBackRightTop = generateSelectionCornerGroup(180, 90, 0);

        selectionBoxFrontLeftBottom = generateSelectionCornerGroup(0, 0, 0);

        selectionBoxFrontRightBottom = generateSelectionCornerGroup(0, -90, 0);

        selectionBoxFrontLeftTop = generateSelectionCornerGroup(180, -90, 0);

        selectionBoxFrontRightTop = generateSelectionCornerGroup(0, 0, 180);

        getChildren().addAll(selectionBoxBackLeftBottom, selectionBoxBackRightBottom,
                             selectionBoxBackLeftTop, selectionBoxBackRightTop,
                             selectionBoxFrontLeftBottom, selectionBoxFrontRightBottom,
                             selectionBoxFrontLeftTop, selectionBoxFrontRightTop);
        
//        selectionBoxFrontRightTop.getChildren().add(ambientLight);

    }

    @Override
    public void shapeChanged(ShapeProvider shapeProvider)
    {
        double halfWidth = shapeProvider.getScaledWidth() / 2;
        double halfDepth = shapeProvider.getScaledDepth() / 2;
        double halfHeight = shapeProvider.getScaledHeight() / 2;
        double minX = shapeProvider.getCentreX() - halfWidth;
        double maxX = shapeProvider.getCentreX() + halfWidth;
        double minZ = shapeProvider.getCentreZ() - halfDepth;
        double maxZ = shapeProvider.getCentreZ() + halfDepth;
        double minY = shapeProvider.getCentreY() - halfHeight;
        double maxY = shapeProvider.getCentreY() + halfHeight;

        selectionBoxBackLeftBottom.setTz(maxZ);
        selectionBoxBackLeftBottom.setTx(minX);
        selectionBoxBackLeftBottom.setTy(maxY);

        selectionBoxBackRightBottom.setTz(maxZ);
        selectionBoxBackRightBottom.setTx(maxX);
        selectionBoxBackRightBottom.setTy(maxY);

        selectionBoxFrontLeftBottom.setTz(minZ);
        selectionBoxFrontLeftBottom.setTx(minX);
        selectionBoxFrontLeftBottom.setTy(maxY);

        selectionBoxFrontRightBottom.setTz(minZ);
        selectionBoxFrontRightBottom.setTx(maxX);
        selectionBoxFrontRightBottom.setTy(maxY);

        selectionBoxBackLeftTop.setTz(maxZ);
        selectionBoxBackLeftTop.setTx(minX);
        selectionBoxBackLeftTop.setTy(minY);

        selectionBoxBackRightTop.setTz(maxZ);
        selectionBoxBackRightTop.setTx(maxX);
        selectionBoxBackRightTop.setTy(minY);

        selectionBoxFrontLeftTop.setTz(minZ);
        selectionBoxFrontLeftTop.setTx(minX);
        selectionBoxFrontLeftTop.setTy(minY);

        selectionBoxFrontRightTop.setTz(minZ);
        selectionBoxFrontRightTop.setTx(maxX);
        selectionBoxFrontRightTop.setTy(minY);

        //Place the scale boxes
//        scaleControls.place(minX, maxX, minY, maxY, minZ, maxZ);
    }

    private Xform generateSelectionCornerGroup(double xRotate, double yRotate, double zRotate)
    {

        final double cylRadius = .05;

        Xform selectionCornerTransform = new Xform();
        Group selectionCorner = new Group();
        selectionCornerTransform.getChildren().add(selectionCorner);

        Box part1 = new Box(cylRadius, cornerBracketLength, cylRadius);
        part1.setMaterial(greenMaterial);
        part1.setDrawMode(DrawMode.LINE);
        part1.setTranslateY(-cornerBracketLength / 2);

        Box part2 = new Box(cylRadius, cornerBracketLength, cylRadius);
        part2.setMaterial(greenMaterial);
        part2.setDrawMode(DrawMode.LINE);
        part2.setRotationAxis(MathUtils.zAxis);
        part2.setRotate(-90);
        part2.setTranslateX(cornerBracketLength / 2);

        Box part3 = new Box(cylRadius, cornerBracketLength, cylRadius);
        part3.setMaterial(greenMaterial);
        part3.setRotationAxis(MathUtils.xAxis);
        part3.setDrawMode(DrawMode.LINE);
        part3.setRotate(-90);
        part3.setTranslateZ(cornerBracketLength / 2);
        selectionCorner.getChildren().addAll(part1, part2, part3);

        selectionCornerTransform.setRotateX(xRotate);
        selectionCornerTransform.setRotateY(yRotate);
        selectionCornerTransform.setRotateZ(zRotate);

        return selectionCornerTransform;
    }

}
