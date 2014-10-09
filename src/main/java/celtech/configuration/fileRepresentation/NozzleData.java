/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celtech.configuration.fileRepresentation;

/**
 *
 * @author Ian
 */
public class NozzleData
{

    private float diameter;
    private float defaultXOffset;
    private float minXOffset;
    private float maxXOffset;
    private float defaultYOffset;
    private float minYOffset;
    private float maxYOffset;
    private float defaultZOffset;
    private float minZOffset;
    private float maxZOffset;
    private float defaultBOffset;
    private float minBOffset;
    private float maxBOffset;
    private String associatedExtruder;

    public float getDiameter()
    {
        return diameter;
    }

    public void setDiameter(float diameter)
    {
        this.diameter = diameter;
    }

    public float getDefaultXOffset()
    {
        return defaultXOffset;
    }

    public void setDefaultXOffset(float defaultXOffset)
    {
        this.defaultXOffset = defaultXOffset;
    }

    public float getMinXOffset()
    {
        return minXOffset;
    }

    public void setMinXOffset(float minXOffset)
    {
        this.minXOffset = minXOffset;
    }

    public float getMaxXOffset()
    {
        return maxXOffset;
    }

    public void setMaxXOffset(float maxXOffset)
    {
        this.maxXOffset = maxXOffset;
    }

    public float getDefaultYOffset()
    {
        return defaultYOffset;
    }

    public void setDefaultYOffset(float defaultYOffset)
    {
        this.defaultYOffset = defaultYOffset;
    }

    public float getMinYOffset()
    {
        return minYOffset;
    }

    public void setMinYOffset(float minYOffset)
    {
        this.minYOffset = minYOffset;
    }

    public float getMaxYOffset()
    {
        return maxYOffset;
    }

    public void setMaxYOffset(float maxYOffset)
    {
        this.maxYOffset = maxYOffset;
    }

    public float getDefaultZOffset()
    {
        return defaultZOffset;
    }

    public void setDefaultZOffset(float defaultZOffset)
    {
        this.defaultZOffset = defaultZOffset;
    }

    public float getMinZOffset()
    {
        return minZOffset;
    }

    public void setMinZOffset(float minZOffset)
    {
        this.minZOffset = minZOffset;
    }

    public float getMaxZOffset()
    {
        return maxZOffset;
    }

    public void setMaxZOffset(float maxZOffset)
    {
        this.maxZOffset = maxZOffset;
    }

    public float getDefaultBOffset()
    {
        return defaultBOffset;
    }

    public void setDefaultBOffset(float defaultBOffset)
    {
        this.defaultBOffset = defaultBOffset;
    }

    public float getMinBOffset()
    {
        return minBOffset;
    }

    public void setMinBOffset(float minBOffset)
    {
        this.minBOffset = minBOffset;
    }

    public float getMaxBOffset()
    {
        return maxBOffset;
    }

    public void setMaxBOffset(float maxBOffset)
    {
        this.maxBOffset = maxBOffset;
    }

    public String getAssociatedExtruder()
    {
        return associatedExtruder;
    }

    public void setAssociatedExtruder(String associatedExtruder)
    {
        this.associatedExtruder = associatedExtruder;
    }
}