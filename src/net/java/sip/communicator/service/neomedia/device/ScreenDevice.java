/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.neomedia.device;

import java.awt.*;

/**
 * The <tt>ScreenDevice</tt> interface represent physical screen display.
 *
 * @author Sebastien Vincent
 */
public interface ScreenDevice
{
    /**
     * Get the screen index.
     *
     * @return screen index
     */
    public int getIndex();

    /**
     * If the screen contains specified point.
     *
     * @param p point coordinate
     * @return true if point belongs to screen, false otherwise
     */
    public boolean containsPoint(Point p);

    /**
     * Get current resolution of <tt>ScreenDevice</tt> device.
     *
     * @return current resolution of the screen
     */
    public Dimension getSize();
}
