import gr.watchful.permchecker.datastructures.Mod;
import gr.watchful.permchecker.datastructures.ModFile;
import gr.watchful.permchecker.datastructures.ModInfo;
import gr.watchful.permchecker.datastructures.ToolSettings;
import gr.watchful.permchecker.listenerevent.NamedScrollingListPanelListener;
import gr.watchful.permchecker.listenerevent.NamedSelectionEvent;
import gr.watchful.permchecker.modhandling.ModFinder;
import gr.watchful.permchecker.modhandling.ModNameRegistry;
import gr.watchful.permchecker.panels.ModEditor;
import gr.watchful.permchecker.panels.ModFileEditor;
import gr.watchful.permchecker.panels.NamedScrollingListPanel;
import gr.watchful.permchecker.utils.ExcelUtils;
import gr.watchful.permchecker.utils.FileUtils;
import gr.watchful.permchecker.utils.OsTypes;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class mainClass extends JFrame implements NamedScrollingListPanelListener {
	private DefaultListModel<Mod> goodMods;
	private DefaultListModel<Mod> badMods;
	private DefaultListModel<ModFile> unknownMods;
	private NamedScrollingListPanel<Mod> good;
	private NamedScrollingListPanel<Mod> bad;
	private NamedScrollingListPanel<ModFile> unknown;
	private JToggleButton packTypeToggle;
	private File permFile;
	private File appstore; //Location for the spreadsheet file
	private JPanel cards;
	private ModEditor modEditor;
	private ModFileEditor modFileEditor;
	private static ModNameRegistry nameRegistry;
	private static ToolSettings settings;

	public mainClass() {
		goodMods = new DefaultListModel<Mod>();
		badMods = new DefaultListModel<Mod>();
		unknownMods = new DefaultListModel<ModFile>();

		nameRegistry = new ModNameRegistry();
		
		settings = new ToolSettings();

        /**
         * Check which OS the system is running, and make the appropriate directories if necessary
         * @author bearbear12345
         */
        OsTypes.OSType ostype = OsTypes.getOperatingSystemType();
        System.out.println("Operating System: " + ostype.toString());
        System.out.println("Searching for application storage directory...");
        switch (ostype) {
            case Windows:
                appstore = new File(System.getenv("APPDATA") + "/PermissionsChecker");
                break;
            case MacOS:
                appstore = new File(System.getProperty("user.home") + "/Library/Application Support/PermissionsChecker");
                break;
            case Linux:
                appstore = new File(System.getProperty("user.home") + "/.permissionsChecker");
                break;
            case Other:
                //TODO ????
                break;
        }
        if (!appstore.exists()) {
            System.out.println("Directory not found! Creating directory: " + appstore.getPath());
            boolean result = appstore.mkdirs();
            if (result) {
                System.out.println(appstore.getPath() + " created!");
            }
        } else {
            System.out.println("Directory exists!");
        }

        permFile = new File(appstore.getPath() + "/PermissionsChecker.xlsx");
        if (!permFile.exists()) {
            try {
                permFile.createNewFile();
            } catch (IOException e) {
                System.out.println("An error occured while creating during setup. Please try again later. If issues persist, contact the author");
            }
        }

		try {
			permFile = File.createTempFile("PermissionsCheckerPermFile",
					".xlsx");
		} catch (IOException e) {
			// TODO Tell user error (No perms?)
		}

		this.setTitle("Permissions Checker"); // Set the window title
		this.setPreferredSize(new Dimension(600, 300)); // and the initial size
		
		//updateListings();

		//TODO move this stuff to a seperate method
		File currentDir = new File(System.getProperty("user.dir"));
		if(isMinecraftDir(currentDir)) {
			discoverMods(currentDir);
		} else if(isMinecraftDir(currentDir.getParentFile())) {
			discoverMods(currentDir.getParentFile());
		} else {
			//TODO implement a selection of modpacks, maybe save modpack locations for later use
			//also allow selecting of multiMC instances folder
			
			//debug
			//discoverMods(new File("C:\\Users\\Gregory\\Desktop\\MultiMC\\instances\\Hammercraft 4.3.0 Custom\\minecraft"));
		}
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setAlignmentX(0f);
		buttonPanel.add(new JLabel("Public"));
		
		packTypeToggle = new JToggleButton();
		packTypeToggle.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateToggleIcons();
				updateSettings();
			}
		});
		
		Dimension temp = new Dimension(92,28);
		packTypeToggle.setMaximumSize(temp);
		packTypeToggle.setMinimumSize(temp);
		packTypeToggle.setPreferredSize(temp);

		try {//try as hard as possible to load the images and set them on the toggle button
			URL leftUrl = getClass().getResource("resources/toggleLeft.png");
			URL rightUrl = getClass().getResource("resources/toggleRight.png");
		    if(leftUrl == null || rightUrl == null) {
		    	ImageIcon leftIcon = new ImageIcon("bin/resources/toggleLeft.png");
		    	ImageIcon rightIcon = new ImageIcon("bin/resources/toggleRight.png");
		    	if(leftIcon.getIconWidth() == -1 || rightIcon.getIconWidth() == -1) {
		    		packTypeToggle.setText("Switch");
		    	} else {
		    		packTypeToggle.setIcon(leftIcon);
		    		packTypeToggle.setSelectedIcon(rightIcon);
		    	}
		    } else {
		    	Image leftImg = ImageIO.read(leftUrl);
		    	Image rightImg = ImageIO.read(rightUrl);
		    	if(leftImg == null || rightImg == null) {
		    		packTypeToggle.setText("Switch");
		    	} else {
		    		packTypeToggle.setIcon(new ImageIcon(leftImg));
		    		packTypeToggle.setSelectedIcon(new ImageIcon(rightImg));
		    	}
		    }
		} catch (IOException ex) {
    		packTypeToggle.setText("Switch");
		}
		
		if(packTypeToggle.getIcon() != null) {
			Dimension temp2 = new Dimension(packTypeToggle.getIcon().getIconWidth()+2,packTypeToggle.getIcon().getIconHeight()+2);
			packTypeToggle.setMaximumSize(temp2);
			packTypeToggle.setMinimumSize(temp2);
			packTypeToggle.setPreferredSize(temp2);
    		
    		packTypeToggle.setBorderPainted(false);
    		packTypeToggle.setContentAreaFilled(false);
    		packTypeToggle.setOpaque(false);
    		packTypeToggle.setFocusPainted(false);
		}
		
		buttonPanel.add(packTypeToggle);
		
		buttonPanel.add(new JLabel("Private"));
		
		topPanel.add(buttonPanel);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.setAlignmentX(0f);
		topPanel.add(mainPanel);
		
		this.add(topPanel);

		good = new NamedScrollingListPanel<Mod>(
				"Good", new Dimension(100, 300), goodMods);
		good.addListener(this);
		mainPanel.add(good);
		bad = new NamedScrollingListPanel<Mod>(
				"Bad", new Dimension(100, 300), badMods);
		bad.addListener(this);
		mainPanel.add(bad);
		unknown = new NamedScrollingListPanel<ModFile>(
				"Unknown", new Dimension(100, 300), unknownMods);
		mainPanel.add(unknown);
		unknown.addListener(this);

		JPanel newWindow = new JPanel();
		JPanel modEditWindow = new JPanel();

		cards = new JPanel(new CardLayout());
		cards.setMinimumSize(new Dimension(300, 300));
		cards.add(newWindow);
		cards.add(modEditWindow);
		
		modEditor = new ModEditor(new Dimension(300,300));
		cards.add(modEditor,"MODEDITOR");
		modFileEditor = new ModFileEditor(new Dimension(300,300), new ModFile(new File("/")));
		cards.add(modFileEditor,"MODFILEEDITOR");
		
		CardLayout cardLayout = (CardLayout)(cards.getLayout());
		cardLayout.show(cards, "MODEDITOR");

		mainPanel.add(cards);

		JMenuBar menuBar = new JMenuBar(); // create the menu
		JMenu menu = new JMenu("Temp"); // with the submenus
		menuBar.add(menu);

		JMenuItem updatePerms = new JMenuItem("Update Permissions");

		// listen to all the menu items and then add them to the menus
		updatePerms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Check last modified - bandwidth
				updateListings();
			}
		});
		menu.add(updatePerms);
		// TODO Folder select (Pre: Check if current folder is valid)
		JMenuItem chooseModpack = new JMenuItem("Choose Modpack");

		// listen to all the menu items and then add them to the menus
		chooseModpack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(System
						.getProperty("user.home"));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File result = fileChooser.getSelectedFile();
					discoverMods(result);
				}
			}
		});
		menu.add(chooseModpack);

		this.setJMenuBar(menuBar);

		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private static boolean isMinecraftDir(File file) {
		return file.getName().equals("minecraft") || file.getName().equals(".minecraft");
	}

	public static void main(String[] args) {
		new mainClass();
	}

	private void discoverMods(File minecraftFolder) {
		ModFinder.discoverAllMods(minecraftFolder, unknownMods, badMods, nameRegistry);
	}

	public void selectionChanged(NamedSelectionEvent event) {
		System.out.println(event.getParentName()+" : "+event.getSelected());
		updateEditor(event.getParentName(),event.getSelected());
	}
	
	private void updateEditor(String list, int selected) {
		if(list.equals("Good")) {
			bad.clearSelection();
			unknown.clearSelection();
			
			CardLayout cardLayout = (CardLayout)(cards.getLayout());
			cardLayout.show(cards, "MODEDITOR");
			
			modEditor.setMod(nameRegistry.getMod(good.getSelected().shortName));
		}
		if(list.equals("Bad")) {
			good.clearSelection();
			unknown.clearSelection();
			
			CardLayout cardLayout = (CardLayout)(cards.getLayout());
			cardLayout.show(cards, "MODEDITOR");

			modEditor.setMod(nameRegistry.getMod(bad.getSelected().shortName));
		}
		if(list.equals("Unknown")) {
			good.clearSelection();
			bad.clearSelection();
			
			CardLayout cardLayout = (CardLayout)(cards.getLayout());
			cardLayout.show(cards, "MODFILEEDITOR");

			modFileEditor.setModFile(unknown.getSelected());
		}
	}
	
	public void updateListings() {
		try {
			FileUtils
					.downloadToFile(
							new URL(
									"https://skydrive.live.com/download?resid=96628E67B4C51B81!105&authkey=!AK7mlmHB0nrxmHg&ithint=file%2c.xlsx"),
							permFile);
			try {
				ArrayList<ArrayList<String>> infos = ExcelUtils.toArray(permFile,1);
				infos.remove(0);//remove the first row, it contains column titles
				ArrayList<ArrayList<String>> mappings = ExcelUtils.toArray(permFile,2);
				nameRegistry.loadMappings(infos, mappings, infos.get(16).get(14), infos.get(16).get(15));
			} catch (FileNotFoundException e) {
				System.out.println("UHOH");
			} catch (IOException e) {
				System.out.println("uhoh");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateToggleIcons() {
		if(packTypeToggle.isSelected()) {
			packTypeToggle.setPressedIcon(packTypeToggle.getIcon());
		} else {
			packTypeToggle.setPressedIcon(packTypeToggle.getSelectedIcon());
		}
	}
	
	public void updateSettings() {
		if(packTypeToggle.isSelected()) {
			settings.packType = ToolSettings.PRIVATE;
		} else {
			settings.packType = ToolSettings.PUBLIC;
		}
		recheckMods();
	}
	
	public void recheckMods() {
		for(int i=0; i<badMods.getSize(); i++) {
			ModInfo temp = nameRegistry.getMod(badMods.get(i).shortName);
			if(temp != null && temp.publicPolicy == ModInfo.OPEN) {
				System.out.println(temp.modName+" is good");
			}
		}
	}
}
