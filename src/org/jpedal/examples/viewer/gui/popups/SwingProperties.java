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
 * SwingProperties.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.popups;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.SingleDisplay;
import org.jpedal.examples.viewer.gui.CheckNode;
import org.jpedal.examples.viewer.gui.CheckRenderer;
import org.jpedal.examples.viewer.gui.SwingGUI;
import org.jpedal.examples.viewer.utils.*;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;
import org.w3c.dom.NodeList;
import org.jpedal.io.Speech;


public class SwingProperties extends JPanel {
	
	Map reverseMessage =new HashMap();

	//Array of menu tabs.
	String[] menuTabs = {"ShowMenubar","ShowButtons","ShowDisplayoptions", "ShowNavigationbar", "ShowSidetabbar"};

	String propertiesLocation = "";
	
	PropertiesFile properties = null;

	//Window Components
	JDialog propertiesDialog;
	
	JButton confirm = new JButton("OK");

	JButton cancel = new JButton("Cancel");

	JTabbedPane tabs = new JTabbedPane();

	//Settings Fields Components

	//DPI viewer value
	JTextField resolution;

	//Search window display style
	JComboBox searchStyle;

	//Show border around page
	JCheckBox border;

	//Show download window
	JCheckBox downloadWindow;

	//Use Hi Res Printing
	JCheckBox HiResPrint;

	//Use Hi Res Printing
	JCheckBox constantTabs;
	
	//Use enhanced viewer
	JCheckBox enhancedViewer;
	
	//Use enhanced viewer
	JCheckBox enhancedFacing;
	
	//Use enhanced viewer
	JCheckBox thumbnailScroll;
	
	//Use enhanced user interface
	JCheckBox enhancedGUI;
	
	//Use right click functionality
	JCheckBox rightClick;

    //Allow scrollwheel zooming
    JCheckBox scrollwheelZoom;

	//perform automatic update check
	JCheckBox update = new JCheckBox(Messages.getMessage("PdfPreferences.CheckForUpdate"));

	//max no of multiviewers
	JTextField maxMultiViewers;

	//inset value
	JTextField pageInsets;
	JLabel pageInsetsText;

    //window title
    JTextField windowTitle;
    JLabel windowTitleText;

	//icons Location
	JTextField iconLocation;
	JLabel iconLocationText;

    //Printer blacklist
	JTextField printerBlacklist;
	JLabel printerBlacklistText;

    //Default printer
    JComboBox defaultPrinter;
    JLabel defaultPrinterText;

    //Default pagesize
    JComboBox defaultPagesize;
    JLabel defaultPagesizeText;

    //Default resolution
    JTextField defaultDPI;
    JLabel defaultDPIText;

	JTextField sideTabLength;
	JLabel sideTabLengthText;
	
    //Use parented hinting functions
    JCheckBox useHinting;

	//Set autoScroll when mouse at the edge of page
	JCheckBox autoScroll;

	//Set whether to prompt user on close
	JCheckBox confirmClose;

	//Set if we should open the file at the last viewed page
	JCheckBox openLastDoc;

	//Set default page layout
	JComboBox pageLayout = new JComboBox(new String[]{"Single Page","Continuous","Continuous Facing", "Facing", "PageFlow"});

	//Speech Options
    JComboBox voiceSelect;

	
	JPanel highlightBoxColor = new JPanel();
	JPanel highlightTextColor = new JPanel();
	JPanel viewBGColor = new JPanel();
	JPanel pdfDecoderBackground = new JPanel();
//	JPanel sideBGColor = new JPanel();
	JPanel foreGroundColor = new JPanel();
	JCheckBox invertHighlight = new JCheckBox("Highlight Inverts Page");
	JCheckBox replaceDocTextCol = new JCheckBox("Replace Document Text Colors");
	JCheckBox replaceDisplayBGCol = new JCheckBox("Replace Display Background Color");
	
	JCheckBox changeTextAndLineArt = new JCheckBox("Change Color of Text and Line art");
	JCheckBox showMouseSelectionBox = new JCheckBox("Show Mouse Selection Box");
	JTextField highlightComposite = new JTextField(String.valueOf(PdfDecoder.highlightComposite));
	
//	private SwingGUI swingGUI;

	private Container parent;

	private boolean preferencesSetup=false;

	private JButton clearHistory;

	private JLabel historyClearedLabel;

	//Only allow numerical input to the field
	KeyListener numericalKeyListener = new KeyListener(){

		boolean consume = false;

		public void keyPressed(KeyEvent e) {
			consume = false;
			if((e.getKeyChar()<'0' || e.getKeyChar()>'9') && (e.getKeyCode()!=8 || e.getKeyCode()!=127))
				consume = true;
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {
			if(consume)
				e.consume();
		}

	};
	
	/**
	 * showPreferenceWindow()
	 *
	 * Ensure current values are loaded then display window.
	 * @param swingGUI 
	 */
	public void showPreferenceWindow(SwingGUI swingGUI){

		if(parent instanceof JFrame)
			propertiesDialog = new JDialog(((JFrame)parent));
		else
			propertiesDialog = new JDialog();

        propertiesDialog.setModal(true);

		if(!preferencesSetup){
			preferencesSetup=true;

			createPreferenceWindow(swingGUI);
		}

        if(properties.getValue("readOnly").toLowerCase().equals("true")){
			JOptionPane.showMessageDialog(
					this,
					"You do not have permission alter jPedal properties.\n"+
					"Access to the properties window has therefore been disabled.",
					"Can not write to properties file", JOptionPane.INFORMATION_MESSAGE);
		}

		
		if(properties.isReadOnly()){
			JOptionPane.showMessageDialog(
					this, 
					"Current properties file is read only.\n" +
					"Any alteration can only be saved as another properties file.", 
					"Properties file is read only", JOptionPane.INFORMATION_MESSAGE);
			confirm.setEnabled(false);
		}else{
			confirm.setEnabled(true);
		}
		
		//		this.swingGUI = swingGUI;
		propertiesDialog.setLocationRelativeTo(parent);
		propertiesDialog.setVisible(true);
	}

	private void saveGUIPreferences(SwingGUI gui){
		Component[] components = tabs.getComponents();
		for(int i=0; i!=components.length; i++){
			if(components[i] instanceof JPanel){
				Component[] panelComponets = ((JPanel)components[i]).getComponents();
				for(int j=0; j!=panelComponets.length; j++){
					if (panelComponets[j] instanceof JScrollPane) {
						Component[] scrollComponents = ((JScrollPane)panelComponets[j]).getComponents();
						for(int k=0; k!=scrollComponents.length; k++){
							if(scrollComponents[k] instanceof JViewport){
								Component[] viewportComponents = ((JViewport)scrollComponents[k]).getComponents();
								for(int l=0; l!=viewportComponents.length; l++){
									if(viewportComponents[l] instanceof JTree){
										JTree tree = ((JTree)viewportComponents[l]);
										CheckNode root = (CheckNode)tree.getModel().getRoot();
										if(root.getChildCount()>0){
											saveMenuPreferencesChildren(root, gui);
										}
									}
								}
							}
							
						}
					}
					if(panelComponets[j] instanceof JButton){
						JButton tempButton = ((JButton)panelComponets[j]);
						String value = ((String)reverseMessage.get(tempButton.getText().substring((Messages.getMessage("PdfCustomGui.HideGuiSection")+ ' ').length())));
						if(tempButton.getText().startsWith(Messages.getMessage("PdfCustomGui.HideGuiSection")+ ' ')){
							properties.setValue(value, "true");
							gui.alterProperty(value, true);
						}else{
							properties.setValue(value, "false");
							gui.alterProperty(value, false);
						}
					}
				}
			}
		}
	}

	private void saveMenuPreferencesChildren(CheckNode root, SwingGUI gui){
		for(int i=0; i!=root.getChildCount(); i++){
			CheckNode node = (CheckNode)root.getChildAt(i);
			String value = ((String)reverseMessage.get(node.getText()));
			if(node.isSelected()){
				properties.setValue(value, "true");
				gui.alterProperty(value, true);
			}else{
				properties.setValue(value, "false");
				gui.alterProperty(value, false);
			}

			if(node.getChildCount()>0){
				saveMenuPreferencesChildren(node, gui);
			}
		}
	}

	/**
	 * createPreferanceWindow(final GUI gui)
	 * Set up all settings fields then call the required methods to build the window
	 * 
	 * @param gui - Used to allow any changed settings to be saved into an external properties file.
	 * 
	 */
	private void createPreferenceWindow(final SwingGUI gui){
		
		//Get Properties file containing current preferences
		properties = gui.getProperties();
		//Get Properties file location
		propertiesLocation = gui.getPropertiesFileLocation();
		
		//Set window title
		propertiesDialog.setTitle(Messages.getMessage("PdfPreferences.windowTitle"));
		
		update.setToolTipText(Messages.getMessage("PdfPreferences.update.toolTip"));
		invertHighlight.setText(Messages.getMessage("PdfPreferences.InvertHighlight"));
		showMouseSelectionBox.setText(Messages.getMessage("PdfPreferences.ShowSelectionBow"));
		invertHighlight.setToolTipText(Messages.getMessage("PdfPreferences.invertHighlight.toolTip"));
		showMouseSelectionBox.setToolTipText(Messages.getMessage("PdfPreferences.showMouseSelection.toolTip"));
		highlightBoxColor.setToolTipText(Messages.getMessage("PdfPreferences.highlightBox.toolTip"));
		highlightTextColor.setToolTipText(Messages.getMessage("PdfPreferences.highlightText.toolTip"));

        //@kieran
        //@removed by Mark as always misused
		//Set up the properties window gui components
		String propValue = properties.getValue("resolution");
		if(propValue.length()>0)
			resolution = new JTextField(propValue);
		else
			resolution = new JTextField(72);
		resolution.setToolTipText(Messages.getMessage("PdfPreferences.resolutionInput.toolTip"));

        propValue = properties.getValue("maxmultiviewers");
		if(propValue.length()>0)
			maxMultiViewers = new JTextField(propValue);
		else
			maxMultiViewers = new JTextField(20);
		maxMultiViewers.setToolTipText(Messages.getMessage("PdfPreferences.maxMultiViewer.toolTip"));

		searchStyle = new JComboBox(
				new String[]{Messages.getMessage("PageLayoutViewMenu.WindowSearch"),
						Messages.getMessage("PageLayoutViewMenu.TabbedSearch"),
						Messages.getMessage("PageLayoutViewMenu.MenuSearch")
						});
		searchStyle.setToolTipText(Messages.getMessage("PdfPreferences.searchStyle.toolTip"));
		
		pageLayout = new JComboBox(
				new String[]{Messages.getMessage("PageLayoutViewMenu.SinglePage"),
						Messages.getMessage("PageLayoutViewMenu.Continuous"),
						Messages.getMessage("PageLayoutViewMenu.Facing"),
						Messages.getMessage("PageLayoutViewMenu.ContinousFacing"),
						Messages.getMessage("PageLayoutViewMenu.PageFlow")});
		pageLayout.setToolTipText(Messages.getMessage("PdfPreferences.pageLayout.toolTip"));
		
		pageInsetsText = new JLabel(Messages.getMessage("PdfViewerViewMenu.pageInsets"));
		pageInsets = new JTextField();
		pageInsets.setToolTipText(Messages.getMessage("PdfPreferences.pageInsets.toolTip"));

        windowTitleText = new JLabel(Messages.getMessage("PdfCustomGui.windowTitle"));
        windowTitle = new JTextField();
        windowTitle.setToolTipText(Messages.getMessage("PdfPreferences.windowTitle.toolTip"));
		
		iconLocationText = new JLabel(Messages.getMessage("PdfViewerViewMenu.iconLocation"));
		iconLocation = new JTextField();
		iconLocation.setToolTipText(Messages.getMessage("PdfPreferences.iconLocation.toolTip"));

        defaultDPIText = new JLabel(Messages.getMessage("PdfViewerPrint.defaultDPI"));
        defaultDPI = new JTextField();
        defaultDPI.setToolTipText(Messages.getMessage("PdfPreferences.defaultDPI.toolTip"));
		
		sideTabLengthText = new JLabel(Messages.getMessage("PdfCustomGui.SideTabLength"));
		sideTabLength = new JTextField();
		sideTabLength.setToolTipText(Messages.getMessage("PdfPreferences.sideTabLength.toolTip"));
		
        useHinting = new JCheckBox(Messages.getMessage("PdfCustomGui.useHinting"));
//		useHinting.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//                if (useHinting.isSelected()) {
//                    JOptionPane.showMessageDialog(null,Messages.getMessage("PdfCustomGui.patentedHintingMessage"));
//                }
//			}
//		});
        useHinting.setToolTipText(Messages.getMessage("PdfPreferences.useHinting.toolTip"));
		
		autoScroll = new JCheckBox(Messages.getMessage("PdfViewerViewMenuAutoscrollSet.text"));
		autoScroll.setToolTipText("Set if autoscroll should be enabled / disabled");

        confirmClose = new JCheckBox(Messages.getMessage("PfdViewerViewMenuConfirmClose.text"));
        confirmClose.setToolTipText("Set if we should confirm closing the viewer");
		
		openLastDoc = new JCheckBox(Messages.getMessage("PdfViewerViewMenuOpenLastDoc.text"));
		openLastDoc.setToolTipText("Set if last document should be opened upon start up");
		
		border = new JCheckBox(Messages.getMessage("PageLayoutViewMenu.Borders_Show"));
		border.setToolTipText("Set if we should display a border for the page");
		
		downloadWindow = new JCheckBox(Messages.getMessage("PageLayoutViewMenu.DownloadWindow_Show"));
		downloadWindow.setToolTipText("Set if the download window should be displayed");
		
		HiResPrint = new JCheckBox(Messages.getMessage("Printing.HiRes"));
		HiResPrint.setToolTipText("Set if hi res printing should be enabled / disabled");
		
		constantTabs = new JCheckBox(Messages.getMessage("PdfCustomGui.consistentTabs"));
		constantTabs.setToolTipText("Set to keep sidetabs consistant between files");
		
		enhancedViewer = new JCheckBox(Messages.getMessage("PdfCustomGui.enhancedViewer"));
		enhancedViewer.setToolTipText("Set to use enahnced viewer mode");
		
		enhancedFacing = new JCheckBox(Messages.getMessage("PdfCustomGui.enhancedFacing"));
		enhancedFacing.setToolTipText("Set to turn facing mode to page turn mode");
		
		thumbnailScroll = new JCheckBox(Messages.getMessage("PdfCustomGui.thumbnailScroll"));
		thumbnailScroll.setToolTipText("Set to show thumbnail whilst scrolling");
		
		enhancedGUI = new JCheckBox(Messages.getMessage("PdfCustomGui.enhancedGUI"));
		enhancedGUI.setToolTipText("Set to enabled the enhanced gui");

        rightClick = new JCheckBox(Messages.getMessage("PdfCustomGui.allowRightClick"));
		rightClick.setToolTipText("Set to enable / disable the right click functionality");

        scrollwheelZoom = new JCheckBox(Messages.getMessage("PdfCustomGui.allowScrollwheelZoom"));
		scrollwheelZoom.setToolTipText("Set to enable zooming when scrolling with ctrl pressed");

		historyClearedLabel = new JLabel(Messages.getMessage("PageLayoutViewMenu.HistoryCleared"));
		historyClearedLabel.setForeground(Color.red);
		historyClearedLabel.setVisible(false);
		clearHistory = new JButton(Messages.getMessage("PageLayoutViewMenu.ClearHistory"));
		clearHistory.setToolTipText("Clears the history of previous files");
		clearHistory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				gui.clearRecentDocuments();

				SwingWorker searcher = new SwingWorker() {
					public Object construct() {
						for (int i = 0; i < 6; i++) {
							historyClearedLabel.setVisible(!historyClearedLabel.isVisible());
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
							}
						}
						return null;
					}
				};

				searcher.start();
			}
		});
		JButton save = new JButton(Messages.getMessage("PdfPreferences.SaveAs"));
		save.setToolTipText("Save preferences in a new file");
		JButton reset = new JButton(Messages.getMessage("PdfPreferences.ResetToDefault"));
		reset.setToolTipText("Reset  and save preferences to program defaults");
		
		//Create JFrame
		propertiesDialog.getContentPane().setLayout(new BorderLayout());
		propertiesDialog.getContentPane().add(this,BorderLayout.CENTER);
		propertiesDialog.pack();
        if (PdfDecoder.isRunningOnMac)
		    propertiesDialog.setSize(600, 475);
        else
		    propertiesDialog.setSize(550, 450);

        confirm.setText(Messages.getMessage("PdfPreferences.OK"));
    	cancel.setText(Messages.getMessage("PdfPreferences.Cancel"));
    	
		/*
		 * Listeners that are reqired for each setting field
		 */
		//Set properties and close the window
		confirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setPreferences(gui);
                if(Viewer.showMessages)
				JOptionPane.showMessageDialog(null, Messages.getMessage("PdfPreferences.savedTo")+propertiesLocation+ '\n' +Messages.getMessage("PdfPreferences.restart"), "Restart Jpedal", JOptionPane.INFORMATION_MESSAGE);
				propertiesDialog.setVisible(false);
			}
		});
		confirm.setToolTipText("Save the preferences in the current loaded preferences file");
		//Close the window, don't save the properties
		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				propertiesDialog.setVisible(false);
			}
		});
		cancel.setToolTipText("Leave preferences window without saving changes");
//		Save the properties into a new file
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//The properties file used when jpedal opened
				String lastProperties = gui.getPropertiesFileLocation();
				
				JFileChooser fileChooser = new JFileChooser();
				
				int i = fileChooser.showSaveDialog(propertiesDialog);
				
				if(i == JFileChooser.CANCEL_OPTION){
					//Do nothing
				}else if(i== JFileChooser.ERROR_OPTION){
					//Do nothing
				}else if(i == JFileChooser.APPROVE_OPTION){
					File f = fileChooser.getSelectedFile();

					if(f.exists())
						f.delete();
					
					//Setup properties in the new location
					gui.setPropertiesFileLocation(f.getAbsolutePath());
					setPreferences(gui);
				}
				//Reset to the properties file used when jpedal opened
				gui.setPropertiesFileLocation(lastProperties);
			}
		});
		//Reset the properties to JPedal defaults
		reset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(propertiesDialog, Messages.getMessage("PdfPreferences.reset") , "Reset to Default", JOptionPane.YES_NO_OPTION);
				//The properties file used when jpedal opened
				if(result == JOptionPane.YES_OPTION){
					String lastProperties = gui.getPropertiesFileLocation();

					File f = new File(lastProperties);
					if(f.exists()){
						f.delete();
//						System.exit(1);
					}

					gui.getProperties().loadProperties(lastProperties);

                    if(Viewer.showMessages)
					JOptionPane.showMessageDialog(propertiesDialog, Messages.getMessage("PdfPreferences.restart"));
					propertiesDialog.setVisible(false);
				}
			}
		});
		
		
		highlightComposite.addKeyListener(new KeyListener(){

			boolean consume = false;

			public void keyPressed(KeyEvent e) {
				consume = false;
				if((((JTextField) e.getSource()).getText().contains(".") && e.getKeyChar()=='.') &&
						((e.getKeyChar()<'0' || e.getKeyChar()>'9') && (e.getKeyCode()!=8 || e.getKeyCode()!=127)))
					consume = true;
			}

			public void keyReleased(KeyEvent e) {}

			public void keyTyped(KeyEvent e) {
				if(consume)
					e.consume();
			}
			
		});
		highlightComposite.setToolTipText("Set the transparency of the highlight");
		
		resolution.addKeyListener(numericalKeyListener);
		maxMultiViewers.addKeyListener(numericalKeyListener);
		
		/**
		 * Set the current properties from the properties file
		 */
		setLayout(new BorderLayout());

//		JButtonBar toolbar = new JButtonBar(JButtonBar.VERTICAL);
		JPanel toolbar = new JPanel();
		
		BoxLayout layout = new BoxLayout(toolbar, BoxLayout.Y_AXIS);
		toolbar.setLayout(layout);

		//if(PdfDecoder.isRunningOnMac)
		//	toolbar.setPreferredSize(new Dimension(120,0));

		add(new ButtonBarPanel(toolbar), BorderLayout.CENTER);

		toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		Dimension dimension = new Dimension(5,40);
		Box.Filler filler = new Box.Filler(dimension, dimension, dimension);

		confirm.setPreferredSize(cancel.getPreferredSize());

		if(properties.isReadOnly())
			confirm.setEnabled(false);
		else{
			confirm.setEnabled(true);
		}
		
		buttonPanel.add(reset);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(confirm);
		buttonPanel.add(save);
		getRootPane().setDefaultButton(confirm);

		buttonPanel.add(filler);
		buttonPanel.add(cancel);
		buttonPanel.add(filler);

		buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));

		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setPreferences(SwingGUI gui){
		int borderStyle = 0;
		int pageMode = (pageLayout.getSelectedIndex()+1);
		if(pageMode<Display.SINGLE_PAGE || pageMode>Display.PAGEFLOW)
			pageMode = Display.SINGLE_PAGE;
		if(border.isSelected()){
			borderStyle = 1;
		}
		
		int hBox = highlightBoxColor.getBackground().getRGB();
		int hText = highlightTextColor.getBackground().getRGB();
		int vbg = viewBGColor.getBackground().getRGB();
		int pbg = pdfDecoderBackground.getBackground().getRGB();
		int vfg = foreGroundColor.getBackground().getRGB();
//		int sbbg = sideBGColor.getBackground().getRGB();
		boolean changeTL = changeTextAndLineArt.isSelected();
		boolean isInvert = invertHighlight.isSelected();
		boolean replaceTextColors = replaceDocTextCol.isSelected();
		boolean replacePdfDisplayBackground = replaceDisplayBGCol.isSelected();
		boolean isBoxShown = showMouseSelectionBox.isSelected();
		
		/**
		 * set preferences from all but menu options
		 */
		properties.setValue("borderType", String.valueOf(borderStyle));
        properties.setValue("useHinting", String.valueOf(useHinting.isSelected()));
		properties.setValue("pageMode", String.valueOf(pageMode));
		properties.setValue("pageInsets", String.valueOf(pageInsets.getText()));
        properties.setValue("windowTitle", String.valueOf(windowTitle.getText()));
		String loc = iconLocation.getText();
		if(!loc.endsWith("/") && !loc.endsWith("\\"))
			loc = loc+ '/';
		properties.setValue("iconLocation", String.valueOf(loc));
		properties.setValue("sideTabBarCollapseLength", String.valueOf(sideTabLength.getText()));
		properties.setValue("autoScroll", String.valueOf(autoScroll.isSelected()));
        properties.setValue("confirmClose", String.valueOf(confirmClose.isSelected()));
		properties.setValue("openLastDocument", String.valueOf(openLastDoc.isSelected()));
		properties.setValue("resolution", String.valueOf(resolution.getText()));
		properties.setValue("searchWindowType", String.valueOf(searchStyle.getSelectedIndex()));
		properties.setValue("automaticupdate", String.valueOf(update.isSelected()));
		properties.setValue("maxmultiviewers", String.valueOf(maxMultiViewers.getText()));
		properties.setValue("showDownloadWindow", String.valueOf(downloadWindow.isSelected()));
		properties.setValue("useHiResPrinting", String.valueOf(HiResPrint.isSelected()));
		properties.setValue("consistentTabBar", String.valueOf(constantTabs.isSelected()));
		properties.setValue("highlightComposite", String.valueOf(highlightComposite.getText()));
		properties.setValue("highlightBoxColor", String.valueOf(hBox));
		properties.setValue("highlightTextColor", String.valueOf(hText));
		properties.setValue("vbgColor", String.valueOf(vbg));
		properties.setValue("pdfDisplayBackground", String.valueOf(pbg));
		properties.setValue("vfgColor", String.valueOf(vfg));
		properties.setValue("replaceDocumentTextColors", String.valueOf(replaceTextColors));
		properties.setValue("replacePdfDisplayBackground", String.valueOf(replacePdfDisplayBackground));
//		properties.setValue("sbbgColor", String.valueOf(sbbg));
		properties.setValue("changeTextAndLineart", String.valueOf(changeTL));
		properties.setValue("invertHighlights", String.valueOf(isInvert));
		properties.setValue("showMouseSelectionBox", String.valueOf(isBoxShown));
		properties.setValue("allowRightClick", String.valueOf(rightClick.isSelected()));
		properties.setValue("allowScrollwheelZoom", String.valueOf(scrollwheelZoom.isSelected()));
		properties.setValue("enhancedViewerMode", String.valueOf(enhancedViewer.isSelected()));
		properties.setValue("enhancedFacingMode", String.valueOf(enhancedFacing.isSelected()));
		properties.setValue("previewOnSingleScroll", String.valueOf(thumbnailScroll.isSelected()));
		properties.setValue("enhancedGUI", String.valueOf(enhancedGUI.isSelected()));
        properties.setValue("printerBlacklist", String.valueOf(printerBlacklist.getText()));
        if (((String)defaultPrinter.getSelectedItem()).startsWith("System Default"))
            properties.setValue("defaultPrinter", "");
        else
            properties.setValue("defaultPrinter", String.valueOf(defaultPrinter.getSelectedItem()));
        properties.setValue("defaultDPI", String.valueOf(defaultDPI.getText()));
        properties.setValue("defaultPagesize", String.valueOf(defaultPagesize.getSelectedItem()));
		//Save all options found in a tree
		saveGUIPreferences(gui);
	}

	class ButtonBarPanel extends JPanel {

		private Component currentComponent;
		
//		Switch between idependent and properties dependent 
		//private boolean newPreferencesCode = true;

		public ButtonBarPanel(JPanel toolbar) {
			setLayout(new BorderLayout());
			
			//Add scroll pane as too many options
			JScrollPane jsp = new JScrollPane();
			jsp.getViewport().add(toolbar);
			jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			
			add(jsp, BorderLayout.WEST);
			
			ButtonGroup group = new ButtonGroup();

            addButton(Messages.getMessage("PdfPreferences.GeneralTitle"), "/org/jpedal/examples/viewer/res/display.png", createGeneralSettings(), toolbar, group);

            addButton(Messages.getMessage("PdfPreferences.PageDisplayTitle"), "/org/jpedal/examples/viewer/res/pagedisplay.png", createPageDisplaySettings(), toolbar, group);

            addButton(Messages.getMessage("PdfPreferences.InterfaceTitle"), "/org/jpedal/examples/viewer/res/interface.png", createInterfaceSettings(), toolbar, group);

			
			
		}

		private JPanel makePanel(String title) {
			JPanel panel = new JPanel(new BorderLayout());
			JLabel topLeft = new JLabel(title);
			topLeft.setFont(topLeft.getFont().deriveFont(Font.BOLD));
			topLeft.setOpaque(true);
			topLeft.setBackground(panel.getBackground().brighter());
			
//			JLabel topRight = new JLabel("( "+propertiesLocation+" )");
//			topRight.setOpaque(true);
//			topRight.setBackground(panel.getBackground().brighter());
			
			JPanel topbar = new JPanel(new BorderLayout());
			topbar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			topbar.setFont(topbar.getFont().deriveFont(Font.BOLD));
			topbar.setOpaque(true);
			topbar.setBackground(panel.getBackground().brighter());
			
			topbar.add(topLeft, BorderLayout.WEST);
//			topbar.add(topRight, BorderLayout.EAST);
			
			panel.add(topbar, BorderLayout.NORTH);
			panel.setPreferredSize(new Dimension(400, 300));
			panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			return panel;
		}


        /*
		 * Creates a pane holding all General settings
		 */
		private JPanel createGeneralSettings(){

			/**
			 * Set values from Properties file
			 */
			String propValue = properties.getValue("resolution");
			if(propValue.length()>0)
				resolution.setText(propValue);

            propValue = properties.getValue("useHinting");
            if(propValue.length()>0 && propValue.equals("true"))
                useHinting.setSelected(true);
            else
                useHinting.setSelected(false);

            propValue = properties.getValue("autoScroll");
			if(propValue.equals("true"))
				autoScroll.setSelected(true);
			else
				autoScroll.setSelected(false);

            propValue = properties.getValue("confirmClose");
            if(propValue.equals("true"))
                confirmClose.setSelected(true);
            else
                confirmClose.setSelected(false);

            propValue = properties.getValue("automaticupdate");
			if(propValue.equals("true"))
				update.setSelected(true);
			else
				update.setSelected(false);

			propValue = properties.getValue("openLastDocument");
			if(propValue.equals("true"))
				openLastDoc.setSelected(true);
			else
				openLastDoc.setSelected(false);

			JPanel panel = makePanel(Messages.getMessage("PdfPreferences.GeneralTitle"));

			JPanel pane = new JPanel();
            JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.insets = new Insets(5,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralSection"));
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			pane.add(label, c);

            c.gridy++;

            c.insets = new Insets(10,0,0,5);
			c.gridx = 0;
			JLabel label2 = new JLabel(Messages.getMessage("PdfViewerViewMenu.Resolution"));
			label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label2, c);

			c.insets = new Insets(10,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			pane.add(resolution, c);

            c.gridy++;

            c.gridwidth = 2;
            c.gridx = 0;
            useHinting.setMargin(new Insets(0,0,0,0));
            useHinting.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(useHinting, c);

            c.gridy++;

            c.gridwidth = 2;
			c.gridx = 0;
			autoScroll.setMargin(new Insets(0,0,0,0));
			autoScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(autoScroll, c);

            c.gridy++;

			c.gridwidth = 2;
			c.gridx = 0;
			confirmClose.setMargin(new Insets(0,0,0,0));
			confirmClose.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(confirmClose, c);

            c.gridy++;

            c.insets = new Insets(15,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label3 = new JLabel(Messages.getMessage("PdfPreferences.StartUp"));
			label3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label3.setFont(label3.getFont().deriveFont(Font.BOLD));
			pane.add(label3, c);

            c.gridy++;

            c.insets = new Insets(10,0,0,0);
            c.weighty = 0;
            c.weightx = 1;
            c.gridwidth = 2;
            c.gridx = 0;
            update.setMargin(new Insets(0,0,0,0));
            update.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(update, c);

            c.gridy++;

			c.gridwidth = 2;
			c.gridx = 0;
			openLastDoc.setMargin(new Insets(0,0,0,0));
			openLastDoc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(openLastDoc, c);

            c.gridy++;

            c.gridwidth = 2;
			c.gridx = 0;
			JPanel clearHistoryPanel = new JPanel();
			clearHistoryPanel.setLayout(new BoxLayout(clearHistoryPanel, BoxLayout.X_AXIS));
			clearHistoryPanel.add(clearHistory);
			clearHistoryPanel.add(Box.createHorizontalGlue());

			clearHistoryPanel.add(historyClearedLabel);
			clearHistoryPanel.add(Box.createHorizontalGlue());
			pane.add(clearHistoryPanel, c);

            c.gridy++;

			c.weighty = 1;
			c.gridx = 0;
			pane.add(Box.createVerticalGlue(), c);

			panel.add(scroll, BorderLayout.CENTER);

			return panel;
		}


        /*
		 * Creates a pane holding all Page Display settings (e.g Insets, borders, display modes, etc)
		 */
		private JPanel createPageDisplaySettings(){

			/**
			 * Set values from Properties file
			 */
			String propValue = properties.getValue("enhancedViewerMode");
			if(propValue.length()>0 && propValue.equals("true"))
				enhancedViewer.setSelected(true);
			else
				enhancedViewer.setSelected(false);

            propValue = properties.getValue("borderType");
            if(propValue.length()>0)
                if(Integer.parseInt(propValue)==1)
                    border.setSelected(true);
                else
                    border.setSelected(false);

			propValue = properties.getValue("pageInsets");
			if(propValue!=null && propValue.length() != 0)
				pageInsets.setText(propValue);
			else
				pageInsets.setText("25");

            propValue = properties.getValue("pageMode");
			if(propValue.length()>0){
				int mode = Integer.parseInt(propValue);
				if(mode<Display.SINGLE_PAGE || mode>Display.PAGEFLOW)
					mode = Display.SINGLE_PAGE;

				pageLayout.setSelectedIndex(mode-1);
			}

			propValue = properties.getValue("enhancedFacingMode");
			if(propValue.length()>0 && propValue.equals("true"))
				enhancedFacing.setSelected(true);
			else
				enhancedFacing.setSelected(false);
			
			propValue = properties.getValue("previewOnSingleScroll");
			if(propValue.length()>0 && propValue.equals("true"))
				thumbnailScroll.setSelected(true);
			else
				thumbnailScroll.setSelected(false);

			JPanel panel = makePanel(Messages.getMessage("PdfPreferences.PageDisplayTitle"));

			JPanel pane = new JPanel();
			JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.insets = new Insets(5,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			c.gridy = 0;
			JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralSection"));
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			pane.add(label, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,0);
			c.gridwidth = 2;
			c.gridx = 0;
			enhancedViewer.setMargin(new Insets(0,0,0,0));
			enhancedViewer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(enhancedViewer, c);

            c.gridy++;

            c.gridwidth = 2;
			c.gridx = 0;
			border.setMargin(new Insets(0,0,0,0));
			border.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(border, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,0);
			c.gridwidth = 2;
			c.gridx = 0;
			pane.add(pageInsetsText, c);
			c.gridwidth = 2;
			c.gridx = 1;
			pane.add(pageInsets, c);

            c.gridy++;

            c.insets = new Insets(15,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label2 = new JLabel(Messages.getMessage("PdfPreferences.DisplayModes"));
			label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label2.setFont(label2.getFont().deriveFont(Font.BOLD));
			pane.add(label2, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,5);
			c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label1 = new JLabel(Messages.getMessage("PageLayoutViewMenu.PageLayout"));
			label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label1, c);

			c.insets = new Insets(5,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			pane.add(pageLayout, c);

            c.gridy++;

            
            c.gridwidth = 2;
            c.gridx = 0;
            thumbnailScroll.setMargin(new Insets(0,0,0,0));
            thumbnailScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(thumbnailScroll, c);
            
            c.gridy++;

			c.weighty = 1;
			c.gridx = 0;
			pane.add(Box.createVerticalGlue(), c);
			panel.add(scroll, BorderLayout.CENTER);

			return panel;
		}



        /*
		 * Creates a pane holding all Interface settings (e.g Search Style, icons, etc)
		 */
		private JPanel createInterfaceSettings(){

			/**
			 * Set values from Properties file
			 */
			String propValue = properties.getValue("enhancedGUI");
			if(propValue.length()>0 && propValue.equals("true"))
				enhancedGUI.setSelected(true);
            else
                enhancedGUI.setSelected(false);

            propValue = properties.getValue("allowRightClick");
            if(propValue.length()>0 && propValue.equals("true"))
                rightClick.setSelected(true);
            else
                rightClick.setSelected(false);

            propValue = properties.getValue("allowScrollwheelZoom");
            if(propValue.length()>0 && propValue.equals("true"))
                scrollwheelZoom.setSelected(true);
            else
                scrollwheelZoom.setSelected(false);

            propValue = properties.getValue("windowTitle");
            if (propValue!=null && propValue.length() != 0)
                windowTitle.setText(propValue);

            propValue = properties.getValue("iconLocation");
            if(propValue!=null && propValue.length() != 0)
                iconLocation.setText(propValue);
            else
                iconLocation.setText("/org/jpedal/examples/viewer/res/");

            propValue = properties.getValue("searchWindowType");
            if(propValue.length()>0)
                searchStyle.setSelectedIndex(Integer.parseInt(propValue));
            else
                searchStyle.setSelectedIndex(0);

            propValue = properties.getValue("maxmultiviewers");
            if (propValue != null && propValue.length()>0)
                maxMultiViewers.setText(propValue);

            propValue = properties.getValue("sideTabBarCollapseLength");
            if(propValue!=null && propValue.length() != 0)
                sideTabLength.setText(propValue);
            else
                sideTabLength.setText("30");

            propValue = properties.getValue("consistentTabBar");
            if(propValue.length()>0 && propValue.equals("true"))
                constantTabs.setSelected(true);
            else
                constantTabs.setSelected(false);
            
            String showBox = properties.getValue("showMouseSelectionBox");
            if(showBox.length()>0 && showBox.toLowerCase().equals("true"))
                showMouseSelectionBox.setSelected(true);
            else
            	showMouseSelectionBox.setSelected(false);

            JPanel panel = makePanel(Messages.getMessage("PdfPreferences.InterfaceTitle"));

            JTabbedPane tabs = new JTabbedPane();

			JPanel pane = new JPanel();
			JScrollPane scroll = new JScrollPane(pane);
            scroll.setBorder(BorderFactory.createEmptyBorder());
			pane.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

            c.insets = new Insets(5,0,0,5);
            c.gridwidth = 1;
            c.gridy = 0;
            c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label = new JLabel(Messages.getMessage("PdfPreferences.GeneralTitle"));
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			pane.add(label, c);

            c.gridy++;

			c.insets = new Insets(5,0,0,5);
            c.gridx = 0;
            c.gridwidth = 2;
            enhancedGUI.setMargin(new Insets(0,0,0,0));
            enhancedGUI.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            pane.add(enhancedGUI, c);

            c.gridy++;

            c.insets = new Insets(3,0,0,0);
            c.gridwidth=1;
            c.gridx=0;
            pane.add(windowTitleText, c);
            c.gridx = 1;
            pane.add(windowTitle, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,0);
			c.gridwidth = 1;
			c.gridx = 0;
			pane.add(iconLocationText, c);
			c.gridx = 1;
			pane.add(iconLocation, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,5);
			c.gridx = 0;
			JLabel label5 = new JLabel(Messages.getMessage("PageLayoutViewMenu.SearchLayout"));
			label5.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(label5, c);

			c.insets = new Insets(5,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			pane.add(searchStyle, c);

            c.gridy++;

            
            c.insets = new Insets(15,0,0,5);
            c.gridwidth = 1;
            c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label1 = new JLabel(Messages.getMessage("PdfPreferences.SideTab"));
			label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label1.setFont(label1.getFont().deriveFont(Font.BOLD));
			pane.add(label1, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,0);
			c.gridwidth = 1;
			c.gridx = 0;
			sideTabLengthText.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(sideTabLengthText, c);

			c.insets = new Insets(5,0,0,0);
			c.weightx = 1;
			c.gridx = 1;
			pane.add(sideTabLength, c);

            c.gridy++;

            c.insets = new Insets(5,0,0,0);
            c.gridwidth = 2;
			c.gridx = 0;
			constantTabs.setMargin(new Insets(0,0,0,0));
			constantTabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane.add(constantTabs, c);

            c.gridy++;

            c.weighty = 1;
			c.gridx = 0;
			pane.add(Box.createVerticalGlue(), c);
			//pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

            tabs.add(Messages.getMessage("PdfPreferences.AppearanceTab"), scroll);

            
            JPanel pane2 = new JPanel();
            JScrollPane scroll2 = new JScrollPane(pane2);
            scroll2.setBorder(BorderFactory.createEmptyBorder());
			pane2.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;


            c.insets = new Insets(5,0,0,5);
            c.gridwidth = 1;
            c.gridy = 0;
            c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label3 = new JLabel(Messages.getMessage("PdfPreferences.GeneralTitle"));
			label3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label3.setFont(label3.getFont().deriveFont(Font.BOLD));
			pane2.add(label3, c);

            c.gridy++;

            c.gridwidth = 2;
			c.gridx = 0;
			rightClick.setMargin(new Insets(0,0,0,0));
			rightClick.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane2.add(rightClick, c);

            c.gridy++;

            c.gridwidth = 2;
			c.gridx = 0;
			scrollwheelZoom.setMargin(new Insets(0,0,0,0));
			scrollwheelZoom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			pane2.add(scrollwheelZoom, c);
            
            c.gridy++;

            c.insets = new Insets(0,0,0,5);
            c.gridwidth = 1;
            c.gridx = 0;
            pane2.add(showMouseSelectionBox, c);

            c.gridy++;

            c.weighty = 1;
			c.gridx = 0;
			pane2.add(Box.createVerticalGlue(), c);
			//pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0.3f,0.5f,1f), 1), "Display Settings"));

            tabs.add(Messages.getMessage("PdfPreferences.Mouse"), scroll2);
            
            JPanel pane3 = new JPanel();
            JScrollPane scroll3 = new JScrollPane(pane3);
            scroll3.setBorder(BorderFactory.createEmptyBorder());
			pane3.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;


            c.insets = new Insets(5,0,0,5);
            c.gridwidth = 1;
            c.gridy = 0;
            c.weighty = 0;
			c.weightx = 0;
			c.gridx = 0;
			JLabel label6 = new JLabel(Messages.getMessage("PdfPreferences.GeneralTitle"));
			label6.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			label6.setFont(label6.getFont().deriveFont(Font.BOLD));
			pane3.add(label6, c);

            
			panel.add(tabs, BorderLayout.CENTER);

			return panel;
		}


         /*
		 * Creates a pane holding all Printing settings
		 */
		
		private void  addMenuToTree(int tab, NodeList nodes, CheckNode top, java.util.List previous){
			
			for(int i=0; i!=nodes.getLength(); i++){
				
				if(i<nodes.getLength()){
					String name = nodes.item(i).getNodeName();
					if(!name.startsWith("#")){
						//Node to add
						CheckNode newLeaf = new CheckNode(Messages.getMessage("PdfCustomGui."+name));
						newLeaf.setEnabled(true);
						//Set to reversedMessage for saving of preferences
						reverseMessage.put(Messages.getMessage("PdfCustomGui."+name), name);
						String propValue = properties.getValue(name);
						//Set if should be selected
						if(propValue.length()>0 && propValue.equals("true")){
							newLeaf.setSelected(true);
						}else{
							newLeaf.setSelected(false);
						}
						
						//If has child nodes
						if(nodes.item(i).hasChildNodes()){
							//Store this top value
							previous.add(top);
							//Set this node to ned top
							top.add(newLeaf);
							//Add new menu to tree
							addMenuToTree(tab, nodes.item(i).getChildNodes(), newLeaf, previous);
						}else{
							//Add to current top
							top.add(newLeaf);
						}
					}
				}
			}
		}



		private void show(Component component) {
			if (currentComponent != null) {
				remove(currentComponent);
			}

			add("Center", currentComponent = component);
			revalidate();
			repaint();
		}

		private void addButton(String title, String iconUrl, final Component component, JPanel bar, ButtonGroup group) {
			Action action = new AbstractAction(title, new ImageIcon(getClass().getResource(iconUrl))) {
				public void actionPerformed(ActionEvent e) {
					show(component);
				}
			};

			JToggleButton button = new JToggleButton(action);
			button.setVerticalTextPosition(JToggleButton.BOTTOM);
		    button.setHorizontalTextPosition(JToggleButton.CENTER);
		    
			button.setContentAreaFilled(false);
			if(PdfDecoder.isRunningOnMac)
				button.setHorizontalAlignment(AbstractButton.LEFT);
			
			//Center buttons
			button.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			bar.add(button);

			group.add(button);

			if (group.getSelection() == null) {
				button.setSelected(true);
				show(component);
			}
		}


    }

	public void setParent(Container parent) {
		this.parent = parent;
	}

	public void dispose() {
		
		this.removeAll();
		
		reverseMessage =null;
		
		menuTabs=null;
		propertiesLocation  =null;
		
		if(propertiesDialog!=null)
		propertiesDialog.removeAll();
		propertiesDialog=null;
		
		confirm  =null;

		cancel  =null;

		if(tabs!=null)
		tabs.removeAll();
		tabs=null;

		resolution=null;

		searchStyle=null;

		border =null;

		downloadWindow =null;

		HiResPrint =null;

		constantTabs=null;

		enhancedViewer=null;
		
		enhancedFacing=null;
		
		thumbnailScroll=null;
		
		enhancedGUI=null;
		
		rightClick=null;

        scrollwheelZoom=null;
		
		update  =null;

		maxMultiViewers =null;

		pageInsets =null;
		pageInsetsText = null;

        windowTitle = null;
        windowTitleText = null;
		
		iconLocation = null;
		iconLocationText = null;

        printerBlacklist = null;
        printerBlacklistText = null;

        defaultPrinter = null;
        defaultPrinterText = null;

        defaultPagesize = null;
        defaultPagesizeText = null;

        defaultDPI = null;
        defaultDPIText = null;

		sideTabLength = null;
		sideTabLengthText = null;
		
        useHinting = null;
		
		autoScroll =null;

        confirmClose = null;

		openLastDoc =null;
		
		pageLayout =null;

		if(highlightBoxColor!=null)
			highlightBoxColor.removeAll();
		highlightBoxColor  =null;
		
		if(highlightTextColor!=null)
			highlightTextColor.removeAll();
		highlightTextColor =null;
		
		if(viewBGColor!=null)
			viewBGColor.removeAll();
		viewBGColor =null;
		
		if(pdfDecoderBackground!=null)
			pdfDecoderBackground.removeAll();
		pdfDecoderBackground =null;
		
		if(foreGroundColor!=null)
			foreGroundColor.removeAll();
		foreGroundColor =null;
		
//		if(sideBGColor!=null)
//			sideBGColor.removeAll();
//		sideBGColor =null;
		
		
		if(invertHighlight!=null)
			invertHighlight.removeAll();
		invertHighlight =null;
		
		if(replaceDocTextCol!=null)
			replaceDocTextCol.removeAll();
		replaceDocTextCol =null;
		
		if(replaceDisplayBGCol!=null)
			replaceDisplayBGCol.removeAll();
		replaceDisplayBGCol =null;
		
		if(changeTextAndLineArt!=null)
			changeTextAndLineArt.removeAll();
		changeTextAndLineArt =null;
		
		showMouseSelectionBox = null;
		
		if(highlightComposite!=null)
			highlightComposite.removeAll();
		highlightComposite =null;
		
		if(propertiesDialog!=null)
			propertiesDialog.removeAll();
		parent =null;

		clearHistory =null;

		historyClearedLabel =null;
		
	}
	
	private static boolean hasFreetts()
	{
		return false;
		/**/
	}
}