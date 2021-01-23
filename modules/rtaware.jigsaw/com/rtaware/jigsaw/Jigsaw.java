package com.rtaware.jigsaw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.rtaware.jigsaw.event.JigsawEvent;
import com.rtaware.jigsaw.record.JigsawRecord;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandle;

import java.nio.file.Path;

public class Jigsaw implements ActionListener {
	private JFrame frame;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem openMenuItem;
	private JMenuItem exitMenuItem;
	private int rows = 5;
	private int cols = 5;
	private int chunks = rows * cols;
	private JLabel[] jigsawImagesRight;
	private JLabel[] jigsawImagesLeft;
	private LinkedList<Integer> randomNumbers = new LinkedList<>();
	private LinkedHashMap<Integer, Integer> randomOperands = new LinkedHashMap<>();
	private Random randomNumber = new Random();
	private JPanel leftPanel;
	private JPanel rightPanel;
	private int scaledWidth = 600;
	private int scaledHeight = 400;
	private GridLayout layout = new GridLayout(rows, cols);
	private Border blackline = BorderFactory.createLineBorder(Color.BLACK);
	private String displayTheme = "Nimbus";
	private String operation = "Addition";
	private File basePath;
	private File configPath;
	private File configFile;
	private File impagePath;
	private AtomicInteger attemptCout = new AtomicInteger();

	void setUp() {
		try {

			basePath = new File(System.getProperty("user.home") + File.separator + ".jigsaw");
			if (!basePath.exists()) {
				basePath.mkdir();
			}

			configPath = new File(basePath + File.separator + "conf");
			configFile = new File(basePath + File.separator + "conf" + File.separator + "conf.properties");
			impagePath = new File(basePath + File.separator + "images");

			if (!configPath.exists()) {
				configPath.mkdir();
			}

			if (!configFile.exists()) {
				configFile.createNewFile();
				try (OutputStream output = new FileOutputStream(configFile)) {
					Properties prop = new Properties();
					prop.setProperty("width", Integer.toString(scaledWidth));
					prop.setProperty("height", Integer.toString(scaledHeight));
					prop.setProperty("rows", Integer.toString(rows));
					prop.setProperty("columns", Integer.toString(cols));
					prop.setProperty("theme", displayTheme);
					prop.setProperty("operation", operation);
					prop.store(output, null);
				} catch (IOException io) {
					javax.swing.JOptionPane.showMessageDialog(null, "Unable to Configure");
					io.printStackTrace();
				}
			}

			if (!impagePath.exists()) {
				impagePath.mkdir();
			}

		} catch (IOException ex) {
			javax.swing.JOptionPane.showMessageDialog(null, "Unable to Setup");
			ex.printStackTrace();
			System.exit(0);
		}
	}

	void loadProperties() {
		try (InputStream input = new FileInputStream(configFile)) {
			Properties prop = new Properties();
			prop.load(input);

			scaledWidth = Integer.parseInt(prop.getProperty("width"));
			scaledHeight = Integer.parseInt(prop.getProperty("height"));
			rows = Integer.parseInt(prop.getProperty("rows"));
			cols = Integer.parseInt(prop.getProperty("columns"));
			displayTheme = prop.getProperty("theme");
			operation = prop.getProperty("operation");
			chunks = rows * cols;

		} catch (IOException ex) {
			javax.swing.JOptionPane.showMessageDialog(null, "Unable to Get Properties");
			ex.printStackTrace();
		}
	}

	public int getResultNative(int lhs, int rhs) {
		try {

			String nativeHome = "";
			if (null != System.getenv("NATIVE_HOME")) {
				nativeHome = System.getenv("NATIVE_HOME");
			} else {
				javax.swing.JOptionPane.showMessageDialog(frame, "Please Set Env  NATIVE_HOME");
				System.exit(0);
			}

			LibraryLookup lbLookUp = LibraryLookup.ofPath(Path.of(nativeHome + "/add.so"));
			var lbSymbol = lbLookUp.lookup("add").get();
			FunctionDescriptor funcDesc = FunctionDescriptor.of(CLinker.C_INT, CLinker.C_INT, CLinker.C_INT);
			MethodType methodType = MethodType.methodType(int.class, int.class, int.class);
			MethodHandle methodHandler = CLinker.getInstance().downcallHandle(lbSymbol.address(), methodType, funcDesc);
			int result = (int) methodHandler.invoke(lhs, rhs);
			if(result == (lhs + rhs)) {
			    return result;
			} else {
			    return (lhs + rhs);
			}	

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return (lhs + rhs);
	}

	void SetLookAndFeel() {
		try {
			LookAndFeelInfo[] lf_info = UIManager.getInstalledLookAndFeels();
			for (int i = 0; i < lf_info.length; i++) {
				if (lf_info[i].getName().equals(displayTheme)) {
					UIManager.setLookAndFeel(lf_info[i].getClassName());
					continue;
				}
			}
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
	}

	void splitImages(File imageFile) {
		try {
			FileInputStream fis = new FileInputStream(imageFile);
			BufferedImage inputImage = ImageIO.read(fis);

			BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

			Graphics2D g2d = resizedImage.createGraphics();
			g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
			g2d.dispose();

			ImageIO.write(resizedImage, "jpg", new File(impagePath + File.separator + "resizedImage.jpg"));

			int chunkWidth = resizedImage.getWidth() / cols; // determines the chunk width and height
			int chunkHeight = resizedImage.getHeight() / rows;
			int count = 0;
			BufferedImage imgs[] = new BufferedImage[chunks]; // Image array to hold image chunks
			for (int x = 0; x < rows; x++) {
				for (int y = 0; y < cols; y++) {
					imgs[count] = new BufferedImage(chunkWidth, chunkHeight, resizedImage.getType());
					Graphics2D gr = imgs[count++].createGraphics();
					gr.drawImage(resizedImage, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x,
							chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
					gr.dispose();
				}
			}

			for (int i = 0; i < imgs.length; i++) {
				ImageIO.write(imgs[i], "jpg", new File(impagePath + File.separator + "img" + i + ".jpg"));
			}
			randomizeImage();
			javax.swing.JOptionPane.showMessageDialog(frame, "Image Processed");

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	void randomizeImage() {
		randomNumbers.clear();
		randomOperands.clear();
		for (int i = 0; i < chunks; i++) {
			while (true) {
				Integer randNo = randomNumber.nextInt(chunks);
				Integer randOp = randomNumber.nextInt(12);
				if (!randomNumbers.contains(randNo) && (randOp != 0)) {
					randomNumbers.add(randNo);
					randomOperands.put(randNo, randOp);
					break;
				}
			}
		}
	}

	void paintRightJigsaw() {
		rightPanel.removeAll();
		jigsawImagesRight = null;
		jigsawImagesRight = new JLabel[chunks];
		for (int i = 0; i < chunks; i++) {
			jigsawImagesRight[i] = new JLabel();
			Integer randOp = randomOperands.get(i);
			jigsawImagesRight[i].setToolTipText((i + 1) + getOperand() + randOp);
			jigsawImagesRight[i].addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {

					String operands = ((JLabel) e.getSource()).getToolTipText();
					String status = "FAIL";
					String splitterString = getOperand().trim();

					if (operation.equals("Addition")) {
						splitterString = "\\" + splitterString;
					}
					String[] ops = operands.split(splitterString);

					int leftHand = 0;
					int rightHand = 0;
					if (ops.length >= 1) {
						leftHand = Integer.parseInt(ops[0].trim());
						rightHand = Integer.parseInt(ops[1].trim());
					}

					String inputResult = javax.swing.JOptionPane.showInputDialog(frame,
							"Resolve " + leftHand + getOperand() + rightHand);

					try {
						JigsawEvent jigsawEvent = new JigsawEvent();
						jigsawEvent.begin();

						int iResult = Integer.parseInt(inputResult.trim());
						int result = getResult(leftHand, rightHand);

						if (result == iResult) {
							File imageFile = new File(impagePath + File.separator + "img" + (leftHand - 1) + ".jpg");
							FileInputStream imageIS = new FileInputStream(imageFile);
							BufferedImage imageBuffer = ImageIO.read(imageIS);
							((JLabel) e.getSource()).setIcon(new ImageIcon(imageBuffer));
							status = "PASS";
						}
						JigsawRecord jigsawRecord = new JigsawRecord(attemptCout.incrementAndGet(), leftHand, rightHand,
								operation, iResult, result, status);
						jigsawEvent.setJigsawAttempt(jigsawRecord.jigsawAttempt());
						jigsawEvent.setLhOperand(jigsawRecord.lhOperand());
						jigsawEvent.setRhOperand(jigsawRecord.rhOperand());
						jigsawEvent.setJigsawOperation(jigsawRecord.jigsawOperation());
						jigsawEvent.setEnteredValue(jigsawRecord.enteredValue());
						jigsawEvent.setActualValue(jigsawRecord.actualValue());
						jigsawEvent.setJigsawStatus(jigsawRecord.jigsawStatus());
						jigsawEvent.commit();

					} catch (Exception ex) {

					}

				}
			});
			rightPanel.add(jigsawImagesRight[i]);
		}
		rightPanel.revalidate();

	}

	void paintLeftJigsaw() {
		leftPanel.removeAll();
		jigsawImagesLeft = null;
		jigsawImagesLeft = new JLabel[chunks];
		for (int randNo : randomNumbers) {
			Integer randOp = randomOperands.get(randNo);
			jigsawImagesLeft[randNo] = new JLabel();
			int result = getResult(randNo + 1, randOp);

			jigsawImagesLeft[randNo].setToolTipText("" + result);
			try {
				File imageFile = new File(basePath + "/images/img" + randNo + ".jpg");
				FileInputStream imageIS = new FileInputStream(imageFile);
				BufferedImage imageBuffer = ImageIO.read(imageIS);
				jigsawImagesLeft[randNo].setIcon(new ImageIcon(imageBuffer));
			} catch (Exception ex) {
				ex.printStackTrace();

			}
			leftPanel.add(jigsawImagesLeft[randNo]);
		}
		leftPanel.revalidate();
	}

	private int getResult(int leftHand, int rightHand) {
		int result = 0;
		switch (operation) {
		case "Addition":
			result = getResultNative(leftHand, rightHand);
			break;
		case "Subtraction":
			result = leftHand - rightHand;
			break;
		case "Multiplication":
			result = leftHand * rightHand;
			break;
		case "Division":
			result = leftHand / rightHand;
			break;
		default:
			result = leftHand * rightHand;
		}
		return result;
	}

	private String getOperand() {

		String operator = " x ";
		switch (operation) {
		case "Addition":
			operator = " + ";
			break;
		case "Subtraction":
			operator = " - ";
			break;
		case "Multiplication":
			operator = " x ";
			break;
		case "Division":
			operator = " / ";
			break;
		default:
			operator = " x ";
		}
		return operator;
	}

	public Jigsaw() throws Exception {
		setUp();
		loadProperties();
		SetLookAndFeel();

		frame = new JFrame();
		frame.setLayout(new GridLayout(1, 1));

		layout.setHgap(0);
		layout.setVgap(0);

		leftPanel = new JPanel();
		leftPanel.setLayout(layout);
		leftPanel.setBorder(blackline);

		rightPanel = new JPanel();
		rightPanel.setLayout(layout);
		rightPanel.setBorder(blackline);

		ViewMenu();
		frame.setTitle("Jigsaw");
		frame.add(leftPanel);
		frame.add(rightPanel);
		frame.setVisible(true);
		frame.setBounds(0, 0, (2 * scaledWidth) + 20, scaledHeight + 60);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void ViewMenu() {
		try {
			// MenuBar
			menuBar = new JMenuBar();
			fileMenu = new JMenu("File");
			openMenuItem = new JMenuItem("Open");
			exitMenuItem = new JMenuItem("Exit");

			openMenuItem.addActionListener(this);
			exitMenuItem.addActionListener(this);

			fileMenu.add(openMenuItem);
			fileMenu.add(exitMenuItem);

			menuBar.add(fileMenu);
			frame.setJMenuBar(menuBar);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String args[]) {
		try {
			new Jigsaw();
		} catch (Exception e) {
			javax.swing.JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == openMenuItem) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(impagePath);
			fileChooser.setFileFilter(new FileNameExtensionFilter("jpg", "jpeg"));
			int result = fileChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				splitImages(selectedFile);
				jigsawImagesLeft = null;
				jigsawImagesRight = null;
				paintLeftJigsaw();
				paintRightJigsaw();
				attemptCout.set(0);
			}
		} else if (event.getSource() == exitMenuItem) {
			System.exit(0);
		}
	}
}
