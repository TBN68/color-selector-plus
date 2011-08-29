/*
 * Color Selector Plus
 * Advanced Color Selector tool for Processing IDE
 *
 * Copyright (C) 2011 Manindra Moharana.
 */

package backUps;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import processing.app.*;
import processing.core.*;
import java.io.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ColorSelector2 extends JFrame implements KeyListener,
		WindowFocusListener {
	private ZoomScreen zoomScreen;
	private JTextField txtcolorValue;
	private JTextField txtB;
	private JTextField txtG;
	private JTextField txtR;
	private JTextField txtH;
	private JTextField txtS;
	private JTextField txtV;
	private JPanel selectedColor;
	private JPanel tabColorPicker, panelColorPallete;
	private JLabel lblMsg;
	private JPanel panelZoomView;
	private JSlider zoomSlider;
	JPanel tabColorMixer;

	// Normal panel border
	final BevelBorder bev = new BevelBorder(BevelBorder.LOWERED, null, null,
			null, null);

	// Selected panel border
	final LineBorder line = new LineBorder(new Color(0, 0, 0), 2);
	private final MixerApplet mixerApplet;
	private final JSlider hueSlider;
	private final JPanel mixerPanel;
	ArrayList<Color> standardColors = new ArrayList<Color>();
	final JButton btnAddToPalette;
	Editor editor;
	String currentFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ColorSelector2 frame = new ColorSelector2();
					frame.setLocation(400, 40);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// System.exit(0);
	}

	
	/**
	 * Create the frame. Add all components to it. I've been using the Eclipse
	 * SWT Designer plugin to place components, so haven't used too many layout
	 * managers.
	 */
	public ColorSelector2() {
		// Must change the next line while building tool! Had 12 instances
		// running once with dispose_on_close!
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Base.setIcon(this);
		setResizable(false);
		setTitle("Color Selector Plus");
		addWindowFocusListener(this);
		setAlwaysOnTop(true);
		setLocation(300, 50);

		// To get the System look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Unable to load System look and feel");
		}

		addKeyListener(this);
		addWindowFocusListener(this);

		setBounds(300, 50, 338, 695);// setBounds(100, 100, 338, 688);
		// Absolute layout
		getContentPane().setLayout(null);

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		// Used to activate the selectedColorPanel for grabbing color on
		// switching tabs.
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				getSelectedPalletePanel();
			}
		});
		tabbedPane.setBounds(0, 0, 334, 378);
		getContentPane().add(tabbedPane);

		tabColorMixer = new JPanel();
		// tabColorMixer.addKeyListener(this);
		tabColorMixer.setFocusable(true);
		tabColorMixer.setName("ColorMixer");
		tabColorMixer.setFocusTraversalKeysEnabled(true);
		tabbedPane.addTab("Color Mixer", null, tabColorMixer, null);
		tabColorMixer.setLayout(null);

		hueSlider = new JSlider();
		hueSlider.setValue(100);
		hueSlider.setPaintTicks(true);
		hueSlider.setOrientation(SwingConstants.VERTICAL);
		hueSlider.setMinimum(0);
		hueSlider.setMaximum(255);
		hueSlider.setFocusable(false);
		hueSlider.setBounds(0, 11, 31, 260);
		tabColorMixer.add(hueSlider);

		mixerPanel = new JPanel();
		mixerPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		mixerPanel.setBounds(59, 11, 260, 260);
		tabColorMixer.add(mixerPanel);
		mixerPanel.setLayout(new BorderLayout());
		mixerApplet = new MixerApplet();
		mixerApplet.init();
		mixerPanel.add(mixerApplet, BorderLayout.CENTER);
		mixerApplet.hue = (int) (359 * (hueSlider.getValue() / 255.0f));
		hueSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				mixerApplet.hue = (int) (359 * (hueSlider.getValue() / 255.0f));
				// System.out
				// .println(mixerApplet.hue + "," + mixerApplet.saturation
				// + "," + mixerApplet.brightness);
				setColorValue(mixerApplet.getSelectedColorRGB());
				selectedColor.setBackground(mixerApplet.getSelectedColorRGB());
			}
		});

		HuePanel panelHue = new HuePanel();
		panelHue.setBorder(new LineBorder(new Color(0, 0, 0)));
		panelHue.setBounds(32, hueSlider.getY() + 1, 20, 256);
		panelHue.setLayout(new BorderLayout());
		panelHue.addMouseListener(new MouseAdapter() {
			@Override
			// Clicking on the hue panel selects the hue
			public void mousePressed(MouseEvent e) {
				hueSlider.setValue(hueSlider.getMaximum() - e.getY());
			}
		});

		panelHue.addMouseMotionListener(new MouseMotionAdapter() {
			// Dragging does the same
			public void mouseDragged(MouseEvent e) {
				hueSlider.setValue(hueSlider.getMaximum() - e.getY());
			}
		});

		tabColorMixer.add(panelHue);
		populateStandardColors();
		JPanel standardColorpanel = new JPanel();
		standardColorpanel.setBounds(10, 282, 309, 57);

		// Add a 2x8 palette containg standard colors
		// Each of them has their mouse event handlers or selction/deselection
		int sRow = 2, sCol = 8;
		standardColorpanel.setLayout(new GridLayout(sRow, sCol, 7, 7));
		for (int i = 0; i < sRow * sCol; i++) {
			final JPanel standardColorPalette = new JPanel();
			standardColorPalette.setBorder(bev);
			standardColorPalette.setBackground(standardColors.get(i));
			standardColorPalette.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						// Selected a pallete panel
						if (standardColorPalette.getBorder().equals(bev)) {

							for (int j = 0; j < standardColorPallete.size(); j++) {
								standardColorPallete.get(j).setBorder(bev);
							}
							standardColorPalette.setBorder(line);

							// When selecting a palette panel, only panels
							// containg non white colors should set the value of
							// selectedColor panel

							if (!standardColorPalette.getBackground().equals(
									Color.WHITE)) {
								selectedColor
										.setBackground(standardColorPalette
												.getBackground());
								hueSlider.setValue(getHue(standardColorPalette
										.getBackground()));
								setColorValue(standardColorPalette
										.getBackground());
							}
						}
					}

				}
			});

			standardColorpanel.add(standardColorPalette);
			standardColorPallete.add(standardColorPalette);
		}
		standardColorPallete.get(0).setBorder(line);

		tabColorMixer.add(standardColorpanel);

		tabColorPicker = new JPanel();

		tabColorPicker.setFocusCycleRoot(true);
		tabColorPicker.setName("ColorPicker");
		tabColorPicker.setLayout(null);
		tabbedPane.addTab("Color Picker", null, tabColorPicker, null);

		panelZoomView = new JPanel();
		panelZoomView.setBorder(new BevelBorder(BevelBorder.LOWERED, null,
				null, null, null));
		panelZoomView.setBounds(14, 11, 300, 300);
		tabColorPicker.add(panelZoomView);
		panelZoomView.setLayout(new BorderLayout());

		zoomScreen = new ZoomScreen();
		zoomScreen.init();
		panelZoomView.add(zoomScreen);

		zoomSlider = new JSlider();
		zoomSlider.setValue(35);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				zoomScreen.zoomLevel = zoomSlider.getMaximum() + 5
						- zoomSlider.getValue();
			}
		});
		zoomSlider.setFocusable(false);
		zoomSlider.setSnapToTicks(true);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setMinorTickSpacing(10);
		zoomSlider.setMinimum(5);
		zoomSlider.setMaximum(75);
		zoomSlider.setMajorTickSpacing(10);
		zoomSlider.setBounds(44, 316, 113, 29);
		tabColorPicker.add(zoomSlider);

		JLabel label = new JLabel("Zoom");
		label.setFocusable(false);
		label.setFocusTraversalKeysEnabled(false);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(14, 322, 35, 14);
		tabColorPicker.add(label);

		lblMsg = new JLabel(" Press Spacebar to grab a Color");
		lblMsg.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblMsg.setFocusTraversalKeysEnabled(false);
		lblMsg.setFocusable(false);
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		lblMsg.setBounds(156, 316, 163, 29);
		tabColorPicker.add(lblMsg);

		panelColorPallete = new JPanel();
		panelColorPallete.setBounds(5, 527, 322, 99);
		getContentPane().add(panelColorPallete);
		int pRow = 3, pCol = 8;
		panelColorPallete.setLayout(new GridLayout(pRow, pCol, 7, 7));

		JLabel lblPalette = new JLabel("Palette");
		lblPalette.setBounds(10, 509, 46, 14);
		getContentPane().add(lblPalette);

		JSeparator separator = new JSeparator();
		separator.setBounds(50, 517, 273, 2);
		getContentPane().add(separator);

		// Add the main color panel containg the color slots
		for (int i = 0; i < pRow * pCol; i++) {
			final JPanel currentPalettePanel = new JPanel();
			currentPalettePanel
					.setToolTipText("Press right mouse button to clear");
			currentPalettePanel.setBorder(bev);
			if (i == 0)
				currentPalettePanel.setBorder(line);
			currentPalettePanel.setBackground(new Color(255, 255, 255));
			currentPalettePanel.setFocusable(true);
			currentPalettePanel.addKeyListener(this);
			currentPalettePanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						// Selected a pallete panel
						if (currentPalettePanel.getBorder().equals(bev)) {

							for (int j = 0; j < palletePanels.size(); j++) {
								palletePanels.get(j).setBorder(bev);
							}
							currentPalettePanel.setBorder(line);

							// When selecting a palette panel, only panels
							// containg non white colors should set the value of
							// selectedColor panel
							getSelectedPalletePanel();
							if (!currentPalettePanel.getBackground().equals(
									Color.WHITE)) {
								selectedColor.setBackground(currentPalettePanel
										.getBackground());
								// mixerApplet.setColor(temp.getBackground().get,
								// y)
								hueSlider.setValue(getHue(currentPalettePanel
										.getBackground()));
								setColorValue(currentPalettePanel
										.getBackground());
								// hueSlider.setValue(te)
							}
						}
					}

					// Right click clears the palette and selects it
					if (e.getButton() == MouseEvent.BUTTON3) {
						currentPalettePanel.setBackground(new Color(255, 255,
								255));
						for (int j = 0; j < palletePanels.size(); j++) {
							palletePanels.get(j).setBorder(bev);
						}
						currentPalettePanel.setBorder(line);
						getSelectedPalletePanel();
					}

				}
			});

			panelColorPallete.add(currentPalettePanel);
			palletePanels.add(currentPalettePanel);
		}

		// add the keylisteners. Boring...
		tabbedPane.addKeyListener(this);
		//
		tabColorPicker.addKeyListener(this);

		// add key listeners to all components in color picker
		// required for the spacebar color grab shortcut
		// for (int i = 0; i < tabColorPicker.getComponentCount(); i++) {
		// tabColorPicker.getComponent(i).addKeyListener(this);
		// }

		JButton btnSavePalette = new JButton("Save");
		btnSavePalette.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				savePaletteData();
				saveCurrentPalettePath();
			}
		});
		btnSavePalette.setFocusable(false);
		btnSavePalette.setBounds(10, 637, 57, 23);
		getContentPane().add(btnSavePalette);

		JButton btnLoadPalette = new JButton("Load");
		btnLoadPalette.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadPaletteData(getLoadingPath());
				saveCurrentPalettePath();
			}
		});
		btnLoadPalette.setFocusable(false);
		btnLoadPalette.setBounds(158, 637, 73, 23);
		getContentPane().add(btnLoadPalette);

		JButton btnClearAll = new JButton("Clear All");
		btnClearAll.setFocusable(false);
		btnClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < palletePanels.size(); i++) {
					palletePanels.get(i).setBackground(Color.WHITE);
					palletePanels.get(i).setBorder(bev);
				}
				palletePanels.get(0).setBorder(line);
				currentFile = "";
			}
		});
		btnClearAll.setBounds(241, 637, 81, 23);
		getContentPane().add(btnClearAll);

		selectedColor = new JPanel();
		selectedColor.setBounds(244, 401, 72, 75);
		getContentPane().add(selectedColor);

		selectedColor.setFocusable(false);
		selectedColor.setFocusTraversalKeysEnabled(false);
		selectedColor.setBorder(new LineBorder(new Color(0, 0, 0)));
		selectedColor.setBackground(SystemColor.menu);
		selectedColor.setBackground(Color.WHITE);

		txtB = new JTextField();
		txtB.setBackground(Color.WHITE);
		txtB.setEditable(false);
		txtB.setBounds(202, 402, 33, 20);
		getContentPane().add(txtB);
		txtB.setText("0");
		txtB.setHorizontalAlignment(SwingConstants.RIGHT);
		txtB.setColumns(3);

		JLabel label_3 = new JLabel("Green");
		label_3.setBounds(86, 404, 46, 14);
		getContentPane().add(label_3);
		label_3.setFocusTraversalKeysEnabled(false);
		label_3.setFocusable(false);
		label_3.setHorizontalAlignment(SwingConstants.CENTER);

		txtG = new JTextField();
		txtG.setBackground(Color.WHITE);
		txtG.setEditable(false);
		txtG.setBounds(130, 401, 33, 20);
		getContentPane().add(txtG);
		txtG.setText("0");
		txtG.setHorizontalAlignment(SwingConstants.RIGHT);
		txtG.setColumns(3);

		txtR = new JTextField();
		txtR.setBackground(Color.WHITE);
		txtR.setEditable(false);
		txtR.setBounds(56, 402, 33, 20);
		getContentPane().add(txtR);
		txtR.setInheritsPopupMenu(true);
		txtR.setText("0");
		txtR.setHorizontalAlignment(SwingConstants.RIGHT);
		txtR.setColumns(3);

		txtH = new JTextField();
		txtH.setBackground(Color.WHITE);
		txtH.setEditable(false);
		txtH.setBounds(56, 431, 33, 20);
		getContentPane().add(txtH);
		txtH.setText("0");
		txtH.setHorizontalAlignment(SwingConstants.RIGHT);
		txtH.setColumns(3);

		JLabel label_6 = new JLabel("Sat");
		label_6.setBounds(86, 433, 46, 14);
		getContentPane().add(label_6);
		label_6.setFocusTraversalKeysEnabled(false);
		label_6.setFocusable(false);
		label_6.setHorizontalAlignment(SwingConstants.CENTER);

		txtS = new JTextField();
		txtS.setBackground(Color.WHITE);
		txtS.setEditable(false);
		txtS.setBounds(130, 430, 33, 20);
		getContentPane().add(txtS);
		txtS.setText("0");
		txtS.setHorizontalAlignment(SwingConstants.RIGHT);
		txtS.setColumns(3);

		JLabel label_7 = new JLabel("Val");
		label_7.setBounds(158, 434, 46, 14);
		getContentPane().add(label_7);
		label_7.setFocusTraversalKeysEnabled(false);
		label_7.setFocusable(false);
		label_7.setHorizontalAlignment(SwingConstants.CENTER);

		txtV = new JTextField();
		txtV.setBackground(Color.WHITE);
		txtV.setEditable(false);
		txtV.setBounds(202, 431, 33, 20);
		getContentPane().add(txtV);
		txtV.setText("0");
		txtV.setHorizontalAlignment(SwingConstants.RIGHT);
		txtV.setColumns(3);

		JLabel label_8 = new JLabel("Blue");
		label_8.setBounds(158, 405, 46, 14);
		getContentPane().add(label_8);
		label_8.setFocusTraversalKeysEnabled(false);
		label_8.setFocusable(false);
		label_8.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel label_5 = new JLabel("Hue");
		label_5.setBounds(12, 434, 46, 14);
		getContentPane().add(label_5);
		label_5.setFocusTraversalKeysEnabled(false);
		label_5.setFocusable(false);
		label_5.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel label_4 = new JLabel("Red");
		label_4.setBounds(12, 405, 46, 14);
		getContentPane().add(label_4);
		label_4.setFocusTraversalKeysEnabled(false);
		label_4.setFocusable(false);
		label_4.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel label_2 = new JLabel("Hex");
		label_2.setBounds(140, 459, 46, 14);
		getContentPane().add(label_2);
		label_2.setFocusTraversalKeysEnabled(false);
		label_2.setFocusable(false);
		label_2.setHorizontalAlignment(SwingConstants.CENTER);

		txtcolorValue = new JTextField();
		txtcolorValue.setBackground(Color.WHITE);
		txtcolorValue.setEditable(false);
		txtcolorValue.setBounds(180, 456, 55, 20);
		getContentPane().add(txtcolorValue);
		txtcolorValue.setFocusTraversalKeysEnabled(false);
		txtcolorValue.setColumns(7);

		JButton btnCopyToClipboard = new JButton("Copy hex to Clipboard");
		btnCopyToClipboard.setBounds(29, 483, 146, 23);
		getContentPane().add(btnCopyToClipboard);
		btnCopyToClipboard.setFocusable(false);

		btnAddToPalette = new JButton("Add to Palette");
		btnAddToPalette.setBounds(190, 483, 111, 23);
		getContentPane().add(btnAddToPalette);
		btnAddToPalette.setFocusable(false);
		btnAddToPalette.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int t = getSelectedPalletePanel();
				if (t >= 0 && t < palletePanels.size()) {
					palletePanels.get(t).setBackground(
							selectedColor.getBackground());

					for (int i = 0; i < palletePanels.size(); i++) {
						if (palletePanels.get(i).getBackground()
								.equals(Color.WHITE)) {
							palletePanels.get(t).setBorder(bev);
							palletePanels.get(i).setBorder(line);
							break;
						}
					}
				} else {
					// Exception
					JOptionPane.showMessageDialog(btnAddToPalette,
							"Error: Panel Index returned :" + t);
				}
			}
		});
		btnCopyToClipboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				copyToClipboard(txtcolorValue.getText());
			}
		});

		setColorValue(Color.WHITE);
		txtH.setText(hueSlider.getValue() + "");

		JLabel lblColor = new JLabel("Color");
		lblColor.setBounds(10, 382, 46, 14);
		getContentPane().add(lblColor);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(40, 390, 283, 2);
		getContentPane().add(separator_1);

		JButton btnSaveAs = new JButton("Save As");
		btnSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				currentFile = "";
				savePaletteData();
				saveCurrentPalettePath();
			}
		});
		btnSaveAs.setBounds(77, 637, 71, 23);
		getContentPane().add(btnSaveAs);

		loadPaletteData(loadPreviousPalette());

	}

	// Add the standard colors to the colorList
	private void populateStandardColors() {

		standardColors.add(new Color(0x880015));
		standardColors.add(new Color(0xED1C24));
		standardColors.add(new Color(0xFF7F27));
		standardColors.add(new Color(0xFFF200));
		standardColors.add(new Color(0x22B14C));
		standardColors.add(new Color(0x00A2E8));
		standardColors.add(new Color(0x3F48CC));
		standardColors.add(new Color(0xA349A4));
		standardColors.add(new Color(0xC3C3C3));
		standardColors.add(new Color(0xB97A57));
		standardColors.add(new Color(0xFFAEC9));
		standardColors.add(new Color(0xFFC90E));
		standardColors.add(new Color(0xEFE4B0));
		standardColors.add(new Color(0xB5E61D));
		standardColors.add(new Color(0x99D9EA));
		standardColors.add(new Color(0x7092BE));
		standardColors.add(new Color(0xC8BFE7));
	}

	// Get hue valur of a color in the range of 0-255
	public int getHue(Color pointColor) {
		float hsb[] = Color.RGBtoHSB(pointColor.getRed(),
				pointColor.getGreen(), pointColor.getBlue(), null);
		return (int) (hsb[0] * 255);
	}

	ArrayList<JPanel> palletePanels = new ArrayList<JPanel>();
	ArrayList<JPanel> standardColorPallete = new ArrayList<JPanel>();

	public void grabColor() {
		setColorValue(zoomScreen.pointColor);
		btnAddToPalette.doClick();
	}

	void copyToClipboard(String s) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable text = new StringSelection(s);
		clipboard.setContents(text, null);
	}

	// Save the colors(their int values) in a binary file
	private void savePaletteData() {
		String data = "";
		for (int i = 0; i < palletePanels.size(); i++) {
			data += palletePanels.get(i).getBackground().getRGB() + ",";
		}
		String fileName;
		if (currentFile == "") {
			FileDialog fd = new FileDialog(new Frame(), "Save Palette...",
					FileDialog.SAVE);
			fd.setFile("*.*");
			editor.getSketch().prepareDataFolder();
			fd.setDirectory(editor.getSketch().getDataFolder()
					.getAbsolutePath());
			fd.setLocation(50, 50);
			fd.setVisible(true);
			fileName = fd.getFile();
			if (fileName == null) {
				return;
			}
			fileName = fd.getDirectory() + File.separator + fileName
					+ ".pallete";
			// System.out.println("Saving as new palette.");
		} else {
			fileName = currentFile;
			// System.out.println("Updating palette: " + fileName);
		}

		File dataFile = new File(fileName);
		try {
			dataFile.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(dataFile);
			DataOutputStream dataOut = new DataOutputStream(fileOut);
			for (int i = 0; i < palletePanels.size(); i++) {
				dataOut.writeInt(palletePanels.get(i).getBackground().getRGB());
			}
			fileOut.close();
			System.out.println("Palette Saved.");
			currentFile = dataFile.getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Saves the path of the current .palette file in a settings file so that it
	// is loaded next
	// time automatically when the app is restarted.
	private boolean saveCurrentPalettePath() {
		if (currentFile == null || currentFile == "")
			return false;
		File dataFile = new File("settings.temp");
		try {
			dataFile.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(dataFile);
			DataOutputStream dataOut = new DataOutputStream(fileOut);

			dataOut.writeChars(currentFile);
			fileOut.close();
			// System.out.println("Saved palette path: " + currentFile);
			File oldFile = new File("settings");
			if (oldFile.exists()) {
				if (oldFile.delete()) {
					// System.out.println("Old settings deleted.");

				} else {
					// System.out.println("Couldn't update settings.");
				}
			}

			dataFile.renameTo(new File("ColorSelectorPlusSettings"));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	// Loads the previous loaded .palette file
	private String loadPreviousPalette() {
		File dataFile = new File("ColorSelectorPlusSettings");
		if (!dataFile.exists()) {
			// System.out.println("settings file doesn't exist.");
			currentFile = "";
			return "";
		}
		String filePath = "";
		try {
			FileInputStream file_input;
			file_input = new FileInputStream(dataFile);
			DataInputStream dataIn = new DataInputStream(file_input);
			while (true) {
				try {
					filePath += dataIn.readChar();
				} catch (EOFException eof) {
					// System.out.println("File : " + filePath);
					break;

				} catch (IOException e) {
					// System.out
					// .println("settings/last loaded file doesn't exist.");
					filePath = "";
					e.printStackTrace();
				}
			}

			if (filePath == "") {
				return filePath;
			} else {
				// System.out.println("Last loaded file: " + filePath);
				return filePath;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// Gets the path of the .palette file intended to be loaded
	private String getLoadingPath() {
		FileDialog fd = new FileDialog(new Frame(), "Load Palette...",
				FileDialog.LOAD);
		fd.setFile("*.*");
		// fd.setDirectory("C:\\Users\\20012107\\Documents");
		editor.getSketch().prepareDataFolder();
		fd.setDirectory(editor.getSketch().getDataFolder().getAbsolutePath());
		fd.setLocation(50, 50);
		fd.setVisible(true);
		String fileName = fd.getFile();
		if (fileName == null) {
			return "";
		}
		return (fd.getDirectory() + File.separator + fileName);
	}

	// Loads palette data from the path fileName
	private void loadPaletteData(String fileName) {

		if (fileName == null || fileName == "")
			return;
		File dataFile = new File(fileName);
		// System.out.println("Loading file: " + dataFile.getAbsolutePath());

		try {
			// Wrap the FileInputStream with a DataInputStream
			FileInputStream file_input = new FileInputStream(dataFile);
			DataInputStream dataIn = new DataInputStream(file_input);

			for (int i = 0; i < palletePanels.size(); i++) {
				Color c;
				int cInt;
				try {
					cInt = dataIn.readInt();
					c = new Color(cInt);
				} catch (IOException e) {
					System.out.println("IO Exception : " + e);
					c = Color.WHITE;
				} catch (Exception e) {
					System.out
							.println("Some data in the file is corrupt. Failed to load all palette colors.");
					c = Color.WHITE;
				}
				palletePanels.get(i).setBackground(c);
			}

			int k = 0;
			for (int i = 0; i < palletePanels.size(); i++) {
				if (palletePanels.get(i).getBackground().equals(Color.WHITE)) {
					k = i;
					break;
				}
			}

			for (int i = 0; i < palletePanels.size(); i++) {
				if (k == i) {
					palletePanels.get(i).setBorder(line);
				} else {
					palletePanels.get(i).setBorder(bev);
				}
			}

			dataIn.close();
			currentFile = dataFile.getAbsolutePath();
		} catch (IOException e) {
			System.out.println("IO Exception: " + e);
		}
	}

	// Flag to check if selected color is blank(white)
	boolean isBlankPanelSelected = true;

	// Returns the index of teh currently selected panel
	public int getSelectedPalletePanel() {
		for (int i = 0; i < palletePanels.size(); i++) {
			if (palletePanels.get(i).getBorder().equals(line)) {
				if (palletePanels.get(i).getBackground().equals(Color.WHITE))
					isBlankPanelSelected = true;
				else
					isBlankPanelSelected = false;
				return i;
			}

		}
		return -1;
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (tabColorPicker.isVisible()) {
			if (!isBlankPanelSelected)
				System.out
						.println("Select an empty panel from the palette to grab a color. Right click to clear a panel.");
			if (e.getKeyCode() == KeyEvent.VK_SPACE && isBlankPanelSelected) {

				setColorValue(zoomScreen.pointColor);
				btnAddToPalette.doClick();
				// System.out.println("Grabbed.");
			}

		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	public void setColorValue(Color pointColor) {
		txtcolorValue.setText("#"
				+ Integer.toHexString(pointColor.getRGB()).substring(2)
						.toUpperCase());
		txtR.setText(pointColor.getRed() + "");
		txtG.setText(pointColor.getGreen() + "");
		txtB.setText(pointColor.getBlue() + "");
		float hsb[] = Color.RGBtoHSB(pointColor.getRed(),
				pointColor.getGreen(), pointColor.getBlue(), null);
		txtH.setText((int) (hsb[0] * 360) + "");
		txtS.setText((int) (hsb[1] * 255) + "");
		txtV.setText((int) (hsb[2] * 255) + "");
		selectedColor.setBackground(pointColor);
		mixerApplet.setColor((int) (hsb[1] * 255), 255 - (int) (hsb[2] * 255));

	}

	// The magnifier applet
	private class ZoomScreen extends Applet implements Runnable, KeyListener {

		public int imgWidth = 300;
		public int imgHeight = 300;
		private ZoomPanel zoomPanel;
		private JPanel parentPanel;

		public void init() {
			mouseC = mouseP = MouseInfo.getPointerInfo().getLocation();
			parentPanel = new JPanel();
			zoomPanel = new ZoomPanel();
			setLayout(new BorderLayout());
			parentPanel.setLayout(new BorderLayout());
			parentPanel.add(zoomPanel, BorderLayout.CENTER);
			add(parentPanel);
			try {
				robot = new Robot(gs[0]);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread th = new Thread(this);
			th.start();
			addKeyListener(this);
		}

		public int zoomLevel = 25; // 15 to 55, with steps of 10
		private GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		private GraphicsDevice[] gs = ge.getScreenDevices();
		private Rectangle bounds;
		private BufferedImage desktop = new BufferedImage(zoomLevel, zoomLevel,
				BufferedImage.TYPE_INT_RGB);
		private BufferedImage resizedImage;
		private Robot robot;

		private BufferedImage getScreen() {

			// zoomLevel = 12;

			bounds = new Rectangle(mouseC.x - zoomLevel / 2, mouseC.y
					- zoomLevel / 2, zoomLevel, zoomLevel);
			desktop = new BufferedImage(zoomLevel, zoomLevel,
					BufferedImage.TYPE_INT_RGB);

			desktop = robot.createScreenCapture(bounds);
			resizedImage = new BufferedImage(imgWidth, imgHeight,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(desktop, 0, 0, imgWidth, imgHeight, null);
			g.dispose();
			return resizedImage;
		}

		private int zm;
		private Point mouseC, mouseP;

		// Continuously listen for mouse movement and repaint applet
		public void run() {
			while (true) {
				mouseC = MouseInfo.getPointerInfo().getLocation();
				if (tabColorPicker.isVisible())
					if (!mouseC.equals(mouseP)) {
						zoomPanel.repaint();
						mouseP = mouseC;

					}

			}
		}

		public Color pointColor = new Color(0);
		public BufferedImage screen;

		// The panel that draws the zoomed image
		// Image wasn't directly drawn onto the applet due to weird refresh
		// rates
		// Refreshing was insonsistent and showed lag
		private class ZoomPanel extends JPanel {
			// private BufferedImage image;

			@Override
			public void paintComponent(Graphics g) {
				// g.drawImage(image, 0, 0, null);
				zm = imgHeight / zoomLevel;
				screen = getScreen();
				g.drawImage(screen, 0, 0, this);
				g.setColor(Color.black);
				g.drawRect(imgWidth / 2 - zm / 2, imgHeight / 2 - zm / 2, zm,
						zm);
				pointColor = new Color(screen.getRGB(imgWidth / 2 + 1,
						imgHeight / 2));
				if (tabColorPicker.isVisible() && isBlankPanelSelected) {
					selectedColor.setBackground(pointColor);
					setColorValue(pointColor);
				}

			}
		}

		@Override
		// Spacebar to grab a color
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				setColorValue(pointColor);
				btnAddToPalette.doClick();
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	// Panel displaying the hue range
	private class HuePanel extends JPanel implements MouseListener,
			MouseMotionListener {

		public void paintComponent(Graphics g) {
			// super.paint(g);
			int h = 0;
			for (int i = this.getHeight(); i >= 0; i--) {
				g.setColor(new Color(Color.HSBtoRGB(
						i / ((float) this.getHeight() - 1), 1.0f, 1.0f)));
				g.drawLine(0, h, this.getWidth(), h);
				h++;
			}
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseDragged(MouseEvent arg0) {

		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

	}

	// The color mixer applet
	// Code borowed from ColorSelector.java - part of the processing
	// distribution
	// loadPixels() and updatePixels() are more smooth at updating the panel
	// that drawing on the paint
	// area
	private class MixerApplet extends PApplet {

		static final int WIDE = 256;
		static final int HIGH = 256;
		boolean firstRun = true;
		int lastX, lastY;
		int hue = 200, brightness = 0, saturation = 0;

		public void setup() {
			size(WIDE, HIGH); // , P3D);

			colorMode(HSB, 360, 256, 256);
			noFill();
			rectMode(CENTER);
			loadPixels();

		}

		public Color getSelectedColorRGB() {

			Color c = new Color(Color.HSBtoRGB((float) hue / 359f,
					(float) saturation / 255f, (float) brightness / 255f));
			// println(" RGB: " + c.getBlue() + "," + c.getGreen() + ","
			// + c.getRed());
			return c;
		}

		public void draw() {

			if ((width != WIDE) || (height < HIGH)) {
				System.out.println("bad size " + width + " " + height);
				return;
			}

			int index = 0;
			for (int j = 0; j < 256; j++) {
				for (int i = 0; i < 256; i++) {
					g.pixels[index++] = color(hue, i, 255 - j);
				}
			}

			updatePixels();
			stroke((brightness > 100) ? 0 : 255);
			fill(0);
			strokeWeight(2);
			strokeCap(PROJECT);
			if (firstRun) {
				lastX = lastY = 20;
			}
			line(lastX - 10, lastY, lastX - 5, lastY);
			line(lastX + 10, lastY, lastX + 5, lastY);
			line(lastX, lastY - 10, lastX, lastY - 5);
			line(lastX, lastY + 10, lastX, lastY + 5);
			noFill();
			strokeWeight(1);
		}

		public void mousePressed() {
			updateMouse();
		}

		public void mouseDragged() {
			updateMouse();
		}

		public void setColor(int x, int y) {
			if (x >= 0 && x <= 255 && y >= 0 && y <= 255) {
				lastX = x;
				lastY = y;
				brightness = height - lastY - 1;
			} else
				println("Illegal arguments to setColor():" + x + "," + y);
		}

		public void updateMouse() {
			if ((mouseX >= 0) && (mouseX < 256) && (mouseY >= 0)
					&& (mouseY < 256)) {
				brightness = height - mouseY - 1;
				saturation = mouseX;

				firstRun = false;
				lastX = mouseX;
				lastY = mouseY;

				setColorValue(getSelectedColorRGB());
				txtH.setText(hueSlider.getValue() + "");
			}
		}

		public void keyPressed() {
			//println("Mixer applet keypressed");
		}
	}

	public void windowGainedFocus(WindowEvent e) {
		lblMsg.setText("Press Spacebar to grab a Color");
	}

	public void windowLostFocus(WindowEvent e) {
		lblMsg.setText("<html>Click on the Color Selector Plus<br> window to start grabbing a Color</html>");
	}
}
