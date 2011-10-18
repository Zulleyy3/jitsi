/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.*;
import net.java.sip.communicator.impl.gui.main.call.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.skin.*;
import net.java.sip.communicator.util.swing.*;
import net.java.sip.communicator.util.swing.event.*;

/**
 * The <tt>UnknownContactPanel</tt> replaces the contact list, when a
 * <tt>SearchFilter</tt> founds no matches. It is meant to propose the user
 * some alternatives if she's looking for a contact, which is not contained in
 * the contact list.
 *
 * @author Yana Stamcheva
 * @author Adam Netocny
 */
public class UnknownContactPanel
    extends TransparentPanel
    implements  TextFieldChangeListener,
                Skinnable
{
    private final JButton addContact = new JButton(
        GuiActivator.getResources().getI18NString("service.gui.ADD_CONTACT"));

    private final JButton callContact = new JButton(
        GuiActivator.getResources().getI18NString("service.gui.CALL_CONTACT"));

    private final JTextPane textArea = new JTextPane();

    /**
     * The main application window.
     */
    private MainFrame parentWindow;

    /**
     * An empty constructor allowing to extend this class.
     */
    public UnknownContactPanel() {}

    /**
     * Creates the <tt>UnknownContactPanel</tt> by specifying the parent window.
     * @param window the parent window
     */
    public UnknownContactPanel(MainFrame window)
    {
        super(new BorderLayout());

        this.parentWindow = window;

        TransparentPanel mainPanel = new TransparentPanel(new BorderLayout());

        this.add(mainPanel, BorderLayout.NORTH);

        TransparentPanel buttonPanel
            = new TransparentPanel(new GridLayout(0, 1));

        if (!ConfigurationManager.isAddContactDisabled())
        {
            addContact.setAlignmentX(JButton.CENTER_ALIGNMENT);

            addContact.setMnemonic(GuiActivator.getResources()
                .getI18nMnemonic("service.gui.ADD_CONTACT"));

            buttonPanel.add(addContact);

            addContact.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    AddContactDialog dialog
                        = new AddContactDialog(parentWindow);

                    dialog.setContactAddress(
                        parentWindow.getCurrentSearchText());
                    dialog.setVisible(true);
                }
            });
        }

        List<ProtocolProviderService> telephonyProviders
            = CallManager.getTelephonyProviders();

        if (telephonyProviders!= null && telephonyProviders.size() > 0)
        {
            callContact.setAlignmentX(JButton.CENTER_ALIGNMENT);

            callContact.setMnemonic(GuiActivator.getResources()
                .getI18nMnemonic("service.gui.CALL_CONTACT"));

            buttonPanel.add(callContact);

            callContact.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    String searchText = parentWindow.getCurrentSearchText();

                    if (searchText == null)
                        return;

                    CallManager.createCall(searchText, callContact);
                }
            });
        }

        initTextArea();
        mainPanel.add(textArea, BorderLayout.CENTER);

        if (callContact.getParent() != null)
        {
            textArea.setText(GuiActivator.getResources()
                .getI18NString( "service.gui.NO_CONTACTS_FOUND",
                    new String[]{'"'
                                + parentWindow.getCurrentSearchText() + '"'}));
        }
        else
        {
            textArea.setText(GuiActivator.getResources()
                .getI18NString( "service.gui.NO_CONTACTS_FOUND_SHORT"));
        }

        if (buttonPanel.getComponentCount() > 0)
        {
            TransparentPanel southPanel
                = new TransparentPanel(new FlowLayout(FlowLayout.CENTER));
            southPanel.add(buttonPanel);

            mainPanel.add(southPanel, BorderLayout.SOUTH);
        }

        loadSkin();
    }

    /**
     * Clicks the call contact button in order to call the unknown contact.
     */
    public void startCall()
    {
        callContact.doClick();
    }

    /**
     * Clicks the add contact button in order to add the unknown contact
     * to the contact list.
     */
    public void addUnknownContact()
    {
        addContact.doClick();
    }

    /**
     * Invoked when any text is inserted in the search field.
     */
    public void textInserted()
    {
        updateTextArea(parentWindow.getCurrentSearchText());
    }

    /**
     * Invoked when any text is removed from the search field.
     */
    public void textRemoved()
    {
        updateTextArea(parentWindow.getCurrentSearchText());
    }

    /**
     * Creates the text area.
     */
    private void initTextArea()
    {
        textArea.setOpaque(false);
        textArea.setEditable(false);
        StyledDocument doc = textArea.getStyledDocument();

        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(standard, textArea.getFont().getFamily());
        StyleConstants.setFontSize(standard, 12);
        doc.setParagraphAttributes(0, 0, standard, true);

        parentWindow.addSearchFieldListener(this);
    }

    /**
     * Updates the text area to take into account the new search text.
     * @param searchText the search text to update
     */
    private void updateTextArea(String searchText)
    {
        if (callContact.getParent() != null)
        {
            textArea.setText(GuiActivator.getResources()
                .getI18NString("service.gui.NO_CONTACTS_FOUND",
                    new String[]{'"' + searchText + '"'}));

            this.revalidate();
            this.repaint();
        }
    }

    /**
     * Reloads button resources.
     */
    public void loadSkin()
    {
        addContact.setIcon(GuiActivator.getResources()
            .getImage("service.gui.icons.ADD_CONTACT_16x16_ICON"));
        callContact.setIcon(GuiActivator.getResources()
            .getImage("service.gui.icons.CALL_16x16_ICON"));
    }
}