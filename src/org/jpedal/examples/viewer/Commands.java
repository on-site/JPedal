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
 * Commands.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import org.jpedal.Display;
import org.jpedal.SingleDisplay;
import org.jpedal.constants.JPedalSettings;
import org.jpedal.constants.PDFImageProcessing;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.external.Options;
import org.jpedal.linear.LinearThread;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.SwingWorker;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.viewer.gui.SwingGUI;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.gui.generic.GUIThumbnailPanel;
import org.jpedal.examples.viewer.gui.popups.*;
import org.jpedal.examples.viewer.gui.swing.SearchList;
import org.jpedal.examples.viewer.gui.swing.SwingSearchWindow;
//<start-wrap>
import org.jpedal.examples.viewer.objects.SignData;
//<end-wrap>
import org.jpedal.examples.viewer.utils.*;
import org.jpedal.exception.PdfException;
import org.jpedal.external.JPedalActionHandler;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchType;
import org.jpedal.io.JAIHelper;
import org.jpedal.io.JAITiffHelper;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.acroforms.actions.DefaultActionHandler;
import org.jpedal.utils.*;
import org.jpedal.utils.repositories.Vector_Rectangle;

import javax.imageio.ImageIO;
import javax.print.PrintServiceLookup;
import javax.swing.*;

import java.awt.*;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;


import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.beans.PropertyVetoException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;

/**This class contains code to execute the actual commands.*/
public class Commands {

    /**custom hi-res val for JPedal settings*/
    public static boolean hires = true;

    public boolean extractingAsImage = false;

    //flag to track if page decoded twice
    private int lastPageDecoded = -1;

    //<link><a name="commandInts" />

    public static final int INFO = 1;
	public static final int BITMAP = 2;
	public static final int IMAGES = 3;
	public static final int TEXT = 4;
	public static final int SAVE = 5;
	public static final int PRINT = 6;
	public static final int EXIT = 7;
	public static final int AUTOSCROLL = 8;
	public static final int DOCINFO = 9;
	public static final int OPENFILE = 10;
	public static final int BOOKMARK = 11;
	public static final int FIND = 12;
	public static final int SNAPSHOT = 13;
	public static final int OPENURL = 14;
	public static final int VISITWEBSITE = 15;
	public static final int PREVIOUSDOCUMENT = 16;
	public static final int NEXTDOCUMENT = 17;
	public static final int PREVIOUSRESULT = 18;
	public static final int NEXTRESULT = 19;
	public static final int TIP = 20;
	public static final int CASCADE = 21;
	public static final int TILE = 22;
	public static final int UPDATE = 23;
	public static final int PREFERENCES = 24;
	public static final int COPY = 25;
	public static final int SELECTALL = 26;
	public static final int DESELECTALL = 27;
	public static final int UPDATEGUILAYOUT = 28;
	public static final int MOUSEMODE = 29;
	public static final int PANMODE = 30;
	public static final int TEXTSELECT = 31;
    public static final int SEPARATECOVER = 32;
	

	public static final int FIRSTPAGE = 50;
	public static final int FBACKPAGE = 51;
	public static final int BACKPAGE = 52;
	public static final int FORWARDPAGE = 53;
	public static final int FFORWARDPAGE = 54;
	public static final int LASTPAGE = 55;
	public static final int GOTO = 56;

	public static final int SINGLE = 57;
	public static final int CONTINUOUS = 58;
	public static final int CONTINUOUS_FACING = 59;
	public static final int FACING = 60;
	public static final int PAGEFLOW = 62;

	public static final int FULLSCREEN=61;

	public static final int RSS = 997;
    public static final int HELP = 998;
    public static final int BUY = 999;
	
	//combo boxes start at 250
	public static final int QUALITY = 250;
	public static final int ROTATION = 251;
	public static final int SCALING = 252;

	//<link><a name="constants" />
	// external/itext menu options start at 500 - add your own CONSTANT here
	// and refer to action using name at ALL times
	public static final int SAVEFORM = 500;
	public static final int PDF = 501;
	public static final int ROTATE=502;
	public static final int DELETE=503;
	public static final int ADD=504;
	public static final int SECURITY=505;
	public static final int ADDHEADERFOOTER=506;
	public static final int STAMPTEXT=507;
	public static final int STAMPIMAGE=508;
	public static final int SETCROP=509;
	public static final int NUP = 510;
	public static final int HANDOUTS = 511;
	public static final int SIGN = 512;
	//public static final int NEWFUNCTION = 513;

	public static final int HIGHLIGHT = 600;
	public static final int SCROLL = 601;
	
	// commands for the forward and back tracking of views, ie when a page changes
	public static final int ADDVIEW = 700,FORWARD = 701,BACK = 702,PAGECOUNT=703,CURRENTPAGE=704;

    public static final int GETOUTLINEPANEL =705;
    public static final int GETTHUMBNAILPANEL =706;
    public static final int GETPAGECOUNTER =707;
    public static final int PAGEGROUPING =708;
    
    public static final int SETPAGECOLOR=709;
    public static final int SETUNDRAWNPAGECOLOR=710;
    public static final int REPLACETEXTCOLOR=711;
    public static final int SETTEXTCOLOR=712;
    public static final int CHANGELINEART=713;
    public static final int SETDISPLAYBACKGROUND=714;
    public static final int SETREPLACEMENTCOLORTHRESHOLD=715;

	

    private boolean isPDf = false;

    //status values returned by command
    public static final Integer FIRST_DOCUMENT_SEARCH_RESULT_NOW_SHOWN = 1;
    public static final Integer SEARCH_RETURNED_TO_START = 2;
    public static final Integer SEARCH_NOT_FOUND = 3;

	private boolean allHighlightsShown = false;
	
	private Values commonValues;
	private SwingGUI currentGUI;
	private PdfDecoder decode_pdf;

    /** Enable / Disable Point and Click image extraction */
    private boolean ImageExtractionAllowed = true;

	private GUIThumbnailPanel thumbnails;

	/**window for full screen mode*/
	Window win;

    /**location to restore after full screen*/
    private Point screenPosition=null;
	
	/**Multi page tiff image loading*/
	private int tiffImageToLoad = 0;

    public InputStream inputStream=null;
	
	/**Objects required to load Tiff using JAI*/
    private JAITiffHelper tiffHelper=null;

	/**image if file tiff or png or jpg*/
	private BufferedImage img=null;

	private int noOfRecentDocs;
	private RecentDocuments recent;

	private JMenuItem[] recentDocuments;

	private final Font headFont=new Font("SansSerif",Font.BOLD,10);

	/**flag used for text popup display to show if menu disappears*/
	private boolean display=true;

	private PropertiesFile properties;

	final private GUISearchWindow searchFrame;

	private SearchList results;

	private Printer currentPrinter;

    private boolean irregularSizesWarningShown=false;

	/**atomic lock for open thread*/
	private boolean isOpening;
	private boolean fileIsURL;

	

	private boolean openingTransferedFile;

    /**
     * tracks mouse operation mode currently selected
     */
    private MouseMode mouseMode=new MouseMode();

    public Display getPages() {
        return pages;
    }

    /**access to page display*/
    public Display pages=null;

    public Commands(Values commonValues,SwingGUI currentGUI,PdfDecoder decode_pdf,GUIThumbnailPanel thumbnails,
			PropertiesFile properties , GUISearchWindow searchFrame,Printer currentPrinter) {
		this.commonValues=commonValues;
		this.currentGUI=currentGUI;
		this.decode_pdf=decode_pdf;

        pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));

		this.thumbnails=thumbnails;
		this.properties=properties;
		this.currentPrinter=currentPrinter;

		this.noOfRecentDocs=properties.getNoRecentDocumentsToDisplay();
		recentDocuments = new JMenuItem[noOfRecentDocs];
		
		this.recent=new RecentDocuments(noOfRecentDocs,properties);

		this.searchFrame=searchFrame;
		
	}
	
	public void addToRecentDocuments(String selectedFile){
		recent.addToFileList(selectedFile);
	}

	private boolean executingCommand = false;
	
    public boolean isExecutingCommand() {
		return executingCommand;
	}

	//<link><a name="commands" />
    /**
	 * main routine which executes code for current command
     *
     * Values can also be passed in so it can be called from your own code
     *
     * some commands return a status Object otherwise null
	 */
	public Object executeCommand(int ID, Object[] args) {

        Object status =null;

        executingCommand = true;
        
        //If we are running command in thread do not mark command as executed at end of method, it is handled by thread.
        boolean isCommandInThread = false;
        
        String fileToOpen;
		
		Map jpedalActionHandlers = (Map) decode_pdf.getExternalHandler(Options.JPedalActionHandlers);
		
		if(jpedalActionHandlers != null) {
			JPedalActionHandler jpedalAction = (JPedalActionHandler) jpedalActionHandlers.get(ID);
			if (jpedalAction != null) {
				jpedalAction.actionPerformed(currentGUI, this);
				return null;
			}
		}
		
		switch(ID){


		case HIGHLIGHT:

            decode_pdf.getTextLines().clearHighlights();

			if(args!=null){
			
				Rectangle[] highlights = (Rectangle[]) args[0];
				int page = (Integer) args[1];
				boolean areaSelect = true;
				if(args.length>2)
					areaSelect = (Boolean)args[2];
				//decode_pdf.getTextLines().clearHighlights();
				
				//add text highlight                    
				decode_pdf.getTextLines().addHighlights(highlights, areaSelect, page);

				//highlights[0].x=1;

				//                decode_pdf.scrollRectToHighlight(highlights[0]);

                decode_pdf.invalidate();
				decode_pdf.repaint();
			}
			break;
			
		case SCROLL:
			if(args==null){
			
			} else {
				Rectangle scrollTo = (Rectangle)args[0];
				int page = commonValues.getCurrentPage();
				if(args.length>1 && args[1]!=null){
					page = (Integer) args[1];
				}
				
				if (scrollTo != null) {
					scrollRectToHighlight(scrollTo, page);
					decode_pdf.invalidate();
					decode_pdf.repaint();
				}
			}
			break;
		
		case INFO:
			
			if(args==null)
				currentGUI.getInfoBox();
			else{

			}
			break;

		case BITMAP:
			if(args==null){
				if (commonValues.getSelectedFile() == null) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.OpenFile"));
				} else {
					// get values from user
					SaveBitmap current_selection = new SaveBitmap(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int userChoice = current_selection.display(currentGUI.getFrame(), Messages.getMessage("PdfViewer.SaveAsBitmap"));

					// get parameters and call if YES
					if (fileIsURL) {
						currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.CannotExportFromURL"));
					} else if (userChoice == JOptionPane.OK_OPTION) {
					}
				}
			}else{

			}
			
			break;

		case IMAGES:
			if(args==null){
				if (commonValues.getSelectedFile() == null) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				} else {
					// get values from user

					SaveImage current_selection = new SaveImage(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int userChoice = current_selection.display(currentGUI.getFrame(), Messages.getMessage("PdfViewerTitle.SaveImagesFromPageRange"));

					// get parameters and call if YES
					if (fileIsURL) {
						currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.CannotExportFromURL"));
					} else if (userChoice == JOptionPane.OK_OPTION) {
						Exporter exporter = new Exporter(currentGUI, commonValues.getSelectedFile(), decode_pdf);
						exporter.extractImagesOnPages(current_selection);
					}
				}
			}else{

			}
			
			break;
			
		case TEXT:
			if(args==null){
				if (commonValues.getSelectedFile() == null) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				} else if (!decode_pdf.isExtractionAllowed()) {
					currentGUI.showMessageDialog("Not allowed");
				} else {
					// get values from user
					SaveText current_selection = new SaveText(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int userChoice = current_selection.display(currentGUI.getFrame(), Messages.getMessage("PdfViewerTitle.SaveTextFromPageRange"));

					// get parameters and call if YES
					if (fileIsURL) {
						currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.CannotExportFromURL"));
					} else if (userChoice == JOptionPane.OK_OPTION) {
						Exporter exporter = new Exporter(currentGUI, commonValues.getSelectedFile(), decode_pdf);
						exporter.extractTextOnPages(current_selection);
					}
				}
			}else{

			}
			
			break;

		case SAVE:
			if(args==null)
				saveFile();
			else{

			}
			
			break;

        case PRINT:

            currentGUI.showMessageDialog("OS version does not support printing");
            /**/
			
			break;

		case EXIT:
			if(args==null){
				if (Printer.isPrinting()) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerStillPrinting.text"));
                }else
					exit();
	        } else {

			}
			
			break;

		case AUTOSCROLL:
			if(args==null)
				currentGUI.toogleAutoScrolling();
			else{

			}
			
			break;

		case DOCINFO:
			if(args==null){
				if(!commonValues.isPDF())
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ImageSearch"));
				else
					currentGUI.showDocumentProperties(commonValues.getSelectedFile(), commonValues.getInputDir(), commonValues.getFileSize(), commonValues.getPageCount(), commonValues.getCurrentPage());
				} else {

			}
			
			break;

		case OPENFILE:

            /**
            * special case for pges build where file inside jar
            */
            //<start-wrap>
            //reset value to null
            inputStream=null;
            //<end-wrap>

			if(args==null){
			
				/**
				 * warn user on forms
				 */
				handleUnsaveForms();

				if(Printer.isPrinting())
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPrintWait.message"));
				else if(Values.isProcessing() || isOpening)
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
				else{
					
					selectFile();

					fileIsURL = false;
				}
			}else{
				if (args.length == 2 && args[0] instanceof byte[] && args[1] instanceof String) {

					byte[] data = (byte[]) args[0];
					String filename = (String) args[1];

					commonValues.setFileSize(data.length);

					commonValues.setSelectedFile(filename);
					currentGUI.setViewerTitle(null);

					if ((commonValues.getSelectedFile() != null) && (Values.isProcessing() == false)) {
						// reset the viewableArea before opening a new file
						decode_pdf.resetViewableArea();
						/**/

						try {
                            isPDf=true;
                            currentGUI.setMultiPageTiff(false);

							// get any user set dpi
                            String hiresFlag = System.getProperty("org.jpedal.hires");
							if (Commands.hires || hiresFlag != null)
								commonValues.setUseHiresImage(true);
							// get any user set dpi
                            String memFlag = System.getProperty("org.jpedal.memory");
							if (memFlag != null) {
								commonValues.setUseHiresImage(false);
							}

							// reset flag
							thumbnails.resetToDefault();

							// flush forms list
							currentGUI.setNoPagesDecoded();

							// remove search frame if visible
							if (searchFrame != null)
								searchFrame.removeSearchWindow(false);

							commonValues.maxViewY = 0;// rensure reset for any  viewport

							currentGUI.setQualityBoxVisible(commonValues.isPDF());

							commonValues.setCurrentPage(1);

							if (currentGUI.isSingle())
								decode_pdf.closePdfFile();

							decode_pdf.openPdfArray(data);

							currentGUI.updateStatusMessage("opening file");

							boolean fileCanBeOpened = true;

							if ((decode_pdf.isEncrypted()) && (!decode_pdf.isFileViewable())) {
								fileCanBeOpened = false;

								String password =System.getProperty("org.jpedal.password");
								
								if(password==null)
								password=currentGUI.showInputDialog(Messages.getMessage("PdfViewerPassword.message")); //$NON-NLS-1$

								/** try and reopen with new password */
								if (password != null) {
									decode_pdf.setEncryptionPassword(password);
									// decode_pdf.verifyAccess();

									if (decode_pdf.isFileViewable())
										fileCanBeOpened = true;

								}

								if (!fileCanBeOpened)
									currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPasswordRequired.message"));

							}
							if (fileCanBeOpened) {

								if (properties.getValue("Recentdocuments").equals("true")) {
									properties.addRecentDocument(commonValues.getSelectedFile());
									updateRecentDocuments(properties.getRecentDocuments());
								}

								addToRecentDocuments(commonValues.getSelectedFile());

								/** reset values */
								commonValues.setCurrentPage(1);
							}

							processPage();
						} catch (PdfException e) {
						}
					}

				} else if (args.length >= 1) {

                    if(args[0] instanceof InputStream){

                        inputStream=(InputStream)args[0];

                        currentGUI.resetNavBar();
                        String newFile ="InputStream-"+System.currentTimeMillis()+".pdf";

                        commonValues.setSelectedFile(newFile);
                        fileIsURL = true;

                        /**
                         * decode pdf
                         */
                        if (inputStream != null) {
                            try {
                                commonValues.setFileSize(0);
                                currentGUI.setViewerTitle(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            /**
                             * open the file
                             */
                            if (!Values.isProcessing()) {

                                /** if running terminate first */
                                thumbnails.terminateDrawing();

                                decode_pdf.flushObjectValues(true);

                                // reset the viewableArea before opening a new file
                                decode_pdf.resetViewableArea();

                                currentGUI.stopThumbnails();

                                //if (!currentGUI.isSingle())
                                  //  openNewMultiplePage(commonValues.getSelectedFile());

                                try {
                                    //Set to true to show our default download window
                                    currentGUI.setUseDownloadWindow(false);
                                    openFile(commonValues.getSelectedFile());

                                    while (Values.isProcessing()) {
                                        Thread.sleep(1000);

                                    }
                                } catch (Exception e) {
                                }
                            }

                        } else { // no file selected so redisplay old
                            decode_pdf.repaint();
                            // currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
                        }

                    }else{
                        File file;
                        if (args[0] instanceof File)
                            file = (File) args[0];
                        else if (args[0] instanceof String){
                        	String filename = (String) args[0];
                        	char[] str = filename.toCharArray();
                        	if(str[1]==':' || str[0]=='\\' ||str[0]=='/')//root
                        		file = new File(filename);
                        	else{
                        		String parent = new File(commonValues.getSelectedFile()).getParent();
                        		file =new File(parent,filename);
                        		try {
                        			file = file.getCanonicalFile();
								} catch (Exception e) {
									file =new File(parent,filename);
								}
                        	}
                        }else {
                            file = null;
                        }

                        /**
                         * decode
                         */
                        if (file != null) {
                            /** save path so we reopen her for later selections */
                            try {
                                commonValues.setInputDir(file.getParentFile().getCanonicalPath());
                                open(file.getAbsolutePath());

                                /**
                                 * see if second value as Named Dest and store object ref if set
                                 */
                                String bookmarkPage=null;
                                if(args.length>1 && args[1] instanceof String ){ //it may be a named destination ( ie bookmark=Test1)

                                    String bookmark=(String) args[1];
                                    bookmarkPage=decode_pdf.getIO().convertNameToRef(bookmark);

                                }
                                
                                if(bookmarkPage!=null){ //and goto named Dest if present

                                    //read the object
                                    PdfObject namedDest=new OutlineObject(bookmarkPage);
                                    decode_pdf.getIO().readObject(namedDest);

                                    //and generic open Dest code
                                    decode_pdf.getFormRenderer().getActionHandler().gotoDest(namedDest, ActionHandler.MOUSECLICKED, PdfDictionary.Dest );
                                }

                                while (Values.isProcessing()) {
                                    // Do nothing until pdf loaded
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }
                            } catch (IOException e1) {
                            }
                        } else { // no file selected so redisplay old
                            decode_pdf.repaint();
                            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
                        }
                    }
                    }
                }

			break;

		case BOOKMARK:
			
			//Only works if a bookmark is specified and the currentGUI is not null
			if(args.length>=1 && currentGUI!=null){
				String bookmark = (String)args[0];
				
				currentGUI.setBookmarks(true);
				
				String page = currentGUI.getBookmark(bookmark);
				
				if(page!=null){
					int p = Integer.parseInt(page);

					try {
						decode_pdf.decodePage(p);
						decode_pdf.repaint();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			break;
			
		case SNAPSHOT:
			if(args==null){
				if (decode_pdf.getDisplayView() != Display.SINGLE_PAGE) {
					currentGUI.showMessageDialog(Messages.getMessage("PageLayoutMessage.SinglePageOnly"));
				} else {
                    extractingAsImage=true;
                    PdfDecoder.showMouseBox = true;
					decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				}
			}else{

			}
			
			break;

		case FIND:
			if(args==null){
				if (commonValues.getSelectedFile() == null) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				} else if (!commonValues.isPDF()) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ImageSearch"));
				} else if (decode_pdf.getDisplayView() != Display.SINGLE_PAGE &&
						decode_pdf.getDisplayView() != Display.CONTINUOUS &&
						decode_pdf.getDisplayView() != Display.CONTINUOUS_FACING &&
						decode_pdf.getDisplayView() != Display.FACING) {
					currentGUI.showMessageDialog(Messages.getMessage("PageLayoutMessage.SingleContfacingFacingPageOnly"));
				} else if ((!searchFrame.isSearchVisible())) {
					searchFrame.find(decode_pdf, commonValues);
				} //else
				searchFrame.grabFocusInInput();
				
				/**
				 * Take highlighted text and add to search
				 */
				if(decode_pdf.getTextLines().getHighlightedAreas(commonValues.getCurrentPage())!=null){
					String searchText = copySelectedText();
					((SwingSearchWindow)searchFrame).setSearchText(searchText);
					//System.out.println("SET TEXT");
				}
			} else {
                boolean multiTerms, singlePageSearch;
                int searchType;
                String value = (String)args[0];
				if (args.length > 1) {
                    searchType = (Integer) args[1];
                    multiTerms = (Boolean) args[2];
                    singlePageSearch = (Boolean) args[3];
                } else {
                    searchType = SearchType.DEFAULT;
                    multiTerms = false;
                    singlePageSearch = false;
                }
                allHighlightsShown = (searchType & SearchType.HIGHLIGHT_ALL_RESULTS)
                        == SearchType.HIGHLIGHT_ALL_RESULTS;
				
				searchFrame.findWithoutWindow(decode_pdf, commonValues, searchType,
						multiTerms, singlePageSearch, value);
				
//				while(searchFrame.isSearching()){
//					System.out.println("Currently Searching");
//				}
//				System.out.println("Search has stopped");
				
			}

			break;

        case PAGEGROUPING:

            if(args!=null){

                int i= (Integer) args[0];
                if(i==decode_pdf.getlastPageDecoded()){
                    try {
                        status=decode_pdf.getGroupingObject();
                    } catch (PdfException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        decode_pdf.decodePageInBackground(i);
                        status=decode_pdf.getBackgroundGroupingObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
			    }
                //ensure done
                decode_pdf.waitForDecodingToFinish();
            }
            break;

		case OPENURL:

            //reset value
            inputStream=null;

			if(args==null){
				/**
				 * warn user on forms
				 */
				handleUnsaveForms();

				currentGUI.resetNavBar();
				String newFile = selectURL();
				if (newFile != null) {
					commonValues.setSelectedFile(newFile);
					fileIsURL = true;
				}
			}else{

				currentGUI.resetNavBar();
				String newFile = (String) args[0];
				if (newFile != null) {
					commonValues.setSelectedFile(newFile);
					fileIsURL = true;

					boolean failed = false;
					try {
						URL testExists = new URL(newFile);
						URLConnection conn = testExists.openConnection();

						if (conn.getContent() == null)
							failed = true;
					} catch (Exception e) {
						failed = true;
					}

					if (failed)
						newFile = null;

					/**
					 * decode pdf
					 */
					if (newFile != null) {
						try {
							commonValues.setFileSize(0);
							currentGUI.setViewerTitle(null);
						} catch (Exception e) {
							e.printStackTrace();
						}

						/**
						 * open the file
						 */
						if ((newFile != null) && (Values.isProcessing() == false)) {

							/** if running terminate first */
							thumbnails.terminateDrawing();

							decode_pdf.flushObjectValues(true);

							// reset the viewableArea before opening a new file
							decode_pdf.resetViewableArea();

							currentGUI.stopThumbnails();

							if (!currentGUI.isSingle())
								openNewMultiplePage(commonValues.getSelectedFile());

							try {
								 //Set to true to show our default download window
								currentGUI.setUseDownloadWindow(false);
								openFile(commonValues.getSelectedFile());

								while (Values.isProcessing()) {
									Thread.sleep(1000);

								}
							} catch (Exception e) {
							}
						}

					} else { // no file selected so redisplay old
						decode_pdf.repaint();
						// currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
					}
				}
			}
			
			break;

        case HELP:
        	
        	if(args==null){
        		currentGUI.getHelpBox();
        	}
        	
//			if(args==null){
//				try {
//					BrowserLauncher.openURL("https://idrsolutions.fogbugz.com/default.asp?support");
//				} catch (IOException e1) {
//					currentGUI.showMessageDialog("Please visit https://idrsolutions.fogbugz.com/default.asp?support");
//				}
//			}
			
			break;
			
			
        case BUY:
			if(args==null){
				try {
					BrowserLauncher.openURL("http://www.idrsolutions.com/jpedal-pricing/");
				} catch (IOException e1) {
					currentGUI.showMessageDialog("Please visit http://www.jpedal.org/pricing.php");
				}
			}
			
			break;
        case RSS:
        	if(args==null)
				currentGUI.getRSSBox();
			else{

			}
			
			break;
		case VISITWEBSITE:
			if(args==null){
				try {
					BrowserLauncher.openURL(Messages.getMessage("PdfViewer.VisitWebsite"));
				} catch (IOException e1) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
				}
			}

			break;

		case TIP:
			if(args==null){
				TipOfTheDay tipOfTheDay = new TipOfTheDay(currentGUI.getFrame(), "/org/jpedal/examples/viewer/res/tips", properties);
				tipOfTheDay.setVisible(true);
			}else{

			}
			
			break;

		case CASCADE:
			if(args==null)
				cascade();

			break;
		case TILE:
			if(args==null)
				tile();

			break;
		case UPDATE:
			if(args==null){
				checkForUpdates(true);
			}else{

			}
			
			break;
		case PREFERENCES:

			if(args==null){
				currentGUI.showPreferencesDialog();
			}else{

			}

			break;

		case COPY:
				String copyText = copySelectedText();
				
				StringSelection ss = new StringSelection(copyText);
		        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			break;

		case SELECTALL:
			
			if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE){


				Rectangle[] allHighlights = decode_pdf.getTextLines().getLineAreas(commonValues.getCurrentPage());

                int coordX,coordY,width,height;
                if(allHighlights!=null){ //breaks in legacy text mode so trap
                    int top = 0;
                    int bottom = 0;
                    for(int r=0; r!= allHighlights.length; r++){
                        if(allHighlights[r].y> allHighlights[top].y){
                            top = r;
                        }
                        if(allHighlights[r].y< allHighlights[bottom].y){
                            bottom = r;
                        }
                    }

                    coordX = allHighlights[top].x;
                    coordY = allHighlights[top].y+(allHighlights[top].height/2);
                    height = (allHighlights[bottom].y+(allHighlights[bottom].height/2))-coordY;
                    width = (allHighlights[bottom].x+ allHighlights[bottom].width)-coordX;
                }else{
                //breaks in legacy text mode so trap
				    height = decode_pdf.getPdfPageData().getCropBoxHeight(commonValues.getCurrentPage());
				    width = decode_pdf.getPdfPageData().getCropBoxWidth(commonValues.getCurrentPage());
				    coordX = decode_pdf.getPdfPageData().getCropBoxX(commonValues.getCurrentPage());
				    coordY = decode_pdf.getPdfPageData().getCropBoxY(commonValues.getCurrentPage());
                }

				// values require to manipulate selected text
				commonValues.m_x1 = coordX;
				commonValues.m_x2 = coordX + width;
				commonValues.m_y1 = coordY;
				commonValues.m_y2 = coordY + height;

				// add an outline rectangle to the display
				Rectangle currentRectangle = new Rectangle(coordX, coordY, width, height);

				// Remove all previous highlight areas
				decode_pdf.updateCursorBoxOnScreen(null, null); // remove box
				//decode_pdf.removeFoundTextAreas(null); // remove highlighted text
				decode_pdf.getTextLines().clearHighlights();
				pages.setHighlightedImage(null);// remove image highlight

				decode_pdf.updateCursorBoxOnScreen(currentRectangle, DecoderOptions.highlightColor);
				
				decode_pdf.getTextLines().addHighlights(allHighlights, true, commonValues.getCurrentPage());

				decode_pdf.repaint();
			}else{
				currentGUI.showMessageDialog(Messages.getMessage("PageLayoutMessage.SinglePageOnly"));
			}
			break;
		case DESELECTALL:
				/** remove any outline and reset variables used to track change */
				decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
	            decode_pdf.getTextLines().clearHighlights(); //remove highlighted text
	            //decode_pdf.removeFoundTextAreas(null); //remove highlighted text
	            pages.setHighlightedImage(null);// remove image highlight
			break;
		case UPDATEGUILAYOUT:
			if(args == null){
			
			}else{
				String value = null;
				boolean show = false;

				if(args[0] instanceof String)
					value = (String) args[0];
				
				if(args[1] instanceof Boolean)
					show = (Boolean) args[1];
				
				if(value!=null)
					currentGUI.alterProperty(value, show);
				else
					throw new RuntimeException("String input was null");
				}
			break;
		case MOUSEMODE:
			if(args == null){
                if(mouseMode.getMouseMode()==MouseMode.MOUSE_MODE_TEXT_SELECT){
					
                	//Set Mouse Mode
					mouseMode.setMouseMode(MouseMode.MOUSE_MODE_PANNING);
					
					//Update Buttons
					URL url=currentGUI.getURLForImage(currentGUI.getIconLocation()+"mouse_pan.png");
					if(url!=null){

						ImageIcon fontIcon = new ImageIcon(url);
						currentGUI.mouseMode.setIcon(fontIcon);
					}
					currentGUI.snapshotButton.setEnabled(false);
					
					//Update Cursor
					decode_pdf.setDefaultCursor(currentGUI.getCursor(SwingGUI.GRAB_CURSOR));
					
				}else if(mouseMode.getMouseMode()==MouseMode.MOUSE_MODE_PANNING){
					
					//Set Mouse Mode
					mouseMode.setMouseMode(MouseMode.MOUSE_MODE_TEXT_SELECT);

                    //Update buttons and mouse cursor
                    //decode_pdf.setPDFCursor(Cursor.getDefaultCursor());
				
					URL url = currentGUI.getURLForImage(currentGUI.getIconLocation()+"mouse_select.png");
					if(url!=null){

						ImageIcon fontIcon = new ImageIcon(url);
						currentGUI.mouseMode.setIcon(fontIcon);
					}

					currentGUI.snapshotButton.setEnabled(true);
					
					//Update Cursor
					decode_pdf.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}else{
				
			}
			break;
		case TEXTSELECT:
			if(args == null){
				
				//Set mouse mode
				mouseMode.setMouseMode(MouseMode.MOUSE_MODE_TEXT_SELECT);
				
				//Update buttons
				URL url=currentGUI.getURLForImage(currentGUI.getIconLocation()+"mouse_select.png");
				if(url!=null){

					ImageIcon fontIcon = new ImageIcon(url);
					currentGUI.mouseMode.setIcon(fontIcon);
				}
			}else{
				
			}
			break;
        case SEPARATECOVER:
            if (args == null){
                //swap option
                pages.setBoolean(Display.BoolValue.SEPARATE_COVER, !pages.getBoolean(Display.BoolValue.SEPARATE_COVER));

                //update view
                if (decode_pdf.getDisplayView() == Display.FACING)
                    executeCommand(Commands.FACING,null);

                //set properties file
                if (pages.getBoolean(Display.BoolValue.SEPARATE_COVER))
                    properties.setValue("separateCoverOn","true");
                else
                    properties.setValue("separateCoverOn","false");
            }else{

            }
            break;
		case PANMODE:
			if(args == null){
				
				//Set Mouse Mode
				mouseMode.setMouseMode(MouseMode.MOUSE_MODE_PANNING);

				//Update buttons
				URL url=currentGUI.getURLForImage(currentGUI.getIconLocation()+"mouse_pan.png");
				if(url!=null){

					ImageIcon fontIcon = new ImageIcon(url);
					currentGUI.mouseMode.setIcon(fontIcon);
				}
				
				//Update Cursor
				decode_pdf.setDefaultCursor(currentGUI.getCursor(SwingGUI.GRAB_CURSOR));
			}else{
				
			}
			break;
		case PREVIOUSDOCUMENT:
			if(args==null){
				if (Printer.isPrinting())
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPrintWait.message"));
				else if (Values.isProcessing() || isOpening)
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
				else {
					fileToOpen = recent.getPreviousDocument();
					if (fileToOpen != null)
						open(fileToOpen);
				}
			}
			
			break;

		case NEXTDOCUMENT:
			if(args==null){
				if (Printer.isPrinting())
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPrintWait.message"));
				else if (Values.isProcessing() || isOpening)
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
				else {
					fileToOpen = recent.getNextDocument();
					if (fileToOpen != null)
						open(fileToOpen);
				}
			}
			
			break;

		case PREVIOUSRESULT:
			if(args==null){
				status = null;
				
				if(results==null)
					results = searchFrame.getResults(commonValues.getCurrentPage());
				
				int index = results.getSelectedIndex();

				if(index<0){
					index=0;
					results.setSelectedIndex(index);
				}
				
//				Object currPage = results.getTextPages().get(new Integer(index));
				Object currPage = commonValues.getCurrentPage();
				
				if(index==0 ||
						results.getResultCount()==0){
					int currentPage = commonValues.getCurrentPage()-1;
					if(currentPage<1)
						currentPage = commonValues.getPageCount();
					results = searchFrame.getResults(currentPage);
					while(results.getResultCount()<1 && currentPage>0 && 
							searchFrame.getStyle()==SwingSearchWindow.SEARCH_MENU_BAR){
						results = searchFrame.getResults(currentPage);
						currentPage--;
					}
					
					if(results.getResultCount()<1 &&
							currentPage==0){					
						currentPage = commonValues.getPageCount();
						results = searchFrame.getResults(currentPage);
						status = SEARCH_RETURNED_TO_START;
						
						while(results.getResultCount()<1 && currentPage>=commonValues.getCurrentPage() && 
								searchFrame.getStyle()==SwingSearchWindow.SEARCH_MENU_BAR){
							results = searchFrame.getResults(currentPage);
							currentPage--;
						}
					}
					index=results.getResultCount()-1;
					if(results.getResultCount()<1){
						status = SEARCH_NOT_FOUND;
					}
				}else{
					index--;
				}
				
				results.setSelectedIndex(index);
				
				if(!Values.isProcessing()){//{if (!event.getValueIsAdjusting()) {

					float scaling=currentGUI.getScaling();
					//int inset=currentGUI.getPDFDisplayInset();

					int id=results.getSelectedIndex();

					if(!allHighlightsShown)
						decode_pdf.getTextLines().clearHighlights();

					if(id!=-1){

						Integer key= id;
						Object newPage=results.getTextPages().get(key);

						if(newPage!=null){
							int nextPage= (Integer) newPage;
							
							//move to new page
							if(commonValues.getCurrentPage()!=nextPage){
								commonValues.setCurrentPage(nextPage);

								currentGUI.resetStatusMessage(Messages.getMessage("PdfViewer.LoadingPage")+ ' ' +commonValues.getCurrentPage());

								/**reset as rotation may change!*/
								decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage());
								
								
								//decode the page
								currentGUI.decodePage(false);

								decode_pdf.invalidate();
							}
							
							while(Values.isProcessing()){
								//Ensure page has been processed else highlight may be incorrect
							}
							
							Rectangle[] finalHighlight;
							Rectangle highlight = (Rectangle) searchFrame.getTextRectangles().get(key);
							
							if((Integer) currPage != nextPage)
								if(allHighlightsShown){
									Vector_Rectangle storageVector = new Vector_Rectangle();

									Integer kInteger;
									
									//Integer allKeys = new Integer(id);
									for(int k=0; k!=results.getModel().getSize(); k++){

										kInteger= k;
										//int currentPage = ((Integer)newPage).intValue();
										if((Integer) results.getTextPages().get(kInteger) ==nextPage){

											Object h= searchFrame.getTextRectangles().get(kInteger);

											if(h instanceof Rectangle){
												storageVector.addElement((Rectangle)h);
											}
											if(h instanceof Rectangle[]){
												Rectangle[] areas = (Rectangle[])h;
												for(int i=0; i!=areas.length; i++){
													storageVector.addElement(areas[i]);
												}
											}
										}
									}

									storageVector.trim();
									finalHighlight = storageVector.get();
									decode_pdf.getTextLines().addHighlights(finalHighlight, true,nextPage);
								}
							
							

							//draw rectangle
							//int scrollInterval = decode_pdf.getScrollInterval();
							//previous one to revert back to but other more accurate
//							decode_pdf.scrollRectToVisible(new Rectangle((int)((highlight.x*scaling)+scrollInterval),(int)(mediaGUI.cropH-((highlight.y-currentGUI.cropY)*scaling)-scrollInterval*2),scrollInterval*4,scrollInterval*6));

							//int x = (int)((highlight.x-currentGUI.cropX)*scaling)+inset;
							//int y = (int)((currentGUI.cropH-(highlight.y-currentGUI.cropY))*scaling)+inset;
							//int w = (int)(highlight.width*scaling);
							//int h = (int)(highlight.height*scaling);

							//Rectangle view = decode_pdf.getVisibleRect();
//							if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE){
//								decode_pdf.scrollRectToVisible(highlight);
//							}else{
//								decode_pdf.scrollRectToHighlight(highlight);
//							}
							scrollRectToHighlight(highlight,commonValues.getCurrentPage());

							if(!allHighlightsShown)
								decode_pdf.getTextLines().addHighlights(new Rectangle[]{highlight}, true, nextPage);
							
							pages.refreshDisplay();
							decode_pdf.repaint();

						}
					}
				}
                currentGUI.hideRedundentNavButtons();
			}
			
			if(commonValues.getCurrentPage()==searchFrame.getFirstPageWithResults() &&
					results.getSelectedIndex()==0){
				status = FIRST_DOCUMENT_SEARCH_RESULT_NOW_SHOWN;
			}
			
			break;

		case NEXTRESULT:
			if(args==null){
				final boolean boeingDebug = false;
				status = null;
				
				if(results==null)
					results = searchFrame.getResults(commonValues.getCurrentPage());
				
				int index = results.getSelectedIndex();
				
				if(boeingDebug){
					System.out.println("Next result called");
					System.out.println("Index=="+index);
				}
				if(index<0){
					index=0;
					results.setSelectedIndex(index);
					if(boeingDebug){
						System.out.println("Index invalid. Index changed to "+index);
					}
				}
				
//				Object currPage = results.getTextPages().get(new Integer(index));
				Object currPage = commonValues.getCurrentPage();
				
				if(boeingDebug){
					System.out.println("Current results count == "+results.getResultCount());
				}
				
				if(index==results.getResultCount()-1 ||
						results.getResultCount()==0){
					index=0;
					int currentPage = commonValues.getCurrentPage()+1;
					if(boeingDebug){
						System.out.println("Compare current page with page count");
						System.out.println("Current Page == "+currentPage);
						System.out.println("Page Count=="+commonValues.getPageCount());
					}
					
					if(currentPage>commonValues.getPageCount()){
						if(boeingDebug){
							System.out.println("Out of page range. Resetting to page 1");
						}
						currentPage = 1;
					}
					
					results = searchFrame.getResults(currentPage);
					
					while(results.getResultCount()<1 && currentPage<commonValues.getPageCount()+1 && 
							searchFrame.getStyle()==SwingSearchWindow.SEARCH_MENU_BAR){
						results = searchFrame.getResults(currentPage);
						if(boeingDebug){
							System.out.println("No Results on page "+currentPage);
						}
						currentPage++;
					}

					if(results.getResultCount()<1 &&
							currentPage==commonValues.getPageCount()+1){
						if(boeingDebug){
							System.out.println("Reached end of document, return to page 1");
						}
						currentPage=1;
						status = SEARCH_RETURNED_TO_START;
						while(results.getResultCount()<1 && currentPage<=commonValues.getCurrentPage() && 
								searchFrame.getStyle()==SwingSearchWindow.SEARCH_MENU_BAR){
							results = searchFrame.getResults(currentPage);
							if(boeingDebug){
								System.out.println("No results on page "+currentPage);
							}
							currentPage++;
						}
						
						if(results.getResultCount()<1){
							if(boeingDebug){
								System.out.println("No results found in document");
							}
							status = SEARCH_NOT_FOUND;
						}
					}
				}else{
					if(boeingDebug){
						System.out.println("Next results on page "+commonValues.getCurrentPage());
					}
					index++;
				}

				results.setSelectedIndex(index);

                //@kieran - altered by Mark so search can display
				if (1==1 || !Values.isProcessing()) {// {if(!event.getValueIsAdjusting()){
					float scaling = currentGUI.getScaling();
					//int inset = currentGUI.getPDFDisplayInset();

					int id = results.getSelectedIndex();

					if(!allHighlightsShown)
						decode_pdf.getTextLines().clearHighlights();

					if (id != -1) {
						Integer key = id;
						Object newPage = results.getTextPages().get(key);

						if (newPage != null) {
							int nextPage = (Integer) newPage;
							// move to new page
							if (commonValues.getCurrentPage() != nextPage) {
								commonValues.setCurrentPage(nextPage);

								currentGUI.resetStatusMessage(Messages.getMessage("PdfViewer.LoadingPage") + ' ' + commonValues.getCurrentPage());

								/** reset as rotation may change! */
								decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage());

								
								// decode the page
								currentGUI.decodePage(false);
								
								decode_pdf.invalidate();
							}
							
							while(Values.isProcessing()){
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                                //Ensure page has been processed else highlight may be incorrect
							}
							
							Rectangle[] finalHighlight;
							Rectangle highlight = (Rectangle)results.textAreas().get(key);
							//int pageOfHighlight = ((Integer) results.getTextPages().get(key)).intValue();

                            //@kieran - added by Mark to trap null
							if(currPage!=null && (Integer) currPage != nextPage)
								if(allHighlightsShown){
									Vector_Rectangle storageVector = new Vector_Rectangle();

									
									//Integer allKeys = new Integer(id)
									Integer kInteger;
									for(int k=0; k!=results.getModel().getSize(); k++){

										kInteger= k;
										//int currentPage = ((Integer)newPage).intValue();
										if((Integer) results.getTextPages().get(kInteger) ==nextPage){

											Object h= searchFrame.getTextRectangles().get(kInteger);

											if(h instanceof Rectangle){
												storageVector.addElement((Rectangle)h);
											}
											if(h instanceof Rectangle[]){
												Rectangle[] areas = (Rectangle[])h;
												for(int i=0; i!=areas.length; i++){
													storageVector.addElement(areas[i]);
												}
											}
										}
									}

									storageVector.trim();
									finalHighlight = storageVector.get();
									decode_pdf.getTextLines().addHighlights(finalHighlight, true, nextPage);
								}
							
							// draw rectangle

							// previous one to revert back to but other more accurate 
							// decode_pdf.scrollRectToVisible(newRectangle((int)((highlight.x*scaling)+scrollInterval),(int)(mediaGUI.cropH-((highlight.y-currentGUI.cropY)*scaling)-scrollInterval*2),scrollInterval*4,scrollInterval*6));

							//int x = (int) ((highlight.x - currentGUI.cropX) * scaling) + inset;
							//int y = (int) ((currentGUI.cropH - (highlight.y - currentGUI.cropY)) * scaling) + inset;
							//int w = (int) (highlight.width * scaling);
							//int h = (int) (highlight.height * scaling);
							
//							Rectangle view = decode_pdf.getVisibleRect();
//							
//							if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE){
//								decode_pdf.scrollRectToVisible(highlight);
//							}else{
//								decode_pdf.scrollRectToHighlight(highlight);
//							}
							//decode_pdf.scrollRectToHighlight(highlight,commonValues.getCurrentPage());
//							decode_pdf.scrollRectToHighlight(highlight,pageOfHighlight);
							
							if(!allHighlightsShown)
								decode_pdf.getTextLines().addHighlights(new Rectangle[]{highlight}, true, nextPage);

                            pages.refreshDisplay();
							decode_pdf.repaint();

						}
					}
				}
                currentGUI.hideRedundentNavButtons();
			}else{

			}
			if(commonValues.getCurrentPage()==searchFrame.getFirstPageWithResults() &&
					results.getSelectedIndex()==0){
				status = FIRST_DOCUMENT_SEARCH_RESULT_NOW_SHOWN;
			}
			
			break;
		case FIRSTPAGE:
			if(args==null){
				if((commonValues.getSelectedFile()!=null)&&(commonValues.getPageCount()>1)&&(commonValues.getCurrentPage()!=1))
//					back(commonValues.getCurrentPage() - 1);
					navigatePages(-(commonValues.getCurrentPage()-1));
			}else{

			}

			break;

		case FBACKPAGE:
			if(args==null){
				if(commonValues.getSelectedFile()!=null)
					if(commonValues.getCurrentPage()<10)
//						back(commonValues.getCurrentPage() - 1);

						navigatePages(-(commonValues.getCurrentPage() - 1));
					else
//						back(10);
						navigatePages(-10);
					} else {

			}

			break;

		case BACKPAGE:
			if(args==null){
				if(commonValues.getSelectedFile()!=null)
//					back(1);
					navigatePages(-1);
			}else{
				if(commonValues.getSelectedFile()!=null)
//					back(Integer.parseInt((String) args[0]));
				navigatePages(-Integer.parseInt((String) args[0]));
				while (Values.isProcessing()) {
					//Wait while pdf is loading
				}
			}
			
			break;

		case FORWARDPAGE:
			if(args==null){
				if(commonValues.getSelectedFile()!=null)
//					forward(1);
				navigatePages(1);
			}else{
				if(commonValues.getSelectedFile()!=null)
//					forward(Integer.parseInt((String) args[0]));
					navigatePages(Integer.parseInt((String) args[0]));
				while (Values.isProcessing()) {
					//Wait while pdf is loading
					}
				}
			break;

		case FFORWARDPAGE:
			if(args==null){
				if(commonValues.getSelectedFile()!=null)
					if(commonValues.getPageCount()<commonValues.getCurrentPage()+10)
//						forward(commonValues.getPageCount()-commonValues.getCurrentPage());
				navigatePages(commonValues.getPageCount()-commonValues.getCurrentPage());
					else
//						forward(10);
						navigatePages(10);
			}
			
			break;

		case LASTPAGE:
            if(args==null){
			if((commonValues.getSelectedFile()!=null)&&(commonValues.getPageCount()>1)&&(commonValues.getPageCount()-commonValues.getCurrentPage()>0))
//					forward(commonValues.getPageCount() - commonValues.getCurrentPage());
					navigatePages(commonValues.getPageCount() - commonValues.getCurrentPage());
		}else{

			}
			
			break;

		case GOTO:
			if(args==null){
				String page = currentGUI.showInputDialog(Messages.getMessage("PdfViewer.EnterPageNumber"), Messages.getMessage("PdfViewer.GotoPage"), JOptionPane.QUESTION_MESSAGE);
				if(page != null)
					gotoPage(page);
			}else{
				gotoPage((String)args[0]);
			}

			break;
		case SINGLE:
            if (!decode_pdf.isOpen())
                return null;

            if(args==null){
				
				currentGUI.getCombo(Commands.SCALING).setEnabled(true);
                currentGUI.mouseMode.setEnabled(true);
                currentGUI.snapshotButton.setEnabled(true);

				currentGUI.alignLayoutMenuOption(Display.SINGLE_PAGE);

				if (SwingUtilities.isEventDispatchThread()) {

				decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

        		} else {
        			final Runnable doPaintComponent = new Runnable() {

        				public void run() {
        					decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
        				}
        			};
        			SwingUtilities.invokeLater(doPaintComponent);
        		}
				//decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
                pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));
                currentGUI.hideRedundentNavButtons();
				currentGUI.resetRotationBox();
				currentGUI.getFrame().setMinimumSize(new Dimension(0,0));
				currentGUI.zoom(false);
			}
			
			break;
		case CONTINUOUS:
            if (!decode_pdf.isOpen())
                return null;

            if(args==null){
				
				currentGUI.getCombo(Commands.SCALING).setEnabled(true);
                currentGUI.mouseMode.setEnabled(true);
                currentGUI.snapshotButton.setEnabled(true);

				currentGUI.alignLayoutMenuOption(Display.CONTINUOUS);
				if (SwingUtilities.isEventDispatchThread()) {

					decode_pdf.setDisplayView(Display.CONTINUOUS, Display.DISPLAY_CENTERED);
					//decode_pdf.setDisplayView(Display.CONTINUOUS, Display.DISPLAY_CENTERED);
					pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));
					currentGUI.hideRedundentNavButtons();
					currentGUI.setSelectedComboIndex(Commands.ROTATION, 0);
					currentGUI.getFrame().setMinimumSize(new Dimension(0,0));
				} else {
					isCommandInThread = true;
        			final Runnable doPaintComponent = new Runnable() {

        				public void run() {
        					decode_pdf.setDisplayView(Display.CONTINUOUS, Display.DISPLAY_CENTERED);
        					//decode_pdf.setDisplayView(Display.CONTINUOUS, Display.DISPLAY_CENTERED);
        	                pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));
        	                currentGUI.hideRedundentNavButtons();
        					currentGUI.setSelectedComboIndex(Commands.ROTATION, 0);
        					currentGUI.getFrame().setMinimumSize(new Dimension(0,0));
        					
        			        executingCommand = false;
        				}
        			};
        			SwingUtilities.invokeLater(doPaintComponent);
        		}
				
				
			}else{
				
			}
			
			break;
		case CONTINUOUS_FACING:
            if (!decode_pdf.isOpen())
                return null;

            if(args==null){

                currentGUI.getCombo(Commands.SCALING).setEnabled(true);
                currentGUI.mouseMode.setEnabled(true);
                currentGUI.snapshotButton.setEnabled(true);

                //Fit to page
                //Set combo ID to -1 to avoid the actionListener being called which would change the position
                currentGUI.getCombo(Commands.SCALING).setID(-1);
                currentGUI.setSelectedComboIndex(Commands.SCALING, 0);
                currentGUI.getCombo(Commands.SCALING).setID(Commands.SCALING);
                currentGUI.zoom(false);

				currentGUI.alignLayoutMenuOption(Display.CONTINUOUS_FACING);
				if (SwingUtilities.isEventDispatchThread()) {

        			decode_pdf.setDisplayView(Display.CONTINUOUS_FACING, Display.DISPLAY_CENTERED);
//    				decode_pdf.setDisplayView(Display.CONTINUOUS_FACING, Display.DISPLAY_CENTERED);
                    pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));

                    int p = commonValues.getCurrentPage();
                    if((p & 1)==1 && p!=1)
                        p--;
                    commonValues.setCurrentPage(p);
                    currentGUI.setPage(p);
                    currentGUI.hideRedundentNavButtons();

    				currentGUI.setSelectedComboIndex(Commands.ROTATION, 0);
    				currentGUI.getFrame().setMinimumSize(new Dimension(0,0));

        		} else {
        			isCommandInThread = true;
        			final Runnable doPaintComponent = new Runnable() {

        				public void run() {
        					decode_pdf.setDisplayView(Display.CONTINUOUS_FACING, Display.DISPLAY_CENTERED);
//        					decode_pdf.setDisplayView(Display.CONTINUOUS_FACING, Display.DISPLAY_CENTERED);
        	                pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));

        	                int p = commonValues.getCurrentPage();
        	                if((p & 1)==1 && p!=1)
        	                    p--;
        	                commonValues.setCurrentPage(p);
        	                currentGUI.setPage(p);
        	                currentGUI.hideRedundentNavButtons();

        					currentGUI.setSelectedComboIndex(Commands.ROTATION, 0);
        					currentGUI.getFrame().setMinimumSize(new Dimension(0,0));
        					
        			        executingCommand = false;
        				}
        			};
        			SwingUtilities.invokeLater(doPaintComponent);
        		}

			}else{
				
			}

			break;
		case FACING:
            if (!decode_pdf.isOpen())
                return null;

            if(args==null){
				if (pages.getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getPdfPageData().hasMultipleSizes() && !irregularSizesWarningShown) {
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.PageDragIrregularSizes"));
                    irregularSizesWarningShown = true;
                }

				currentGUI.getCombo(Commands.SCALING).setEnabled(true);
                currentGUI.mouseMode.setEnabled(true);
                currentGUI.snapshotButton.setEnabled(true);

                //Fit to page
                currentGUI.setSelectedComboIndex(Commands.SCALING, 0);
                currentGUI.zoom(false);
                
				currentGUI.alignLayoutMenuOption(Display.FACING);
				if (SwingUtilities.isEventDispatchThread()) {

                decode_pdf.setDisplayView(Display.FACING, Display.DISPLAY_CENTERED);
              //decode_pdf.setDisplayView(Display.FACING, Display.DISPLAY_CENTERED);
                pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));

                int p = commonValues.getCurrentPage();
                if(pages.getBoolean(Display.BoolValue.SEPARATE_COVER) && ((p & 1)==1 && p!=1))
                    p--;
                else if (!pages.getBoolean(Display.BoolValue.SEPARATE_COVER) && ((p & 1)==0))
                    p--;
                commonValues.setCurrentPage(p);
                currentGUI.setPage(p);
                currentGUI.hideRedundentNavButtons();
                

        	    currentGUI.decodePage(false);//ensure all pages appear
				currentGUI.setSelectedComboIndex(Commands.ROTATION, 0);
				currentGUI.getFrame().setMinimumSize(new Dimension(0,0));
				
        		} else {
        			isCommandInThread = true;
        			final Runnable doPaintComponent = new Runnable() {

        				public void run() {
        					decode_pdf.setDisplayView(Display.FACING, Display.DISPLAY_CENTERED);
        					//decode_pdf.setDisplayView(Display.FACING, Display.DISPLAY_CENTERED);
        	                pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));

        	                int p = commonValues.getCurrentPage();
        	                if(pages.getBoolean(Display.BoolValue.SEPARATE_COVER) && ((p & 1)==1 && p!=1))
        	                    p--;
        	                else if (!pages.getBoolean(Display.BoolValue.SEPARATE_COVER) && ((p & 1)==0))
        	                    p--;
        	                commonValues.setCurrentPage(p);
        	                currentGUI.setPage(p);
        	                currentGUI.hideRedundentNavButtons();
        	                

        	        	    currentGUI.decodePage(false);//ensure all pages appear
        					currentGUI.setSelectedComboIndex(Commands.ROTATION, 0);
        					currentGUI.getFrame().setMinimumSize(new Dimension(0,0));
        					
        			        executingCommand = false;
        				}
        			};
        			SwingUtilities.invokeLater(doPaintComponent);
        		}
				
			}else{
				
			}
			break;


		case FULLSCREEN:
			if(args==null){
				// Determine if full-screen mode is supported directly
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice gs = ge.getDefaultScreenDevice();
				if (gs.isFullScreenSupported()) {
					// Full-screen mode is supported
				} else {
					// Full-screen mode will be simulated
				}

				// Create a window for full-screen mode; add a button to leave full-screen mode
				if (win == null) {
					Frame frame = new Frame(gs.getDefaultConfiguration());
					win = new Window(frame);
				}else {
				//added to allow actions from named actions to interact the full screen as it needs to.
					if(gs.getFullScreenWindow()!=null && 
							gs.getFullScreenWindow().equals(win)){
						exitFullScreen();
						break;
					}
				}

				if (currentGUI.getFrame() instanceof JFrame) {
					((JFrame) currentGUI.getFrame()).getContentPane().remove(currentGUI.getDisplayPane());
					// Java 1.6 has issues with original pane remaining visible so hide when fullscreen selected
					currentGUI.getFrame().setVisible(false);
				} else
					currentGUI.getFrame().remove(currentGUI.getDisplayPane());

				win.add(currentGUI.getDisplayPane(), BorderLayout.CENTER);

				// Create a button that leaves full-screen mode
				Button btn = new Button("Return");
				win.add(btn, BorderLayout.NORTH);

				btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						exitFullScreen();
					}
				});

				try {

                    screenPosition=currentGUI.getFrame().getLocation();
					// Enter full-screen mode
					gs.setFullScreenWindow(win);
					win.validate();
					currentGUI.zoom(false);
				} catch (Error e) {
					currentGUI.showMessageDialog("Full screen mode not supported on this machine.\n" +
					"JPedal will now exit");

					this.exit();
					// ...
				}// finally {
				// Exit full-screen mode
				//	gs.setFullScreenWindow(null);
				//}
			}else{
				
			}
			break;


		case SCALING:
			if(args==null){
				if (!Values.isProcessing()) {
                    if(commonValues.getSelectedFile()!=null) {
                        //store centre location
                        final Rectangle r = decode_pdf.getVisibleRect();
                        final double x = (r.getX()+(r.getWidth()/2))/decode_pdf.getBounds().getWidth();
                        final double y = (r.getY()+(r.getHeight()/2))/decode_pdf.getBounds().getHeight();

                        //scale
                        currentGUI.zoom(false);

                        //center on stored location
                        Thread t = new Thread() {
                            public void run() {
                                try {
                                    Rectangle area=new Rectangle(
                                            (int)((x*decode_pdf.getWidth())-(r.getWidth()/2)),
                                            (int)((y*decode_pdf.getHeight())-(r.getHeight()/2)),
                                            (int)decode_pdf.getVisibleRect().getWidth(),
                                            (int)decode_pdf.getVisibleRect().getHeight());
                                    addAView(-1, area, null);
                                    decode_pdf.scrollRectToVisible(area);
                                    decode_pdf.repaint();
                                } catch (Exception e) {e.printStackTrace();}
                            }
                        };
                        SwingUtilities.invokeLater(t);
                    }
                }
			}else{
				currentGUI.setScalingFromExternal((String) args[0]);
				currentGUI.zoom(true);
				while (Values.isProcessing()) {
					// wait while we scale your document
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}

			break;

		case ROTATION:
			if(args==null){
				if(commonValues.getSelectedFile()!=null)
					currentGUI.rotate();
				//currentGUI.getDisplayPane();
			}else{
				int rotation = Integer.parseInt((String) args[0]);
				while (Values.isProcessing()) {
					// wait while we rotate your document
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
				// JOptionPane.showConfirmDialog(null, "out loop rotation " +rotation);
				currentGUI.setRotationFromExternal(rotation);
				currentGUI.zoom(true);
			}
			
			break;

        //<start-wrap>
			/**
			 * external/itext menu options start at 500 - add your own code here
			 */
		case SAVEFORM:
			if(args==null){
				saveChangedForm();
			}else{

			}
			break;

		case PDF:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					SavePDF current_selection = new SavePDF(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int userChoice=current_selection.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerTitle.SavePagesAsPdf"));


					//get parameters and call if YES
					if (userChoice == JOptionPane.OK_OPTION){
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.extractPagesToNewPDF(current_selection);
					}
				}
			}else{

			}
			break;

        case ROTATE:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					RotatePDFPages current_selection = new RotatePDFPages(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int userChoice=current_selection.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerRotation.text"));

					//get parameters and call if YES
					if (userChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.rotate(commonValues.getPageCount(), currentPageData, current_selection);
						open(commonValues.getSelectedFile());
					}

				}
			}else{

			}
			break;

		case SETCROP:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					CropPDFPages cropPage = new CropPDFPages(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int cropPageChoice=cropPage.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerTooltip.PDFCropPages"));

					//get parameters and call if YES
					if (cropPageChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.setCrop(commonValues.getPageCount(), currentPageData, cropPage);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

		case NUP:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
//					get values from user
					ExtractPDFPagesNup nup = new ExtractPDFPagesNup(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int nupChoice=nup.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerNUP.titlebar"));

					//get parameters and call if YES
					if (nupChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						//decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.nup(commonValues.getPageCount(), currentPageData, nup);
						//open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

		case HANDOUTS:
			if(args==null){
				if(fileIsURL)
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.CannotExportFromURL"));

				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					if(!fileIsURL){//ensure file choose not displayed if opened from URL
						JFileChooser chooser1 = new JFileChooser();
						chooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);

						int approved1=chooser1.showSaveDialog(null);
						if(approved1==JFileChooser.APPROVE_OPTION){

							File file = chooser1.getSelectedFile();

							ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                            itextFunctions.handouts(file.getAbsolutePath());
						}
					}
				}
			}else{

			}
			break;

		case SIGN:
			if(args==null) {
				if(fileIsURL) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.CannotExportFromURL"));
				}
				else if (commonValues.getSelectedFile()==null) {
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}
				else if(!decode_pdf.isExtractionAllowed())
				{
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.ExtractionNotAllowed")); 
				}
				else {
					SignData signData=new SignData();
					SignWizardModel signer = new SignWizardModel(signData, commonValues.getSelectedFile(), commonValues.getInputDir());
					Wizard signWizard = new Wizard((Frame) currentGUI.getFrame(), signer); 

					if(signWizard.showModalDialog()!= JOptionPane.OK_OPTION) break;

					if(!signData.validate()) {
						currentGUI.showMessageDialog(signData.toString());
						break;
					}

					int response = JOptionPane.showConfirmDialog(currentGUI.getFrame(), 
							signData.toString(),
							Messages.getMessage("PdfViewerGeneral.IsThisCorrect"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);

					if(response==JOptionPane.OK_OPTION) {
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.Sign(signData);
					}
					else {
						JOptionPane.showMessageDialog(currentGUI.getFrame(),
								Messages.getMessage("PdfViewerMessage.SigningOperationCancelled"),
								Messages.getMessage("PdfViewerGeneral.Warning"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
			break;

		case DELETE:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					DeletePDFPages deletedPages = new DeletePDFPages(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int deletedPagesChoice=deletedPages.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerDelete.text"));

					//get parameters and call if YES
					if (deletedPagesChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.delete(commonValues.getPageCount(), currentPageData, deletedPages);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

        case ADDHEADERFOOTER:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{

					//get values from user
					AddHeaderFooterToPDFPages addHeaderFooter = new AddHeaderFooterToPDFPages(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int headerFooterPagesChoice=addHeaderFooter.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerTitle.AddHeaderAndFooters"));

					//get parameters and call if YES
					if (headerFooterPagesChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.addHeaderFooter(commonValues.getPageCount(), currentPageData, addHeaderFooter);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

		case STAMPTEXT:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					StampTextToPDFPages stampText = new StampTextToPDFPages(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int stampTextChoice=stampText.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerStampText.text"));

					//get parameters and call if YES
					if (stampTextChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.stampText(commonValues.getPageCount(), currentPageData, stampText);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

		case STAMPIMAGE:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{

					//get values from user
					StampImageToPDFPages stampImage = new StampImageToPDFPages(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int stampImageChoice=stampImage.display(currentGUI.getFrame(),Messages.getMessage("PdfViewerStampImage.text"));

					//get parameters and call if YES
					if (stampImageChoice == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.stampImage(commonValues.getPageCount(), currentPageData, stampImage);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

		case ADD:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					InsertBlankPDFPage addPage = new InsertBlankPDFPage(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int positionToAdd=addPage.display(currentGUI.getFrame(),Messages.getMessage("PdfViewer.BlankPage"));

					//get parameters and call if YES
					if (positionToAdd == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.add(commonValues.getPageCount(), currentPageData, addPage);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;

		case SECURITY:
			if(args==null){
				if(commonValues.getSelectedFile()==null){
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFile"));
				}else{
					//get values from user
					EncryptPDFDocument encryptPage = new EncryptPDFDocument(commonValues.getInputDir(), commonValues.getPageCount(), commonValues.getCurrentPage());
					int encrypt=encryptPage.display(currentGUI.getFrame(),"Standard Security");

					//get parameters and call if YES
					if (encrypt == JOptionPane.OK_OPTION){

						PdfPageData currentPageData=decode_pdf.getPdfPageData();

						decode_pdf.closePdfFile();
						ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
                        itextFunctions.encrypt(commonValues.getPageCount(), currentPageData, encryptPage);
						open(commonValues.getSelectedFile());
					}
				}
			}else{

			}
			break;
        //<end-wrap>


		case BACK:
			goBackAView();
			break;
			
		case FORWARD:
			goForwardAView();
			break;
			
		case ADDVIEW:
			addAView((Integer) args[0],(Rectangle)args[1],(Integer)args[2]);
			break;

        case CURRENTPAGE:

            if(decode_pdf==null)
                status= -1;
            else
                status= currentGUI.getPageNumber();

            break;

        case PAGECOUNT:

            if(decode_pdf==null)
                status= -1;
            else
                status= decode_pdf.getPageCount();

            break;

        case GETPAGECOUNTER:

            status=currentGUI.pageCounter2;

            break;

        case GETTHUMBNAILPANEL:

            //ensure setup
            currentGUI.setBookmarks(true);

            status=currentGUI.getThumbnailPanel();

            break;

        case GETOUTLINEPANEL:

            //ensure setup
            currentGUI.setBookmarks(true);
            
            status=currentGUI.getOutlinePanel();

            break;
            


			//<link><a name="link" />

			//case MYFUNCTION:
			/**add your code here. We recommend you put it in an external class such as
			 *
			 * MyFactory.newFunction(parameters);
			 *
			 * or
			 *
			 * ItextFunctions itextFunctions=new ItextFunctions(currentGUI.getFrame(),commonValues.getSelectedFile(),decode_pdf);
			 * itextFunctions.newFeature(parameters);
			 */
			//break;

		default:
			System.out.println("No menu item set");

		}

		//Mark as executed is not running in thread
		if(!isCommandInThread)
        	executingCommand = false;
		
		return status;
        
    }

	protected void exitFullScreen() {

		final Runnable doPaintComponent = new Runnable() {
			public void run() {

				// Return to normal windowed mode
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice gs = ge.getDefaultScreenDevice();
				gs.setFullScreenWindow(null);

				win.remove(currentGUI.getDisplayPane());

				if (currentGUI.getFrame() instanceof JFrame) {
					((JFrame) currentGUI.getFrame()).getContentPane().add(currentGUI.getDisplayPane(), BorderLayout.CENTER);
					// Java 1.6 has issues with original pane remaining visible so show when fullscreen turned off
					currentGUI.getFrame().setVisible(true);

                    //restore to last position which we saved on entering full screen
                    if(screenPosition!=null)
                    currentGUI.getFrame().setLocation(screenPosition);
                    screenPosition=null;

				} else
					currentGUI.getFrame().add(currentGUI.getDisplayPane(), BorderLayout.CENTER);

				currentGUI.getDisplayPane().invalidate();
				currentGUI.getDisplayPane().updateUI();

				if (currentGUI.getFrame() instanceof JFrame)
					((JFrame) currentGUI.getFrame()).getContentPane().validate();
				else
					currentGUI.getFrame().validate();

				win.dispose();
				
				win = null;
				
				currentGUI.zoom(false);
			}
		};

        SwingUtilities.invokeLater(doPaintComponent);

    }

	private void cascade() {
		JDesktopPane desktopPane = currentGUI.getMultiViewerFrames();

		JInternalFrame[] frames = desktopPane.getAllFrames();

		/** 
		 * reverse the order of these frames, so when they are cascaded they will 
		 * maintain the order they were to start with 
		 */
		for (int left = 0, right = frames.length - 1; left < right; left++, right--) {
			// exchange the first and last
			JInternalFrame temp = frames[left];
			frames[left] = frames[right];
			frames[right] = temp;
		}

		int x = 0;
		int y = 0;
		int width = desktopPane.getWidth() / 2;
		int height = desktopPane.getHeight() / 2;

        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) { // if its minimized leave it there
                try {
                    frame.setMaximum(false);
                    frame.reshape(x, y, width, height);
                    frame.setSelected(true);

                    x += 25;
                    y += 25;
                    // wrap around at the desktop edge
                    if (x + width > desktopPane.getWidth())
                        x = 0;
                    if (y + height > desktopPane.getHeight())
                        y = 0;
                } catch (PropertyVetoException e) {
                }
            }
        }
	}

	private void tile() {

		JDesktopPane desktopPane = currentGUI.getMultiViewerFrames();

		JInternalFrame[] frames = desktopPane.getAllFrames();

		// count frames that aren't iconized
		int frameCount = 0;
        for (JInternalFrame frame1 : frames) {
            if (!frame1.isIcon())
                frameCount++;
        }

		int rows = (int) Math.sqrt(frameCount);
		int cols = frameCount / rows;
		int extra = frameCount % rows;
		// number of columns with an extra row

		int width = desktopPane.getWidth() / cols;
		int height = desktopPane.getHeight() / rows;
		int r = 0;
		int c = 0;
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                try {
                    frame.setMaximum(false);
                    frame.reshape(c * width, r * height, width, height);
                    r++;
                    if (r == rows) {
                        r = 0;
                        c++;
                        if (c == cols - extra) { // start adding an extra row
                            rows++;
                            height = desktopPane.getHeight() / rows;
                        }
                    }
                } catch (PropertyVetoException e) {
                }
            }
        }
	}

	int startX=0,startY=0;


	private MultiViewListener multiViewListener;
	
	/**
	 * this is the code which does all the work
	 * @return
	 */
	private PdfDecoder openNewMultiplePage(String fileName){

		JDesktopPane desktopPane = currentGUI.getMultiViewerFrames();

		/**
		 * setup PDF object
		 */
		final PdfDecoder localPdf=new PdfDecoder(true);
//		System.out.println("new pdf = "+localPdf.getClass().getName() + "@" + Integer.toHexString(localPdf.hashCode()));
		
		decode_pdf=localPdf;

		currentGUI.setPdfDecoder(decode_pdf);

		if (SwingUtilities.isEventDispatchThread()) {

		decode_pdf.setDisplayView(Display.SINGLE_PAGE,Display.DISPLAY_CENTERED);

		} else {
			final Runnable doPaintComponent = new Runnable() {

				public void run() {
					decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
				}
			};
			SwingUtilities.invokeLater(doPaintComponent);
		}
		//decode_pdf.setDisplayView(Display.SINGLE_PAGE,Display.DISPLAY_CENTERED);
        pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));

		//decode_pdf.addExternalHandler(this, Options.MultiPageUpdate);
		PdfDecoder.init(true);
		decode_pdf.setExtractionMode(0,1); //values extraction mode,dpi of images, dpi of page as a factor of 72

		int inset= GUI.getPDFDisplayInset();
		decode_pdf.setInset(inset,inset);
		
		if(decode_pdf.getDecoderOptions().getDisplayBackgroundColor()!=null)
			decode_pdf.setBackground(decode_pdf.getDecoderOptions().getDisplayBackgroundColor());
		else
			if (decode_pdf.useNewGraphicsMode)
				decode_pdf.setBackground(new Color(55,55,65));
			else{
				decode_pdf.setBackground(new Color(190,190,190));
			}
		decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage(),currentGUI.getRotation());

		/**
		 * setup Internal frame to hold PDF
		 **/
		JInternalFrame pdf=new JInternalFrame(fileName, true, true, true, true);
		String s = String.valueOf(startX);
		pdf.setName(s);
		pdf.setSize(250,250);
		pdf.setVisible(true);
		pdf.setLocation(startX,startY);

		startX=startX+25;
		startY=startY+25;

		/**
		 * listener to switch to this object if window selected
		 */
		multiViewListener = new MultiViewListener(decode_pdf, currentGUI, commonValues, this);
		pdf.addInternalFrameListener(multiViewListener);
		
		pdf.addComponentListener(new ComponentListener(){
			public void componentHidden(ComponentEvent e) {}

			//Prevent window from becoming unselectable
			public void componentMoved(ComponentEvent e) {
				Component c = e.getComponent();
				Component parent = c.getParent();

				if(c.getLocation().y<0){
					//Prevent internal window from leaving off the top of the panel
					c.setLocation(c.getLocation().x, 0);

				}else if((c.getLocation().y + c.getSize().height) > (parent.getSize().height + (c.getSize().height/2))){
					//Don't go too far off the bottom of the screen (half internal window size)
					c.setLocation(c.getLocation().x, (parent.getSize().height - (c.getSize().height/2)));
				}

				if(c.getLocation().x < -(c.getSize().width/2)){
					//Don't go too far off the left of the screen (half internal window size)
					c.setLocation(-(c.getSize().width/2) , c.getLocation().y);

				}else if((c.getLocation().x + c.getSize().width) > (parent.getSize().width + (c.getSize().width/2))){
					//Don't go too far off the right of the screen (half internal window size)
					c.setLocation((parent.getSize().width - (c.getSize().width/2)),c.getLocation().y);
				}
			}
			public void componentResized(ComponentEvent e) {
				if(decode_pdf.getParent()!=null && currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
					currentGUI.zoom(false);
			}
			public void componentShown(ComponentEvent e) {}
		});

		/**
		 * add the pdf display to show page
		 **/
		JScrollPane scrollPane=new JScrollPane();
		scrollPane.getViewport().add(localPdf);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(80);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(80);

		pdf.getContentPane().add(scrollPane);

		desktopPane.add(fileName,pdf);

		/**
		 * ensure at front and active
		 */
		try {
			pdf.setSelected(true);
		} catch (PropertyVetoException e1) {
			e1.printStackTrace();
		}
		pdf.toFront();
		pdf.requestFocusInWindow();
		
		return decode_pdf;

	}

	public void openTransferedFile(final String file) throws PdfException {
		
		while(openingTransferedFile || Values.isProcessing()){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		openingTransferedFile = true;
		
		currentGUI.resetNavBar();

		boolean isURL = file.startsWith("http:") || file.startsWith("file:");
		try {

			if(!isURL){
				fileIsURL=false;
				commonValues.setFileSize(new File(file).length() >> 10);
			}else
				fileIsURL=true;

			commonValues.setSelectedFile(file);
			
			currentGUI.setViewerTitle(null);
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " getting paths");
		}

		/** check file exists */
		File testFile = new File(commonValues.getSelectedFile());
		if (!isURL && !testFile.exists()) {
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerFile.text") + commonValues.getSelectedFile() + Messages.getMessage("PdfViewerNotExist"));

			/** open the file*/
		} else if ((commonValues.getSelectedFile() != null) && (Values.isProcessing() == false)) {
			
			if(currentGUI.isSingle())
				decode_pdf.flushObjectValues(true);
			else
				decode_pdf=openNewMultiplePage(commonValues.getSelectedFile());

			
			//reset the viewableArea before opening a new file
			decode_pdf.resetViewableArea();
			/**/
			
			try {
				openFile(commonValues.getSelectedFile());
			} catch (PdfException e) {
				openingTransferedFile = false;
				throw e;
			}
			
			if(commonValues.isPDF())
				openingTransferedFile=false;
			
//			SwingWorker worker = new SwingWorker() {
//				public Object construct() {
//					
//					openFile(commonValues.getSelectedFile());
//					
//					/** 
//					 * if it is an image being decoded then opening isn't complete until the thread
//					 * decodeImage is finished
//					 */
//					if(commonValues.isPDF())
//						openingTransferedFile=false;
//					
//					return null;
//				}
//			};
//			worker.start();
		}
	}
	
	public boolean openingTransferedFiles(){
		return openingTransferedFile;
	}

	/**add listeners to forms to track changes - could also do other tasks like send data to
	 * database server
	 */
	private void saveChangedForm() {
		org.jpedal.objects.acroforms.rendering.AcroRenderer formRenderer=decode_pdf.getFormRenderer();

		if(formRenderer==null)
			return;

		List names=null;

		try {
			names = formRenderer.getComponentNameList();
		} catch (PdfException e1) {
			e1.printStackTrace();
		}

		if(names==null){
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NoFields"));
		}else{
			/**
			 * create the file chooser to select the file
			 */
			File file;
			String fileToSave="";
			boolean finished=false;
			while(!finished){
				JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
				chooser.setSelectedFile(new File(commonValues.getInputDir()+ '/' +commonValues.getSelectedFile()));
				chooser.addChoosableFileFilter(new FileFilterer(new String[]{"pdf"}, "Pdf (*.pdf)"));
				chooser.addChoosableFileFilter(new FileFilterer(new String[]{"fdf"}, "fdf (*.fdf)"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//set default name to current file name
				int approved=chooser.showSaveDialog(null);
				if(approved==JFileChooser.APPROVE_OPTION){
					file = chooser.getSelectedFile();
					fileToSave=file.getAbsolutePath();

					if(!fileToSave.endsWith(".pdf")){
						fileToSave += ".pdf";
						file=new File(fileToSave);
					}

					if(fileToSave.equals(commonValues.getSelectedFile())){
						currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.SaveError"));
						continue;
					}

					if(file.exists()){
						int n=currentGUI.showConfirmDialog(fileToSave+ '\n' +
								Messages.getMessage("PdfViewerMessage.FileAlreadyExists")+".\n" +
								Messages.getMessage("PdfViewerMessage.ConfirmResave"),
								Messages.getMessage("PdfViewerMessage.Resave")
								,JOptionPane.YES_NO_OPTION);
						if(n==1)
							continue;
					}
					finished=true;
				}else{
					return;
				}
			}


            //<start-wrap>
			ItextFunctions itextFunctions=new ItextFunctions(currentGUI,commonValues.getSelectedFile(),decode_pdf);
            itextFunctions.saveFormsData(fileToSave);
            //<end-wrap>

			/**
			 * reset flag and graphical clue
			 */
			commonValues.setFormsChanged(false);
			currentGUI.setViewerTitle(null);

		}
	}
	
	/**
	 * warns user forms unsaved and offers save option
	 */
	public void handleUnsaveForms() {
			if((commonValues.isFormsChanged())&&(commonValues.isItextOnClasspath())){
				int n = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerFormsUnsavedOptions.message"),Messages.getMessage("PdfViewerFormsUnsavedWarning.message"), JOptionPane.YES_NO_OPTION);
	
				if(n==JOptionPane.YES_OPTION)
					saveChangedForm();
			}
		commonValues.setFormsChanged(false);
	}

	/**
	 * extract selected area as a rectangle and show onscreen
	 */
	public void extractSelectedScreenAsImage() {

		/**ensure co-ords in right order*/
		int t_x1=commonValues.m_x1;
		int t_x2=commonValues.m_x2;
		int t_y1=commonValues.m_y1;
		int t_y2=commonValues.m_y2;

		if(commonValues.m_y1<commonValues.m_y2){
			t_y2=commonValues.m_y1;
			t_y1=commonValues.m_y2;
		}

		if(commonValues.m_x1>commonValues.m_x2){
			t_x2=commonValues.m_x1;
			t_x1=commonValues.m_x2;
		}
		float scaling = 100;

		if(PdfDecoder.isRunningOnWindows)
			scaling = 100*currentGUI.getScaling();


		final BufferedImage snapShot=decode_pdf.getSelectedRectangleOnscreen(t_x1,t_y1,t_x2,t_y2,scaling);

		/**
		 * put in panel
		 */
		//if(temp!=null){
		JPanel image_display = new JPanel();
		image_display.setLayout( new BorderLayout() );

		//wrap image so we can display
		if( snapShot != null ){
			IconiseImage icon_image = new IconiseImage( snapShot );

			//add image if there is one
			image_display.add( new JLabel( icon_image ), BorderLayout.CENTER );
		}else{
			return;
		}

		final JScrollPane image_scroll = new JScrollPane();
		image_scroll.getViewport().add( image_display );

		//set image size
		int imgSize=snapShot.getWidth();
		if(imgSize<snapShot.getHeight())
			imgSize=snapShot.getHeight();
		imgSize=imgSize+50;
		if(imgSize>450)
			imgSize=450;

		/**resizeable pop-up for content*/
		Container frame = currentGUI.getFrame();
		final JDialog displayFrame =  new JDialog((JFrame)null,true);
		if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET){
			displayFrame.setLocationRelativeTo(null);
			displayFrame.setLocation(frame.getLocationOnScreen().x+10,frame.getLocationOnScreen().y+10);
		}

		displayFrame.setSize(imgSize,imgSize);
		displayFrame.setTitle(Messages.getMessage("PdfViewerMessage.SaveImage"));
		displayFrame.getContentPane().setLayout(new BorderLayout());
		displayFrame.getContentPane().add(image_scroll,BorderLayout.CENTER);

		JPanel buttonBar=new JPanel();
		buttonBar.setLayout(new BorderLayout());
		displayFrame.getContentPane().add(buttonBar,BorderLayout.SOUTH);

		/**
		 * yes option allows user to save content
		 */
		JButton yes=new JButton(Messages.getMessage("PdfMessage.Yes"));
		yes.setFont(new Font("SansSerif", Font.PLAIN, 12));
		buttonBar.add(yes,BorderLayout.WEST);
		yes.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				displayFrame.setVisible(false);

				File file;
				String fileToSave;
				boolean finished=false;
				while(!finished){
					final JFileChooser chooser = new JFileChooser(System.getProperty( "user.dir" ) );

					chooser.addChoosableFileFilter( new FileFilterer( new String[] { "tif", "tiff" }, "TIFF" ) );
					chooser.addChoosableFileFilter( new FileFilterer( new String[] { "jpg","jpeg" }, "JPEG" ) );

					int approved = chooser.showSaveDialog(image_scroll);

					if(approved==JFileChooser.APPROVE_OPTION){

						file = chooser.getSelectedFile();
						fileToSave=file.getAbsolutePath();

						String format=chooser.getFileFilter().getDescription();

						if(format.equals("All Files"))
							format="TIFF";

						if(!fileToSave.toLowerCase().endsWith(('.' +format).toLowerCase())){
							fileToSave += '.' +format;
							file=new File(fileToSave);
						}

						if(file.exists()){

							int n=currentGUI.showConfirmDialog(fileToSave+ '\n' +
									Messages.getMessage("PdfViewerMessage.FileAlreadyExists")+".\n" +
									Messages.getMessage("PdfViewerMessage.ConfirmResave"),
									Messages.getMessage("PdfViewerMessage.Resave"),JOptionPane.YES_NO_OPTION);
							if(n==1)
								continue;
						}

						if(JAIHelper.isJAIused())
							JAIHelper.confirmJAIOnClasspath();


						//Do the actual save
						if(snapShot!=null) {
							if(JAIHelper.isJAIused())
								javax.media.jai.JAI.create("filestore", snapShot, fileToSave, format);
							else if(format.toLowerCase().startsWith("tif"))
								currentGUI.showMessageDialog("Please setup JAI library for Tiffs");
							else{
								try {
									ImageIO.write(snapShot,format,new File(fileToSave));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
						finished=true;
					}else{
						return;
					}
				}

				displayFrame.dispose();

			}
		});

		/**
		 * no option just removes display
		 */
		JButton no=new JButton(Messages.getMessage("PdfMessage.No"));
		no.setFont(new Font("SansSerif", Font.PLAIN, 12));
		buttonBar.add(no,BorderLayout.EAST);
		no.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				displayFrame.dispose();
			}});

		/**show the popup*/
		displayFrame.setVisible(true);

	}
	
	/**
	 * Increase size of highlight area to ensure it fully encompasses
	 * the text to be extracted
	 * @param highlight : original highlight area
	 * @return updated highlgiht area
	 */
	private static Rectangle adjustHighlightForExtraction(Rectangle highlight){
		
		int x = highlight.x - 3;
		int y = highlight.y - 3;
		int width = highlight.width + 6;
		int height = highlight.height + 6;
		
		return new Rectangle(x,y,width,height);
	}
	
	/**
	 * routine to link GUI into text extraction functions
	 */
	public String copySelectedText() {

        if(!decode_pdf.isExtractionAllowed()){
            currentGUI.showMessageDialog("Not allowed");
            return "";
        }

        String returnValue = "";
        Rectangle[] selected = decode_pdf.getTextLines().getHighlightedAreas(commonValues.getCurrentPage());
        if(selected==null)
            return "";
        boolean multipleAreas = (selected.length>1);
        for(int t=0; t!=selected.length; t++){

            /**ensure co-ords in right order*/
            selected[t] = adjustHighlightForExtraction(selected[t]);

            int t_x1=selected[t].x;
            int t_x2=selected[t].x+selected[t].width;
            int t_y1=selected[t].y+selected[t].height;
            int t_y2=selected[t].y;

//			int t_x1=commonValues.m_x1;
//			int t_x2=commonValues.m_x2;
//			int t_y1=commonValues.m_y1;
//			int t_y2=commonValues.m_y2;

            if(t_y1<t_y2){
                int temp = t_y2;
                t_y2=t_y1;
                t_y1=temp;
            }

            if(t_x1>t_x2){
                int temp = t_x2;
                t_x2=t_x1;
                t_x1=temp;
            }

            if(t_x1<currentGUI.cropX)
                t_x1 = currentGUI.cropX;
            if(t_x1>currentGUI.cropW-currentGUI.cropX)
                t_x1 = currentGUI.cropW-currentGUI.cropX;

            if(t_x2<currentGUI.cropX)
                t_x2 = currentGUI.cropX;
            if(t_x2>currentGUI.cropW-currentGUI.cropX)
                t_x2 = currentGUI.cropW-currentGUI.cropX;

            if(t_y1<currentGUI.cropY)
                t_y1 = currentGUI.cropY;
            if(t_y1>currentGUI.cropH-currentGUI.cropY)
                t_y1 = currentGUI.cropH-currentGUI.cropY;

            if(t_y2<currentGUI.cropY)
                t_y2 = currentGUI.cropY;
            if(t_y2>currentGUI.cropH-currentGUI.cropY)
                t_y2 = currentGUI.cropH-currentGUI.cropY;

            /**
             * bringup display and process user requests
             */
            display = true;

            while (display) {

                String extractedText;

                try {
                    /** create a grouping object to apply grouping to data */
                    PdfGroupingAlgorithms currentGrouping = decode_pdf.getGroupingObject();

                    //switch off display - pops up again if help selected
                    display = false;


                    /**get the text*/
                    extractedText = currentGrouping.extractTextInRectangle(
                            t_x1, t_y1, t_x2, t_y2, commonValues.getCurrentPage(), false,
                            true);

                    /**
                     * find out if xml or text - as we need to turn xml off before
                     * extraction. So we assume xml and strip out. This is obviously
                     */
                    if(extractedText==null || extractedText.length() == 0){
                        if(!multipleAreas){
                            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoTextFound"));
                        }
                    }else
                        extractedText=Strip.stripXML(extractedText, decode_pdf.isXMLExtraction()).toString();

                    if (extractedText != null) //Add extractedText & newline to returnValue
                        returnValue = returnValue + extractedText+((char)0x0D)+((char)0x0A);

                } catch (PdfException e) {
                    System.err.println("Exception " + e.getMessage()
                            + " in file " + commonValues.getSelectedFile());
                    e.printStackTrace();
                }
            }
        }
        if(returnValue.length()>2)
            return returnValue.substring(0, returnValue.length()-2);
        else
            return "";

    }
	
	
	private JScrollPane updateExtractionExample(AbstractButton button, boolean xml) throws PdfException {
		
		//int page = commonValues.getCurrentHighlightedPage();
		//if(page==-1)
		//	page = commonValues.getCurrentPage();
		//
		//Rectangle[] areas= decode_pdf.getHighlightedAreas(page);
		
		Rectangle[] areas= decode_pdf.getTextLines().getHighlightedAreas(commonValues.getCurrentPage());

		JScrollPane scroll = new JScrollPane();
		String finalString = "";


		for(int t=0; t!=areas.length; t++){
			if(areas[t]!=null){
				
				areas[t] = adjustHighlightForExtraction(areas[t]);
				
				int t_x1=areas[t].x;
				int t_x2=areas[t].x + areas[t].width;
				int t_y1=areas[t].y + areas[t].height;
				int t_y2=areas[t].y;


				if(t_y1<t_y2){
					int temp = t_y2;
					t_y2=t_y1;
					t_y1=temp;
				}

				if(t_x1>t_x2){
					int temp = t_x2;
					t_x2=t_x1;
					t_x1=temp;
				}

				if(t_x1<currentGUI.cropX)
					t_x1 = currentGUI.cropX;
				if(t_x1>currentGUI.cropX+currentGUI.cropW)
					t_x1 = currentGUI.cropX+currentGUI.cropW;

				if(t_x2<currentGUI.cropX)
					t_x2 = currentGUI.cropX;
				if(t_x2>currentGUI.cropX+currentGUI.cropW)
					t_x2 = currentGUI.cropX+currentGUI.cropW;
				
				if(t_y1<currentGUI.cropY)
					t_y1 = currentGUI.cropY;
				if(t_y1>currentGUI.cropY+currentGUI.cropH)
					t_y1 = currentGUI.cropY+currentGUI.cropH;

				if(t_y2<currentGUI.cropY)
					t_y2 = currentGUI.cropY;
				if(t_y2>currentGUI.cropY+currentGUI.cropH)
					t_y2 = currentGUI.cropY+currentGUI.cropH;


				if(button.getText().equals("Table")){
					finalString = finalString + extractTextTable(decode_pdf.getGroupingObject(), xml, t_x1, t_x2, t_y1, t_y2);
				}

				if(button.getText().equals("Rectangle")){
					finalString = finalString + extractTextRectangle(decode_pdf.getGroupingObject(), xml, t_x1, t_x2, t_y1, t_y2)+ ' ';
				}

				if(button.getText().equals("WordList")){
					finalString = finalString + extractTextList(decode_pdf.getGroupingObject(), xml, t_x1, t_x2, t_y1, t_y2);
				}
			}
		}

		if(finalString.length() != 0){
			if(button.getText().equals("Table")){
				try {
					scroll =  currentGUI.createPane(new JTextPane(), finalString , xml);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			if(button.getText().equals("Rectangle")){
				try {
					scroll = currentGUI.createPane(new JTextPane(), finalString , xml);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if(button.getText().equals("WordList")){
				try {
					scroll = currentGUI.createPane(new JTextPane(), finalString , xml);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			scroll.setPreferredSize(new Dimension(315, 150));
			scroll.setMinimumSize(new Dimension(315, 150));

			Component[] coms = scroll.getComponents();
			for(int i=0; i!=coms.length; i++){
				if(scroll.getComponent(i) instanceof JViewport){
					JViewport view = (JViewport)scroll.getComponent(i);
					Component[] coms1 = view.getComponents();
					for(int j=0; j!=coms1.length; j++){
						if(coms1[j] instanceof JTextPane)
							((JTextPane)coms1[j]).setEditable(false);
					}
				}
			}
			return scroll;
		}
		return null;
	}
	
	/**
	 * routine to link GUI into text extraction functions
	 */
	public void extractSelectedText() {

		if(!decode_pdf.isExtractionAllowed()){
			currentGUI.showMessageDialog("Not allowed");
			return ;
		}
		
		//int page = commonValues.getCurrentHighlightedPage();
		//if(page==-1)
		//	page = commonValues.getCurrentPage();
		//
		//final Rectangle[] areas= decode_pdf.getHighlightedAreas(page);
		
		final Rectangle[] selected = decode_pdf.getTextLines().getHighlightedAreas(commonValues.getCurrentPage());
		
		/**ensure co-ords in right order*/
		if(selected==null){
			JOptionPane.showMessageDialog(decode_pdf, "There is no text selected.\nPlease highlight the text you wish to extract.", "No Text selected", JOptionPane.ERROR_MESSAGE);
			return;
		}

		/**Window gui components*/
		final JScrollPane examplePane = new JScrollPane();
		final JPanel display_value = new JPanel();
		final ButtonGroup group = new ButtonGroup();
		final JRadioButton text = new JRadioButton("Extract as Text");
		final JRadioButton xml = new JRadioButton("Extract  as  XML");
		final JRadioButton rectangleGrouping = new JRadioButton(Messages.getMessage("PdfViewerRect.label"));
		final JRadioButton tableGrouping = new JRadioButton(Messages.getMessage("PdfViewerTable.label"));
		final JRadioButton wordListExtraction = new JRadioButton(Messages.getMessage("PdfViewerWordList.label"));
		final SpringLayout layout = new SpringLayout();
		final JFrame extractionFrame = new JFrame(Messages.getMessage("PdfViewerCoords.message")+
				' ' + commonValues.m_x1
				+ " , " + commonValues.m_y1 + " , " + (commonValues.m_x2-commonValues.m_x1) + " , " + (commonValues.m_y2-commonValues.m_y1));
		//JLabel demoMessage = new JLabel(Messages.getMessage("PdfViewerDemo.message")); 
		JLabel demoMessage = new JLabel("                         ");
		ButtonGroup type = new ButtonGroup();
		Object[] options = new Object[]{Messages.getMessage("PdfViewerHelpMenu.text"),
				Messages.getMessage("PdfViewerCancel.text"),
				Messages.getMessage("PdfViewerextract.text")};
		JButton help = new JButton((String)options[0]);
		JButton cancel = new JButton((String)options[1]);
		JButton extract = new JButton((String)options[2]);
		display_value.setLayout(layout);
		
		/**
		 * Used to udpate the example scrollpane when an option is changed.
		 */
		final Runnable r = new Runnable(){
			public void run() {
				Enumeration en = group.getElements();
				while(en.hasMoreElements()){ //First find which button has been changed
					final AbstractButton button = (AbstractButton) en.nextElement();
					if(button.isSelected()){
						try{
							 // Remove old scrollpane from the window
							Component[] com = display_value.getComponents();
							for(int i=0; i!=com.length; i++){
								if (com[i] instanceof JScrollPane) {
									display_value.remove(com[i]);
								}
							}
							
							//Create a new scrollpane with changes settings and add to the window
							JScrollPane scroll = updateExtractionExample(button, xml.isSelected());
							if(scroll!=null){
								layout.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, display_value);
								layout.putConstraint(SpringLayout.NORTH, scroll, 5, SpringLayout.SOUTH, tableGrouping);
								display_value.add(scroll);
							}else{
								
								JLabel noExample = new JLabel("No Example Available");
								
								Font exampleFont = noExample.getFont();
								exampleFont = exampleFont.deriveFont(exampleFont.getStyle(), 20f);//change as ME has no deriveFont(size only)
								
								noExample.setFont(exampleFont);
								noExample.setForeground(Color.RED);
								
								layout.putConstraint(SpringLayout.EAST, noExample, -75, SpringLayout.EAST, display_value);
								layout.putConstraint(SpringLayout.NORTH, noExample, 50, SpringLayout.SOUTH, tableGrouping);
								display_value.add(noExample);
							}
						}catch(PdfException ex){
							ex.printStackTrace();
						}
						break;
					}
				}

				//Update the display to ensure it is going to be displayed correctly
				display_value.updateUI();
			}
		};
		
		//Add demo message to the bottom of the display
        
		/**
		 *Add grouping buttons to the top of the display
		 */
		//Rectangle grouping
		rectangleGrouping.setSelected(true);
		rectangleGrouping.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				xml.setText("Extract  as  XML");
				text.setText("Extract as Text");
				SwingUtilities.invokeLater(r);
			}
		});
		group.add(rectangleGrouping);
		rectangleGrouping.setToolTipText(Messages.getMessage("PdfViewerRect.message"));
		layout.putConstraint(SpringLayout.WEST, rectangleGrouping, 10, SpringLayout.WEST, display_value);
		layout.putConstraint(SpringLayout.NORTH, rectangleGrouping, 5, SpringLayout.NORTH, display_value);
		display_value.add(rectangleGrouping);
		//Table Grouping
		tableGrouping.setSelected(true);
		tableGrouping.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				xml.setText("Extract as XHTML");
				text.setText("Extract as CSV");
				SwingUtilities.invokeLater(r);
			}
		});
		group.add(tableGrouping);
		tableGrouping.setToolTipText(Messages.getMessage("PdfViewerTable.message"));
		layout.putConstraint(SpringLayout.WEST, tableGrouping, 50, SpringLayout.EAST, rectangleGrouping);
		layout.putConstraint(SpringLayout.NORTH, tableGrouping, 5, SpringLayout.NORTH, display_value);
		display_value.add(tableGrouping);
		//WordList Grouping
		wordListExtraction.setSelected(true);
		wordListExtraction.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				xml.setText("Extract  as  XML");
				text.setText("Extract as Text");
				SwingUtilities.invokeLater(r);
			}
		});
		group.add(wordListExtraction);
		wordListExtraction.setToolTipText(Messages.getMessage("PdfViewerWordList.message"));
		layout.putConstraint(SpringLayout.EAST, wordListExtraction, -5, SpringLayout.EAST, display_value);
		layout.putConstraint(SpringLayout.NORTH, wordListExtraction, 5, SpringLayout.NORTH, display_value);
		display_value.add(wordListExtraction);

		
		//Add example pane to the window
		examplePane.setPreferredSize(new Dimension(315, 150));
		examplePane.setMinimumSize(new Dimension(315, 150));
		layout.putConstraint(SpringLayout.EAST, examplePane, -5, SpringLayout.EAST, display_value);
		layout.putConstraint(SpringLayout.NORTH, examplePane, 5, SpringLayout.SOUTH, tableGrouping);
		display_value.add(examplePane);

		//Add xml and text radio buttons
		type.add(xml);
		type.add(text);
		xml.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){					
				SwingUtilities.invokeLater(r);
			}
		});
		text.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(r);
			}
		});
		text.setSelected(true);
		layout.putConstraint(SpringLayout.WEST, xml, 5, SpringLayout.WEST, display_value);
		layout.putConstraint(SpringLayout.SOUTH, xml, -5, SpringLayout.NORTH, extract);
		display_value.add(xml);
		layout.putConstraint(SpringLayout.EAST, text, -5, SpringLayout.EAST, display_value);
		layout.putConstraint(SpringLayout.SOUTH, text, -5, SpringLayout.NORTH, extract);
		display_value.add(text);
		
		
		//Add the bottom buttons. Extract, Help and Cancel
		layout.putConstraint(SpringLayout.SOUTH, extract, -5, SpringLayout.NORTH, demoMessage);
		layout.putConstraint(SpringLayout.EAST, extract, -5, SpringLayout.EAST, display_value);
		display_value.add(extract);
		layout.putConstraint(SpringLayout.SOUTH, cancel, -5, SpringLayout.NORTH, demoMessage);
		layout.putConstraint(SpringLayout.EAST, cancel, -5, SpringLayout.WEST, extract);
		display_value.add(cancel);
		layout.putConstraint(SpringLayout.SOUTH, help, -5, SpringLayout.NORTH, demoMessage);
		layout.putConstraint(SpringLayout.EAST, help, -5, SpringLayout.WEST, cancel);
		display_value.add(help);

		help.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JTextArea info = new JTextArea(Messages.getMessage("PdfViewerGroupingInfo.message"));

				currentGUI.showMessageDialog(info);
				display = true;
			}
		});
		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				extractionFrame.setVisible(false);
			}
		});
		extract.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try{
					String finalValue = ""; // Total data extracted so far
					String extractedText; // Data extracted with each loop
					int value = 0;
					boolean isXML = true;
					
					//Find which option is selected the use value to execute correctly
					for (Enumeration enum1 = group.getElements() ; enum1.hasMoreElements() ;) {
						if(((AbstractButton) enum1.nextElement()).isSelected())
							break;
						else
							value++;
					}
					
					for(int t=0; t!=selected.length; t++){
						extractedText = "";
						
						/**ensure co-ords in right order*/
						selected[t] = adjustHighlightForExtraction(selected[t]);
						
						int t_x1=selected[t].x;
						int t_x2=selected[t].x+selected[t].width;
						int t_y1=selected[t].y+selected[t].height;
						int t_y2=selected[t].y;
						
						if(t_y1<t_y2){
							int temp = t_y2;
							t_y2=t_y1;
							t_y1=temp;
						}

						if(t_x1>t_x2){
							int temp = t_x2;
							t_x2=t_x1;
							t_x1=temp;
						}

						if(t_x1<currentGUI.cropX)
							t_x1 = currentGUI.cropX;
						if(t_x1>currentGUI.cropW-currentGUI.cropX)
							t_x1 = currentGUI.cropW-currentGUI.cropX;

						if(t_x2<currentGUI.cropX)
							t_x2 = currentGUI.cropX;
						if(t_x2>currentGUI.cropW-currentGUI.cropX)
							t_x2 = currentGUI.cropW-currentGUI.cropX;

						if(t_y1<currentGUI.cropY)
							t_y1 = currentGUI.cropY;
						if(t_y1>currentGUI.cropH-currentGUI.cropY)
							t_y1 = currentGUI.cropH-currentGUI.cropY;

						if(t_y2<currentGUI.cropY)
							t_y2 = currentGUI.cropY;
						if(t_y2>currentGUI.cropH-currentGUI.cropY)
							t_y2 = currentGUI.cropH-currentGUI.cropY;
						
						switch (value) {
						case 0: //text extraction

							/**get the text*/
							extractedText = extractTextRectangle(decode_pdf.getGroupingObject(), xml.isSelected(), t_x1, t_x2, t_y1, t_y2)+((char)0x0D)+((char)0x0A);

							break;

						case 1: //table

							extractedText = extractTextTable(decode_pdf.getGroupingObject(), xml.isSelected(), t_x1, t_x2, t_y1, t_y2);
							break;

						case 2: //text wordlist extraction
							
							extractedText=extractTextList(decode_pdf.getGroupingObject(), xml.isSelected(), t_x1, t_x2, t_y1, t_y2);
							break;

						default: //just in case user edits code and to handle cancel
							break;

						}
						finalValue = finalValue + extractedText;
					}
					
					
					//Once all data is stored in finalValue, produce output window
					if (finalValue != null) {
						
						//Create scrollpane containg the final data
						JScrollPane scroll=new JScrollPane();
						try {
							JTextPane text_pane=new JTextPane();
							scroll = currentGUI.createPane(text_pane,finalValue,  isXML);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
						scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
						scroll.setPreferredSize(new Dimension(400,400));

						/**Create a resizeable pop-up for content*/
						final JDialog displayFrame =  new JDialog((JFrame)null,true);
						if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET){
							Container frame = currentGUI.getFrame();
							displayFrame.setLocation(frame.getLocationOnScreen().x+10,frame.getLocationOnScreen().y+10);
						}
						displayFrame.setSize(450,450);
						displayFrame.setTitle(Messages.getMessage("PdfViewerExtractedText.menu"));
						displayFrame.getContentPane().setLayout(new BorderLayout());
						displayFrame.getContentPane().add(scroll,BorderLayout.CENTER);

						//Add buttons
						JPanel buttonBar=new JPanel();
						buttonBar.setLayout(new BorderLayout());
						displayFrame.getContentPane().add(buttonBar,BorderLayout.SOUTH);

						/**
						 * yes option allows user to save content
						 */
						JButton yes=new JButton(Messages.getMessage("PdfViewerMenu.return"));
						yes.setFont(new Font("SansSerif", Font.PLAIN, 12));
						buttonBar.add(yes,BorderLayout.WEST);
						yes.addActionListener(new ActionListener(){

							public void actionPerformed(ActionEvent e) {
								display=true;
								displayFrame.dispose();

							}
						});

						/**
						 * no option just removes display
						 */
						JButton no=new JButton(Messages.getMessage("PdfViewerFileMenuExit.text"));
						no.setFont(new Font("SansSerif", Font.PLAIN, 12));
						buttonBar.add(no,BorderLayout.EAST);
						no.addActionListener(new ActionListener(){

							public void actionPerformed(ActionEvent e) {

								displayFrame.dispose();
							}});

						/**show the popup*/
						displayFrame.setVisible(true);
					}
				}catch(PdfException e1){
					e1.printStackTrace();
				}
			}
		});
		
		//Add display panel to the extraction options window
		extractionFrame.getContentPane().add(display_value, BorderLayout.CENTER);
		extractionFrame.setSize(350, 300);
		
		
		//Initialise example 
		SwingUtilities.invokeLater(r);
		
		//Set location over window
		extractionFrame.setLocationRelativeTo(currentGUI.getFrame());
		extractionFrame.setResizable(false);
		
		//Display
		extractionFrame.setVisible(true);	
		
	}

	private String extractTextList(PdfGroupingAlgorithms currentGrouping, boolean isXML, int t_x1, int t_x2, int t_y1, int t_y2) throws PdfException {
		
		//int page = commonValues.getCurrentHighlightedPage();
		//if(page==-1)
		//	page = commonValues.getCurrentPage();
		
		String extractedText = "";
		//always reset to use unaltered co-ords
		PdfGroupingAlgorithms.useUnrotatedCoords=true;

		//page data so we can choose portrait or landscape
		PdfPageData pageData=decode_pdf.getPdfPageData();
		int rotation=pageData.getRotation(commonValues.getCurrentPage());
		if(rotation!=0){
			int alterCoords=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerRotatedCoords.message"),
					Messages.getMessage("PdfViewerOutputFormat.message"),
					JOptionPane.YES_NO_OPTION);

			if(alterCoords==0)
				PdfGroupingAlgorithms.useUnrotatedCoords=false;
		}


		/**get the text*/
		List words = currentGrouping.extractTextAsWordlist(
				t_x1,
				t_y1,
				t_x2,
				t_y2,
				commonValues.getCurrentPage(),
				true,null);

		if(words==null)
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoTextFound")+"\nx1:"+t_x1+" y1:"+t_y1+" x2:"+t_x2+" y2:"+t_y2);

		if(words!=null){
			//put words into list
			StringBuilder textOutput=new StringBuilder();
			Iterator wordIterator=words.iterator();

			while(wordIterator.hasNext()){

				String currentWord=(String) wordIterator.next();

				/**remove the XML formatting if present - not needed for pure text*/
				if(!isXML)
					currentWord=Strip.convertToText(currentWord, decode_pdf.isXMLExtraction());

				int wx1=(int)Float.parseFloat((String) wordIterator.next());
				int wy1=(int)Float.parseFloat((String) wordIterator.next());
				int wx2=(int)Float.parseFloat((String) wordIterator.next());
				int wy2=(int)Float.parseFloat((String) wordIterator.next());

				/**this could be inserting into a database instead*/
				textOutput.append(currentWord).append(',').append(wx1).append(',').append(wy1).append(',').append(wx2).append(',').append(wy2).append('\n');

			}
			if(textOutput.toString()!=null)
				extractedText =  textOutput.toString();
		}
		return extractedText;
	}

	private String extractTextTable(PdfGroupingAlgorithms currentGrouping, boolean isCSV, int t_x1, int t_x2, int t_y1, int t_y2) throws PdfException {
		
		//int page = commonValues.getCurrentHighlightedPage();
		//if(page==-1)
		//	page = commonValues.getCurrentPage();
		
		//rest to default in case text option selected
		
		Map content;

		/**
		 * find out if xml or text - as we need to turn xml off before
		 * extraction. So we assume xml and strip out. This is obviously
		 */

		if(!isCSV)
			content = currentGrouping.extractTextAsTable(t_x1,
					t_y1, t_x2, t_y2, commonValues.getCurrentPage(), true, false,
					false, false, 0);
		else
			content = currentGrouping.extractTextAsTable(t_x1,
					t_y1, t_x2, t_y2, commonValues.getCurrentPage(), false, true,
					true, false, 1);
		
		if(content.get("content") !=null)
			return (String)content.get("content");
		else
			return "";
	}

	private String extractTextRectangle(PdfGroupingAlgorithms currentGrouping, boolean isXML, int t_x1, int t_x2, int t_y1, int t_y2) throws PdfException {
		
		//int page = commonValues.getCurrentHighlightedPage();
		//if(page==-1)
		//	page = commonValues.getCurrentPage();
		
		/**get the text*/
		String extractedText = currentGrouping.extractTextInRectangle(
				t_x1, t_y1, t_x2, t_y2, commonValues.getCurrentPage(), false,
				true);

		/**
		 * find out if xml or text - as we need to turn xml off before
		 * extraction. So we assume xml and strip out. This is obviously
		 */
		
		if(extractedText==null){
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoTextFound")+"\nx1:"+t_x1+" y1:"+t_y1+" x2:"+t_x2+" y2:"+t_y2);
			return "";
		}else if(!isXML){
			extractedText=Strip.stripXML(extractedText, decode_pdf.isXMLExtraction()).toString();
		}
		
		return extractedText;
	}

	/**
	 * called by nav functions to decode next page
	 */
	private void decodeImage(final boolean resizePanel) {
		
        //remove any search highlight
		decode_pdf.getTextLines().clearHighlights();
		//decode_pdf.addHighlights(null, false);
		//decode_pdf.setMouseHighlightAreas(null); now a duplicate of above
		
		//stop user changing scaling while decode in progress
		currentGUI.resetComboBoxes(false);

		currentGUI.setPageLayoutButtonsEnabled(false);

        /** flush any previous pages */
        decode_pdf.getDynamicRenderer().flush();
        pages.refreshDisplay();

        /** if running terminate first */
		thumbnails.terminateDrawing();

		Values.setProcessing(true);

		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				try {

					currentGUI.updateStatusMessage(Messages.getMessage("PdfViewerDecoding.page"));

					if (img != null)
						addImage(img);

					PdfPageData page_data = decode_pdf.getPdfPageData();
					
					if (img != null)
						page_data.setMediaBox(new float[]{0,0,img.getWidth(),img.getHeight()});

					page_data.checkSizeSet(1);
					currentGUI.resetRotationBox();

					/**
					 * make sure screen fits display nicely
					 */
					if ((resizePanel) && (thumbnails.isShownOnscreen()))
						currentGUI.zoom(false);

					if (Thread.interrupted())
						throw new InterruptedException();
					
					currentGUI.setPageNumber();
					
					currentGUI.setViewerTitle(null); // restore title
					
				} catch (Exception e) {
					currentGUI.setViewerTitle(null); //restore title
					
				}

				currentGUI.setStatusProgress(100);

				//reanable user changing scaling
				currentGUI.resetComboBoxes(true);
				
				//ensure drawn
				decode_pdf.repaint();

				openingTransferedFile=false;
				
				return null;
			}
		};

		worker.start();
	}

	/**
	 *  initial method called to open a new PDF
	 * @throws PdfException 
	 */
	protected boolean openUpFile(String selectedFile) throws PdfException {

        commonValues.maxViewY=0;//ensure reset for any viewport
        
        searchFrame.resetSearchWindow();
        
        //Turn MultiPageTiff flag off to ensure no mistakes
        commonValues.setMultiTiff(false);

        boolean fileCanBeOpened = true;

        if(currentGUI.isSingle())
            decode_pdf.closePdfFile();

        /** ensure all data flushed from PdfDecoder before we decode the file */
        //decode_pdf.flushObjectValues(true);

        try {
            //System.out.println("commonValues.isPDF() = "+commonValues.isPDF()+" <<<");
            /** opens the pdf and reads metadata */
            if(commonValues.isPDF()){
                if(inputStream!=null || selectedFile.startsWith("http") || selectedFile.startsWith("file:") || selectedFile.startsWith("jar:")){
                    try{

                        //<link><a name="linearized" />
                        /**
                         * code below checks if file linearized and loads rest in background if it is
                         */
                        boolean isLinearized=false;

                        //use for all inputStream as we can't easily test
                        if(inputStream!=null)
                            isLinearized=true;
                        else if(commonValues.getModeOfOperation()!=Values.RUNNING_APPLET)
                            isLinearized=isPDFLinearized(commonValues.getSelectedFile());

                        if(!isLinearized){

                            /**
                            FileDownload fd=null;
                            if(coords==null)
                            	fd = new FileDownload(false, null);
                            else
                            	fd = new FileDownload(currentGUI.isUseDownloadWindow(), coords);
                            File tempFile = fd.createWindow(commonValues.getSelectedFile());
                            /**/

                            if(commonValues.getSelectedFile().startsWith("jar:")) {
                                InputStream is=this.getClass().getResourceAsStream(commonValues.getSelectedFile().substring(4));

                                decode_pdf.openPdfFileFromInputStream(is,false);
                            }else{
                                final DownloadProgress dlp = new DownloadProgress(currentGUI,commonValues.getSelectedFile());
                                Thread t = new Thread() {
                                    public void run() {
                                        while(dlp.isDownloading()){
                                            currentGUI.setDownloadProgress("download", dlp.getProgress());
                                            try {
                                                Thread.sleep(500);
                                            } catch(Exception e) {
                                            }
                                        }
                                    }
                                };
                                t.start();
                                dlp.startDownload();

                                //currentGUI.showMessageDialog("cached 4");
                                File tempFile = dlp.getFile();

                                //currentGUI.showMessageDialog("about to open "+tempFile.getCanonicalPath());
                                decode_pdf.openPdfFile(tempFile.getCanonicalPath());
                                //currentGUI.showMessageDialog("opened");
                            }

                        }else{

                        	//currentGUI.showMessageDialog("loading linearized");
                            //update viewer to show this
                            currentGUI.setViewerTitle("Loading linearized PDF "+commonValues.getSelectedFile());

                            //now load linearized  part
                            if(inputStream!=null)
                                decode_pdf.openPdfFileFromInputStream(inputStream, true);
                            else
                                decode_pdf.openPdfFileFromURL(commonValues.getSelectedFile(), true);

                            PdfObject linearObj=(PdfObject) decode_pdf.getJPedalObject(PdfDictionary.Linearized);
                            int linearfileLength=linearObj.getInt(PdfDictionary.L);

                            String message="Downloading ";
                            linearfileLength=linearfileLength/1024;
                            if(linearfileLength<1024)
                                message=message+linearfileLength+" kB";
                            else{
                                linearfileLength=linearfileLength/1024;
                                message=message+linearfileLength+" M";
                            }

                            final String fMessage=message;

                            Thread fullReaderer = new Thread() {
                                public void run() {

                                    LinearThread linearizedBackgroundReaderer = (LinearThread) decode_pdf.getJPedalObject(PdfDictionary.LinearizedReader);

                                    while(linearizedBackgroundReaderer!=null && linearizedBackgroundReaderer.isAlive()){

                                        try {
                                            Thread.sleep(1000);
                                            //System.out.println(".");
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        currentGUI.setDownloadProgress(fMessage,linearizedBackgroundReaderer.getPercentageLoaded());
                                                                                
                                    }

                                    currentGUI.setDownloadProgress(fMessage, 100);

                                    processPage();

                                }
                            };

                            fullReaderer.start();

                        }
                    }catch(Exception e){
                        currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.UrlError")+" file="+selectedFile+ '\n' +e.getMessage());
                        decode_pdf.closePdfFile();
                        fileCanBeOpened=false;
                    }
                }else{

                    //alternate code to open via array or test byteArray
                    //not recommended for large files
                    final boolean test=false;
                    if(test){
                        File fileSize=new File(commonValues.getSelectedFile());
                        byte[] bytes=new byte[(int) fileSize.length()];
                        FileInputStream fis= null;
                        try {
                            fis = new FileInputStream(commonValues.getSelectedFile());
                            fis.read(bytes);
                            decode_pdf.openPdfArray(bytes);
                            //System.out.println("open via decode_pdf.openPdfArray(bytes)");

                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }else{
                        try{
                            decode_pdf.openPdfFile(commonValues.getSelectedFile());
                        }catch(RuntimeException e){


                            //customise message for missing bouncycastle error
                            String message;
                            if(e.getMessage().contains("bouncycastle")){
                                message=e.getMessage();
                            }else{
                                message="Exception in code "+e.getMessage()+" please send to IDRsolutions";
                            }

                           currentGUI.showMessageDialog(message);
                           LogWriter.writeLog("Exception "+e.getMessage());
                        }
                    }

                    if(decode_pdf.getPageCount()>1)
                        currentGUI.setPageLayoutButtonsEnabled(true);

                }


                //reset thumbnails
                currentGUI.reinitThumbnails();


            }else{

                //set values for page display
                decode_pdf.resetForNonPDFPage(1);

                lastPageDecoded=1;

                boolean isTiff= selectedFile.toLowerCase().contains(".tif");

                //decode image
                if(JAIHelper.isJAIused())
                    JAIHelper.confirmJAIOnClasspath();

                boolean isURL = selectedFile.startsWith("http:") || selectedFile.startsWith("file:");

                if(isTiff && JAIHelper.isJAIused()){
                    try {

                        tiffHelper=new JAITiffHelper(commonValues.getSelectedFile());
                        int pageCount=tiffHelper.getTiffPageCount();

                        //Default to first page
                        tiffImageToLoad = 0;

                        //Multiple pages held within Tiff
                        if(pageCount>1){
                            //Set page count
                            decode_pdf.resetForNonPDFPage(pageCount);
                            commonValues.setPageCount(pageCount);
                            lastPageDecoded=1;
                            //Flag to show this is a Tiff with multiple pages
                            commonValues.setMultiTiff(true);
                            currentGUI.setMultiPageTiff(true);

                        }

                        drawMultiPageTiff();

                    } catch (Exception e) {
                        e.printStackTrace();
                        LogWriter.writeLog("Exception " + e + Messages.getMessage("PdfViewerError.Loading") + commonValues.getSelectedFile());
                    }
                }else{
                    String propValue = properties.getValue("showtiffmessage");
                    boolean showTiffMessage = isTiff && (propValue.length()>0 && propValue.equals("true"));

                    if (showTiffMessage) {
                        JPanel panel = new JPanel();
                        panel.setLayout(new GridBagLayout());

                        final GridBagConstraints p = new GridBagConstraints();
                        p.anchor = GridBagConstraints.WEST;
                        p.gridx = 0;
                        p.gridy = 0;

                        String str = "<html>Some Tiff images do not display correctly without JAI support turned on.  "
                                + "<br>See <a href=\"http://www.idrsolutions.com/jvm-flags\"> http://www.idrsolutions.com/jvm-flags"
                                + "</a> for information on enabling JAI.";

                        JCheckBox cb = new JCheckBox();
                        cb.setText(Messages.getMessage("PdfViewerFormsWarning.CheckBox"));

                        Font font = cb.getFont();
                        JEditorPane ta = new JEditorPane();
                        ta.addHyperlinkListener(new HyperlinkListener() {
                            public void hyperlinkUpdate(HyperlinkEvent e) {
                                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                                    try {
                                        BrowserLauncher.openURL(e.getURL()
                                                .toExternalForm());
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });

                        ta.setEditable(false);
                        ta.setContentType("text/html");
                        ta.setText(str);
                        ta.setOpaque(false);
                        ta.setFont(font);

                        p.ipady = 20;
                        panel.add(ta, p);
                        p.ipady = 0;
                        p.gridy = 1;
                        panel.add(cb, p);

                        JOptionPane.showMessageDialog(currentGUI.getFrame(), panel);
                        if (cb.isSelected())
                            properties.setValue("showtiffmessage", "false");
                    }
                    try {
                        // Load the source image from a file.
                        if (isURL)
                            img = ImageIO.read(new URL(selectedFile));
                        else{
                            img = ImageIO.read(new File(selectedFile));
                        }
                    } catch (Exception e) {
                        LogWriter.writeLog("Exception " + e + "loading " + commonValues.getSelectedFile());
                    }

                }
            }
            //<<>>
            currentGUI.updateStatusMessage("opening file");

            /** popup window if needed */
            if ((fileCanBeOpened)&&(decode_pdf.isEncrypted()) && (!decode_pdf.isFileViewable())) {
                fileCanBeOpened = false;


                String password = System.getProperty("org.jpedal.password");
                if(password==null)
                password=currentGUI.showInputDialog(Messages.getMessage("PdfViewerPassword.message")); //$NON-NLS-1$

                /** try and reopen with new password */
                if (password != null) {
                    decode_pdf.setEncryptionPassword(password);
                    //decode_pdf.verifyAccess();

                    if (decode_pdf.isFileViewable())
                        fileCanBeOpened = true;

                }

                if(!fileCanBeOpened)
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPasswordRequired.message"));

            }

//			currentGUI.reinitialiseTabs();

            if (fileCanBeOpened) {

                if (properties.getValue("Recentdocuments").equals("true")) {
                    properties.addRecentDocument(commonValues.getSelectedFile());
                    updateRecentDocuments(properties.getRecentDocuments());
                }

                recent.addToFileList(commonValues.getSelectedFile());

                /** reset values */
                commonValues.setCurrentPage(1);
            }


        } catch (PdfException e) {
            System.err.println(("Exception " + e + " opening file"));

            if(currentGUI.isSingle()) {

                if(Viewer.showMessages)
                    ErrorDialog.showError(e,Messages.getMessage("PdfViewerOpenerror"),currentGUI.getFrame(),commonValues.getSelectedFile());

                if(Viewer.exitOnClose)
                    System.exit(1);
                else{
                    currentGUI.getFrame().setVisible(false);
                    if(currentGUI.getFrame() instanceof JFrame)
                        ((JFrame)currentGUI.getFrame()).dispose();
                }
            }

            throw e;
        }

        if(!decode_pdf.isOpen() && commonValues.isPDF() && decode_pdf.getJPedalObject(PdfDictionary.Linearized)==null)
            return false;
        else
            return fileCanBeOpened;
    }

	/**
	 *  checks file can be opened (permission)
	 * @throws PdfException 
	 */
	public void openFile(String selectedFile) throws PdfException {

		isPDf=false;
        currentGUI.setMultiPageTiff(false);

		//get any user set dpi
        String hiresFlag = System.getProperty("org.jpedal.hires");
		if(Commands.hires || hiresFlag != null)
			commonValues.setUseHiresImage(true);

		//get any user set dpi
		String memFlag=System.getProperty("org.jpedal.memory");
		if(memFlag!=null){
			commonValues.setUseHiresImage(false);
		}

		//reset flag
		thumbnails.resetToDefault();

		//flush forms list
		currentGUI.setNoPagesDecoded();

		//remove search frame if visible
		if(searchFrame!=null)
			searchFrame.removeSearchWindow(false);

		commonValues.maxViewY=0;// rensure reset for any viewport
		String ending=selectedFile.toLowerCase().trim();
		commonValues.setPDF(ending.endsWith(".pdf")||ending.endsWith(".fdf"));
        isPDf=ending.endsWith(".pdf")||ending.endsWith(".fdf");

		//switch off continous mode for images
		if(!commonValues.isPDF()){

			if (SwingUtilities.isEventDispatchThread()) {

			decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

    		} else {
    			final Runnable doPaintComponent = new Runnable() {

    				public void run() {
    					decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
    				}
    			};
    			SwingUtilities.invokeLater(doPaintComponent);
    		}
			//decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
            pages= ((SingleDisplay)decode_pdf.getExternalHandler(Options.Display));
        }
		currentGUI.setQualityBoxVisible(commonValues.isPDF());

		commonValues.setCurrentPage(1);
		
		//Thread t = new Thread(new Runnable(){
		
			//public void run() {
				try {
					boolean fileCanBeOpened = openUpFile(commonValues.getSelectedFile());

					if(fileCanBeOpened)
						processPage();
					else{
						currentGUI.setViewerTitle(Messages.getMessage("PdfViewer.NoFile"));
                        decode_pdf.getDynamicRenderer().flush();
                        pages.refreshDisplay();
                        currentGUI.zoom(false);
						commonValues.setPageCount(1);
						commonValues.setCurrentPage(1);
					}
				}catch(Exception e){
					e.printStackTrace(); 
					System.err.println(Messages.getMessage("PdfViewerError.Exception")+ ' ' + e + ' ' + Messages.getMessage("PdfViewerError.DecodeFile"));

				}
			//}
			
		//});
		//t.start();

		//commonValues.setProcessing(false);
	}


	/**
	 * decode and display selected page
	 */
	protected void processPage() {

		if (commonValues.isPDF() && ((decode_pdf.isOpen() || !commonValues.isPDF() || decode_pdf.getJPedalObject(PdfDictionary.Linearized)!=null))) {

            /**
			 * get PRODUCER and if OCR disable text printing
			 */
			PdfFileInformation currentFileInformation=decode_pdf.getFileInformationData();

			/**switch all on by default*/
			decode_pdf.setRenderMode(PdfDecoder.RENDERIMAGES+PdfDecoder.RENDERTEXT);

			String[] values=currentFileInformation.getFieldValues();
			String[] fields= PdfFileInformation.getFieldNames();

			/** holding all creators that produce OCR pdf's */
			String[] ocr = {"TeleForm","dgn2pdf"};

			for(int i=0;i<fields.length;i++){

				if((fields[i].equals("Creator"))||(fields[i].equals("Producer"))){

                    for (String anOcr : ocr) {

                        if (values[i].equals(anOcr)) {

                            decode_pdf.setRenderMode(PdfDecoder.RENDERIMAGES);

                        }
                    }

                    //track Abbyy and tell JPedal to redraw highlights
                    if(values[i].equals("ABBYY FineReader 8.0 Professional Edition")){
					    decode_pdf.setRenderMode(PdfDecoder.RENDERIMAGES+PdfDecoder.RENDERTEXT +PdfDecoder.OCR_PDF);
                    }
				}
            }

            boolean currentProcessingStatus= Values.isProcessing();
            Values.setProcessing(true);	//stops listeners processing spurious event

            Values.setProcessing(currentProcessingStatus);

		}

		/**special customisations for images*/
		if(commonValues.isPDF())
			commonValues.setPageCount(decode_pdf.getPageCount());
		else if(!commonValues.isMultiTiff()){
			commonValues.setPageCount(1);
			decode_pdf.useHiResScreenDisplay(true);
        }


		if(commonValues.getPageCount()<commonValues.getCurrentPage()){
			commonValues.setCurrentPage(commonValues.getPageCount());
			System.err.println(commonValues.getCurrentPage()+ " out of range. Opening on last page");
			LogWriter.writeLog(commonValues.getCurrentPage()+ " out of range. Opening on last page");
		}


		//values extraction mode,dpi of images, dpi of page as a factor of 72

		decode_pdf.setExtractionMode(PdfDecoder.TEXT, currentGUI.getScaling());

		/**
		 * update the display, including any rotation
		 */
		currentGUI.setPageNumber();

		currentGUI.resetRotationBox();

        if(commonValues.isPDF()){
			currentGUI.messageShown=false;
			currentGUI.decodePage(true);
		}else{
			//resize (ensure at least certain size)
			currentGUI.zoom(false);

			//add a border
			decode_pdf.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));

			decodeImage(true);

			Values.setProcessing(false);
		}
	}

	/** opens a pdf file and calls the display/decode routines */
	public void selectFile() {
		
		//remove search frame if visible
		if(searchFrame!=null)
			searchFrame.removeSearchWindow(false);

		/**
		 * create the file chooser to select the file
		 */
		final JFileChooser chooser= new JFileChooser(commonValues.getInputDir());
		chooser.setName("chooser");
		if(commonValues.getSelectedFile()!=null)
			chooser.setSelectedFile(new File(commonValues.getSelectedFile()));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		String[] pdf = new String[] { "pdf" };
		String[] fdf = new String[] { "fdf" };
		String[] png = new String[] { "png","tif","tiff","jpg","jpeg" };
		chooser.addChoosableFileFilter(new FileFilterer(png, "Images (Tiff, Jpeg,Png)"));
		chooser.addChoosableFileFilter(new FileFilterer(fdf, "fdf (*.fdf)"));
		chooser.addChoosableFileFilter(new FileFilterer(pdf, "Pdf (*.pdf)"));

		final int state = chooser.showOpenDialog(currentGUI.getFrame());

		final File file = chooser.getSelectedFile();

        /**
		 * decode
		 */
		if (file != null && state == JFileChooser.APPROVE_OPTION) {
			
			currentGUI.resetNavBar();

			String ext=file.getName().toLowerCase();
			boolean isValid=((ext.endsWith(".pdf"))||(ext.endsWith(".fdf"))||
					(ext.endsWith(".tif"))||(ext.endsWith(".tiff"))||
					(ext.endsWith(".png"))||
					(ext.endsWith(".jpg"))||(ext.endsWith(".jpeg")));

            if(isValid){
				/** save path so we reopen her for later selections */
				try {
					commonValues.setInputDir(chooser.getCurrentDirectory().getCanonicalPath());
					open(file.getAbsolutePath());

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}else{
				decode_pdf.repaint();
				currentGUI.showMessageDialog( Messages.getMessage("PdfViewer.NotValidPdfWarning"));
			}

		} else { //no file selected so redisplay old
			decode_pdf.repaint();
			currentGUI.showMessageDialog( Messages.getMessage("PdfViewerMessage.NoSelection"));
		}
	}

	private String selectURL() {

		String selectedFile = currentGUI.showInputDialog(Messages.getMessage("PdfViewerMessage.RequestURL"));

		//lose any spaces
		if(selectedFile!=null)
			selectedFile=selectedFile.trim();

		if ((selectedFile != null) && !selectedFile.trim().startsWith("http://") && !selectedFile.trim().startsWith("https://") && !selectedFile.trim().startsWith("file:/")) { //simon
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.URLMustContain"));
			selectedFile = null;
		}

		if(selectedFile!=null){
			boolean isValid = ((selectedFile.endsWith(".pdf"))
					|| (selectedFile.endsWith(".fdf")) || (selectedFile.endsWith(".tif"))
					|| (selectedFile.endsWith(".tiff")) || (selectedFile.endsWith(".png"))
					|| (selectedFile.endsWith(".jpg")) || (selectedFile.endsWith(".jpeg")));


			if (!isValid) {
				currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NotValidPdfWarning"));
				selectedFile=null;
			}
		}

		if(selectedFile!=null){

			commonValues.setSelectedFile(selectedFile);

			boolean failed=false;
			try {
				URL testExists=new URL(selectedFile);
				URLConnection conn=testExists.openConnection();

				if(conn.getContent()==null)
					failed=true;
			} catch (Exception e) {
				failed=true;
			}

			if(failed){
				selectedFile=null;
				currentGUI.showMessageDialog("URL "+selectedFile+ ' ' +Messages.getMessage("PdfViewerError.DoesNotExist"));
			}
		}

		//ensure immediate redraw of blank screen
		//decode_pdf.invalidate();
		//decode_pdf.repaint();

		/**
		 * decode
		 */
		if (selectedFile != null ) {
			try {

				commonValues.setFileSize(0);

				/** save path so we reopen her for later selections */
				//commonValues.setInputDir(new URL(commonValues.getSelectedFile()).getPath());

				currentGUI.setViewerTitle(null);

			} catch (Exception e) {
				System.err.println(Messages.getMessage("PdfViewerError.Exception")+ ' ' + e + ' ' +Messages.getMessage("PdfViewerError.GettingPaths"));
			}

			/**
			 * open the file
			 */
			if ((selectedFile != null) && (Values.isProcessing() == false)) {

				/**
				 * trash previous display now we are sure it is not needed
				 */
				//decode_pdf.repaint();

				/** if running terminate first */
				thumbnails.terminateDrawing();

				decode_pdf.flushObjectValues(true);

				//reset the viewableArea before opening a new file
				decode_pdf.resetViewableArea();

				currentGUI.stopThumbnails();

				if(!currentGUI.isSingle())
					openNewMultiplePage(commonValues.getSelectedFile());

				try {
					openFile(commonValues.getSelectedFile());
				} catch (PdfException e) {
				}
			}

		} else { //no file selected so redisplay old
			decode_pdf.repaint();
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
		}

		return selectedFile;
	}

//	/**move forward one page*/
//	private void forward(int count) {
//
//		//new page number
//		int updatedTotal=commonValues.getCurrentPage()+count;
//
//		/**
//		 * example code to show how to check if page is now available
//		 */
//		//if loading on linearized thread, see if we can actually display
//		if(!decode_pdf.isPageAvailable(updatedTotal)){
//			currentGUI.showMessageDialog("Page "+updatedTotal+" is not yet loaded");
//			return;
//		}
//
//		//Stop existing page decodes in pageFlow
//		if (decode_pdf.getDisplayView() == Display.PAGEFLOW) {
//			Display d = ((Display)decode_pdf.getExternalHandler(Options.Display));
//			d.stopGeneratingPage();
//			d.drawBorder();
//		}
//
//		if (!commonValues.isProcessing()) { //lock to stop multiple accesses
//
//			/**if in range update count and decode next page. Decoded pages are cached so will redisplay
//			 * almost instantly*/
//
//			if (updatedTotal <= commonValues.getPageCount()) {
//
//				if(commonValues.isMultiTiff()){
//
//					//Update page number and draw new page
//					tiffImageToLoad = (decode_pdf.getlastPageDecoded()-1) + count;
//					drawMultiPageTiff();
//
//					//Update Tiff page
//					commonValues.setCurrentPage(updatedTotal);
//					decode_pdf.setlastPageDecoded(tiffImageToLoad+1);
//					currentGUI.setPageNumber();
//
//					//Display new page
//					decode_pdf.repaint();
//
//				}else{
//					/**
//					 * adjust for double jump on facing
//					 */
//					if(decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){
//						if((updatedTotal & 1)==1){
//							if(updatedTotal<commonValues.getPageCount())
//								updatedTotal++;
//							else if(commonValues.getPageCount()-updatedTotal>1)
//								updatedTotal--;
//						}
//					}
//
//					/**
//					 * animate if using drag in facing
//					 */
//					if (count == 1 && decode_pdf.turnoverOn &&
//							decode_pdf.getPageCount() != 2 &&
//							currentGUI.getPageTurnScalingAppropriate() &&
//							decode_pdf.getDisplayView() == Display.FACING &&
//							updatedTotal/2 != commonValues.getCurrentPage()/2 &&
//							!decode_pdf.getPdfPageData().hasMultipleSizes() &&
//							!((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().getPageTurnAnimating()) {
//						float pageW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
//						float pageH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
//						if (decode_pdf.getPdfPageData().getRotation(1)%180==90) {
//							float temp = pageW;
//							pageW = pageH;
//							pageH = temp;
//						}
//
//						final Point corner = new Point();
//						corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)-pageW);
//						corner.y = (int)(decode_pdf.getInsetH()+pageH);
//
//						final Point cursor = new Point();
//						cursor.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)+pageW);
//						cursor.y = (int)(decode_pdf.getInsetH()+pageH);
//
//						final int newPage = updatedTotal;
//						Thread animation = new Thread() {
//							public void run() {
//								// Fall animation
//								int velocity = 1;
//
//								//ensure cursor is not outside expected range
//								if (cursor.x <= corner.x)
//									cursor.x = corner.x-1;
//
//								//Calculate distance required
//								double distX = (corner.x-cursor.x);
//
//								//Loop through animation
//								while (cursor.getX() >= corner.getX()) {
//
//									//amount to move this time
//									double xMove = velocity*distX*0.001;
//
//									//make sure always moves at least 1 pixel in each direction
//									if (xMove > -1)
//										xMove = -1;
//
//									cursor.setLocation(cursor.getX() + xMove, cursor.getY());
//									decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);
//
//									//Double speed til moving 32/frame
//									if (velocity < 32)
//										velocity = velocity*2;
//
//									//sleep til next frame
//									try {
//										Thread.currentThread().sleep(50);
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//
//								}
//
//								//change page
//								commonValues.setCurrentPage(newPage);
//								currentGUI.setPageNumber();
//								decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
//								currentGUI.decodePage(false);
//
//								//unlock corner drag
//								((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(false);
//
//								//hide turnover
//								decode_pdf.setUserOffsets(0, 0,org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
//							}
//						};
//						//lock corner drag
//						((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(true);
//
//						animation.start();
//					} else {
//						commonValues.setCurrentPage(updatedTotal);
//						currentGUI.setPageNumber();
//
//						if(decode_pdf.getDisplayView() == Display.CONTINUOUS ||decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING
//								||decode_pdf.getDisplayView() == Display.PAGEFLOW){
//
//							currentGUI.decodePage(false);
//
//							return ;
//						}
//
//						currentGUI.resetStatusMessage("Loading Page "+commonValues.getCurrentPage());
//						/**reset as rotation may change!*/
//						decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
//
//						//decode the page
//						if(commonValues.isPDF())
//							currentGUI.decodePage(false);
//
//						//if scaling to window reset screen to fit rotated page
//						//					if(currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
//						//					currentGUI.zoom();
//					}
//				}
//			}
//		}else
//			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
//
//	}
//
//
//
//	/** move back one page */
//	private void back(int count) {
//
//        int updatedTotal=commonValues.getCurrentPage()-count;
//
//        //if loading on linearized thread, see if we can actually display
//        if(!decode_pdf.isPageAvailable(updatedTotal)){
//            currentGUI.showMessageDialog("Page "+updatedTotal+" is not yet loaded");
//            return;
//        }
//
//        //Stop existing page decodes in pageFlow
//        if (decode_pdf.getDisplayView() == Display.PAGEFLOW) {
//            Display d = ((Display)decode_pdf.getExternalHandler(Options.Display));
//            d.stopGeneratingPage();
//            d.drawBorder();
//        }
//
//		if (!commonValues.isProcessing()) { //lock to stop multiple accesses
//
//			/**
//			 * if in range update count and decode next page. Decoded pages are
//			 * cached so will redisplay almost instantly
//			 */
//			if (updatedTotal >= 1) {
//
//				if(commonValues.isMultiTiff()){
//
//					//Update page number and draw new page
//					tiffImageToLoad = (decode_pdf.getlastPageDecoded()-1) - count;
//					drawMultiPageTiff();
//
//					//Update Tiff page
//					commonValues.setCurrentPage(updatedTotal);
//					decode_pdf.setlastPageDecoded(tiffImageToLoad+1);
//					currentGUI.setPageNumber();
//
//					//Display new page
//					decode_pdf.repaint();
//
//				}else{
//
//					/**
//					 * adjust for double jump on facing
//					 */
//					if(decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){
//						if((updatedTotal & 1)==1 && updatedTotal!=1)
//							updatedTotal--;
//                    }
//
//                    /**
//                     * animate if using drag in facing
//                     */
//                    if (count == 1 && decode_pdf.turnoverOn &&
//                            decode_pdf.getDisplayView() == Display.FACING &&
//                            currentGUI.getPageTurnScalingAppropriate() &&
//                            decode_pdf.getPageCount() != 2 &&
//                            (updatedTotal != commonValues.getCurrentPage()-1 || updatedTotal==1) &&
//                            !decode_pdf.getPdfPageData().hasMultipleSizes() &&
//                            !((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().getPageTurnAnimating()) {
//                        float pageW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
//                        float pageH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
//                        if (decode_pdf.getPdfPageData().getRotation(1)%180==90) {
//                            float temp = pageW;
//                            pageW = pageH;
//                            pageH = temp;
//                        }
//
//                        final Point corner = new Point();
//                        corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)+pageW);
//                        corner.y = (int)(decode_pdf.getInsetH()+pageH);
//
//                        final Point cursor = new Point();
//                        cursor.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)-pageW);
//                        cursor.y = (int)(decode_pdf.getInsetH()+pageH);
//
//                        final int newPage = updatedTotal;
//                        Thread animation = new Thread() {
//                            public void run() {
//                                // Fall animation
//                                int velocity = 1;
//
//                                //ensure cursor is not outside expected range
//                                if (cursor.x >= corner.x)
//                                    cursor.x = corner.x-1;
//
//                                //Calculate distance required
//                                double distX = (corner.x-cursor.x);
//
//                                //Loop through animation
//                                while (cursor.getX() <= corner.getX()) {
//
//                                    //amount to move this time
//                                    double xMove = velocity*distX*0.001;
//
//                                    //make sure always moves at least 1 pixel in each direction
//                                    if (xMove < 1)
//                                        xMove = 1;
//
//                                    cursor.setLocation(cursor.getX() + xMove, cursor.getY());
//                                    decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);
//
//                                    //Double speed til moving 32/frame
//                                    if (velocity < 32)
//                                        velocity = velocity*2;
//
//                                    //sleep til next frame
//                                    try {
//                                        Thread.currentThread().sleep(50);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//
//                                //change page
//                                commonValues.setCurrentPage(newPage);
//                                currentGUI.setPageNumber();
//                                decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
//                                currentGUI.decodePage(false);
//
//                                //hide turnover
//                                decode_pdf.setUserOffsets(0, 0,org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
//
//                                //Unlock corner drag
//                                ((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(false);
//                            }
//                        };
//                        //lock corner drag
//                        ((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(true);
//                                                
//                        animation.start();
//                    } else {
//                        commonValues.setCurrentPage(updatedTotal);
//                        currentGUI.setPageNumber();
//
//
//                        if(decode_pdf.getDisplayView() == Display.CONTINUOUS ||
//                                decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING ||
//                                decode_pdf.getDisplayView() == Display.PAGEFLOW){
//
//                            currentGUI.decodePage(false);
//
//                            return ;
//                        }
//
//                        currentGUI.resetStatusMessage("loading page "+commonValues.getCurrentPage());
//
//                        /** reset as rotation may change! */
//                        decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
//
//                        //would reset scaling on page change to default
//                        //currentGUI.setScalingToDefault(); //set to 100%
//
//                        if(commonValues.isPDF())
//                            currentGUI.decodePage(false);
//
//                        //if scaling to window reset screen to fit rotated page
//                        //if(currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
//                        //	currentGUI.zoom();
//                    }
//				}
//			}
//		}else
//			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
//		
//	}
	
	/**
	 * Change the viewed page by count from the current page.
	 * Positive values will move page forwards.
	 * Negative values will move page backwards
	 * 
	 * @param count :: Next page to be displayed is CurrentPage + count
	 */
	private void navigatePages(int count) {
		
		if(count==0)
			return;
		
		//new page number
		int updatedTotal=commonValues.getCurrentPage()+count;


		if(count>0){
			/**
			 * example code to show how to check if page is now available
			 */
			//if loading on linearized thread, see if we can actually display
			if(!decode_pdf.isPageAvailable(updatedTotal)){
				currentGUI.showMessageDialog("Page "+updatedTotal+" is not yet loaded");
				return;
			}

			if (!Values.isProcessing()) { //lock to stop multiple accesses

				/**if in range update count and decode next page. Decoded pages are cached so will redisplay
				 * almost instantly*/

				if (updatedTotal <= commonValues.getPageCount()) {

					if(commonValues.isMultiTiff()){

						//Update page number and draw new page
						tiffImageToLoad = (lastPageDecoded-1) + count;
						drawMultiPageTiff();

						//Update Tiff page
						commonValues.setCurrentPage(updatedTotal);
						lastPageDecoded=tiffImageToLoad+1;
						currentGUI.setPageNumber();

						//Display new page
						decode_pdf.repaint();

					}else{
						/**
						 * adjust for double jump on facing
						 */
						if(decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){
							if (pages.getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() != Display.FACING) {
                                updatedTotal++;

                                if(updatedTotal>commonValues.getPageCount())
                                    updatedTotal = commonValues.getPageCount();

                                if((updatedTotal & 1)==1 && updatedTotal!=1)
                                    updatedTotal--;

                                if (decode_pdf.getDisplayView() == Display.FACING)
                                    count = ((updatedTotal)/2) - ((commonValues.getCurrentPage())/2);
                            } else {
                                updatedTotal++;

                                if ((updatedTotal & 1)==0)
                                    updatedTotal--;

                                count = ((updatedTotal+1)/2) - ((commonValues.getCurrentPage()+1)/2);
                            }
						}

						/**
						 * animate if using drag in facing
						 */
						if (count == 1 && decode_pdf.getDisplayView() == Display.FACING &&
        						pages.getBoolean(Display.BoolValue.TURNOVER_ON) &&
								decode_pdf.getPageCount() != 2 &&
								currentGUI.getPageTurnScalingAppropriate() &&
								updatedTotal/2 != commonValues.getCurrentPage()/2 &&
								!decode_pdf.getPdfPageData().hasMultipleSizes() &&
								!((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().getPageTurnAnimating()) {
							float pageW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
							float pageH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
							if (decode_pdf.getPdfPageData().getRotation(1)%180==90) {
								float temp = pageW;
								pageW = pageH;
								pageH = temp;
							}

							final Point corner = new Point();
							corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)-pageW);
							corner.y = (int)(decode_pdf.getInsetH()+pageH);

							final Point cursor = new Point();
							cursor.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)+pageW);
							cursor.y = (int)(decode_pdf.getInsetH()+pageH);

							final int newPage = updatedTotal;
							Thread animation = new Thread() {
								public void run() {
									// Fall animation
									int velocity = 1;

									//ensure cursor is not outside expected range
									if (cursor.x <= corner.x)
										cursor.x = corner.x-1;

									//Calculate distance required
									double distX = (corner.x-cursor.x);

									//Loop through animation
									while (cursor.getX() >= corner.getX()) {

										//amount to move this time
										double xMove = velocity*distX*0.001;

										//make sure always moves at least 1 pixel in each direction
										if (xMove > -1)
											xMove = -1;

										cursor.setLocation(cursor.getX() + xMove, cursor.getY());
										decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);

										//Double speed til moving 32/frame
										if (velocity < 32)
											velocity = velocity*2;

										//sleep til next frame
										try {
											Thread.sleep(50);
										} catch (Exception e) {
											e.printStackTrace();
										}

									}

									//change page
									commonValues.setCurrentPage(newPage);
									//currentGUI.setPageNumber();
									decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
									currentGUI.decodePage(false);

									//unlock corner drag
									((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(false);

									//hide turnover
									decode_pdf.setUserOffsets(0, 0,org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
								}
							};
							//lock corner drag
							((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(true);

							animation.start();
						} else {
							commonValues.setCurrentPage(updatedTotal);
//							currentGUI.setPageNumber();

							if(decode_pdf.getDisplayView() == Display.CONTINUOUS ||
                                    decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){

								currentGUI.decodePage(false);

								return ;
							}

							currentGUI.resetStatusMessage("Loading Page "+commonValues.getCurrentPage());
							/**reset as rotation may change!*/
							decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());

							//decode the page
							if(commonValues.isPDF())
								currentGUI.decodePage(false);

							//if scaling to window reset screen to fit rotated page
							//						if(currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
							//						currentGUI.zoom();
						}
					}
				}
			}else
				currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));

		}else{
			//if loading on linearized thread, see if we can actually display
			if(!decode_pdf.isPageAvailable(updatedTotal)){
				currentGUI.showMessageDialog("Page "+updatedTotal+" is not yet loaded");
				return;
			}

			if (!Values.isProcessing()) { //lock to stop multiple accesses

				/**
				 * if in range update count and decode next page. Decoded pages are
				 * cached so will redisplay almost instantly
				 */
				if (updatedTotal >= 1) {

					if(commonValues.isMultiTiff()){

						//Update page number and draw new page
						tiffImageToLoad = (lastPageDecoded-1) + count;
						drawMultiPageTiff();

						//Update Tiff page
						commonValues.setCurrentPage(updatedTotal);
						lastPageDecoded=tiffImageToLoad+1;
						currentGUI.setPageNumber();

						//Display new page
						decode_pdf.repaint();

					}else{

						/**
						 * adjust for double jump on facing
						 */
						if(decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){
							if (pages.getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() != Display.FACING) {
                                if (count == -1)
                                    updatedTotal--;

                                if (updatedTotal < 1)
                                    updatedTotal = 1;

                                if((updatedTotal & 1)==1 && updatedTotal!=1)
                                    updatedTotal--;

                                if (decode_pdf.getDisplayView() == Display.FACING)
                                    count = ((updatedTotal)/2) - ((commonValues.getCurrentPage())/2);
                            } else {
                                if ((updatedTotal & 1)==0)
                                    updatedTotal--;

                                if (decode_pdf.getDisplayView() == Display.FACING)
                                    count = ((updatedTotal+1)/2) - ((commonValues.getCurrentPage()+1)/2);
                            }
						}

						/**
						 * animate if using drag in facing
						 */
						if (count == -1 && decode_pdf.getDisplayView() == Display.FACING &&
        						pages.getBoolean(Display.BoolValue.TURNOVER_ON) &&
								currentGUI.getPageTurnScalingAppropriate() &&
								decode_pdf.getPageCount() != 2 &&
								(updatedTotal != commonValues.getCurrentPage()-1 || updatedTotal==1) &&
								!decode_pdf.getPdfPageData().hasMultipleSizes() &&
								!((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().getPageTurnAnimating()) {
							float pageW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
							float pageH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
							if (decode_pdf.getPdfPageData().getRotation(1)%180==90) {
								float temp = pageW;
								pageW = pageH;
								pageH = temp;
							}

							final Point corner = new Point();
							corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)+pageW);
							corner.y = (int)(decode_pdf.getInsetH()+pageH);

							final Point cursor = new Point();
							cursor.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)-pageW);
							cursor.y = (int)(decode_pdf.getInsetH()+pageH);

							final int newPage = updatedTotal;
							Thread animation = new Thread() {
								public void run() {
									// Fall animation
									int velocity = 1;

									//ensure cursor is not outside expected range
									if (cursor.x >= corner.x)
										cursor.x = corner.x-1;

									//Calculate distance required
									double distX = (corner.x-cursor.x);

									//Loop through animation
									while (cursor.getX() <= corner.getX()) {

										//amount to move this time
										double xMove = velocity*distX*0.001;

										//make sure always moves at least 1 pixel in each direction
										if (xMove < 1)
											xMove = 1;

										cursor.setLocation(cursor.getX() + xMove, cursor.getY());
										decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);

										//Double speed til moving 32/frame
										if (velocity < 32)
											velocity = velocity*2;

										//sleep til next frame
										try {
											Thread.sleep(50);
										} catch (Exception e) {
											e.printStackTrace();
										}

									}

									//change page
									commonValues.setCurrentPage(newPage);
//									currentGUI.setPageNumber();
									decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
									currentGUI.decodePage(false);

									//hide turnover
									decode_pdf.setUserOffsets(0, 0,org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);

									//Unlock corner drag
									((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(false);
								}
							};
							//lock corner drag
							((DefaultActionHandler)decode_pdf.getFormRenderer().getActionHandler()).getSwingMouseHandler().setPageTurnAnimating(true);

							animation.start();
						} else {
							commonValues.setCurrentPage(updatedTotal);
//							currentGUI.setPageNumber();


							if(decode_pdf.getDisplayView() == Display.CONTINUOUS ||
									decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){

								currentGUI.decodePage(false);

								return ;
							}

							currentGUI.resetStatusMessage("loading page "+commonValues.getCurrentPage());

							/** reset as rotation may change! */
							decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());

							//would reset scaling on page change to default
							//currentGUI.setScalingToDefault(); //set to 100%

							if(commonValues.isPDF())
								currentGUI.decodePage(false);

							//if scaling to window reset screen to fit rotated page
							//if(currentGUI.getSelectedComboIndex(Commands.SCALING)<3)
							//	currentGUI.zoom();
						}
					}
				}
			}else
				currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));

		}
		
		//After changing page, ensure buttons are updated, redundent buttons are hidden
        currentGUI.hideRedundentNavButtons();
	}

	public void gotoPage(String page) {
		int newPage;

        page = page.split("/")[0];

		//allow for bum values
		try{
			newPage=Integer.parseInt(page);

            //if loading on linearized thread, see if we can actually display
            if(!decode_pdf.isPageAvailable(newPage)){
                currentGUI.showMessageDialog("Page "+newPage+" is not yet loaded");
                currentGUI.pageCounter2.setText(String.valueOf(commonValues.getCurrentPage()));
                return;
            }

            /**
			 * adjust for double jump on facing
			 */
			if(decode_pdf.getDisplayView() == Display.FACING || decode_pdf.getDisplayView() == Display.CONTINUOUS_FACING){
				if((pages.getBoolean(Display.BoolValue.SEPARATE_COVER) || decode_pdf.getDisplayView() != Display.FACING) && (newPage & 1)==1 && newPage!=1){
					newPage--;
				} else if (!pages.getBoolean(Display.BoolValue.SEPARATE_COVER) && (newPage & 1)==0) {
                    newPage--;
                }
			}

			//allow for invalid value
			if ((newPage > decode_pdf.getPageCount()) | (newPage < 1)) {

				currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPageLabel.text")+ ' ' +
						page+ ' ' +Messages.getMessage("PdfViewerOutOfRange.text")+ ' ' +decode_pdf.getPageCount());

				newPage=commonValues.getCurrentPage();



				currentGUI.setPageNumber();
			}
			
		}catch(Exception e){
			currentGUI.showMessageDialog('>' +page+ "< "+Messages.getMessage("PdfViewerInvalidNumber.text"));
			newPage=commonValues.getCurrentPage();
			currentGUI.pageCounter2.setText(String.valueOf(commonValues.getCurrentPage()));
		}

		navigatePages(newPage-commonValues.getCurrentPage());

        if (decode_pdf.getDisplayView() == Display.PAGEFLOW)
        	navigatePages(0);

	}


	private void open(final String file) {
		currentGUI.resetNavBar();


		boolean isURL = file.startsWith("http:") || file.startsWith("file:");
		try {

			if(!isURL){
				fileIsURL=false;
				commonValues.setFileSize(new File(file).length() >> 10);
			}else
				fileIsURL=true;

			commonValues.setSelectedFile(file);
			currentGUI.setViewerTitle(null);

		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " getting paths");
		}

		/** check file exists */
		File testFile = new File(commonValues.getSelectedFile());
		if (!isURL && !testFile.exists()) {
			currentGUI.showMessageDialog(Messages.getMessage("PdfViewerFile.text") + commonValues.getSelectedFile() + Messages.getMessage("PdfViewerNotExist"));

			/** open the file*/
		} else if ((commonValues.getSelectedFile() != null) && (Values.isProcessing() == false)) {

			if(currentGUI.isSingle())
				decode_pdf.flushObjectValues(true);
			else
				decode_pdf=openNewMultiplePage(commonValues.getSelectedFile());

			//reset the viewableArea before opening a new file
			decode_pdf.resetViewableArea();
			/**/

            try {
                openFile(commonValues.getSelectedFile());
            } catch (PdfException e) {
            }
		}
	}

	public void updateRecentDocuments(String[] recentDocs) {
		if(recentDocs == null)
			return;

		for(int i=0; i<recentDocs.length;i++){
			if(recentDocs[i] != null){

                String shortenedFileName = RecentDocuments.getShortenedFileName(recentDocs[i]);

                if(recentDocuments[i]==null)
                recentDocuments[i]=new JMenuItem();

                recentDocuments[i].setText(i+1 + ": " + shortenedFileName);
				if(recentDocuments[i].getText().equals(i+1 + ": "))
					recentDocuments[i].setVisible(false);
				else
					recentDocuments[i].setVisible(true);
				recentDocuments[i].setName(recentDocs[i]);
			}
		}
	}
	
	public void enableRecentDocuments(boolean enable) {
		if(recentDocuments == null)
			return;

		for(int i=0; i<recentDocuments.length;i++){
			if(recentDocuments[i]!=null && !recentDocuments[i].getText().equals(i+1 + ": ")){
				recentDocuments[i].setVisible(enable);
				recentDocuments[i].setEnabled(enable);
			}
		}
	}

	private void drawMultiPageTiff(){

		if(tiffHelper!=null){
            img=tiffHelper.getImage(tiffImageToLoad);

            if(img!=null){
                /** flush any previous pages */
                decode_pdf.getDynamicRenderer().flush();
                pages.refreshDisplay();

                addImage(img);
            }
        }
	}

    /**
     * used to display non-PDF files
     */
    public void addImage(BufferedImage img) {
        GraphicsState gs = new GraphicsState();
        gs.CTM[0][0] = img.getWidth();
        gs.CTM[1][1] = img.getHeight();

        decode_pdf.getDynamicRenderer().drawImage(1, img, gs, false, "image", PDFImageProcessing.NOTHING, -1);

        //Set size for the given page as each page can be a different size
        if(currentGUI.isMultiPageTiff()){

            if (img != null)
                decode_pdf.getPdfPageData().setMediaBox(new float[]{0,0,img.getWidth(),img.getHeight()});

            decode_pdf.getPdfPageData().checkSizeSet(1);
        }
    }



	public void recentDocumentsOption(JMenu file) {
		String[] recentDocs=properties.getRecentDocuments();
		if(recentDocs == null)
			return;

		for(int i=0;i<noOfRecentDocs;i++){

			if(recentDocs[i]==null)
				recentDocs[i]="";

			try{
				String fileNameToAdd=recentDocs[i];
				String shortenedFileName = RecentDocuments.getShortenedFileName(fileNameToAdd);
	
				recentDocuments[i] = new JMenuItem(i+1 + ": " + shortenedFileName);
				if(recentDocuments[i].getText().equals(i+1 + ": "))
					recentDocuments[i].setVisible(false);
				
				recentDocuments[i].setName(fileNameToAdd);
				recentDocuments[i].addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
	
						if(Printer.isPrinting())
							currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPrintWait.message"));
						else if(Values.isProcessing() || isOpening)
							currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
						else{
							/**
							 * warn user on forms
							 */
							handleUnsaveForms();

							JMenuItem item = (JMenuItem)e.getSource();
							String fileName = item.getName();
	
							if (fileName.length() != 0)
								open(fileName);
						}
					}
				});
				file.add(recentDocuments[i]);
			}catch(Exception ee){
				LogWriter.writeLog("Problem with file "+recentDocs[i] );
			}
		}
	}



	private void saveFile() {	

		/**
		 * create the file chooser to select the file
		 */
		File file;
		String fileToSave;
		boolean finished=false;

		while(!finished){
			JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
			chooser.setSelectedFile(new File(commonValues.getInputDir()+ '/' +commonValues.getSelectedFile()));
			chooser.addChoosableFileFilter(new FileFilterer(new String[]{"pdf"}, "Pdf (*.pdf)"));
			chooser.addChoosableFileFilter(new FileFilterer(new String[]{"fdf"}, "fdf (*.fdf)"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			//set default name to current file name 
			int approved=chooser.showSaveDialog(null);
			if(approved==JFileChooser.APPROVE_OPTION){

				FileInputStream fis=null;
				FileOutputStream fos=null;

				file = chooser.getSelectedFile();
				fileToSave=file.getAbsolutePath();

				if(!fileToSave.endsWith(".pdf")){
					fileToSave += ".pdf";
					file=new File(fileToSave);
				}

				if(fileToSave.equals(commonValues.getSelectedFile()))
					return;

				if(file.exists()){
					int n=currentGUI.showConfirmDialog(fileToSave+ '\n' +
							Messages.getMessage("PdfViewerMessage.FileAlreadyExists")+ '\n' +
							Messages.getMessage("PdfViewerMessage.ConfirmResave"),
							Messages.getMessage("PdfViewerMessage.Resave"),JOptionPane.YES_NO_OPTION);
					if(n==1)
						continue;
				}

				try {
					fis=new FileInputStream(commonValues.getSelectedFile());
					fos=new FileOutputStream(fileToSave);

					byte[] buffer=new byte[4096];
					int bytes_read;

					while((bytes_read=fis.read(buffer))!=-1)
						fos.write(buffer,0,bytes_read);
				} catch (Exception e1) {

					//e1.printStackTrace();
					currentGUI.showMessageDialog(Messages.getMessage("PdfViewerException.NotSaveInternetFile"));
				}

				try{
					fis.close();
					fos.close();
				} catch (Exception e2) {
					//e2.printStackTrace();
				}

				finished=true;
			}else{
				return;
			}
		}
	}

	/**Clean up and exit program*/
	private void exit() {

		thumbnails.terminateDrawing();

		//<start-wrap>
		/**
		 * warn user on forms
		 */
		handleUnsaveForms();
		//<end-wrap>

        //MARK - idea would it be good to link the cross for closing the window to this command, so user can also cancel if accidental.

        //<start-wrap>
		/**
		 * create the dialog
		 */
//		currentGUI.showConfirmDialog(new JLabel(Messages.getMessage("PdfViewerExiting")),
//				Messages.getMessage("PdfViewerprogramExit"), 
//				JOptionPane.DEFAULT_OPTION,
//				JOptionPane.PLAIN_MESSAGE);

          //<end-wrap>
		/**cleanup*/
		decode_pdf.closePdfFile();
		
		//needed to save recent files
		try {
			properties.setValue("lastDocumentPage", String.valueOf(commonValues.getCurrentPage()));
			//properties.writeDoc();
		} catch (Exception e1) {
		}
		
		//formClickTest needs this so that it does not exit after first test.
        if(!Viewer.exitOnClose){/**/
        	currentGUI.getFrame().setVisible(false);
        	if(currentGUI.getFrame() instanceof JFrame)
        		((JFrame)currentGUI.getFrame()).dispose();
        }else{
        	System.exit(1);
        }
    }
	
	public boolean checkForUpdates(boolean showMessages) {
		boolean connectionSuccessful = true;
		boolean wasUpdateAvailable = false;

		try {

            String version=null;

             version="http://www.jpedal.org/version.txt";
            /**/

            //<start-demo>
			/**
            //<end-demo>
            version="http://www.jpedal.org/demo_version.txt";
            /**/

            if(version!=null){
                //read the available version from a file on the server
                URL versionFile = new URL(version);
                URLConnection connection = versionFile.openConnection();

                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                String availableVersion = in.readLine();
                in.close();

                // read the available version from a file on the server
                availableVersion = availableVersion.substring(0, 7);

                String currentVersion = PdfDecoder.version.substring(0, 7);

                //Parse build strings and work out if newer version available
                boolean updateAvailable = false;
                String[] available = availableVersion.split("b");
                String[] current = currentVersion.split("b");
                if (Double.parseDouble(available[0])>Double.parseDouble(current[0])   //Check version number
                        || available[0].equals(current[0]) && Integer.parseInt(available[1])>Integer.parseInt(current[1])) //Check build
                        updateAvailable = true;

                //System.out.println(currentVersion+" "+availableVersion);
                if (updateAvailable) { // we have a later version
                    UpdateDialog updateDialog = new UpdateDialog(currentGUI.getFrame(), currentVersion, availableVersion);
                    updateDialog.setVisible(true);
                    wasUpdateAvailable = true;
                } else { // the current version is up to date
                    if(showMessages) { // want to display this fact to the user
                        currentGUI.showMessageDialog("The current version is up to date", "Up to date", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
		} catch (Exception e) {
			connectionSuccessful = false;
		} finally {
			if(!connectionSuccessful && showMessages){
				currentGUI.showMessageDialog("Error making connection so unable to check for updates", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

//		System.out.println(connectionSuccessful+" ><");
		return wasUpdateAvailable;
	}

	public void setPdfDecoder(PdfDecoder decode_pdf) {
		this.decode_pdf = decode_pdf;
	}

	public void setPageProperties(Object rotation, Object scaling) {
		if(multiViewListener!=null)
			multiViewListener.setPageProperties(rotation, scaling);

	}

	public void clearRecentDocuments() {
		properties.removeRecentDocuments();

		for (int i = 0; i < noOfRecentDocs; i++) {
			recentDocuments[i].setText(i+1 + ": ");
			recentDocuments[i].setVisible(false);
		}
	}

	/**
	 * Returns the searchList of the last search preformed.
	 * If currently searching this method will return the results for last completed search
	 * @return SearchList of all results, all data but the actual highlgiht areas
	 */
	public SearchList getSearchList() {
			return searchFrame.getResults();
	}

    public MouseMode getMouseMode() {
        return mouseMode;
    }

//############viewStack ########

    /** used to store the IE views so we can go back to previous views and store changes */
    protected ViewStack viewStack = new ViewStack();

    public void addAView(int page, Rectangle location, Integer scalingType) {
        //if we want to start storing the zoom all we need to do is add the type to Viewable and replace the type in changeTo call
        viewStack.add(page,location,scalingType);
    }

    /**
     * the view stack type object that allows us to store views as they change
     * and then go back through them as needed.
     *
     * @author Chris Wade
     */
    protected class ViewStack {

        private ArrayList ourStack = new ArrayList();
        private int index = -1;
        private int length = 0;

        protected Viewable back(){

            if(index-1>-1 && index-1<length){
                index--;
                return (Viewable)ourStack.get(index);
            }else
                return null;
        }

        protected Viewable forward(){

            if(index+1>-1 && index+1<length){
                index++;
                return (Viewable) ourStack.get(index);
            }else
                return null;
        }

        protected synchronized void add(int page, Rectangle location, Integer scalingType){
            //check capacity will take the new object and location +1 and +1 for the length.
            ourStack.ensureCapacity(index+2);
            ourStack.add(index+1, new Viewable(page,location,scalingType));

            //set the index and length after to ensure correct runing if an exception
            index++;
            length = index+1;
        }

        /** a view of a defined page, Rectangle on page, and scalingtype */
        protected class Viewable {
            private int page;
            private Rectangle location;
            private Integer type;

            protected Viewable(int inPage, Rectangle rectangle,Integer inType) {
                page = inPage;
                location = rectangle;
                type = inType;
            }

            protected Rectangle getLocation(){
                return location;
            }

            protected int getPage(){
                return page;
            }

            protected Integer getType(){
                return type;
            }
        }
    }



	public void goBackAView() {
	}

	public void goForwardAView() {
	}

    public boolean isImageExtractionAllowed(){
        return ImageExtractionAllowed;
    }

    public void setImageExtractionAllowed(boolean allow){
        ImageExtractionAllowed = allow;
    }

    public boolean isPDF() {
        return isPDf;
    }

    /**
     * examine first few bytes to see if linearized and return true linearized file
     * @param pdfUrl
     * @return
     * @throws PdfException
     */
    static final public boolean isPDFLinearized(String pdfUrl) throws PdfException {

        if (pdfUrl.startsWith("jar"))
            return false;

        boolean isLinear=false;
        //read first few bytes
        URL url;
        final InputStream is;

        try {
            url = new URL(pdfUrl);
            is = url.openStream();
            //final String filename = url.getPath().substring(url.getPath().lastIndexOf('/')+1);

            // Download buffer
            byte[] buffer = new byte[128];
            is.read(buffer);
            is.close();


        } catch (IOException e) {
            if(LogWriter.isOutput())
                LogWriter.writeLog("[PDF] Exception " + e + " scanning URL "+ pdfUrl);
            e.printStackTrace();
        }

        return isLinear;

    }

    public void scrollRectToHighlight(Rectangle highlight, int page) {

        int x = 0, y = 0, w = 0, h = 0;
        int insetW=decode_pdf.getInsetW();
        int insetH=decode_pdf.getInsetH();
        float scaling=decode_pdf.getScaling();
        int scrollInterval=decode_pdf.getScrollInterval();

        int displayView=decode_pdf.getDisplayView();
        if(page<1 || page>decode_pdf.getPageCount() || displayView==Display.SINGLE_PAGE){
            page=decode_pdf.pageNumber;
        }

        PdfPageData pageData=decode_pdf.getPdfPageData();
        int cropW = pageData.getCropBoxWidth(page);
        int cropH = pageData.getCropBoxHeight(page);
        int cropX = pageData.getCropBoxX(page);
        int cropY = pageData.getCropBoxY(page);

        switch (decode_pdf.displayRotation) {
            case 0:
                x = (int) ((highlight.x - cropX) * scaling) + insetW;
                y = (int) ((cropH - (highlight.y - cropY)) * scaling) + insetH;
                w = (int) (highlight.width * scaling);
                h = (int) (highlight.height * scaling);

                break;
            case 90:
                x = (int) ((highlight.y - cropY) * scaling) + insetH;
                y = (int) ((highlight.x - cropX) * scaling) + insetW;
                w = (int) (highlight.height * scaling);
                h = (int) (highlight.width * scaling);

                break;
            case 180:
                x = (int) ((cropW - (highlight.x - cropX)) * scaling) + insetW;
                y = (int) ((highlight.y - cropY) * scaling) + insetH;
                w = (int) (highlight.width * scaling);
                h = (int) (highlight.height * scaling);

                break;
            case 270:
                x = (int) ((cropH - (highlight.y - cropY)) * scaling) + insetH;
                y = (int) ((cropW - (highlight.x - cropX)) * scaling) + insetW;
                w = (int) (highlight.height * scaling);
                h = (int) (highlight.width * scaling);

                break;
        }

        if(displayView!=Display.SINGLE_PAGE && displayView!=Display.PAGEFLOW){
            x = x+pages.getXCordForPage(page);
            y = y+pages.getYCordForPage(page);
        }

        Rectangle visibleRect=decode_pdf.getVisibleRect();
        if(x>visibleRect.x+(visibleRect.width/2))
            x = x+((visibleRect.width/2)-(highlight.width/2));
        else
            x = x-((visibleRect.width/2)-(highlight.width/2));

        if(y>visibleRect.y+(visibleRect.height/2))
            y = y+((visibleRect.height/2)-(highlight.height/2));
        else
            y = y-((visibleRect.height/2)-(highlight.height/2));

        Rectangle scrollto = new Rectangle(x - scrollInterval, y - scrollInterval, w + scrollInterval * 2, h + scrollInterval * 2);

        decode_pdf.scrollRectToVisible(scrollto);
    }

}