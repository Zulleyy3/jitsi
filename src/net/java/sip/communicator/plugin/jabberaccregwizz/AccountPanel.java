/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.jabberaccregwizz;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

/**
 * @author Yana Stamcheva
 */
public class AccountPanel
    extends TransparentPanel
    implements  DocumentListener,
                ValidatingPanel
{
    private static final Logger logger = Logger.getLogger(AccountPanel.class);

    private final JPanel userIDPassPanel
        = new TransparentPanel(new BorderLayout(10, 10));

    private final JPanel labelsPanel = new TransparentPanel();

    private final JPanel valuesPanel = new TransparentPanel();

    private final JLabel passLabel
        = new JLabel(Resources.getString("service.gui.PASSWORD"));

    private final JPanel emptyPanel = new TransparentPanel();

    private final JTextField userIDField = new TrimTextField();

    private final JPasswordField passField = new JPasswordField();

    private final JCheckBox rememberPassBox = new SIPCommCheckBox(Resources
        .getString("service.gui.REMEMBER_PASSWORD"));

    private final JabberAccountRegistrationForm parentForm;

    private final JRadioButton existingAccountButton;

    private final JRadioButton createAccountButton;

    private final JPanel mainPanel
        = new TransparentPanel(new BorderLayout(5, 5));

    private Component registrationForm;

    private Component registerChoicePanel;

    /**
     * Creates an instance of <tt>AccountPanel</tt> by specifying the parent
     * wizard page, where it's contained.
     * @param parentForm the parent form where this panel is contained
     */
    public AccountPanel(final JabberAccountRegistrationForm parentForm)
    {
        super(new BorderLayout());

        this.parentForm = parentForm;
        this.parentForm.addValidatingPanel(this);

        JLabel userIDLabel
            = new JLabel(parentForm.getUsernameLabel());

        JLabel userIDExampleLabel = new JLabel(parentForm.getUsernameExample());

        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));

        userIDField.getDocument().addDocumentListener(this);
        rememberPassBox.setSelected(true);

        existingAccountButton = new JRadioButton(
            parentForm.getExistingAccountLabel());

        createAccountButton = new JRadioButton(
            parentForm.getCreateAccountLabel());

        userIDExampleLabel.setForeground(Color.GRAY);
        userIDExampleLabel.setFont(userIDExampleLabel.getFont().deriveFont(8));
        emptyPanel.setMaximumSize(new Dimension(40, 35));
        userIDExampleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        labelsPanel.add(userIDLabel);
        labelsPanel.add(emptyPanel);
        labelsPanel.add(passLabel);

        valuesPanel.add(userIDField);
        valuesPanel.add(userIDExampleLabel);
        valuesPanel.add(passField);

        userIDPassPanel.add(labelsPanel, BorderLayout.WEST);
        userIDPassPanel.add(valuesPanel, BorderLayout.CENTER);
        userIDPassPanel.setBorder(
                BorderFactory.createTitledBorder(
                        Resources.getString(
                                "plugin.sipaccregwizz.USERNAME_AND_PASSWORD")));

        JPanel southPanel = new TransparentPanel(new BorderLayout());
        southPanel.add(rememberPassBox, BorderLayout.WEST);

        String homeLinkString = parentForm.getHomeLinkLabel();

        if (homeLinkString != null && homeLinkString.length() > 0)
        {
            String homeLink = Resources.getSettingsString(
                    "service.gui.APPLICATION_WEB_SITE");

            southPanel.add( createHomeLink(homeLinkString, homeLink),
                            BorderLayout.EAST);
        }

        userIDPassPanel.add(southPanel, BorderLayout.SOUTH);

        this.add(mainPanel, BorderLayout.NORTH);
    }

    /**
     * Creates a register choice panel.
     * @return the created component
     */
    private Component createRegisterChoicePanel()
    {
        JPanel registerChoicePanel = new TransparentPanel(new GridLayout(0, 1));

        existingAccountButton.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if (existingAccountButton.isSelected())
                {
                    mainPanel.remove(registrationForm);
                    mainPanel.add(userIDPassPanel, BorderLayout.CENTER);

                    Window window
                        = SwingUtilities.getWindowAncestor(AccountPanel.this);

                    if (window != null)
                        window.pack();
                }
            }
        });

        createAccountButton.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if (createAccountButton.isSelected())
                {
                    mainPanel.remove(userIDPassPanel);
                    mainPanel.add(registrationForm, BorderLayout.CENTER);
                    SwingUtilities.getWindowAncestor(AccountPanel.this).pack();
                }
            }
        });

        ButtonGroup buttonGroup = new ButtonGroup();

        existingAccountButton.setOpaque(false);
        createAccountButton.setOpaque(false);

        buttonGroup.add(existingAccountButton);
        buttonGroup.add(createAccountButton);

        registerChoicePanel.add(existingAccountButton);
        registerChoicePanel.add(createAccountButton);

        // By default we select the existing account button.
        existingAccountButton.setSelected(true);

        return registerChoicePanel;
    }

    /**
     * Creates the home link label.
     *
     * @param homeLinkText the text of the home link
     * @param homeLink the link
     * @return the created component
     */
    private Component createHomeLink(   String homeLinkText,
                                        final String homeLink)
    {
        JLabel homeLinkLabel =
            new JLabel("<html><a href='"+ homeLink +"'>"
                + homeLinkText + "</a></html>",
                JLabel.RIGHT);

        homeLinkLabel.setFont(homeLinkLabel.getFont().deriveFont(10f));
        homeLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeLinkLabel.setToolTipText(
                Resources.getString(
                "plugin.simpleaccregwizz.SPECIAL_SIGNUP"));
        homeLinkLabel.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                try
                {
                    JabberAccRegWizzActivator.getBrowserLauncher()
                        .openURL(homeLink);
                }
                catch (UnsupportedOperationException ex)
                {
                    // This should not happen, because we check if the
                    // operation is supported, before adding the sign
                    // up.
                    logger.error("The web sign up is not supported.",
                        ex);
                }
            }
        });

        return homeLinkLabel;
    }

    /**
     * Handles the <tt>DocumentEvent</tt> triggered when user types in the
     * UserID field. Enables or disables the "Next" wizard button according to
     * whether the UserID field is empty.
     *
     * @param evt the document event that has triggered this method call.
     */
    public void insertUpdate(DocumentEvent evt)
    {
        parentForm.setServerFieldAccordingToUIN(userIDField.getText());
        parentForm.reValidateInput();
    }

    /**
     * Handles the <tt>DocumentEvent</tt> triggered when user deletes letters
     * from the User ID field. Enables or disables the "Next" wizard button
     * according to whether the User ID field is empty.
     *
     * @param evt the document event that has triggered this method call.
     */
    public void removeUpdate(DocumentEvent evt)
    {
        parentForm.setServerFieldAccordingToUIN(userIDField.getText());
        parentForm.reValidateInput();
    }

    public void changedUpdate(DocumentEvent evt) {}

    /**
     * Returns the username entered in this panel.
     * @return the username entered in this panel
     */
    String getUsername()
    {
        return userIDField.getText();
    }

    /**
     * Sets the username to display in the username field.
     * @param username the username to set
     */
    void setUsername(String username)
    {
        userIDField.setText(username);
        userIDField.setEnabled(false);
    }

    /**
     * Returns the password entered in this panel.
     * @return the password entered in this panel
     */
    char[] getPassword()
    {
        return passField.getPassword();
    }

    /**
     * Sets the password to display in the password field of this panel.
     * @param password the password to set
     */
    void setPassword(String password)
    {
        passField.setText(password);
    }

    /**
     * Indicates if the remember password box is selected.
     * @return <tt>true</tt> if the remember password check box is selected,
     * otherwise returns <tt>false</tt>
     */
    boolean isRememberPassword()
    {
        return rememberPassBox.isSelected();
    }

    /**
     * Selects/deselects the remember password check box depending on the given
     * <tt>isRemember</tt> parameter.
     * @param isRemember indicates if the remember password checkbox should be
     * selected or not
     */
    void setRememberPassword(boolean isRemember)
    {
        rememberPassBox.setSelected(isRemember);
    }

    /**
     * Whether current inserted values into the panel are valid and enough
     * to continue with account creation/modification.
     *
     * @return whether the input values are ok to continue with account
     * creation/modification.
     */
    public boolean isValidated()
    {
        return userIDField.getText() != null
                && userIDField.getText().length() > 0;
    }

    /**
     * Sets to <tt>true</tt> if this panel is opened in a simple form and
     * <tt>false</tt> if it's opened in an advanced form.
     *
     * @param isSimpleForm indicates if this panel is opened in a simple form or
     * in an advanced form
     */
    void setSimpleForm(boolean isSimpleForm)
    {
        JabberAccountCreationFormService createAccountService
            = parentForm.getCreateAccountService();

        mainPanel.removeAll();

        if (isSimpleForm)
        {
            if (createAccountService != null)
            {
                registrationForm = createAccountService.getForm();
                registerChoicePanel = createRegisterChoicePanel();

                mainPanel.add(registerChoicePanel, BorderLayout.NORTH);
            }
            else
            {
                JPanel registerPanel = new TransparentPanel();

                registerPanel.setLayout(
                    new BoxLayout(registerPanel, BoxLayout.Y_AXIS));

                String createAccountInfoString
                    = parentForm.getCreateAccountLabel();

                if (createAccountInfoString != null
                    && createAccountInfoString.length() > 0)
                {
                    registerPanel.add(
                        createRegisterArea(createAccountInfoString));
                }

                String createAccountString
                    = parentForm.getCreateAccountButtonLabel();

                if (createAccountString != null
                        && createAccountString.length() > 0)
                {
                    JPanel buttonPanel = new TransparentPanel(
                            new FlowLayout(FlowLayout.CENTER));

                    buttonPanel.add(createRegisterButton(createAccountString));

                    registerPanel.add(buttonPanel);
                }

                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                mainPanel.add(userIDPassPanel);
                mainPanel.add(Box.createVerticalStrut(10));

                if (registerPanel.getComponentCount() > 0)
                {
                    registerPanel.setBorder(
                        BorderFactory.createTitledBorder(""));

                    mainPanel.add(registerPanel);
                }
            }
        }
        else
        {
            mainPanel.add(userIDPassPanel, BorderLayout.NORTH);
        }
    }

    /**
     * Indicates if the account information provided by this form is for new
     * account or an existing one.
     * @return <tt>true</tt> if the account information provided by this form
     * is for new account or <tt>false</tt> if it's for an existing one
     */
    boolean isCreateAccount()
    {
        return createAccountButton.isSelected();
    }

    /**
     * Creates the register area.
     *
     * @param text the text to show to the user
     * @return the created component
     */
    private Component createRegisterArea(String text)
    {
        JEditorPane registerArea = new JEditorPane();

        registerArea.setAlignmentX(JEditorPane.CENTER_ALIGNMENT);
        registerArea.setOpaque(false);
        registerArea.setContentType("text/html");
        registerArea.setEditable(false);
        registerArea.setText(text);
        /* Display the description with the font we use elsewhere in the UI. */
        registerArea.putClientProperty(
                JEditorPane.HONOR_DISPLAY_PROPERTIES,
                true);
        registerArea.addHyperlinkListener(new HyperlinkListener()
            {
                public void hyperlinkUpdate(HyperlinkEvent e)
                {
                    if (e.getEventType()
                            .equals(HyperlinkEvent.EventType.ACTIVATED))
                    {
                        JabberAccRegWizzActivator
                            .getBrowserLauncher().openURL(e.getURL().toString());
                    }
                }
            });

        return registerArea;
    }

    /**
     * Creates the register button.
     *
     * @param text the text of the button
     * @return the created component
     */
    private Component createRegisterButton(String text)
    {
        JButton registerButton = new JButton(text);

        registerButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Reg OK");

                if (parentForm.isWebSignupSupported())
                {
                    parentForm.webSignup();
                }
            }
        });

        return registerButton;
    }
}
