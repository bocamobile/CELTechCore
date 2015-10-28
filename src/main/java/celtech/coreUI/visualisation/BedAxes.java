package celtech.coreUI.visualisation;

import celtech.coreUI.StandardColours;
import celtech.modelcontrol.ModelContainer;
import celtech.modelcontrol.ModelGroup;
import celtech.utils.Math.MathUtils;
import javafx.geometry.Point2D;
import javafx.scene.Camera;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author Ian
 */
public class BedAxes extends Pane implements CameraViewChangeListener
{

    private Stenographer steno = StenographerFactory.getStenographer(BedAxes.class.getName());
    private final Text xText = new Text();
    private final Line xAxis = new Line();
    private final Text yText = new Text();
    private final Line yAxis = new Line();
    private final Text zText = new Text();
    private final Line zAxis = new Line();
    private final Polygon xArrow = new Polygon();
    private final Polygon yArrow = new Polygon();
    private final Polygon zArrow = new Polygon();
    private final Translate xArrowTranslate = new Translate();
    private final Translate yArrowTranslate = new Translate();
    private final Translate zArrowTranslate = new Translate();
    private final Translate xTextTranslate = new Translate();
    private final Translate yTextTranslate = new Translate();
    private final Translate zTextTranslate = new Translate();
    private final Rotate xArrowRotate = new Rotate();
    private final Rotate yArrowRotate = new Rotate();
    private final Rotate zArrowRotate = new Rotate();

    private final double arrowHeight = 15;
    private final double arrowWidth = 10;
    private final double lineLength = 20;
    private final double lineWidth = 3;
    private final double textOffsetFromOrigin = lineLength + 5;

    private Point2D currentOriginScreenPosition = Point2D.ZERO;
    private final ScreenCoordinateConverter screenCoordinateConverter;

    public BedAxes(ScreenCoordinateConverter screenCoordinateConverter)
    {
        this.screenCoordinateConverter = screenCoordinateConverter;
        initialise();
    }

    public void initialise()
    {
        xText.getTransforms().add(xTextTranslate);
        xText.getStyleClass().add("bed-axis-label");
        xText.setText("X");
        xText.setFill(StandardColours.DIMENSION_LINE_GREEN);
        xAxis.setStroke(StandardColours.DIMENSION_LINE_GREEN);
        xAxis.setStrokeWidth(lineWidth);
        xArrow.setFill(StandardColours.DIMENSION_LINE_GREEN);
        xArrow.getPoints().setAll(0d, arrowWidth / 2,
                0d, -arrowWidth / 2,
                arrowHeight, 0d);
        xArrow.getTransforms().addAll(xArrowTranslate, xArrowRotate);
        xArrowRotate.setPivotX(0);
        xArrowRotate.setPivotY(0);

        yText.getTransforms().add(yTextTranslate);
        yText.getStyleClass().add("bed-axis-label");
        yText.setText("Y");
        yText.setFill(StandardColours.DIMENSION_LINE_GREEN);
        yAxis.setStroke(StandardColours.DIMENSION_LINE_GREEN);
        yAxis.setStrokeWidth(lineWidth);
        yArrow.setFill(StandardColours.DIMENSION_LINE_GREEN);
        yArrow.getPoints().setAll(0d, arrowWidth / 2,
                0d, -arrowWidth / 2,
                arrowHeight, 0d);
        yArrow.getTransforms().addAll(yArrowTranslate, yArrowRotate);
        yArrowRotate.setPivotX(0);
        yArrowRotate.setPivotY(0);

        zText.getTransforms().add(zTextTranslate);
        zText.getStyleClass().add("bed-axis-label");
        zText.setText("Z");
        zText.setFill(StandardColours.DIMENSION_LINE_GREEN);
        zAxis.setStroke(StandardColours.DIMENSION_LINE_GREEN);
        zAxis.setStrokeWidth(lineWidth);

        zArrow.setFill(StandardColours.DIMENSION_LINE_GREEN);
        zArrow.getPoints().setAll(0d, arrowWidth / 2,
                0d, -arrowWidth / 2,
                arrowHeight, 0d);
        zArrow.getTransforms().addAll(zArrowTranslate, zArrowRotate);
        zArrowRotate.setPivotX(0);
        zArrowRotate.setPivotY(0);

        getChildren().addAll(xArrow, xAxis, xText);
        getChildren().addAll(yArrow, yAxis, yText);
        getChildren().addAll(zArrow, zAxis, zText);

        setMouseTransparent(true);
    }

    private double calculateAngle(Point2D origin, Point2D end)
    {
        double xDifference = end.getX() - origin.getX();
        double yDifference = end.getY() - origin.getY();

        double opposite = yDifference;
        double adjacent = xDifference;

        double theta = Math.atan(opposite / adjacent);
        double angle = theta * MathUtils.RAD_TO_DEG;

        if (xDifference < 0)
        {
            if (angle < 0)
            {
                angle = 180 + angle;
            } else
            {
                angle = -180 + angle;
            }
        }

        return angle;
    }

    private void updateArrowAndTextPosition()
    {
        Point2D origin = screenCoordinateConverter.convertWorldCoordinatesToScreen(0, 0, 0);
        Point2D xAxisVector = screenCoordinateConverter.convertWorldCoordinatesToScreen(lineLength, 0, 0);
        Point2D xTextPosition = screenCoordinateConverter.convertWorldCoordinatesToScreen(textOffsetFromOrigin, 0, 0);
        Point2D yAxisVector = screenCoordinateConverter.convertWorldCoordinatesToScreen(0, 0, lineLength);
        Point2D yTextPosition = screenCoordinateConverter.convertWorldCoordinatesToScreen(0, 0, textOffsetFromOrigin);
        Point2D zAxisVector = screenCoordinateConverter.convertWorldCoordinatesToScreen(0, -lineLength, 0);
        Point2D zTextPosition = screenCoordinateConverter.convertWorldCoordinatesToScreen(0, -textOffsetFromOrigin, 0);

        Point2D originLocal = screenToLocal(origin);
        Point2D xAxisLocalEnd = screenToLocal(xAxisVector);
        Point2D yAxisLocalEnd = screenToLocal(yAxisVector);
        Point2D zAxisLocalEnd = screenToLocal(zAxisVector);

        xTextTranslate.setX(xTextPosition.getX());
        xTextTranslate.setY(xTextPosition.getY());
        xAxis.setStartX(originLocal.getX());
        xAxis.setStartY(originLocal.getY());
        xAxis.setEndX(xAxisLocalEnd.getX());
        xAxis.setEndY(xAxisLocalEnd.getY());
        xArrowTranslate.setX(xAxisLocalEnd.getX());
        xArrowTranslate.setY(xAxisLocalEnd.getY());
        double xAngle = calculateAngle(originLocal, xAxisLocalEnd);
        xArrowRotate.setAngle(xAngle);

        yTextTranslate.setX(yTextPosition.getX());
        yTextTranslate.setY(yTextPosition.getY());
        yAxis.setStartX(originLocal.getX());
        yAxis.setStartY(originLocal.getY());
        yAxis.setEndX(yAxisLocalEnd.getX());
        yAxis.setEndY(yAxisLocalEnd.getY());
        yArrowTranslate.setX(yAxisLocalEnd.getX());
        yArrowTranslate.setY(yAxisLocalEnd.getY());
        double yAngle = calculateAngle(originLocal, yAxisLocalEnd);
        yArrowRotate.setAngle(yAngle);

        zTextTranslate.setX(zTextPosition.getX());
        zTextTranslate.setY(zTextPosition.getY());
        zAxis.setStartX(originLocal.getX());
        zAxis.setStartY(originLocal.getY());
        zAxis.setEndX(zAxisLocalEnd.getX());
        zAxis.setEndY(zAxisLocalEnd.getY());
        zArrowTranslate.setX(zAxisLocalEnd.getX());
        zArrowTranslate.setY(zAxisLocalEnd.getY());
        double zAngle = calculateAngle(originLocal, zAxisLocalEnd);
        zArrowRotate.setAngle(zAngle);
    }

    @Override
    public void heresYourCamera(Camera camera)
    {
    }

    @Override
    public void cameraViewOfYouHasChanged()
    {
        updateArrowAndTextPosition();
    }
}
