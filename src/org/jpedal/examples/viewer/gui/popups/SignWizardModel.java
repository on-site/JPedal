/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/java-pdf-library-support/
 *
 * (C) Copyright 1997-2013, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * SignWizardModel.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.popups;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.viewer.objects.SignData;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.examples.viewer.utils.ItextFunctions;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**This class implements the WizardPanelModel and in this
 * case contains the JPanels to be drawn as inner classes.
 * The methods in SignWizardModel are mainly concerned with
 * controlling what panels are next and whether they can be
 * currently reached.
 */
public class SignWizardModel implements WizardPanelModel 
{	
	//Each panel must have a unique String identifier 
	private static final String MODE_SELECT = "0";
	private static final String PFX_PANEL = "1"; 
	private static final String KEYSTORE_PANEL = "3";
	private static final String COMMON_PANEL = "4";
	private static final String ENCRYPTION_PANEL = "5";
	private static final String VISIBLE_SIGNATURE_PANEL = "6";
	
	public static final String NO_FILE_SELECTED = Messages.getMessage("PdfSigner.NoFileSelected");
	
	private static final int MAXIMUM_PANELS = 5;
		
	private SignData signData;
	private PdfDecoder pdfDecoder;
	private String rootDir;
	
	/*The JPanels in this wizard */
	private ModeSelect modeSelect;
	private PFXPanel pFXPanel;
	private KeystorePanel keystorePanel;
	private CommonPanel commonPanel;
	private EncryptionPanel encryptionPanel;
	private SignaturePanel signaturePanel;
	
	/*Maps the JPanels' ID to the panel*/
	private Map panels;
	
	/* The ID of the currently displayed panel*/
	private String currentPanel;
	
	/**
	 * @param signData Will contain all the information acquired from the user for signing a Pdf 
	 * @param pdfFile The path to the Pdf document to be signed.
	 */
	public SignWizardModel(SignData signData, String pdfFile, String rootDir) 
	{
		this.signData = signData;
		this.rootDir = rootDir;
		
		pdfDecoder = new PdfDecoder();
		try {
			pdfDecoder.openPdfFile(pdfFile);
		}
		catch (Exception e) {
            e.printStackTrace();
		}
		
		if(pdfDecoder.isEncrypted()) {
			String password =System.getProperty("org.jpedal.password");
			if(password != null) {
				try {
					pdfDecoder.setEncryptionPassword(password);
				} catch (PdfException e) {
					e.printStackTrace();
				}
			}
		}
		/*JPanel contents vary depending on whether the Pdf has bee previously signed.*/
		testForSignedPDF();
		
		panels = new HashMap();
	    modeSelect = new ModeSelect();
	    pFXPanel = new PFXPanel();
	    keystorePanel = new KeystorePanel();
	    commonPanel = new CommonPanel();	    
	    encryptionPanel = new EncryptionPanel();
	    signaturePanel = new SignaturePanel();
	    
	    panels.put(MODE_SELECT, modeSelect);
	    panels.put(PFX_PANEL, pFXPanel);
	    panels.put(KEYSTORE_PANEL, keystorePanel);
	    panels.put(COMMON_PANEL, commonPanel);
	    panels.put(ENCRYPTION_PANEL, encryptionPanel);
	    panels.put(VISIBLE_SIGNATURE_PANEL, signaturePanel);
	    
	    currentPanel = MODE_SELECT;
	}
	
	/**
	 * A map of the JPanels the Wizard Dialog should contain.
	 * 
	 * @return The ID strings mapped to their corresponding JPanels
	 */
	public Map getJPanels()
	{
		return panels;
	}

	/**
	 * Advance to the next JPanel.
	 * 
	 * @return Unique identifier for the now current JPanel
	 */
	public String next()
	{
		updateSignData();
		
		if(currentPanel.equals(MODE_SELECT)) {
			if(!signData.isKeystoreSign()) {
				return currentPanel = PFX_PANEL;
			}
			else {
				return currentPanel = KEYSTORE_PANEL;
			}	
		}
		else if(currentPanel.equals(PFX_PANEL)) {
			return currentPanel = VISIBLE_SIGNATURE_PANEL;
		}
		else if(currentPanel.equals(KEYSTORE_PANEL)) {
			return currentPanel = VISIBLE_SIGNATURE_PANEL;
		}			
		else if(currentPanel.equals(VISIBLE_SIGNATURE_PANEL)) {
			return currentPanel = ENCRYPTION_PANEL;
		}
		else if(currentPanel.equals(ENCRYPTION_PANEL)) {
			return currentPanel = COMMON_PANEL;
		}
		/* The following exception should never be thrown and is here to alerted me 
		 * should I create a trail of panels that is incorrect */
		throw new NullPointerException("Whoops! Tried to move to a nextID where there is no nextID to be had");
	}

	/**
	 * Set the current JPanel to the previous JPanel.
	 * 
	 * @return Unique identifier for the now current JPanel
	 */
	public String previous()
	{
		updateSignData();
		if(currentPanel.equals(PFX_PANEL) || currentPanel.equals(KEYSTORE_PANEL)) {
			return currentPanel = MODE_SELECT;
		}
		else if(currentPanel.equals(ENCRYPTION_PANEL)) {
			return currentPanel = VISIBLE_SIGNATURE_PANEL;
		}
		else if(currentPanel.equals(VISIBLE_SIGNATURE_PANEL)) {
			if(signData.isKeystoreSign()) {
			    return currentPanel = KEYSTORE_PANEL;
			}
			else {
				return currentPanel = PFX_PANEL;
		    }
		}
		else if(currentPanel.equals(COMMON_PANEL)){
            return currentPanel = ENCRYPTION_PANEL;
		}

		throw new NullPointerException("Tried to move to get a previousID where there is no previous");
	}

	public boolean hasPrevious()
	{
		return !currentPanel.equals(MODE_SELECT);
	}

	public String getStartPanelID()
	{
		return MODE_SELECT;
	}
	
	public boolean isFinishPanel()
	{
		return currentPanel==COMMON_PANEL;
	}
	
	/**
	 * Indicates whether the next or finish button can be enabled.
	 * 
	 * @return true if the current panel can be advanced in its current state
	 */
	public boolean canAdvance()
	{
		if(currentPanel.equals(COMMON_PANEL)) {
			return commonPanel.canFinish();
		}
		else if(currentPanel.equals(PFX_PANEL)){
		    return pFXPanel.canAdvance();
		}
		else if(currentPanel.equals(KEYSTORE_PANEL)){
			return keystorePanel.canAdvance();
		}
		else if(currentPanel.equals(ENCRYPTION_PANEL)) {
			return encryptionPanel.canAdvance();
		}
		else {
			return true;
		}
	}
	
	/**
	 * Harvest user data from the currently displayed panel
	 */
	public void updateSignData()
	{
		if(currentPanel.equals(PFX_PANEL)) {
			pFXPanel.collectData();
		}
		else if(currentPanel.equals(KEYSTORE_PANEL)) {
			keystorePanel.collectData();
		}
		else if(currentPanel.equals(COMMON_PANEL)) {
			commonPanel.collectData();
		}
		else if(currentPanel.equals(ENCRYPTION_PANEL)) {
			encryptionPanel.collectData();
		}
		else if(currentPanel.equals(MODE_SELECT)) {
			modeSelect.collectData();
		}	
		else if(currentPanel.equals(VISIBLE_SIGNATURE_PANEL)) {
			signaturePanel.collectData();
		}
 		else {
 			/*Should never be throw, here to indicate if I've made a mistake in the flow of the JPanels */
			throw new NullPointerException("Tried to update a panel which doesnt exist");
		}
	}
	
	/**
	 * When an event is triggered with one of the registered panels
	 * the wizard will call back this class and check if the panel can be advanced.
	 * 
	 * @param wizard Listeners to enable/disable advance button 
	 */
	public void registerNextChangeListeners(ChangeListener wizard)
	{
		commonPanel.registerChange(wizard);
		pFXPanel.registerChange(wizard);
		keystorePanel.registerChange(wizard);
		encryptionPanel.registerChange(wizard);
	}
	
	/**
	 * Same as the previous method but listens for key changes instead.
	 * 
	  @param wizard Listeners to enable/disable advance button 
	 */
	public void registerNextKeyListeners(KeyListener wizard)
	{
		pFXPanel.registerListener(wizard);
		keystorePanel.registerNextKeyListeners(wizard);
		encryptionPanel.registerNextKeyListeners(wizard);
	}
	
	/**
	 * To avoid memory leaks I want to close the decoder I opened in this
	 * class when ever the dialog is closed.  Also collects any last
	 * data.
	 */
	public void close()
	{
		updateSignData();
		pdfDecoder.closePdfFile();
	}
	
	/**
	 * Don't want to corrupt any Pdf files so a check is performed
	 * to find whether a signature should be appended to the document
	 * or created fresh. 
	 */
	private void testForSignedPDF()
	{
		signData.setAppend(false);
		
		for(int page = 1; page<=pdfDecoder.getPageCount(); page++) {
		    try {
				pdfDecoder.decodePage(page);
				pdfDecoder.waitForDecodingToFinish();
		        AcroRenderer currentFormRenderer = pdfDecoder.getFormRenderer();
		        Iterator signatureObjects = currentFormRenderer.getSignatureObjects();
		        if(signatureObjects!=null) {
		        	signData.setAppend(true);
		        	break;
		        }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean isPdfSigned()
	{
        return signData.isAppendMode();
	}
	
	/**
	 * The individual JPanels that I want to show in the Wizard
	 */
	private class PFXPanel extends JPanel 
	{
		private JLabel keyFileLabel = new JLabel();
		private JButton browseKeyButton = new JButton();
		private JLabel currentKeyFilePath = new JLabel(NO_FILE_SELECTED); 
		private JCheckBox visiblePassCheck = new JCheckBox();

		private JLabel passwordLabel = new JLabel();
		private JPasswordField passwordField = new JPasswordField();
		
		private volatile boolean keyNext = false;
		private volatile boolean passNext = false;
		
		private int y = 0;

		public PFXPanel() 
		{
			try
			{
				init();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		
		private void init() throws Exception
		{
			setLayout(new BorderLayout());
	        add(new TitlePanel(Messages.getMessage("PdfSigner.PfxSignMode")), BorderLayout.NORTH);
			
			JPanel inputPanel = new JPanel(new GridBagLayout());
			inputPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			GridBagConstraints c = new GridBagConstraints();
						
			//Key
			keyFileLabel.setText(Messages.getMessage("PdfSigner.KeyFile")); //@TODO Internalise signing messages  Messages.getMessage() 
			keyFileLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
			c.anchor = GridBagConstraints.FIRST_LINE_START; //Has no effect
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = c.gridy = 0;
			c.insets = new Insets(0,10,10,0);
			inputPanel.add(keyFileLabel, c);

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = ++y;
			c.gridwidth = 3;
			currentKeyFilePath.setPreferredSize(new Dimension(250,20));
			c.insets = new Insets(10,10,10,10);
			inputPanel.add(currentKeyFilePath, c);
			browseKeyButton.setText(Messages.getMessage("PdfViewerOption.Browse")); //Messages.getMessage("PdfViewerOption.Browse"));
			browseKeyButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ){
					JFileChooser chooser = new JFileChooser(rootDir);
					String[] pfx = new String[] { "pfx" }; 
					chooser.addChoosableFileFilter(new FileFilterer(pfx, "Key (pfx)")); 
					int state = chooser.showOpenDialog(null);

					File file = chooser.getSelectedFile();

					if (file != null && state == JFileChooser.APPROVE_OPTION) {
						currentKeyFilePath.setText(file.getAbsolutePath());
						keyNext = true;
					}
				}
			} );
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = 0;
			c.insets = new Insets(0,25,0,10);
			inputPanel.add(browseKeyButton, c);

//			c = new GridBagConstraints();
//			c.gridx = 0;
//			c.gridy = ++y;
//			c.gridwidth = 3;
//			c.fill = GridBagConstraints.HORIZONTAL; 
//			inputPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
			
			//Key password
			passwordLabel.setText(Messages.getMessage("PdfSigner.Password")); //Messages.getMessage("PdfViewerPassword.message"));
			passwordLabel.setFont(new java.awt.Font( "Dialog", Font.BOLD, 14 ));
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = ++y;
			c.insets = new Insets(20,10,10,10);
			inputPanel.add(passwordLabel, c);

			passwordField.addKeyListener( new KeyListener() {			
					public void keyReleased(KeyEvent e) {

					}
					
					public void keyPressed(KeyEvent e) {
						passNext = true;						
					}
					
					public void keyTyped(KeyEvent e) {

					}	
			}); 
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 1;
			c.gridy = y;
			c.gridwidth = 1;
			c.insets = new Insets(20,10,0,10);
			passwordField.setPreferredSize(new Dimension(100,20));
			inputPanel.add(passwordField, c);
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = y;
			c.insets = new Insets(20,0,0,0);
			visiblePassCheck.setToolTipText(Messages.getMessage("PdfSigner.ShowPassword"));
			visiblePassCheck.addActionListener(new ActionListener() {
				private char defaultChar;
				
				public void actionPerformed(ActionEvent e) {
					if(visiblePassCheck.isSelected()) {
						defaultChar = passwordField.getEchoChar();
						passwordField.setEchoChar((char) 0);
					}
					else {
						passwordField.setEchoChar(defaultChar);
					}
				}
			});
			inputPanel.add(visiblePassCheck, c);
			
			add(inputPanel, BorderLayout.CENTER);
			
			add(new ProgressPanel(2), BorderLayout.SOUTH);
		}
		
		public void registerChange(ChangeListener e)
		{
			browseKeyButton.addChangeListener(e);
		}
		
		public void registerListener(KeyListener e)
		{
			passwordField.addKeyListener(e);
		}
		
		public boolean canAdvance()
		{
			return passNext && keyNext;
		}
		
		public void collectData()
		{
			signData.setKeyFilePassword(passwordField.getPassword());
			signData.setKeyFilePath(currentKeyFilePath.getText());
		}
	}
		
    private class ModeSelect extends JPanel 
	{ 
		private String selfString = Messages.getMessage("PdfSigner.HaveKeystore");
		private String otherString = Messages.getMessage("PdfSigner.HavePfx");
		private int y = 0;
		
		private JRadioButton selfButton = new JRadioButton(selfString);
		private String[] certifyOptions = {Messages.getMessage("PdfSigner.NotCertified"), Messages.getMessage("PdfSigner.NoChangesAllowed"), Messages.getMessage("PdfSigner.FormFilling"), Messages.getMessage("PdfSigner.FormFillingAndAnnotations")};	
		private JComboBox certifyCombo = new JComboBox(certifyOptions);
		private int certifyMode = ItextFunctions.NOT_CERTIFIED;
		
		public ModeSelect()
		{
			if(!signData.isAppendMode()) {
				certifyCombo = new JComboBox(certifyOptions);
			}
			else {
				String[] s = {"Not Allowed..."};
				certifyCombo = new JComboBox(s);
			}
			setLayout(new BorderLayout());
	        add(new TitlePanel(Messages.getMessage("PdfSigner.SelectSigningMode")), BorderLayout.NORTH);
						
			JPanel optionPanel = new JPanel();		
			optionPanel.setLayout(new GridBagLayout());
			//buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
			GridBagConstraints c = new GridBagConstraints();
			
			
		    selfButton.setActionCommand(selfString);
		    //selfButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		    c.gridx = 0;
		    c.gridy = y;
			c.anchor = GridBagConstraints.FIRST_LINE_START; //Has no effect
			c.fill = GridBagConstraints.HORIZONTAL; 
		    c.insets = new Insets(10,0,20,0);
		    selfButton.setFont(new Font("Dialog", Font.BOLD, 12));
		    optionPanel.add(selfButton,c);

		    JRadioButton otherButton = new JRadioButton(otherString);
		    otherButton.setActionCommand(otherString);
		    //otherButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		    otherButton.setSelected(true);
		    signData.setSignMode(false);
		    c = new GridBagConstraints();
		    c.gridx = 0;
		    c.gridy = ++y;
		    c.fill = GridBagConstraints.HORIZONTAL; 
		    otherButton.setFont(new Font("Dialog", Font.BOLD, 12));
		    optionPanel.add(otherButton, c);
		    
		    c = new GridBagConstraints();
		    c.gridx = 0;
		    c.gridy = ++y;
		    c.fill = GridBagConstraints.HORIZONTAL; 
		    c.insets = new Insets(30,0,30,0);
		    optionPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
		    	    
		    JLabel certifyLabel = new JLabel(Messages.getMessage("PdfSigner.CertificationAuthor"));
		    certifyLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		    c = new GridBagConstraints();
		    c.gridx = 0;
		    c.gridy = ++y;
		    c.fill = GridBagConstraints.CENTER;
		    optionPanel.add(certifyLabel, c);
		    
		    c = new GridBagConstraints();
		    c.fill = GridBagConstraints.HORIZONTAL; 
		    c.gridx = 0;
		    c.gridy = ++y;
		    c.insets = new Insets(10,0,0,0);
			c.anchor = GridBagConstraints.PAGE_END;	
			certifyCombo.setEnabled(!isPdfSigned());
			certifyCombo.setSelectedIndex(0);
			certifyCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String mode = (String) certifyCombo.getSelectedItem();
					if(mode.equals(Messages.getMessage("PdfSigner.NotCertified"))) {
						certifyMode = ItextFunctions.NOT_CERTIFIED;
					}
					else if(mode.equals(Messages.getMessage("PdfSigner.NoChangesAllowed"))) {
						certifyMode = ItextFunctions.CERTIFIED_NO_CHANGES_ALLOWED;
					}
					else if(mode.equals(Messages.getMessage("PdfSigner.FormFilling"))) {
						certifyMode = ItextFunctions.CERTIFIED_FORM_FILLING;
					}
					else if(mode.equals(Messages.getMessage("PdfSigner.FormFillingAndAnnotations"))) {
						certifyMode = ItextFunctions.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS;
					}
					else {
						throw new NullPointerException("The certifyCombo box is sending a string that is not recognised.");
					}
				}
			});
		    optionPanel.add(certifyCombo, c);
		    
		    if(isPdfSigned()) {
		    	certifyCombo.setToolTipText(Messages.getMessage("PdfSigner.NotPermittedOnSigned"));
		    }
		    
		    add(optionPanel,BorderLayout.CENTER);
		    
		    ButtonGroup group = new ButtonGroup();
		    group.add(selfButton);
		    group.add(otherButton);
		    		    
		    add(new ProgressPanel(1), BorderLayout.SOUTH);
		}
		
		public void collectData()
		{
			signData.setSignMode(selfButton.isSelected());
			signData.setCertifyMode(certifyMode);
		}
	}
		
	private class KeystorePanel extends JPanel 
	{		
		private JLabel keyStoreLabel = new JLabel();
		private JLabel currentKeyStorePath = new JLabel(NO_FILE_SELECTED);
		private JButton browseKeyStoreButton = new JButton();
		
		private JLabel passwordKeyStoreLabel = new JLabel();
		private JPasswordField passwordKeyStoreField = new JPasswordField();
		private JCheckBox visiblePassKeyCheck = new JCheckBox();
		
		private JLabel aliasNameLabel = new JLabel();
		private JTextField aliasNameField = new JTextField();
		private JLabel aliasPasswordLabel = new JLabel();
		private JPasswordField aliasPasswordField = new JPasswordField();	
		private JCheckBox visiblePassAliasCheck = new JCheckBox();
		
		private volatile boolean storeAdvance, storePassAdvance, aliasAdvance, aliasPassAdvance = false;
			
		public KeystorePanel()
		{
			try	{
				init();
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
		
		private void init()
		{
			setLayout(new BorderLayout());
	        add(new TitlePanel(Messages.getMessage("PdfSigner.KeyStoreMode")), BorderLayout.NORTH);
			
			JPanel inputPanel = new JPanel(new GridBagLayout());
			inputPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			GridBagConstraints c = new GridBagConstraints();
			
			//Keystore file
			keyStoreLabel.setText(Messages.getMessage("PdfSigner.SelectKeyStore")); 
			keyStoreLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
			c.anchor = GridBagConstraints.FIRST_LINE_START; 
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = c.gridy = 0;
			c.insets = new Insets(0,10,10,0);
			inputPanel.add(keyStoreLabel, c);
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 3;
			c.insets = new Insets(0,20,0,10);
			currentKeyStorePath.setPreferredSize(new Dimension(250,20));
			inputPanel.add(currentKeyStorePath, c);

			browseKeyStoreButton.setText(Messages.getMessage("PdfViewerOption.Browse"));
			browseKeyStoreButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ){
					JFileChooser chooser = new JFileChooser(rootDir);
					chooser.setFileHidingEnabled(false);
//					String[] keystore = new String[] { "keystore" }; 
//					chooser.addChoosableFileFilter(new FileFilterer(keystore, "*.*")); 
					int state = chooser.showOpenDialog(null);

					File file = chooser.getSelectedFile();

					if (file != null && state == JFileChooser.APPROVE_OPTION) {
						currentKeyStorePath.setText(file.getAbsolutePath());
						storeAdvance = true;
					}
				}
			} );
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 1;
			c.gridy = 0;
			c.insets = new Insets(0,30,0,0);
			inputPanel.add(browseKeyStoreButton, c);

			//KeyStore password
			passwordKeyStoreLabel.setText(Messages.getMessage("PdfSigner.Password")); //Messages.getMessage("PdfViewerPassword.message"));
			passwordKeyStoreLabel.setFont(new java.awt.Font( "Dialog", Font.BOLD, 14 ));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 2;
			c.insets = new Insets(30,10,0,10);
			inputPanel.add(passwordKeyStoreLabel, c);

			passwordKeyStoreField.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					
				}
				
				public void keyPressed(KeyEvent e) {
					
				}
				
				public void keyTyped(KeyEvent e) {
					storePassAdvance = true;
				}
			});
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 1;
			c.gridy = 2;
			c.gridwidth = 1;
			c.insets = new Insets(30,10,0,10);
			//passwordKeyStoreField.setPreferredSize(new Dimension(200,20));
			inputPanel.add(passwordKeyStoreField, c);
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = 2;
			c.insets = new Insets(30,0,0,0);
			visiblePassKeyCheck.setToolTipText(Messages.getMessage("PdfSigner.ShowPassword"));
			visiblePassKeyCheck.addActionListener(new ActionListener() {
				private char defaultChar;
				
				public void actionPerformed(ActionEvent e) {
					if(visiblePassKeyCheck.isSelected()) {
						defaultChar = passwordKeyStoreField.getEchoChar();
						passwordKeyStoreField.setEchoChar((char) 0);
					}
					else {
						passwordKeyStoreField.setEchoChar(defaultChar);
					}
				}
			});
			inputPanel.add(visiblePassKeyCheck, c);
			
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 4;
			c.gridwidth = 4;
			c.insets = new Insets(10,0,10,0);
			inputPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
			
//			//Alias
			aliasNameLabel.setText(Messages.getMessage("PdfSigner.AliasName"));
			aliasNameLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 5;
			c.insets = new Insets(0,10,10,0);
			inputPanel.add(aliasNameLabel,c);
			aliasNameField.addKeyListener(new KeyListener () {
				public void keyReleased(KeyEvent e) {
					
				}
				
				public void keyPressed(KeyEvent e) {
					
				}
				
				public void keyTyped(KeyEvent e) {
					aliasAdvance = true;
				}	
			});
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 1;
			c.gridy = 5;
			c.gridwidth = 2;
			c.insets = new Insets(0,10,0,10);
			aliasNameField.setPreferredSize(new Dimension(150,20));
			inputPanel.add(aliasNameField,c);
			
			aliasPasswordLabel.setText(Messages.getMessage("PdfSigner.AliasPassword"));
			aliasPasswordLabel.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 7;
			c.insets = new Insets(10,10,0,10);
			inputPanel.add(aliasPasswordLabel,c);
			aliasPasswordField.addKeyListener(new KeyListener () {
				public void keyReleased(KeyEvent e) {
                    
				}
				
				public void keyPressed(KeyEvent e) {
					
				}
				
				public void keyTyped(KeyEvent e) {
					aliasPassAdvance = true;
				}
			});
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 1;
			c.gridy = 7;
			//c.gridwidth = 2;
			c.insets = new Insets(0,10,0,10);
			c.anchor = GridBagConstraints.PAGE_END;
			aliasPasswordField.setPreferredSize(new Dimension(100,20));
			inputPanel.add(aliasPasswordField, c);
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = 7;
			c.insets = new Insets(10,0,0,0);
			visiblePassAliasCheck.setToolTipText(Messages.getMessage("PdfSigner.ShowPassword"));
			visiblePassAliasCheck.addActionListener(new ActionListener() {
				private char defaultChar;
				
				public void actionPerformed(ActionEvent e) {
					if(visiblePassAliasCheck.isSelected()) {
						defaultChar = aliasPasswordField.getEchoChar();
						aliasPasswordField.setEchoChar((char) 0);
					}
					else {
						aliasPasswordField.setEchoChar(defaultChar);
					}
				}
			});
			inputPanel.add(visiblePassAliasCheck, c);
			
			add(inputPanel, BorderLayout.CENTER);
			
			add(new ProgressPanel(2), BorderLayout.SOUTH);
		}
		
		public void registerChange(ChangeListener e)
		{
			browseKeyStoreButton.addChangeListener(e);
		}
		
		public void registerNextKeyListeners(KeyListener e)
		{
			passwordKeyStoreField.addKeyListener(e);
			aliasNameField.addKeyListener(e);
			aliasPasswordField.addKeyListener(e);
		}
		
		public boolean canAdvance()
		{
			return storeAdvance && storePassAdvance && aliasAdvance && aliasPassAdvance;
		}	
		
		public void collectData()
		{
            signData.setKeyStorePath(currentKeyStorePath.getText());
            signData.setKeystorePassword(passwordKeyStoreField.getPassword());
            signData.setAlias(aliasNameField.getText());
            signData.setAliasPassword(aliasPasswordField.getPassword());
		}
	}
	
    private class CommonPanel extends JPanel
	{
		private JLabel reasonLabel = new JLabel();
		private JTextField signerReasonArea =new JTextField();

		private JLabel locationLabel = new JLabel();
		private JTextField signerLocationField =new JTextField();

		private JLabel outputFileLabel = new JLabel();
		private JLabel currentOutputFilePath = new JLabel();
		private JButton browseOutputButton = new JButton();

		private volatile boolean canAdvance = false;

		public CommonPanel()
		{
			try	{
				init();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void init()
		{
			setLayout(new BorderLayout());
			add(new TitlePanel(Messages.getMessage("PdfSigner.ReasonAndLocation")), BorderLayout.NORTH);

			JPanel inputPanel = new JPanel(new GridBagLayout());
			inputPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));		
			GridBagConstraints c = new GridBagConstraints();

			//Reason
			reasonLabel.setText(Messages.getMessage("PdfSigner.Reason") + ':');
			reasonLabel.setFont(new java.awt.Font( "Dialog", Font.BOLD, 14 ));
			c.anchor = GridBagConstraints.FIRST_LINE_START; //Has no effect
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = c.gridy = 0;
			c.insets = new Insets(10,0,0,0);
			inputPanel.add(reasonLabel, c);

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 3;
			c.insets = new Insets(10,0,10,0);
			signerReasonArea.setPreferredSize(new Dimension(200,20));
			inputPanel.add(signerReasonArea, c);

			//Location
			locationLabel.setText(Messages.getMessage("PdfSigner.Location")+ ':');
			locationLabel.setFont(new java.awt.Font( "Dialog", Font.BOLD, 14 ));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 2;
			inputPanel.add(locationLabel, c);

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 3;
			c.insets = new Insets(10,0,0,0);
			c.gridwidth = 3;
			signerLocationField.setPreferredSize(new Dimension(200,20));
			inputPanel.add(signerLocationField, c);

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 4;
			c.gridwidth = 3;
			c.insets = new Insets(10,0,0,0);
			inputPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

			//OutputFile
			outputFileLabel.setText(Messages.getMessage("PdfSigner.OutputFile"));
			outputFileLabel.setFont(new java.awt.Font( "Dialog", Font.BOLD, 14 ));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 5;
			c.insets = new Insets(5,10,0,0);
			inputPanel.add(outputFileLabel, c);

			currentOutputFilePath.setText(NO_FILE_SELECTED);
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 0;
			c.gridy = 6;
			c.insets = new Insets(10,0,0,0);
			c.gridwidth = 3;
			currentOutputFilePath.setPreferredSize(new Dimension(100, 20));
			inputPanel.add(currentOutputFilePath, c);

			browseOutputButton.setText(Messages.getMessage("PdfViewerOption.Browse"));
			browseOutputButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ){
					JFileChooser chooser = new JFileChooser(rootDir);
					int state = chooser.showSaveDialog(null);

					File file = chooser.getSelectedFile();

					if (file != null && state == JFileChooser.APPROVE_OPTION) {
						if(file.exists()) {
							JOptionPane.showMessageDialog(null,
									Messages.getMessage("PdfSigner.PleaseChooseAnotherFile"),
									Messages.getMessage("PdfViewerGeneralError.message"),
									JOptionPane.ERROR_MESSAGE);
							canAdvance = false;
							currentOutputFilePath.setText(NO_FILE_SELECTED);
							signData.setOutputFilePath(null);
						}
						else if(file.isDirectory()) {
							JOptionPane.showMessageDialog(null,
									Messages.getMessage("PdfSigner.NoFileSelected"),
									Messages.getMessage("PdfViewerGeneralError.message"),
									JOptionPane.ERROR_MESSAGE);			
							canAdvance = false;
							currentOutputFilePath.setText(NO_FILE_SELECTED);
						}
						else {
							currentOutputFilePath.setText(file.getAbsolutePath());
							canAdvance = true;
						}
					}
				}
			} );
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = 5;
			c.insets = new Insets(5,25,0,25);
			c.anchor = GridBagConstraints.LAST_LINE_END;
			inputPanel.add(browseOutputButton, c);  		

			add(inputPanel, BorderLayout.CENTER);
			
			add(new ProgressPanel(5), BorderLayout.SOUTH);
		}

		public boolean canFinish()
		{
			return canAdvance;
		}	

		public void registerChange(ChangeListener e)
		{
			browseOutputButton.addChangeListener(e);
		}
		
		public void collectData()
		{
			signData.setReason(signerReasonArea.getText());
			signData.setLocation(signerLocationField.getText());
			signData.setOutputFilePath(currentOutputFilePath.getText());
		}
	}
	
	private class EncryptionPanel extends JPanel
	{	
		private JCheckBox encryptionCheck = new JCheckBox("Encrypt"); 
		private JCheckBox allowPrinting = new JCheckBox("Allow Printing");
		private JCheckBox allowModifyContent = new JCheckBox("Allow Content Modification");
		private JCheckBox allowCopy = new JCheckBox("Allow Copy");
		private JCheckBox allowModifyAnnotation = new JCheckBox("Allow Annotation Modification");
		private JCheckBox allowFillIn = new JCheckBox("Allow Fill In");
		private JCheckBox allowScreenReader = new JCheckBox("Allow Screen Reader");
		private JCheckBox allowAssembly = new JCheckBox("Allow Assembly");
		private JCheckBox allowDegradedPrinting = new JCheckBox("Allow Degraded Printing");
		private JPasswordField userPassword = new JPasswordField();
		private JPasswordField ownerPassword = new JPasswordField();
		private JCheckBox flatten = new JCheckBox("Flatten PDF");	
		
		private JCheckBox visiblePassUserCheck = new JCheckBox();
		private JCheckBox visiblePassOwnerCheck = new JCheckBox();
		private boolean ownerAdvance = false;
		private volatile boolean canAdvance = true;
		
		public EncryptionPanel()
		{
			int y = 0;
			setLayout(new BorderLayout());
			add(new TitlePanel(Messages.getMessage("PdfSigner.EncryptionOptions")), BorderLayout.NORTH);

			JPanel optionPanel = new JPanel();		
			optionPanel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.anchor = GridBagConstraints.PAGE_START;
			//encryptionCheck.setFont(new Font("Dialog", Font.BOLD, 12));
			encryptionCheck.setEnabled(!isPdfSigned());
			encryptionCheck.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					canAdvance = !encryptionCheck.isSelected() || ownerAdvance;
				}
			});

			optionPanel.add(encryptionCheck, c);
			encryptionCheck.setSelected(false);

			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.anchor = GridBagConstraints.FIRST_LINE_END;
			flatten.setEnabled(!isPdfSigned());
			optionPanel.add(flatten, c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.gridwidth = 3;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

	        //Encryption Options.			
			allowPrinting.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(allowPrinting, c);
			allowModifyContent.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridwidth = 2;
			optionPanel.add(allowModifyContent, c);
			
			allowCopy.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(allowCopy, c);
			allowModifyAnnotation.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridwidth = 2;
			optionPanel.add(allowModifyAnnotation, c);
			
			allowFillIn.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(allowFillIn, c);
			allowScreenReader.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridwidth = 2;
			optionPanel.add(allowScreenReader, c);
			
			allowAssembly.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(allowAssembly, c);
			allowDegradedPrinting.setEnabled(false);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridwidth = 2;
			optionPanel.add(allowDegradedPrinting, c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.gridwidth = 3;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.insets= new Insets(5,0,0,0);
			optionPanel.add(new JLabel(Messages.getMessage("PdfSigner.UserPassword")), c);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.insets= new Insets(5,0,0,0);
			userPassword.setEnabled(false);
			userPassword.setPreferredSize(new Dimension(100,20));
			userPassword.addKeyListener(new KeyListener () {
				public void keyReleased(KeyEvent e) {
                    
				}
				
				public void keyPressed(KeyEvent e) {
					
				}
				
				public void keyTyped(KeyEvent e) {
					ownerAdvance = true;
					canAdvance = true;
				}
			});
			optionPanel.add(userPassword, c);
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = y;
			c.insets = new Insets(0,0,0,0);
			visiblePassUserCheck.setToolTipText(Messages.getMessage("PdfSigner.ShowPassword"));
			visiblePassUserCheck.addActionListener(new ActionListener() {
				private char defaultChar;
				
				public void actionPerformed(ActionEvent e) {
					if(visiblePassUserCheck.isSelected()) {
						defaultChar = userPassword.getEchoChar();
						userPassword.setEchoChar((char) 0);
					}
					else {
						userPassword.setEchoChar(defaultChar);
					}
				}
			});
			visiblePassUserCheck.setEnabled(false);
			optionPanel.add(visiblePassUserCheck, c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.insets= new Insets(5,0,0,0);
			optionPanel.add(new JLabel(Messages.getMessage("PdfSigner.OwnerPassword")), c);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.insets= new Insets(5,0,0,0);
			ownerPassword.setEnabled(false);
			ownerPassword.setPreferredSize(new Dimension(100,20));
			optionPanel.add(ownerPassword, c);
			
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.gridx = 2;
			c.gridy = y;
			c.insets = new Insets(0,0,0,0);
			visiblePassOwnerCheck.setToolTipText(Messages.getMessage("PdfSigner.ShowPassword"));
			visiblePassOwnerCheck.addActionListener(new ActionListener() {
				private char defaultChar;
				
				public void actionPerformed(ActionEvent e) {
					if(visiblePassOwnerCheck.isSelected()) {
						defaultChar = ownerPassword.getEchoChar();
						ownerPassword.setEchoChar((char) 0);
					}
					else {
						ownerPassword.setEchoChar(defaultChar);
					}
				}
			});
			visiblePassOwnerCheck.setEnabled(false);
			optionPanel.add(visiblePassOwnerCheck, c);
						
			if(isPdfSigned()) {
				c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL; 
				c.gridx = 0;
				c.gridy = ++y;
				c.gridwidth = 3;
				c.insets = new Insets(25,0,0,0);
				JLabel notAvailable = new JLabel(Messages.getMessage("PdfSigner.DisabledSigned"), JLabel.CENTER);
				notAvailable.setForeground(Color.red);
				optionPanel.add(notAvailable, c);
			}
			
			encryptionCheck.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					boolean enable = e.getStateChange()== ItemEvent.SELECTED;
					allowPrinting.setEnabled(enable);
					allowModifyContent.setEnabled(enable);
					allowCopy.setEnabled(enable);
					allowModifyAnnotation.setEnabled(enable);
					allowFillIn.setEnabled(enable);
					allowScreenReader.setEnabled(enable);
					allowAssembly.setEnabled(enable);
					allowDegradedPrinting.setEnabled(enable); 
					userPassword.setEnabled(enable); 
					ownerPassword.setEnabled(enable); 	
					visiblePassUserCheck.setEnabled(enable);
					visiblePassOwnerCheck.setEnabled(enable);	
				}
			});
			
			add(optionPanel, BorderLayout.CENTER);
			add(new ProgressPanel(4), BorderLayout.SOUTH);
		}
					
		public void registerChange(ChangeListener wizard) {
			encryptionCheck.addChangeListener(wizard);
		}

		public void registerNextKeyListeners(KeyListener wizard) {
			userPassword.addKeyListener(wizard);
		}
		
		public boolean canAdvance()
		{
			return canAdvance;
		}

		public void collectData()
		{
			signData.setFlatten(flatten.isSelected());
			signData.setEncrypt(encryptionCheck.isSelected());
			if(encryptionCheck.isSelected()) {
				signData.setEncryptUserPass(userPassword.getPassword());
				signData.setEncryptOwnerPass(ownerPassword.getPassword());
				
				int result = 0;
				
				if(allowPrinting.isSelected()) result |= ItextFunctions.ALLOW_PRINTING;
				if(allowModifyContent.isSelected()) result |= ItextFunctions.ALLOW_MODIFY_CONTENTS;
				if(allowCopy.isSelected()) result |= ItextFunctions.ALLOW_COPY;
				if(allowModifyAnnotation.isSelected()) result |= ItextFunctions.ALLOW_MODIFY_ANNOTATIONS;
				if(allowFillIn.isSelected()) result |= ItextFunctions.ALLOW_FILL_IN;
				if(allowScreenReader.isSelected()) result |= ItextFunctions.ALLOW_SCREENREADERS;
				if(allowAssembly.isSelected()) result |= ItextFunctions.ALLOW_ASSEMBLY;
				if(allowDegradedPrinting.isSelected()) result |= ItextFunctions.ALLOW_DEGRADED_PRINTING;
				
				signData.setEncryptPermissions(result);
			}
		}
	}
	
    private class SignaturePanel extends JPanel
	{
		private JCheckBox visibleCheck = new JCheckBox(Messages.getMessage("PdfSigner.VisibleSignature")); 
		private JComponent sigPreviewComp;
		private JSlider pageSlider;
		private JLabel pageNumberLabel;
		private int currentPage = 1;
		private Point signRectOrigin;
		private Point signRectEnd;
		private int offsetX, offsetY;
		
		private float scale;
		private int previewWidth, previewHeight;
		private volatile boolean drawRect = false;
		private boolean signAreaUndefined = true;
		
		private BufferedImage previewImage;
		
		public SignaturePanel() 
		{        
			
			try {
				previewImage = pdfDecoder.getPageAsImage(currentPage);
			}
			catch (Exception e) {
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
			}
			
			int y = 0;
			setLayout(new BorderLayout());
			add(new TitlePanel(Messages.getMessage("PdfSigner.VisibleSignature") + ' ' + Messages.getMessage("PdfViewerMenu.options")), BorderLayout.NORTH);

			JPanel optionPanel = new JPanel();		
			optionPanel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = y;
			c.insets = new Insets(5,0,0,0);
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.anchor = GridBagConstraints.PAGE_START;
			visibleCheck.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sigPreviewComp.repaint();
					if (pdfDecoder.getPageCount()>1)
					    pageSlider.setEnabled(visibleCheck.isSelected());
				}
				
			});
			optionPanel.add(visibleCheck, c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			c.insets = new Insets(10,0,10,0);
			optionPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
			
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = ++y;
			c.fill = GridBagConstraints.HORIZONTAL; 
			optionPanel.add(previewPanel(), c);
				
			add(optionPanel, BorderLayout.CENTER);
			
			add(new ProgressPanel(3), BorderLayout.SOUTH);
			
		}
		
		public void collectData() {
			signData.setVisibleSignature(visibleCheck.isSelected());
			if(visibleCheck.isSelected()) {
				int height = previewImage.getHeight();
                int x1 = (int) ((signRectOrigin.getX() - offsetX) / scale);
                int y1 = (int) ( height - ((signRectOrigin.getY() - offsetY) / scale));
				int x2 = (int) ((signRectEnd.getX() - offsetX) / scale);
				int y2 = (int) (height - ((signRectEnd.getY() - offsetY) / scale));
				
				PdfPageData pageData = pdfDecoder.getPdfPageData();
				int cropX = pageData.getCropBoxX(currentPage);
				int cropY = pageData.getCropBoxY(currentPage);
				x1 += cropX;
				y1 += cropY;
				x2 += cropX;
				y2 += cropY;
				
				signData.setRectangle(x1,y1,x2,y2);
				signData.setSignPage(currentPage);
			}
		}
	
		private JPanel previewPanel() 
		{
			JPanel result = new JPanel(new BorderLayout());
											
			sigPreviewComp = new JComponent() {			
	            public void paintComponent(Graphics g){
	                sigPreview(g);
	            }
	        };
			sigPreviewComp.setPreferredSize(new Dimension(200,200));
			sigPreviewComp.setToolTipText(Messages.getMessage("PdfSigner.ClickAndDrag"));
	        sigPreviewComp.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
					if(visibleCheck.isSelected()) {
						signRectOrigin.setLocation(e.getX(), e.getY());
						drawRect = true;
						
						Thread rect = new Thread(signAreaThread());
						rect.start();
					}
				}

				public void mouseReleased(MouseEvent e) {
					if(visibleCheck.isSelected()) {
					    drawRect = false;
					    sigPreviewComp.repaint();
					}
				}
         
			});
			
			result.add(sigPreviewComp, BorderLayout.CENTER);
			
			//Add a slider if there is more than one page
			if(pdfDecoder.getPageCount()>1) {
				pageNumberLabel = new JLabel (Messages.getMessage("PdfSigner.PageNumber") + ' ' + currentPage);
				pageNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
				result.add(pageNumberLabel, BorderLayout.NORTH);
				
				pageSlider = new JSlider(JSlider.HORIZONTAL, 1, pdfDecoder.getPageCount(), currentPage);
				pageSlider.setMajorTickSpacing(pdfDecoder.getPageCount() - 1);
				pageSlider.setPaintLabels(true);

				pageSlider.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e)
					{
                        if(pageSlider.getValueIsAdjusting()) {
                        	currentPage = pageSlider.getValue();
                			try {
                				previewImage = pdfDecoder.getPageAsImage(currentPage);
                				sigPreviewComp.repaint();
                				pageNumberLabel.setText(Messages.getMessage("PdfSigner.PageNumber") + ' ' + currentPage);
                			}
                			catch (Exception ex) {
                                //tell user and log
                                if(LogWriter.isOutput())
                                    LogWriter.writeLog("Exception: "+ex.getMessage());
                			}
                        }
					}
				});
				result.add(pageSlider, BorderLayout.SOUTH);		
				pageSlider.setEnabled(false);
			}

			
			return result;
		}
		
		private void sigPreview(Graphics g) 
		{
			int panelWidth = sigPreviewComp.getWidth();
			int panelHeight = sigPreviewComp.getHeight();
			previewWidth = previewImage.getWidth();
			previewHeight = previewImage.getHeight();

			scale = (previewWidth>previewHeight) ? (float) panelWidth / previewWidth : (float) panelHeight / previewHeight; 

			previewWidth *= scale;
			previewHeight *= scale;
			offsetX = (panelWidth - previewWidth) / 2;
			offsetY =  (panelHeight - previewHeight) / 2;


			g.drawImage(previewImage, offsetX , offsetY, previewWidth, previewHeight, null);   

			if(visibleCheck.isSelected()) {
				g.clipRect(offsetX, offsetY, previewWidth, previewHeight);
				drawSignBox(g);
			}
		}
		
		private void drawSignBox(Graphics g)
		{
			if(signAreaUndefined) {
				PdfPageData pageData = pdfDecoder.getPdfPageData();
				signRectOrigin = new Point(offsetX,offsetY);
				signRectEnd = new Point((int) (pageData.getCropBoxWidth(currentPage) * scale) - 1 + offsetX,
						                (int) (pageData.getCropBoxHeight(currentPage) * scale) - 1 + offsetY);
				signAreaUndefined = false;
			}
			int xO = (int) signRectOrigin.getX();
			int yO = (int) signRectOrigin.getY();
			int xE = (int) signRectEnd.getX(); 	
			int yE = (int) signRectEnd.getY();
		    if(xO>xE) {
		    	int temp = xE;
		    	xE = xO;
		    	xO = temp;
		    }
		    if(yO>yE) {
		    	int temp = yO;
		    	yO = yE;
		    	yE = temp;
		    }
			
			g.drawRect(xO, yO, xE - xO, yE - yO);
			g.drawLine(xO, yO, xE, yE);
			g.drawLine(xO, yE, xE, yO);
		}
		
		private Runnable signAreaThread() {
			return new Runnable() {
				public void run() 
				{
					Point origin = sigPreviewComp.getLocationOnScreen();
					
				    while(drawRect) {
				    	try {
				    	    Thread.sleep(100);
				    	}
				    	catch (Exception e) {
                            //tell user and log
                            if(LogWriter.isOutput())
                                LogWriter.writeLog("Exception: "+e.getMessage());
				    	}				    	
				    	double x = MouseInfo.getPointerInfo().getLocation().getX() - origin.getX();
				    	double y = MouseInfo.getPointerInfo().getLocation().getY() - origin.getY();
				    					    	
				    	signRectEnd.setLocation(x, y);					    
				    	sigPreviewComp.repaint();
				    }
				}
			};
		}
	}
		
    private static class ProgressPanel extends JPanel
	{
		public ProgressPanel(int current)
		{
			setBorder(new EtchedBorder());
			JLabel progressLabel = new JLabel("Step " + current + " of " + MAXIMUM_PANELS);
			progressLabel.setAlignmentX(RIGHT_ALIGNMENT);
		    add(progressLabel);
	    }
	}
	
	private static class TitlePanel extends JPanel
	{	
		public TitlePanel(String title)
		{
	        setBackground(Color.gray);
	        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	        
	        JLabel textLabel = new JLabel();
	        textLabel.setBackground(Color.gray);
	        textLabel.setFont(new Font("Dialog", Font.BOLD, 14));
	        textLabel.setText(title);
	        textLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
	        textLabel.setOpaque(true);
	        add(textLabel);
		}
	}
	
}

