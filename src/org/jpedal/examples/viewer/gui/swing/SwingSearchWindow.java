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
 * SwingSearchWindow.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.swing;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jpedal.PdfDecoder;
import org.jpedal.SingleDisplay;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.SwingGUI;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.DefaultSearchListener;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchListener;
import org.jpedal.grouping.SearchType;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;
import org.jpedal.utils.repositories.Vector_Rectangle;
                                    
/**provides interactive search Window and search capabilities*/
public class SwingSearchWindow extends JFrame implements GUISearchWindow{

	public static int SEARCH_EXTERNAL_WINDOW = 0;
	public static int SEARCH_TABBED_PANE = 1;
	public static int SEARCH_MENU_BAR = 2;

	private boolean backGroundSearch = false;
	
	int style = 0;

	/**flag to stop multiple listeners*/
	private boolean isSetup=false;

    boolean usingMenuBarSearch=false;

    int lastPage=-1;

	String defaultMessage="Search PDF Here";

	JProgressBar progress = new JProgressBar(0,100);
	int pageIncrement = 0;
	JTextField searchText=null;
	JTextField searchCount;
	DefaultListModel listModel;
	SearchList resultsList;
	JLabel label = null;

	private JPanel advancedPanel;
	private JComboBox searchType;
	private JCheckBox wholeWordsOnlyBox, caseSensitiveBox, multiLineBox, highlightAll, searchAll, useRegEx;
	
	public void setWholeWords(boolean wholeWords){
		wholeWordsOnlyBox.setSelected(wholeWords);
	}
	
	public void setCaseSensitive(boolean caseSensitive){
		caseSensitiveBox.setSelected(caseSensitive);
	}
	
	public void setMultiLine(boolean multiLine){
		multiLineBox.setSelected(multiLine);
	}
	
	public void setHighlightAll(boolean highlightAllOnPage){
		highlightAll.setSelected(highlightAllOnPage);
	}
	
	public void setRegularExpressionUsage(boolean RegEx){
		useRegEx.setSelected(RegEx);
	}
	
	ActionListener AL=null;
	ListSelectionListener LSL = null;
	WindowListener WL;
	KeyListener KL;

	/**swing thread to search in background*/
	SwingWorker searcher=null;

	/**flag to show searching taking place*/
	public boolean isSearch=false;
	
	/**Flag to show search has happened and needs reset*/
	public boolean hasSearched=false;
	
	public boolean requestInterupt=false;

	JButton searchButton=null;

	/**number fo search items*/
	private int itemFoundCount=0;

	/**used when fiding text to highlight on page*/
	Map textPages=new HashMap();
	Map textRectangles=new HashMap();

	/**Current Search value*/
	String[] searchTerms = {""};
	
	/**Search this page only*/
	boolean singlePageSearch = false;
	
	final JPanel nav=new JPanel();

	Values commonValues;
	SwingGUI currentGUI;
	PdfDecoder decode_pdf;
	
	int searchTypeParameters = SearchType.DEFAULT;
	
	int firstPageWithResults = 0;
	
	/**deletes message when user starts typing*/
	private boolean deleteOnClick;

	public SwingSearchWindow(SwingGUI currentGUI) {
		this.currentGUI=currentGUI;
		this.setName("searchFrame");
	}


	public Component getContentPanel(){
		return getContentPane();
	}
	
	public boolean isSearching(){
		return isSearch;
	}
	
	public void init(final PdfDecoder dec, final Values values){
		
		this.decode_pdf = dec;
		this.commonValues = values;
		
		if(isSetup){ //global variable so do NOT reinitialise
			searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound")+ ' ' +itemFoundCount);
			searchText.selectAll();
			searchText.grabFocus();
		}else{
			isSetup=true;

			setTitle(Messages.getMessage("PdfViewerSearchGUITitle.DefaultMessage"));

			defaultMessage=Messages.getMessage("PdfViewerSearchGUI.DefaultMessage");

			searchText=new JTextField(10);
			searchText.setText(defaultMessage);
			searchText.setName("searchText");

			searchButton=new JButton(Messages.getMessage("PdfViewerSearch.Button"));

			advancedPanel = new JPanel(new GridBagLayout());

			searchType = new JComboBox(new String[] {Messages.getMessage("PdfViewerSearch.MatchWhole"), 
					Messages.getMessage("PdfViewerSearch.MatchAny")});

			wholeWordsOnlyBox = new JCheckBox(Messages.getMessage("PdfViewerSearch.WholeWords"));
			wholeWordsOnlyBox.setName("wholeWords");

			caseSensitiveBox = new JCheckBox(Messages.getMessage("PdfViewerSearch.CaseSense"));
			caseSensitiveBox.setName("caseSensitive");

			multiLineBox = new JCheckBox(Messages.getMessage("PdfViewerSearch.MultiLine"));
			multiLineBox.setName("multiLine");
			
			highlightAll = new JCheckBox(Messages.getMessage("PdfViewerSearch.HighlightsCheckBox"));
			highlightAll.setName("highlightAll");

			useRegEx = new JCheckBox(Messages.getMessage("PdfViewerSearch.RegExCheckBox"));
			useRegEx.setName("useregex");
			
			searchType.setName("combo");

			GridBagConstraints c = new GridBagConstraints();

			advancedPanel.setPreferredSize(new Dimension(advancedPanel.getPreferredSize().width, 150));
			c.gridx = 0;
			c.gridy = 0;

			c.anchor = GridBagConstraints.PAGE_START;
			c.fill = GridBagConstraints.HORIZONTAL; 

			c.weightx = 1;
			c.weighty = 0;
			advancedPanel.add(new JLabel(Messages.getMessage("PdfViewerSearch.ReturnResultsAs")), c);

			c.insets = new Insets(5,0,0,0);
			c.gridy = 1;
			advancedPanel.add(searchType, c);

			c.gridy = 2;
			advancedPanel.add(new JLabel(Messages.getMessage("PdfViewerSearch.AdditionalOptions")), c);

			c.insets = new Insets(0,0,0,0);
			c.weighty = 1;
			c.gridy = 3;
			advancedPanel.add(wholeWordsOnlyBox, c);
			c.weighty = 1;
			c.gridy = 4;
			advancedPanel.add(caseSensitiveBox, c);

			c.weighty = 1;
			c.gridy = 5;
			advancedPanel.add(multiLineBox, c);
			
			c.weighty = 1;
			c.gridy = 6;
			advancedPanel.add(highlightAll, c);
			
			c.weighty = 1;
			c.gridy = 7;
			advancedPanel.add(useRegEx, c);

			advancedPanel.setVisible(false);

			nav.setLayout(new BorderLayout());

			WL = new WindowListener(){
				public void windowOpened(WindowEvent arg0) {}

				//flush objects on close
				public void windowClosing(WindowEvent arg0) {

					removeSearchWindow(true);
				}

				public void windowClosed(WindowEvent arg0) {}

				public void windowIconified(WindowEvent arg0) {}

				public void windowDeiconified(WindowEvent arg0) {}

				public void windowActivated(WindowEvent arg0) {}

				public void windowDeactivated(WindowEvent arg0) {}
			};

			this.addWindowListener(WL);

			nav.add(searchButton,BorderLayout.EAST);

			nav.add(searchText,BorderLayout.CENTER);

			searchAll=new JCheckBox();
			searchAll.setSelected(true);
			searchAll.setText(Messages.getMessage("PdfViewerSearch.CheckBox"));

			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add(searchAll, BorderLayout.NORTH);

			label = new JLabel("<html><center> " + "Show Advanced");
			label.setForeground(Color.blue);
			label.setName("advSearch");

			label.addMouseListener(new MouseListener() {
				boolean isVisible = false;

				String text = "Show Advanced";

				public void mouseEntered(MouseEvent e) {
					if(SingleDisplay.allowChangeCursor)
						nav.setCursor(new Cursor(Cursor.HAND_CURSOR));
					label.setText("<html><center><a href=" + text + '>' + text + "</a></center>");
				}

				public void mouseExited(MouseEvent e) {
					if(SingleDisplay.allowChangeCursor)
						nav.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					label.setText("<html><center>" + text);
				}

				public void mouseClicked(MouseEvent e) {
					if (isVisible) {
						text = Messages.getMessage("PdfViewerSearch.ShowOptions");
						label.setText("<html><center><a href=" + text + '>' + text + "</a></center>");
						advancedPanel.setVisible(false);
					} else {
						text = Messages.getMessage("PdfViewerSearch.HideOptions");
						label.setText("<html><center><a href=" + text + '>' + text + "</a></center>");
						advancedPanel.setVisible(true);
					}

					isVisible = !isVisible;
				}

				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
			});

			label.setBorder(BorderFactory.createEmptyBorder(3, 4, 4, 4));
			topPanel.add(label, BorderLayout.SOUTH);
			//			nav.

			nav.add(topPanel,BorderLayout.NORTH);
			itemFoundCount=0;
			textPages.clear();
			textRectangles.clear();
			listModel = null;

			searchCount=new JTextField(Messages.getMessage("PdfViewerSearch.ItemsFound")+ ' ' +itemFoundCount);
			searchCount.setEditable(false);
			nav.add(searchCount,BorderLayout.SOUTH);

			listModel = new DefaultListModel();
			resultsList=new SearchList(listModel,textPages, textRectangles);
			resultsList.setName("results");

			//<link><a name="search" />
			/**
			 * highlight text on item selected
			 */
			LSL = new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					/** 
					 * Only do something on mouse button up,
					 * prevents this code being called twice
					 * on mouse click
					 */
					if (!e.getValueIsAdjusting()) {

						if(!Values.isProcessing()){//{if (!event.getValueIsAdjusting()) {

							float scaling=currentGUI.getScaling();
							//int inset=currentGUI.getPDFDisplayInset();

							int id=resultsList.getSelectedIndex();

							decode_pdf.getTextLines().clearHighlights();
							//System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));

							if(id!=-1){

								Integer key= id;
								Object newPage=textPages.get(key);

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
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ee) {
                                            ee.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        }
									}



									/**
									 * Highlight all search results on page.
									 */
									if((searchTypeParameters & SearchType.HIGHLIGHT_ALL_RESULTS)== SearchType.HIGHLIGHT_ALL_RESULTS){

//										PdfHighlights.clearAllHighlights(decode_pdf);
										Rectangle[] showAllOnPage;
										Vector_Rectangle storageVector = new Vector_Rectangle();
										int lastPage = -1;
										for(int k=0; k!=resultsList.getModel().getSize(); k++){
											Object page=textPages.get(k);

											if(page!=null){

												int currentPage = (Integer) page;
												if(currentPage!=lastPage){
													storageVector.trim();
													showAllOnPage = storageVector.get();
													
													for(int p=0; p!=showAllOnPage.length; p++){
						                    			System.out.println(showAllOnPage[p]);
						                    		}
													
													decode_pdf.getTextLines().addHighlights(showAllOnPage, true, lastPage);
													lastPage = currentPage;
													storageVector = new Vector_Rectangle();
												}

												Object highlight= textRectangles.get(k);

												if(highlight instanceof Rectangle){
													storageVector.addElement((Rectangle)highlight);
												}
												if(highlight instanceof Rectangle[]){
													Rectangle[] areas = (Rectangle[])highlight;
													for(int i=0; i!=areas.length; i++){
														storageVector.addElement(areas[i]);
													}
												}
												//decode_pdf.addToHighlightAreas(decode_pdf, storageVector, currentPage);
//												}
											}
										}
										storageVector.trim();
										showAllOnPage = storageVector.get();
										
										decode_pdf.getTextLines().addHighlights(showAllOnPage, true, lastPage);
									}else{
//										PdfHighlights.clearAllHighlights(decode_pdf);
										Object page=textPages.get(key);
										int currentPage = (Integer) page;
										
										Vector_Rectangle storageVector = new Vector_Rectangle();
										Rectangle scroll = null;
										Object highlight= textRectangles.get(key);
										if(highlight instanceof Rectangle){
											storageVector.addElement((Rectangle)highlight);
											scroll=(Rectangle)highlight;
										}
										
										if(highlight instanceof Rectangle[]){
											Rectangle[] areas = (Rectangle[])highlight;
											scroll=areas[0];
											for(int i=0; i!=areas.length; i++){
												storageVector.addElement(areas[i]);
											}
										}
										currentGUI.currentCommands.scrollRectToHighlight(scroll,currentPage);
										storageVector.trim();
										decode_pdf.getTextLines().addHighlights(storageVector.get(), true, currentPage);
										//PdfHighlights.addToHighlightAreas(decode_pdf, storageVector, currentPage);
										
									}

									decode_pdf.invalidate();
									decode_pdf.repaint();
									currentGUI.zoom(false);
								}
							}
						}

						//When page changes make sure only relevant navigation buttons are displayed
						if(commonValues.getCurrentPage()==1)
							currentGUI.setBackNavigationButtonsEnabled(false);
						else
							currentGUI.setBackNavigationButtonsEnabled(true);

						if(commonValues.getCurrentPage()==decode_pdf.getPageCount())
							currentGUI.setForwardNavigationButtonsEnabled(false);
						else
							currentGUI.setForwardNavigationButtonsEnabled(true);


					}else{
						resultsList.repaint();

					}
				}
			};

			resultsList.addListSelectionListener(LSL);
			resultsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

			//setup searching
			//if(AL==null){
			AL = new ActionListener(){
				public void actionPerformed(ActionEvent e) {

					if(!isSearch){

						try {
							searchTypeParameters = SearchType.DEFAULT;

							if(wholeWordsOnlyBox.isSelected())
								searchTypeParameters |= SearchType.WHOLE_WORDS_ONLY;

							if(caseSensitiveBox.isSelected())
								searchTypeParameters |= SearchType.CASE_SENSITIVE;

							if(multiLineBox.isSelected())
								searchTypeParameters |= SearchType.MUTLI_LINE_RESULTS;
							
							if(highlightAll.isSelected())
								searchTypeParameters |= SearchType.HIGHLIGHT_ALL_RESULTS;
							
							if(useRegEx.isSelected())
								searchTypeParameters |= SearchType.USE_REGULAR_EXPRESSIONS;
							
							String textToFind = searchText.getText().trim();
							if(searchType.getSelectedIndex() == 0){ // find exact word or phrase
								searchTerms = new String[] { textToFind };
							} else { // match any of the words
								searchTerms = textToFind.split(" ");
								for (int i = 0; i < searchTerms.length; i++) {
									searchTerms[i] = searchTerms[i].trim();
								}
							}
							
							singlePageSearch = !searchAll.isSelected();
							
							searchText();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}else{
						requestInterupt = true;
						//searcher.interrupt();
						isSearch=false;
						searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
					}
					currentGUI.getPdfDecoder().requestFocusInWindow();
				}
			};

			searchButton.addActionListener(AL);
			//}

			searchText.selectAll();
			deleteOnClick=true;

			KL = new KeyListener(){
				public void keyTyped(KeyEvent e) {
					if(searchText.getText().length() == 0){
						currentGUI.nextSearch.setVisible(false);
						currentGUI.previousSearch.setVisible(false);
					}

					//clear when user types
					if(deleteOnClick){
						deleteOnClick=false;
						searchText.setText("");
					}
					int id = e.getID();
					if (id == KeyEvent.KEY_TYPED) {
						char key=e.getKeyChar();

						if(key=='\n'){

							if(!decode_pdf.isOpen()){
								currentGUI.showMessageDialog("File must be open before you can search.");
							}else{
								try {
									currentGUI.nextSearch.setVisible(true);
									currentGUI.previousSearch.setVisible(true);

									currentGUI.nextSearch.setEnabled(false);
									currentGUI.previousSearch.setEnabled(false);

									isSearch=false;
									searchTypeParameters = SearchType.DEFAULT;

									if(wholeWordsOnlyBox.isSelected())
										searchTypeParameters |= SearchType.WHOLE_WORDS_ONLY;

									if(caseSensitiveBox.isSelected())
										searchTypeParameters |= SearchType.CASE_SENSITIVE;

									if(multiLineBox.isSelected())
										searchTypeParameters |= SearchType.MUTLI_LINE_RESULTS;
									
									if(highlightAll.isSelected())
										searchTypeParameters |= SearchType.HIGHLIGHT_ALL_RESULTS;
									
									if(useRegEx.isSelected())
										searchTypeParameters |= SearchType.USE_REGULAR_EXPRESSIONS;
									
									String textToFind = searchText.getText().trim();
									if(searchType.getSelectedIndex() == 0){ // find exact word or phrase
										searchTerms = new String[] { textToFind };
									} else { // match any of the words
										searchTerms = textToFind.split(" ");
										for (int i = 0; i < searchTerms.length; i++) {
											searchTerms[i] = searchTerms[i].trim();
										}
									}
									
									singlePageSearch = !searchAll.isSelected();
									
									
									searchText();
									
									currentGUI.getPdfDecoder().requestFocusInWindow();
								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}

				public void keyPressed(KeyEvent arg0) {}

				public void keyReleased(KeyEvent arg0) {}
			};

			searchText.addKeyListener(KL);
			if(style==SEARCH_EXTERNAL_WINDOW || style==SEARCH_TABBED_PANE){
				//build frame
				JScrollPane scrollPane=new JScrollPane();
				scrollPane.getViewport().add(resultsList);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.getVerticalScrollBar().setUnitIncrement(80);
				scrollPane.getHorizontalScrollBar().setUnitIncrement(80);

				getContentPane().setLayout(new BorderLayout());
				getContentPane().add(scrollPane,BorderLayout.CENTER);
				getContentPane().add(nav,BorderLayout.NORTH);
				getContentPane().add(advancedPanel, BorderLayout.SOUTH);

				//position and size
				Container frame = currentGUI.getFrame();
				if(commonValues.getModeOfOperation() == Values.RUNNING_APPLET){
					if (currentGUI.getFrame() instanceof JFrame)
						frame = ((JFrame)currentGUI.getFrame()).getContentPane();
				}

				if(style==SEARCH_EXTERNAL_WINDOW){
					int w=230;

					int h=frame.getHeight();
					int x1=frame.getLocationOnScreen().x;
					int x=frame.getWidth()+x1;
					int y=frame.getLocationOnScreen().y;
					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

					int width = d.width;
					if(x+w>width && style==SEARCH_EXTERNAL_WINDOW){
						x=width-w;
						frame.setSize(x-x1,frame.getHeight());
					}

					setSize(w,h);
					setLocation(x,y);
				}
				searchAll.setFocusable(false);

				searchText.grabFocus();

			}else{
				//Whole Panel not used, take what is needed
				currentGUI.setSearchText(searchText);
			}
		}
		
	}
	
	/**
	 * find text on page withSwingWindow
	 */
	public void findWithoutWindow(final PdfDecoder dec, final Values values, int searchType, boolean listOfTerms, boolean singlePageOnly, String searchValue){

        if(!isSearch){
			backGroundSearch = true;
			isSearch=true;

			this.decode_pdf = dec;
			this.commonValues = values;

			decode_pdf.setLayout(new BorderLayout());
			decode_pdf.add(progress, BorderLayout.SOUTH);
			progress.setValue(0);
			progress.setMaximum(commonValues.getPageCount());
			progress.setVisible(true);
			decode_pdf.validate();

			String textToFind = searchValue;
			if(!listOfTerms){ // find exact word or phrase
				searchTerms = new String[] { textToFind };
			} else { // match any of the words
				searchTerms = textToFind.split(" ");
				for (int i = 0; i < searchTerms.length; i++) {
					searchTerms[i] = searchTerms[i].trim();
				}
			}

			searchTypeParameters = searchType;

			singlePageSearch = singlePageOnly;

			find(dec, values);
			
		}else{
			currentGUI.showMessageDialog("Please wait for search to finish before starting another.");
		}
	}
	
	/**
	 * find text on page
	 */
	public void find(final PdfDecoder dec, final Values values){


		//		System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
		
		/**
		 * pop up new window to search text (initialise if required
		 */
		if(!backGroundSearch){
			init(dec, values);
			if(style==SEARCH_EXTERNAL_WINDOW)
				setVisible(true);
		}else{
			try {
				searchText();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public void removeSearchWindow(boolean justHide) {

		//System.out.println("remove search window");

		setVisible(false);

		setVisible(false);

		if(searcher!=null)
			searcher.interrupt();

		if(isSetup && !justHide){
			if(listModel!=null)
				listModel.clear();//removeAllElements();

			//searchText.setText(defaultMessage);
			//searchAll=null;
			//if(nav!=null)
			//    nav.removeAll();

			itemFoundCount=0;
			isSearch=false;

		}

        //lose any highlights and force redraw with non-existent box
        if(decode_pdf!=null){
            decode_pdf.getTextLines().clearHighlights();
            decode_pdf.repaint();
        }
    }

	private void searchText() throws Exception {

		/** if running terminate first */
		if ((searcher != null))
			searcher.interrupt();

		if(style==SEARCH_MENU_BAR){
			usingMenuBarSearch = true;
		}else{
			usingMenuBarSearch = false;
		}
		
        //reset list of pages searched
        lastPage=-1;
		
		if(listModel == null)
			listModel = new DefaultListModel();
		
		if(resultsList==null)
			resultsList=new SearchList(listModel,textPages, textRectangles);
		
		resultsList.setStatus(SearchList.SEARCH_INCOMPLETE);
		
		if(!backGroundSearch){
			searchButton.setText(Messages.getMessage("PdfViewerSearchButton.Stop"));
			searchButton.invalidate();
			searchButton.repaint();
			
			searchCount.setText(Messages.getMessage("PdfViewerSearch.Scanning1"));
			searchCount.repaint();
		}

		searcher = new SwingWorker() {
			public Object construct() {

				isSearch=true;
				hasSearched=true;
				
				try {
					
//					System.out.println("seareching pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
					
					listModel.removeAllElements();
					
					if(!backGroundSearch)
						resultsList.repaint();

					textPages.clear();

					textRectangles.clear();
					itemFoundCount = 0;
					decode_pdf.getTextLines().clearHighlights();

					//System.out.println("textToFind = "+textToFind);

					// get page sizes
					PdfPageData pageSize = decode_pdf.getPdfPageData();

					//int x1, y1, x2, y2;

					// page range
					//int startPage = 1;
					//int endPage = commonValues.getPageCount() + 1;
					
					if (singlePageSearch || usingMenuBarSearch) {
						//startPage = commonValues.getCurrentPage();
						//endPage = startPage + 1;
						if(singlePageSearch){
							searchPageRange(pageSize, commonValues.getCurrentPage(), commonValues.getCurrentPage()+1);
						}else{
							for(int p=0; p!=commonValues.getPageCount()+1 && resultsList.getResultCount()<1;p++){
								int page = commonValues.getCurrentPage()+p;
								if(page>commonValues.getPageCount())
									page -=commonValues.getPageCount();
								searchPageRange(pageSize, page, page+1);
							}
						}
                        
					}else if(!backGroundSearch || !usingMenuBarSearch){
                        //this page to end
                        searchPageRange(pageSize, 1, commonValues.getPageCount() + 1);
                    }



					if(!backGroundSearch){
						searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount + "  "
								+ Messages.getMessage("PdfViewerSearch.Done"));
						searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
					}

					resultsList.invalidate();
					resultsList.repaint();
					resultsList.setSelectedIndex(0);
					resultsList.setLength(listModel.capacity());
					currentGUI.setResults(resultsList);

					currentGUI.nextSearch.setEnabled(true);
					currentGUI.previousSearch.setEnabled(true);

					// reset search button
					isSearch = false;
					requestInterupt = false;

                }catch(InterruptedException ee){

                    //Exception caused so use alert user and allow search
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							requestInterupt = false;
							backGroundSearch = false;
                            currentGUI.showMessageDialog("Search stopped by user.");
							if(!backGroundSearch){
                            currentGUI.nextSearch.setEnabled(true);
							currentGUI.previousSearch.setEnabled(true);
							}
						}
					});
                } catch (Exception e) {
					//Exception caused so use alert user and allow search
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							requestInterupt = false;
							backGroundSearch = false;
                            if(Viewer.showMessages)
                            currentGUI.showMessageDialog("An error occured during search. Some results may be missing.\n\nPlease send the file to IDRSolutions for investigation.");
							if(!backGroundSearch){
                            currentGUI.nextSearch.setEnabled(true);
							currentGUI.previousSearch.setEnabled(true);
							}
						}
					});

				}
                
				if(!Values.isProcessing()){//{if (!event.getValueIsAdjusting()) {

					float scaling=currentGUI.getScaling();
					//int inset=currentGUI.getPDFDisplayInset();

					resultsList.setSelectedIndex(0);
					int id=resultsList.getSelectedIndex();

					decode_pdf.getTextLines().clearHighlights();
					//							System.out.println("clicked pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));

					/**
					 * Sometimes the selected index is not registered by this point
					 * Set manual if this is the case
					 */
					if(id==-1 && resultsList.getResultCount()>0){
						id=0;
					}
					
					if(id!=-1){

						Integer key= id;
						Object newPage=textPages.get(key);

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
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
							
							firstPageWithResults = commonValues.getCurrentPage();
							
							/**
							 * Highlight all search results on page.
							 */
							
							if((searchTypeParameters & SearchType.HIGHLIGHT_ALL_RESULTS)== SearchType.HIGHLIGHT_ALL_RESULTS){
								Rectangle[] showAllOnPage;
								Vector_Rectangle storageVector = new Vector_Rectangle();
								int lastPage = -1;
								int currentPage = 0;
//								System.out.println("size = "+getSize());
								for(int k=0; k!=textPages.size(); k++){
									Object page=textPages.get(k);

									if(page!=null){
										currentPage = (Integer) page;
										if(currentPage!=lastPage && lastPage!=-1){
											storageVector.trim();
											showAllOnPage = storageVector.get();
											decode_pdf.getTextLines().addHighlights(showAllOnPage, true, lastPage);
											lastPage = currentPage;
											storageVector = new Vector_Rectangle();
										}

											Object highlight= textRectangles.get(k);

											if(highlight instanceof Rectangle){
												storageVector.addElement((Rectangle)highlight);
											}
											if(highlight instanceof Rectangle[]){
												Rectangle[] areas = (Rectangle[])highlight;
												for(int i=0; i!=areas.length; i++){
													storageVector.addElement(areas[i]);
												}
											}
											//decode_pdf.addToHighlightAreas(decode_pdf, storageVector, currentPage);
//										}
									}
								}
								storageVector.trim();
								showAllOnPage = storageVector.get();
								decode_pdf.getTextLines().addHighlights(showAllOnPage, true, currentPage);
								
							}else{

								Object highlight= textRectangles.get(key);

								if(highlight instanceof Rectangle){
                                    currentGUI.currentCommands.scrollRectToHighlight((Rectangle)highlight,commonValues.getCurrentPage());

									//add text highlight
									decode_pdf.getTextLines().addHighlights(new Rectangle[]{(Rectangle)highlight}, true, commonValues.getCurrentPage());
								}
								if(highlight instanceof Rectangle[]){
                                    currentGUI.currentCommands.scrollRectToHighlight(((Rectangle[])highlight)[0],commonValues.getCurrentPage());

									//add text highlight
									decode_pdf.getTextLines().addHighlights(((Rectangle[])highlight), true, commonValues.getCurrentPage());
								}

							}

							decode_pdf.invalidate();
							decode_pdf.repaint();

						}
					}
				}

				//When page changes make sure only relevant navigation buttons are displayed
				if(commonValues.getCurrentPage()==1)
					currentGUI.setBackNavigationButtonsEnabled(false);
				else
					currentGUI.setBackNavigationButtonsEnabled(true);

				if(commonValues.getCurrentPage()==decode_pdf.getPageCount())
					currentGUI.setForwardNavigationButtonsEnabled(false);
				else
					currentGUI.setForwardNavigationButtonsEnabled(true);

				decode_pdf.remove(progress);
				decode_pdf.validate();
                backGroundSearch = false;
                resultsList.setStatus(SearchList.SEARCH_COMPLETE_SUCCESSFULLY);
				return null;
			}
		};

		searcher.start();
	}

    public int getFirstPageWithResults() {
		return firstPageWithResults;
	}


	private void searchPageRange(PdfPageData pageSize, int startPage, int endPage) throws Exception {

        int x1;
        int x2;
        int y1;
        int y2;// search all pages

        int listCount=0;

        //System.out.println("Search range "+startPage+" "+endPage);

        for (int page = startPage; page < endPage && !requestInterupt; page++) {

//@kieran -changed by Mark to stop thread issue
            if (Thread.interrupted()) {
                continue;
                //throw new InterruptedException();
            }

//System.out.println("page=="+page);

            progress.setValue(progress.getValue()+1);
            decode_pdf.repaint();

            /** common extraction code */
            PdfGroupingAlgorithms currentGrouping;

            /** create a grouping object to apply grouping to data */
            try {
                if (page == commonValues.getCurrentPage())
                    currentGrouping = decode_pdf.getGroupingObject();
                else {
                    decode_pdf.decodePageInBackground(page);
                    currentGrouping = decode_pdf.getBackgroundGroupingObject();
                }

                // tell JPedal we want teasers
                currentGrouping.generateTeasers();

//allow us to add options
                currentGrouping.setIncludeHTML(true);

// set size
                x1 = pageSize.getCropBoxX(page);
                x2 = pageSize.getCropBoxWidth(page);
                y1 = pageSize.getCropBoxY(page);
                y2 = pageSize.getCropBoxHeight(page);

                final SearchListener listener = new DefaultSearchListener();

                SortedMap highlightsWithTeasers = currentGrouping.findMultipleTermsInRectangleWithMatchingTeasers(x1, y1, x2, y2, pageSize.getRotation(page), page, searchTerms, searchTypeParameters, listener);

//changed by MArk
                if (Thread.interrupted()) {
                    continue;
                    //throw new InterruptedException();
                }

/**
 * update data structures with results from this page
 */
                if (!highlightsWithTeasers.isEmpty()) {

//@kieran
//switch on buttons as soon as search produces valid results
                    if(!backGroundSearch){
                        currentGUI.nextSearch.setEnabled(true);
                        currentGUI.previousSearch.setEnabled(true);

                    }
// update count display
                    itemFoundCount = itemFoundCount + highlightsWithTeasers.size();

                    for (Object o : highlightsWithTeasers.entrySet()) {
                        Map.Entry e = (Map.Entry) o;

/*highlight is a rectangle or a rectangle[]*/
                        Object highlight = e.getKey();

                        final String teaser = (String) e.getValue();

                        if (!SwingUtilities.isEventDispatchThread()) {
                            Runnable setTextRun = new Runnable() {
                                public void run() {

                                    //if highights ensure displayed by wrapping in tags
                                    if (!teaser.contains("<b>"))
                                        listModel.addElement(teaser);
                                    else
                                        listModel.addElement("<html>" + teaser + "</html>");
                                }
                            };
                            SwingUtilities.invokeLater(setTextRun);
                        } else {
                            if (!teaser.contains("<b>"))
                                listModel.addElement(teaser);
                            else
                                listModel.addElement("<html>" + teaser + "</html>");
                        }

                        Integer key = listCount;
                        listCount++;
                        textRectangles.put(key, highlight);
                        textPages.put(key, page);
                    }

                }

                // new value or 16 pages elapsed
                if (!backGroundSearch && (!highlightsWithTeasers.isEmpty()) | ((page % 16) == 0)) {
                    searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + ' ' + itemFoundCount + ' '
                            + Messages.getMessage("PdfViewerSearch.Scanning") + page);
                    searchCount.invalidate();
                    searchCount.repaint();
                }
            } catch (PdfException e1) {
                backGroundSearch = false;
                requestInterupt = false;
            }
            if(requestInterupt){
                currentGUI.showMessageDialog("Search stopped by user.");
            }
            lastPage = page;
        }
    }

    public int getListLength(){
		return listModel.capacity();
	}

	public void grabFocusInInput() {
		searchText.grabFocus();

	}

	public boolean isSearchVisible() {
		return this.isVisible();
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public JTextField getSearchText() {
		return searchText;
	}
	
	public void setSearchText(String s) {
		deleteOnClick = false;
		searchText.setText(s);
	}

	public Map getTextRectangles() {
		return textRectangles;
	}

	public SearchList getResults() {

        return resultsList;
	}

    public SearchList getResults(int page) {

        if(usingMenuBarSearch && page !=lastPage && style==SEARCH_MENU_BAR){

            //if(listModel == null)
            listModel = new DefaultListModel();

            textPages.clear();
            textRectangles.clear();
            //if(resultsList==null)
            resultsList=new SearchList(listModel,textPages, textRectangles);

            resultsList.setStatus(SearchList.SEARCH_INCOMPLETE);

            try {
                searchPageRange(decode_pdf.getPdfPageData(), page, page + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }


            lastPage=page;
        }

        return resultsList;
    }
	
    /**
     * Reset search text and menu bar buttons when opening new page
     */
    public void resetSearchWindow(){
    	if(isSetup){
    		
    		searchText.setText(defaultMessage);
			deleteOnClick=true;
			
    		if(hasSearched){
//    			resultsList = null;
    			currentGUI.nextSearch.setVisible(false);
    			currentGUI.previousSearch.setVisible(false);
    			hasSearched = false;
    		}
			currentGUI.getPdfDecoder().requestFocusInWindow();
//			isSetup = false;
    	}
    }
}
