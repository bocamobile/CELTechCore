package celtech.coreUI.components;

import celtech.configuration.ApplicationConfiguration;
import celtech.configuration.MachineType;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author Ian
 */
public class HyperlinkedLabel extends TextFlow
{

    private final Stenographer steno = StenographerFactory.getStenographer(
        HyperlinkedLabel.class.getName());

    private StringProperty text = new SimpleStringProperty("");
    private static final Pattern hyperlinkPattern = Pattern.compile(
        "\\<a href=\"([^\"]+)\">([^<]+)</a>");
    private Map<String, URI> hyperlinkMap = new HashMap<>();

    public void replaceText(String newText)
    {
        getChildren().clear();
        hyperlinkMap.clear();

        Matcher matcher = hyperlinkPattern.matcher(newText);
        int matches = 0;
        int currentIndex = 0;

        while (matcher.find())
        {
            matches++;
            if (matcher.start() > 0)
            {
                String textPortion = newText.substring(currentIndex, matcher.start());
                addPlainText(textPortion);
                currentIndex = matcher.end();
            }
            if (matcher.groupCount() == 2)
            {
                String linkURLString = matcher.group(1);
                String linkText = matcher.group(2);
                try
                {
                    URI linkURI = new URI(linkURLString);
                    hyperlinkMap.put(linkText, linkURI);
                    Hyperlink hyperlink = new Hyperlink();
                    hyperlink.setOnAction((ActionEvent event) ->
                    {
                        Hyperlink newhyperlink = (Hyperlink) event.getSource();
                        final String clickedLinkText = newhyperlink == null ? "" : newhyperlink.
                            getText();
                        if (hyperlinkMap.containsKey(clickedLinkText))
                        {
                            URI linkToVisit = hyperlinkMap.get(clickedLinkText);
                            if (Desktop.isDesktopSupported()
                                && ApplicationConfiguration.getMachineType()
                                != MachineType.LINUX_X86
                                && ApplicationConfiguration.getMachineType()
                                != MachineType.LINUX_X64)
                            {
                                try
                                {
                                    Desktop.getDesktop().browse(linkToVisit);
                                } catch (IOException ex)
                                {
                                    steno.error("Error when attempting to browse to "
                                        + linkToVisit.
                                        toString());
                                }
                            } else
                            {
                                steno.error(
                                    "Couldn't get Desktop - not able to support hyperlinks");
                            }
                        }
                    });
                    hyperlink.setText(linkText);
                    getChildren().add(hyperlink);
                } catch (URISyntaxException ex)
                {
                    steno.error("Error attempting to create UI hyperlink from "
                        + linkURLString);
                }
            } else
            {
                steno.error("Error rendering dialog text: " + newText);
            }
        }

        if (matches == 0)
        {
            //We didn't have any hyperlinks here
            addPlainText(newText);
        }
    }

    private void addPlainText(String textPortion)
    {
        Text plainText = new Text(textPortion);
        plainText.getStyleClass().add("hyperlink-plaintext");
        getChildren().add(plainText);
    }

    public String getText()
    {
        return text.get();
    }

    public void setText(String text)
    {
        this.text.set(text);
    }

    public StringProperty textProperty()
    {
        return text;
    }
}
