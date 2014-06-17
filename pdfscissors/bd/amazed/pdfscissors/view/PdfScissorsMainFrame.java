package bd.amazed.pdfscissors.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;
import bd.amazed.pdfscissors.model.PageGroup;
import bd.amazed.pdfscissors.model.PageRectsMap;
import bd.amazed.pdfscissors.model.PdfFile;
import bd.amazed.pdfscissors.model.TaskPdfOpen;
import bd.amazed.pdfscissors.model.TaskPdfSave;
import bd.amazed.pdfscissors.model.TempFileManager;

/**
 * @author Gagan
 *
 */
public class PdfScissorsMainFrame extends JFrame implements ModelListener {

	private PdfPanel defaultPdfPanel;
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JScrollPane scrollPanel = null;
	/** Panel containing PdfPanels. */
	private JPanel pdfPanelsContainer = null;
	private ButtonGroup rectButtonGroup = null;
	/** Contains all components that are disabled until file open. */
	private Vector<Component> openFileDependendComponents = null;
	private Vector<ModelListener> modelRegisteredListeners;
	private JToolBar toolBar = null;
	private UIHandler uiHandler = null;
	private JToggleButton buttonDraw = null;
	private JToggleButton buttonSelect = null;
	private JButton buttonDeleteRect = null;
	private JButton buttonDelAll = null;
	private JButton buttonSave = null;
	private JButton buttonSplitHorizontal = null;
	private JButton buttonSplitVertical = null;
	private JPanel bottomPanel;
	private JComboBox<String> pageSelectionCombo = null;
	private JMenuBar jJMenuBar = null;
	private JMenu menuFile = null;
	private JMenuItem menuSaveAs = null;
	private JMenuItem menuSave = null;
	private JMenu menuEdit = null;
	private JMenuItem menuCopy = null;
	private JMenuItem menuCut = null;
	private JMenuItem menuPaste = null;
	private JMenuItem menuDelete = null;
	private JButton buttonEqualWidth = null;
	private JButton buttonEqualHeight = null;
	private JMenu menuHelp = null;
	private JMenuItem menuAbout = null;
	private JScrollPane pageGroupScrollPanel = null;
	private JList<PageGroup> pageGroupList = null;
	private PageGroupRenderer pageGroupListRenderer;
	private JButton forwardButton = null;
	private JButton backButton = null;
	private File saveFile;

	public PdfScissorsMainFrame() {
		super();
		modelRegisteredListeners = new Vector<ModelListener>();
		openFileDependendComponents = new Vector<Component>();
		initialize();
		registerComponentsToModel();
		updateOpenFileDependents();
		try {
			URL url = PdfScissorsMainFrame.class.getResource("/icon.png");
			if (url != null) {
				setIconImage(ImageIO.read(url));
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(PdfScissorsMainFrame.class).log(Level.WARNING, "Failed to get frame icon", e);
		}

		try {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		} catch (Throwable e) {
			LoggerFactory.getLogger(PdfScissorsMainFrame.class).log(Level.WARNING, "Failed to set exit on close.", e);
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				getDefaultPdfPanel().closePdfFile(); // TODO may be implement a better way to notify to close
				TempFileManager.getInstance().clean();
			}
		});
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.rectButtonGroup = new ButtonGroup();
		this.uiHandler = new UIHandler();
		this.setContentPane(getJContentPane());
		this.setJMenuBar(getJJMenuBar());
		this.setTitle(Bundle.getString("PdfScissorsMainFrame.PdfScissors"));
		this.setSize(new Dimension(800, 600));
		this.setMinimumSize(new Dimension(200, 200));
		Dimension screen = getToolkit().getScreenSize();
		this.setBounds((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2, getWidth(), getHeight());
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

	}

	private void registerComponentsToModel() {
		Model model = Model.getInstance();
		// we want to maintain listners order, first child components, then me.
		// So we remove old ones first
		for (ModelListener listener : modelRegisteredListeners) {
			model.removeListener(listener);
		}
		modelRegisteredListeners.removeAllElements();

		// if there is any listner component inside scrollpane (like PdfPanel),
		// lets add them first
		if (pdfPanelsContainer != null) {
			int scollPaneComponents = pdfPanelsContainer.getComponentCount();
			Component component = null;

			for (int i = 0; i < scollPaneComponents; i++) {
				component = pdfPanelsContainer.getComponent(i);
				if (component instanceof ModelListener) {
					model.addListener((ModelListener) component);
					modelRegisteredListeners.add((ModelListener) component);
				}

				if (component instanceof UIHandlerListener) {
					uiHandler.addListener((UIHandlerListener) component);
				}
			}
		}

		// finally add myself.
		model.addListener(this);
		modelRegisteredListeners.add(this);

	}

	/**
	 * Enable/disable buttons etc which should be disabled when there is no file.
	 */
	private void updateOpenFileDependents() {
		boolean shouldEnable = Model.getInstance().getPdf().getNormalizedFile() != null;
		for (Component component : openFileDependendComponents) {
			component.setEnabled(shouldEnable);
		}
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getScrollPanel(), BorderLayout.CENTER);
			jContentPane.add(getToolBar(), BorderLayout.NORTH);
			jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
			jContentPane.add(getPageGroupPanel(), BorderLayout.WEST);
		}
		return jContentPane;
	}

	private JScrollPane getPageGroupPanel() {
		if (pageGroupScrollPanel == null) {
			JList<PageGroup> list = getPageGroupList();
			pageGroupScrollPanel = new JScrollPane(list);
			openFileDependendComponents.add(pageGroupScrollPanel);
		}
		return pageGroupScrollPanel;
	}

	private JList<PageGroup> getPageGroupList() {
		if (pageGroupList == null) {
			pageGroupList = new JList<PageGroup>();
			pageGroupList.setMinimumSize(new Dimension(200,100));
			openFileDependendComponents.add(pageGroupList);
			pageGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			pageGroupList.setCellRenderer(getPageGroupListCellRenderer());
			pageGroupList.setBackground(this.getBackground());
			pageGroupList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					int selectedIndex = pageGroupList.getSelectedIndex();
					if (selectedIndex >= 0) {
						PageGroup currentGroup = Model.getInstance().getPageGroups().elementAt(selectedIndex);
						uiHandler.setPageGroup(currentGroup);
					}
				}
			});
		}
		return pageGroupList;
	}

	private PageGroupRenderer getPageGroupListCellRenderer() {
		if (this.pageGroupListRenderer == null) {
			this.pageGroupListRenderer = new PageGroupRenderer();
		}
		return this.pageGroupListRenderer;
	}

	private void setButton(AbstractButton button, String imageLocation, String tooltip, boolean isOpenFileDependent) {
		String imgLocation = imageLocation;
		URL imageURL = PdfScissorsMainFrame.class.getResource(imageLocation);
		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, tooltip));
			button.setText(null);
		} else { // no image found
			button.setText(tooltip);
			System.err.println(Bundle.getString("PdfScissorsMainFrame.ResourceNotFound") + imgLocation);
		}
		button.setToolTipText(tooltip);
		button.setActionCommand(tooltip);
		if (isOpenFileDependent) {
			openFileDependendComponents.add(button);
		}
	}

	public void openFile(IResourceHandler file, int pageGroupType, boolean shouldCreateStackView) {
		// create new scrollpane content
		pdfPanelsContainer = new JPanel();
		pdfPanelsContainer.setLayout(new GridBagLayout());

		int pdfPanelCount = 1; // more to come
		for (int i = 0; i < pdfPanelCount; i++) {
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = i;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.insets = new Insets(2, 0, 2, 0);
			if (i == 0) {
				pdfPanelsContainer.add(getDefaultPdfPanel(), constraints);
			}
		}

		getScrollPanel().setViewportView(pdfPanelsContainer);

		uiHandler.reset();
		uiHandler.removeAllListeners();
		registerComponentsToModel();
		uiHandler.addListener(new UIHandlerLisnterForFrame());

		launchOpenTask(file, pageGroupType, shouldCreateStackView, Bundle.getString("PdfScissorsMainFrame.ReadingPdf"));
	}

	private PdfPanel getDefaultPdfPanel() {
		if (defaultPdfPanel == null) {
			defaultPdfPanel = new PdfPanel(uiHandler);
		}
		return defaultPdfPanel;
	}

	private void launchOpenTask(IResourceHandler file, int groupType, boolean shouldCreateStackView, String string) {
		final TaskPdfOpen task = new TaskPdfOpen(file, groupType, shouldCreateStackView);
		final StackViewCreationDialog stackViewCreationDialog = new StackViewCreationDialog(this);
		stackViewCreationDialog.setModal(true);
		stackViewCreationDialog.enableProgress(task, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// what happens on cancel
				task.cancel();
				stackViewCreationDialog.dispose();
				PdfScissorsMainFrame.this.dispose();

			}
		});
		// what happens on ok
		task.execute();

		stackViewCreationDialog.setVisible(true);

	}

	public FileFilter createFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return file.toString().toLowerCase().endsWith(".pdf");
			}

			@Override
			public String getDescription() {
				return "*.pdf";
			}
		};
	}

	/**
	 * This method initializes scrollPanel
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getScrollPanel() {
		if (scrollPanel == null) {
			scrollPanel = new JScrollPane();
			scrollPanel.setPreferredSize(new Dimension(200, 300));
		}
		return scrollPanel;
	}

	private void handleException(String userFriendlyMessage, Throwable ex) {
		JOptionPane.showMessageDialog(this, Bundle.getString("PdfScissorsMainFrame.Oops") + userFriendlyMessage + Bundle.getString("PdfScissorsMainFrame.TechnikcalDetails") + ex.getMessage());
		ex.printStackTrace();
	}

	/**
	 * This method initializes toolBar
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.add(getButtonSave());
			toolBar.add(getButtonDraw());
			toolBar.add(getButtonSelect());
			toolBar.add(getButtonDeleteRect());
			toolBar.add(getButtonDelAll());
			toolBar.setFloatable(false);
			toolBar.add(getButtonEqualWidth());
			toolBar.add(getButtonEqualHeight());
			toolBar.add(getButtonSplitHorizontal());
			toolBar.add(getButtonSplitVertical());
		}
		return toolBar;
	}

	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();

			backButton = new JButton("<");
			openFileDependendComponents.add(backButton);
			backButton.setToolTipText(Bundle.getString("PdfScissorsMainFrame.BackOnePage"));
			bottomPanel.add(backButton);
			backButton.addActionListener(new PageChangeHandler(false));

			forwardButton = new JButton(">");
			openFileDependendComponents.add(forwardButton);
			forwardButton.setToolTipText(Bundle.getString("PdfScissorsMainFrame.ForwardOnePage"));
			bottomPanel.add(getPageSelectionCombo(), null);
			bottomPanel.add(forwardButton);
			forwardButton.addActionListener(new PageChangeHandler(true));
		}
		return bottomPanel;
	}

	/**
	 * This method initializes buttonSave
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonSave() {
		if (buttonSave == null) {
			buttonSave = new JButton(Bundle.getString("PdfScissorsMainFrame.Save"));
			setButton(buttonSave, "/crop.png", Bundle.getString("PdfScissorsMainFrame.CropAndSave"), true);
			buttonSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(saveFile != null) {
						saveFile(saveFile);
					} else {
						openSaveFileChooser();
					}
				}
			});
		}
		return buttonSave;
	}

	public void setSaveFile(File file) {
		this.saveFile = file;
	}

	private void openSaveFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(createFileFilter());
		IResourceHandler originalPdf = Model.getInstance().getPdf().getOriginalFile();
		fileChooser.setSelectedFile(originalPdf.toFile());
		int retval = fileChooser.showSaveDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File targetFile = fileChooser.getSelectedFile();
			if (targetFile.equals(originalPdf)) {
				if (0 != JOptionPane.showConfirmDialog(this, Bundle.getString("PdfScissorsMainFrame.OverwriteMessage"), Bundle.getString("PdfScissorsMainFrame.ConfirmOverwrite"), JOptionPane.YES_NO_CANCEL_OPTION)) {
					return; // overwrite not allowed by user
				}
			} else if (targetFile.exists()) {
				// confirm overwrite
				if (0 != JOptionPane.showConfirmDialog(this, targetFile.getName() + " " + Bundle.getString("PdfScissorsMainFrame.AlreadyExistsAskOverwrite"), Bundle.getString("PdfScissorsMainFrame.OverwriteMessage"), JOptionPane.YES_NO_CANCEL_OPTION)) {
					return; // overwrite not allowed by user
				}
			}
			saveFile(targetFile);
		}
	}

	private void saveFile(File targetFile) {
		launchSaveTask(Model.getInstance().getPdf(), targetFile);
	}

	private void launchSaveTask(PdfFile pdfFile, File targetFile) {
		PageRectsMap pageRectsMap = Model.getInstance().getPageRectsMap();
		TaskPdfSave taskPdfSave = new TaskPdfSave(pdfFile, targetFile, pageRectsMap , defaultPdfPanel.getWidth(), defaultPdfPanel.getHeight(), this);
		taskPdfSave.execute();
	}

	/**
	 * This method initializes buttonDraw
	 *
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getButtonDraw() {
		if (buttonDraw == null) {
			buttonDraw = new JToggleButton(Bundle.getString("PdfScissorsMainFrame.Draw"), true); // selected initially
			setButton(buttonDraw, "/draw.png", Bundle.getString("PdfScissorsMainFrame.DrawAreaForCropping"), true);
			setToggleButtonGroup(buttonDraw, rectButtonGroup);
			buttonDraw.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					uiHandler.setEditingMode(UIHandler.EDIT_MODE_DRAW);
				}
			});
		}
		return buttonDraw;
	}

	/**
	 * This method initializes buttonSelect
	 *
	 * @return javax.swing.JButton
	 */
	private JToggleButton getButtonSelect() {
		if (buttonSelect == null) {
			buttonSelect = new JToggleButton(Bundle.getString("PdfScissorsMainFrame.Select"));
			setButton(buttonSelect, "/select.png", Bundle.getString("PdfScissorsMainFrame.SelectAndResize"), true);
			setToggleButtonGroup(buttonSelect, rectButtonGroup);
			buttonSelect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					uiHandler.setEditingMode(UIHandler.EDIT_MODE_SELECT);
				}
			});
		}
		return buttonSelect;
	}

	/**
	 * This method initializes buttonDeleteRect
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonDeleteRect() {
		if (buttonDeleteRect == null) {
			buttonDeleteRect = new JButton(Bundle.getString("PdfScissorsMainFrame.Delete"));
			setButton(buttonDeleteRect, "/del.png", Bundle.getString("PdfScissorsMainFrame.DeleteSelectedCropArea"), true);
			buttonDeleteRect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.deleteSelected();
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonDeleteRect;
	}

	/**
	 * This method initializes buttonDelAll
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonDelAll() {
		if (buttonDelAll == null) {
			buttonDelAll = new JButton(Bundle.getString("PdfScissorsMainFrame.DeleteAll"));
			setButton(buttonDelAll, "/delAll.png", Bundle.getString("PdfScissorsMainFrame.DeleteAllCropAreas"), true);
			buttonDelAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getRectCount() <= 0) {
						showDialogNoRectYet();
					} else if (JOptionPane.showConfirmDialog(PdfScissorsMainFrame.this, Bundle.getString("PdfScissorsMainFrame.Delete") + uiHandler.getRectCount() + " " + Bundle.getString("PdfScissorsMainFrame.CropArea") + (uiHandler.getRectCount() > 1 ? "s" : "") // singular/plural   //$NON-NLS-3$ //$NON-NLS-4$
							+ "?", Bundle.getString("PdfScissorsMainFrame.Confirm"), JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
						uiHandler.deleteAll();
					}
				}
			});
		}
		return buttonDelAll;
	}

	/**
	 * This method initializes buttonEqualWidth
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonEqualWidth() {
		if (buttonEqualWidth == null) {
			buttonEqualWidth = new JButton(Bundle.getString("PdfScissorsMainFrame.EqualWidth"));
			setButton(buttonEqualWidth, "/sameWidth.png", Bundle.getString("PdfScissorsMainFrame.SetWidthOfAllAreasSame"), true);
			buttonEqualWidth.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getRectCount() > 0) {
						uiHandler.equalizeWidthOfSelected(defaultPdfPanel.getWidth());
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonEqualWidth;
	}

	/**
	 * This method initializes buttonEqualHeight
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonEqualHeight() {
		if (buttonEqualHeight == null) {
			buttonEqualHeight = new JButton(Bundle.getString("PdfScissorsMainFrame.EqualHeight"));
			setButton(buttonEqualHeight, "/sameHeight.png", Bundle.getString("PdfScissorsMainFrame.SetHeightsOfCropAreasSame"), true);
			buttonEqualHeight.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getRectCount() > 0) {
						uiHandler.equalizeHeightOfSelected(defaultPdfPanel.getHeight());
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonEqualHeight;
	}

	private JButton getButtonSplitHorizontal() {
		if (buttonSplitHorizontal == null) {
			buttonSplitHorizontal = new JButton(Bundle.getString("PdfScissorsMainFrame.SplitHorizontal"));
			setButton(buttonSplitHorizontal, "/splitHorizontal.png", Bundle.getString("PdfScissorsMainFrame.SplitAreaInTwoEqualsHorizontalAreas"), true);
			buttonSplitHorizontal.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.splitHorizontalSelected(defaultPdfPanel);
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonSplitHorizontal;
	}

	private JButton getButtonSplitVertical() {
		if (buttonSplitVertical == null) {
			buttonSplitVertical = new JButton(Bundle.getString("PdfScissorsMainFrame.SplitVertical"));
			setButton(buttonSplitVertical, "/splitVertical.png", Bundle.getString("PdfScissorsMainFrame.SplitAreaInTwoEqualsVerticalAreas"), true);
			buttonSplitVertical.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.splitVerticalSelected(defaultPdfPanel);
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonSplitVertical;
	}

	/**
	 * Ensures other buttons in the group will be unselected when given button is selected.
	 *
	 * @param button
	 * @param group
	 */
	private void setToggleButtonGroup(final JToggleButton button, final ButtonGroup group) {
		group.add(button);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean otherButtonMode = !button.isSelected();
				Enumeration<AbstractButton> otherButtons = group.getElements();
				while (otherButtons.hasMoreElements()) {
					otherButtons.nextElement().setSelected(otherButtonMode);
				}

			}
		});
	}

	@Override
	public void newPdfLoaded(PdfFile pdfFile) {
		updateOpenFileDependents();
		getPageGroupListCellRenderer().setPageSize(pdfFile.getNormalizedPdfWidth(), pdfFile.getNormalizedPdfHeight());
		getPageGroupList().invalidate(); // to recalculate size etc
	}

	@Override
	public void pdfLoadFailed(IResourceHandler failedFile, Throwable cause) {
		handleException(Bundle.getString("PdfScissorsMainFrame.FailedToLoadPdfFile"), cause);
	}

	@Override
	public void pageGroupChanged(Vector<PageGroup> pageGroups) {
		JList<PageGroup> list = getPageGroupList();
		list.removeAll();
		DefaultListModel<PageGroup> listModel = new DefaultListModel<>();
		for (int i = 0; i < pageGroups.size(); i++) {
			listModel.add(i, pageGroups.elementAt(i));
		}
		list.setModel(listModel);
		list.setSelectedIndex(0);
		getPageGroupPanel().getViewport().removeAll();
		getPageGroupPanel().getViewport().add(list);

	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		Rectangle oldView = scrollPanel.getViewport().getViewRect();
		Point newViewPos = new Point();
		newViewPos.x = Math.max(0, (int) ((oldView.x + oldView.width / 2) * newZoomFactor - oldView.width / 2));
		newViewPos.y = Math.max(0, (int) ((oldView.y + oldView.height / 2) * newZoomFactor - oldView.height / 2));
		scrollPanel.getViewport().setViewPosition(newViewPos);
		scrollPanel.revalidate();
	}

	@Override
	public void clipboardCopy(boolean isCut, Rect onClipboard) {
		getMenuPaste().setEnabled(true);
	}

	@Override
	public void clipboardPaste(boolean isCut, Rect onClipboard) {
		if (isCut) {
			getMenuPaste().setEnabled(false);
		}
	}

	/**
	 * This method initializes pageSelectionCombo
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<String> getPageSelectionCombo() {
		if (pageSelectionCombo == null) {
			pageSelectionCombo = new JComboBox<String>();
			openFileDependendComponents.add(pageSelectionCombo);
			pageSelectionCombo.setEditable(true);

			// to align text to center
			DefaultListCellRenderer comboCellRenderer = new DefaultListCellRenderer();
			comboCellRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
			pageSelectionCombo.setRenderer(comboCellRenderer);

			pageSelectionCombo.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					String pageIndex = (String) pageSelectionCombo.getSelectedItem();
					if (pageIndex != null && pageIndex.length() > 0 && Character.isDigit(pageIndex.charAt(0))) { // page
						// number
						uiHandler.setMergeMode(false);
						int pageNumber = Integer.valueOf(pageIndex);
						uiHandler.setPage(pageNumber); // we dont have to add +1, cause first time is all page
						try {
							defaultPdfPanel.decodePage(pageNumber);
						} catch (Exception ex) {
							handleException(Bundle.getString("PdfScissorsMainFrame.FailedToDecodePage") + pageNumber, ex);
						}
						defaultPdfPanel.invalidate();
						defaultPdfPanel.repaint();
					} else {
						uiHandler.setMergeMode(true);
						defaultPdfPanel.repaint();
					}
				}
			});

		}
		return pageSelectionCombo;
	}

	class PageChangeHandler implements ActionListener {

		private boolean forward;

		PageChangeHandler(boolean forward) {
			this.forward = forward;
		}

		public void actionPerformed(ActionEvent e) {
			int currentIndex = getPageSelectionCombo().getSelectedIndex();
			if (forward && currentIndex < getPageSelectionCombo().getItemCount() - 1) {
				currentIndex++;
				getPageSelectionCombo().setSelectedIndex(currentIndex);
			} else if (!forward && currentIndex > 0) {
				currentIndex--;
				getPageSelectionCombo().setSelectedIndex(currentIndex);
			}
		}

	} // inner class BackButtonListener

	class UIHandlerLisnterForFrame implements UIHandlerListener {

		@Override
		public void editingModeChanged(int newMode) {
			AbstractButton selectedButton = null;
			if (newMode == UIHandler.EDIT_MODE_DRAW) {
				selectedButton = getButtonDraw();
			} else if (newMode == UIHandler.EDIT_MODE_SELECT) {
				selectedButton = getButtonSelect();
			}
			selectedButton.setSelected(true);
			Enumeration<AbstractButton> otherButtons = rectButtonGroup.getElements();
			while (otherButtons.hasMoreElements()) {
				AbstractButton otherButton = otherButtons.nextElement();
				if (selectedButton != otherButton) {
					otherButton.setSelected(false);
				}
			}
		}

		@Override
		public void pageChanged(int index) {

		}

		@Override
		public void pageGroupSelected(PageGroup pageGroup) {
			// update combo page list
			int pageCount = pageGroup.getPageCount();
			JComboBox<String> combo = getPageSelectionCombo();
			combo.removeAllItems();
			if (pageCount > 1) {
				combo.addItem(Bundle.getString("PdfScissorsMainFrame.StackedView"));
			}
			for (int i = 0; i < pageCount; i++) {
				combo.addItem(String.valueOf(pageGroup.getPageNumberAt(i)));
			}
			combo.setSelectedIndex(0);
			if (pageCount <= 1) {
				forwardButton.setVisible(false);
				forwardButton.setEnabled(false);
				backButton.setVisible(false);
				backButton.setEnabled(false);
			} else {
				forwardButton.setVisible(true);
				forwardButton.setEnabled(true);
				backButton.setVisible(true);
				backButton.setEnabled(true);
			}
			getScrollPanel().revalidate();

			if(uiHandler.getRectCount() > 0) {
				uiHandler.setEditingMode(UIHandler.EDIT_MODE_SELECT);
			} else {
				uiHandler.setEditingMode(UIHandler.EDIT_MODE_DRAW);
			}
		}

		@Override
		public void rectsStateChanged() {
			getPageGroupList().repaint();
		}

	}

	/**
	 * This method initializes jJMenuBar
	 *
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getMenuFile());
			jJMenuBar.add(getMenuEdit());
			jJMenuBar.add(getMenuHelp());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes menuFile
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getMenuFile() {
		if (menuFile == null) {
			menuFile = new JMenu(Bundle.getString("PdfScissorsMainFrame.File"));
			menuFile.setMnemonic(KeyEvent.VK_F);
			menuFile.add(getMenuSave());
			menuFile.add(getMenuSaveAs());
		}
		return menuFile;
	}

	/**
	 * This method initializes menuSave
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuSaveAs() {
		if (menuSaveAs == null) {
			menuSaveAs = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.CropAndSaveAs"), KeyEvent.VK_S);
			menuSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			menuSaveAs.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					openSaveFileChooser();
				}
			});
		}
		return menuSaveAs;
	}

	/**
	 * This method initializes menuSave
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuSave() {
		if (menuSave == null) {
			menuSave = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.CropAndSave"), KeyEvent.VK_U);
			menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
			menuSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(saveFile != null) {
						saveFile(saveFile);
					} else {
						openSaveFileChooser();
					}
				}
			});
		}
		return menuSave;
	}

	/**
	 * This method initializes menuEdit
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getMenuEdit() {
		if (menuEdit == null) {
			menuEdit = new JMenu(Bundle.getString("PdfScissorsMainFrame.Edit"));
			menuEdit.setMnemonic(KeyEvent.VK_E);
			menuEdit.add(getMenuCopy());
			menuEdit.add(getMenuCut());
			menuEdit.add(getMenuPaste());
			menuEdit.add(getMenuDelete());
		}
		return menuEdit;
	}

	/**
	 * This method initializes menuCopy
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuCopy() {
		if (menuCopy == null) {
			menuCopy = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.Copy"), KeyEvent.VK_C);
			menuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
			menuCopy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Model.getInstance().copyToClipboard(false, uiHandler.getSelectedRect());
				}
			});
			openFileDependendComponents.add(menuCopy);
		}
		return menuCopy;
	}

	/**
	 * This method initializes menuCut
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuCut() {
		if (menuCut == null) {
			menuCut = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.Cut"), KeyEvent.VK_X);
			menuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
			menuCut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Model.getInstance().copyToClipboard(true, uiHandler.getSelectedRect());
				}
			});
			openFileDependendComponents.add(menuCut);

		}
		return menuCut;
	}

	/**
	 * This method initializes menuPaste
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuPaste() {
		if (menuPaste == null) {
			menuPaste = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.Paste"), KeyEvent.VK_V);
			menuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
			if (Model.getInstance().getClipboardRect() == null) {
				menuPaste.setEnabled(false);
			}
			menuPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Model.getInstance().pasteFromClipboard();
				}
			});
			openFileDependendComponents.add(menuPaste);
		}
		return menuPaste;
	}

	/**
	 * This method initializes menuDelete
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuDelete() {
		if (menuDelete == null) {
			menuDelete = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.Delete"), KeyEvent.VK_D);
			menuDelete.setAccelerator(KeyStroke.getKeyStroke(Bundle.getString("PdfScissorsMainFrame.Delete").toUpperCase()));
			if (Model.getInstance().getClipboardRect() == null) {
				menuPaste.setEnabled(false);
			}
			menuDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					uiHandler.deleteSelected();
				}
			});
			openFileDependendComponents.add(menuDelete);
		}
		return menuDelete;
	}

	private void showDialogNoRectYet() {
		JOptionPane.showMessageDialog(PdfScissorsMainFrame.this, Bundle.getString("PdfScissorsMainFrame.SelectRectangleTool"));
	}

	/**
	 * This method initializes menuHelp
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getMenuHelp() {
		if (menuHelp == null) {
			menuHelp = new JMenu(Bundle.getString("PdfScissorsMainFrame.Help"));
			menuHelp.setMnemonic(KeyEvent.VK_H);
			menuHelp.add(getMenuAbout());
		}
		return menuHelp;
	}

	/**
	 * This method initializes menuAbout
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuAbout() {
		if (menuAbout == null) {
			menuAbout = new JMenuItem(Bundle.getString("PdfScissorsMainFrame.About"), KeyEvent.VK_A);
			menuAbout.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AboutView(PdfScissorsMainFrame.this).setVisible(true);
				}
			});
		}
		return menuAbout;
	}

}
