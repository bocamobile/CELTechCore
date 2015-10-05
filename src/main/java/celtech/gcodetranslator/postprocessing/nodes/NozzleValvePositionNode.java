package celtech.gcodetranslator.postprocessing.nodes;

import celtech.gcodetranslator.postprocessing.nodes.providers.Movement;
import celtech.gcodetranslator.postprocessing.nodes.providers.MovementProvider;
import celtech.gcodetranslator.postprocessing.nodes.providers.Renderable;
import celtech.gcodetranslator.postprocessing.nodes.providers.NozzlePosition;
import celtech.gcodetranslator.postprocessing.nodes.providers.NozzlePositionProvider;

/**
 *
 * @author Ian
 */
public class NozzleValvePositionNode extends GCodeEventNode implements NozzlePositionProvider, Renderable
{
    private boolean fastAsPossible = false;
    private final NozzlePosition nozzlePosition = new NozzlePosition();
    
    public void setMoveAsFastAsPossible(boolean fastAsPossible)
    {
        this.fastAsPossible = fastAsPossible;
    }

    @Override
    public String renderForOutput()
    {
        StringBuilder stringToOutput = new StringBuilder();
        stringToOutput.append('G');
        if (fastAsPossible)
        {
            stringToOutput.append('0');
        } else
        {
            stringToOutput.append('1');
        }
        stringToOutput.append(' ');
        stringToOutput.append(nozzlePosition.renderForOutput());
        stringToOutput.append(getCommentText());

        return stringToOutput.toString().trim();
    }

    @Override
    public NozzlePosition getNozzlePosition()
    {
        return nozzlePosition;
    }
}
