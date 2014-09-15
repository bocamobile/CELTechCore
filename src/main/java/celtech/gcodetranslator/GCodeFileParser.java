package celtech.gcodetranslator;

import celtech.gcodetranslator.events.BlankLineEvent;
import celtech.gcodetranslator.events.CommentEvent;
import celtech.gcodetranslator.events.EndOfFileEvent;
import celtech.gcodetranslator.events.ExtrusionEvent;
import celtech.gcodetranslator.events.GCodeEvent;
import celtech.gcodetranslator.events.GCodeParseEvent;
import celtech.gcodetranslator.events.LayerChangeEvent;
import celtech.gcodetranslator.events.MCodeEvent;
import celtech.gcodetranslator.events.NozzleChangeEvent;
import celtech.gcodetranslator.events.RetractDuringExtrusionEvent;
import celtech.gcodetranslator.events.RetractEvent;
import celtech.gcodetranslator.events.SpiralExtrusionEvent;
import celtech.gcodetranslator.events.TravelEvent;
import celtech.gcodetranslator.events.UnretractEvent;
import celtech.utils.SystemUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.DoubleProperty;
import libertysystems.stenographer.Stenographer;
import libertysystems.stenographer.StenographerFactory;

/**
 *
 * @author Ian
 */
public class GCodeFileParser {

    private final Stenographer steno = StenographerFactory.getStenographer(GCodeFileParser.class.getName());
    private final ArrayList<GCodeTranslationEventHandler> listeners = new ArrayList<>();

    /**
     *
     * @param eventHandler
     */
    public void addListener(GCodeTranslationEventHandler eventHandler) {
        listeners.add(eventHandler);
    }

    /**
     *
     * @param eventHandler
     */
    public void removeListener(GCodeTranslationEventHandler eventHandler) {
        listeners.remove(eventHandler);
    }

    /**
     *
     * @param inputfilename
     * @param percentProgress
     */
    public void parse(String inputfilename, DoubleProperty percentProgress) {
        File inputFile = new File(inputfilename);

        int linesInFile = SystemUtils.countLinesInFile(inputFile);
        int linesSoFar = 0;
        double lastPercentSoFar = 0;

        ArrayList<String> lineRepository = new ArrayList<>();

        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(inputFile));

            String line;
            while ((line = fileReader.readLine()) != null) {
                linesSoFar++;
                double percentSoFar = ((double) linesSoFar / (double) linesInFile) * 100;
                if (percentSoFar - lastPercentSoFar >= 1) {
                    percentProgress.set(percentSoFar);
                    lastPercentSoFar = percentSoFar;
                }

                GCodeParseEvent eventToOutput = null;

                String comment = null;

                boolean invalidLine = false;
                boolean passthroughLine = false;

                boolean xPresent = false;
                double xValue = 0.0;

                boolean yPresent = false;
                double yValue = 0.0;

                boolean zPresent = false;
                double zValue = 0.0;

                boolean ePresent = false;
                double eValue = 0.0;

                boolean fPresent = false;
                double fValue = 0.0;

                boolean gPresent = false;
                int gValue = 0;

                boolean mPresent = false;
                int mValue = 0;

                boolean sPresent = false;
                int sValue = 0;

                boolean tPresent = false;
                int tValue = 0;

                String[] commentSplit = line.trim().split(";");

                if (commentSplit.length == 2) {
                    //There was a comment
                    comment = commentSplit[1];
                }

                String[] lineParts = commentSplit[0].split(" ");

                for (String partToConsider : lineParts) {
                    if (partToConsider != null) {
                        if (partToConsider.length() > 0) {
                            char command = partToConsider.charAt(0);
                            String value = partToConsider.substring(1);

                            switch (command) {
                                case 'G':
                                    gValue = Integer.valueOf(value);
                                    gPresent = true;
                                    break;
                                case 'M':
                                    mPresent = true;
                                    mValue = Integer.valueOf(value);
                                    break;
                                case 'S':
                                    sPresent = true;
                                    sValue = Integer.valueOf(value);
                                    break;
                                case 'T':
                                    tPresent = true;
                                    tValue = Integer.valueOf(value);
                                    break;
                                case 'X':
                                    xPresent = true;
                                    xValue = Double.valueOf(value);
                                    break;
                                case 'Y':
                                    yPresent = true;
                                    yValue = Double.valueOf(value);
                                    break;
                                case 'Z':
                                    zPresent = true;
                                    zValue = Double.valueOf(value);
                                    break;
                                case 'E':
                                    ePresent = true;
                                    eValue = Double.valueOf(value);
                                    break;
                                case 'F':
                                    fPresent = true;
                                    fValue = Double.valueOf(value);
                                    break;
                            }
                        } else {
                            invalidLine = true;
                        }
                    } else {
                        invalidLine = true;
                        steno.debug("Discarded null");
                    }
                }

                if (mPresent) {
                    MCodeEvent event = new MCodeEvent();

                    event.setMNumber(mValue);

                    if (sPresent) {
                        event.setSNumber(sValue);
                    }

                    if (comment != null) {
                        event.setComment(comment);
                    }

                    eventToOutput = event;
                } else if (tPresent) {
                    NozzleChangeEvent event = new NozzleChangeEvent();
                    event.setNozzleNumber(tValue);

                    if (comment != null) {
                        event.setComment(comment);
                    }

                    eventToOutput = event;
                } else if (gPresent && !xPresent && !yPresent && !zPresent && !ePresent) {
                    GCodeEvent event = new GCodeEvent();

                    event.setGNumber(gValue);

                    if (comment != null) {
                        event.setComment(comment);
                    }

                    eventToOutput = event;
                } else if (gPresent && zPresent && !xPresent && !yPresent && !ePresent) {
                    LayerChangeEvent event = new LayerChangeEvent();

                    event.setZ(zValue);

                    if (fPresent) {
                        event.setFeedRate(fValue);
                    }

                    if (comment != null) {
                        event.setComment(comment);
                    }

                    eventToOutput = event;
                } else if (gPresent && ePresent && !xPresent && !yPresent && !zPresent) {
                    if (eValue < 0) {
                        //Must be a retract
                        RetractEvent event = new RetractEvent();

                        event.setE(eValue);

                        if (fPresent) {
                            event.setFeedRate(fValue);
                        }

                        if (comment != null) {
                            event.setComment(comment);
                        }

                        eventToOutput = event;
                    } else {
                        UnretractEvent event = new UnretractEvent();
                        event.setE(eValue);

                        if (fPresent) {
                            event.setFeedRate(fValue);
                        }

                        if (comment != null) {
                            event.setComment(comment);
                        }

                        eventToOutput = event;
                    }
                } else if (gPresent && xPresent && yPresent && !ePresent && !zPresent) {
                    TravelEvent event = new TravelEvent();
                    event.setX(xValue);
                    event.setY(yValue);

                    if (fPresent) {
                        event.setFeedRate(fValue);
                    }

                    if (comment != null) {
                        event.setComment(comment);
                    }

                    eventToOutput = event;
                } else if (gPresent && xPresent && yPresent && ePresent && !zPresent) {
                    if (eValue > 0) {
                        ExtrusionEvent event = new ExtrusionEvent();

                        event.setX(xValue);
                        event.setY(yValue);
                        event.setE(eValue);

                        if (fPresent) {
                            event.setFeedRate(fValue);
                        }

                        if (comment != null) {
                            event.setComment(comment);
                        }

                        eventToOutput = event;
                    } else {
                        RetractDuringExtrusionEvent event = new RetractDuringExtrusionEvent();

                        event.setX(xValue);
                        event.setY(yValue);
                        event.setE(eValue);

                        if (fPresent) {
                            event.setFeedRate(fValue);
                        }

                        if (comment != null) {
                            event.setComment(comment);
                        }

                        eventToOutput = event;
                    }
                } else if (comment != null && !passthroughLine) {
                    CommentEvent event = new CommentEvent();
                    event.setComment(comment);

                    eventToOutput = event;
                } else if (line.equals("")) {
                    BlankLineEvent event = new BlankLineEvent();

                    eventToOutput = event;
                } else {
                    for (GCodeTranslationEventHandler listener : listeners) {
                        listener.unableToParse(line);
                    }
                }

                if (eventToOutput != null) {
                    eventToOutput.setLinesSoFar(linesSoFar);
                    try {
                        for (GCodeTranslationEventHandler listener : listeners) {
                            listener.processEvent(eventToOutput);
                        }
                    } catch (PostProcessingError ex) {
                        steno.error("Error processing event - aborting - " + eventToOutput);
                    }
                }
            }

            //End of file - poke the processor
            try {
                for (GCodeTranslationEventHandler listener : listeners) {
                    listener.processEvent(new EndOfFileEvent());
                }
            } catch (PostProcessingError ex) {
                steno.error("Error processing end of file event");
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
        } catch (IOException ex) {
            System.out.println("IO Exception");
        }

    }

}
