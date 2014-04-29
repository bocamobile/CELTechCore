/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.coreUI.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author Ian
 */
public class InfoScreenIndicatorController implements Initializable
{

    @FXML
    private Group blueParts;

    private Color blueColour = Color.rgb(38, 166, 217);
    private Color greyColour = Color.rgb(115, 115, 115);

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    }

    public void setSelected(boolean selected)
    {
        if (selected)
        {
            for (Node node : blueParts.getChildren())
            {
                if (node instanceof SVGPath)
                {
                    ((SVGPath) node).setFill(greyColour);
                }
            }
        } else
        {
            for (Node node : blueParts.getChildren())
            {
                if (node instanceof SVGPath)
                {
                    ((SVGPath) node).setFill(blueColour);
                }
            }
        }
    }

}
