package me.ramazanenescik04.diken.studio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import me.ramazanenescik04.diken.game.world.World;
import me.ramazanenescik04.diken.gui.component.Button;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.component.ScrollPanel;
import me.ramazanenescik04.diken.gui.component.Text;
import me.ramazanenescik04.diken.gui.component.TextField;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.gui.window.Window;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StudioResourceWindow extends Window {
	private static final long serialVersionUID = 1L;
	private static final String RESERVED_EMPTY_RESOURCE = "empty";
	
	private final World world;
	private final EnumSet<EnumResource> supportedTypes = EnumSet.of(
		EnumResource.IMAGE,
		EnumResource.SOUND,
		EnumResource.CURSOR,
		EnumResource.ANIMATION
	);
	
	private ScrollPanel resourceScrollPanel;
	private Panel resourceListPanel;
	private TextField keyField;
	private Button typeButton;
	private Button addButton;
	private Button removeSelectedButton;
	private Text statusText;
	
	private EnumResource selectedType = EnumResource.IMAGE;
	private String selectedKey;

	public StudioResourceWindow(World world) {
		super(0, 0, 360, 280);
		this.world = world;
		this.resizable = true;
		setTitle("Resource Manager");
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("win-icons");
		setIcon(icons.getBitmap(8, 0));
	}
	
	@Override
	protected void open() {
		Panel panel = getContentPane();
		panel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		
		keyField = new TextField("new_resource", 8, 6, 220, 18);
		typeButton = new Button(selectedType.name(), 234, 6, 118, 18).setRunnable(() -> {
			cycleType();
		});
		addButton = new Button("Add", 8, 28, 54, 18).setRunnable(() -> {
			addResourceFromForm();
		});
		removeSelectedButton = new Button("Delete Selected", 66, 28, 120, 18).setRunnable(() -> {
			removeResourceByKey(selectedKey);
		});
		removeSelectedButton.setButtonColor(0xffdf8e8e);
		removeSelectedButton.setTextColor(0xff000000);
		
		statusText = new Text("Ready", 8, 52, 0xff243028);
		
		resourceScrollPanel = new ScrollPanel(8, 64, Math.max(1, panel.getWidth() - 16), Math.max(1, panel.getHeight() - 72));
		resourceListPanel = new Panel(0, 0, Math.max(1, resourceScrollPanel.getWidth() - 18), 1);
		
		panel.add(keyField);
		panel.add(typeButton);
		panel.add(addButton);
		panel.add(removeSelectedButton);
		panel.add(statusText);
		panel.add(resourceScrollPanel);
		
		resourceScrollPanel.setScrollComponent(resourceListPanel);
		layoutControls();
		rebuildResourceList();
	}
	
	@Override
	public void resized() {
		super.resized();
		layoutControls();
		rebuildResourceList();
	}
	
	private void layoutControls() {
		Panel panel = getContentPane();
		if (panel == null || keyField == null || typeButton == null || addButton == null
				|| removeSelectedButton == null || statusText == null || resourceScrollPanel == null) {
			return;
		}
		
		int panelWidth = Math.max(1, panel.getWidth());
		int panelHeight = Math.max(1, panel.getHeight());
		int typeWidth = 118;
		
		keyField.setBounds(8, 6, Math.max(80, panelWidth - typeWidth - 18), 18);
		typeButton.setBounds(panelWidth - typeWidth - 8, 6, typeWidth, 18);
		addButton.setBounds(8, 28, 54, 18);
		removeSelectedButton.setBounds(66, 28, 120, 18);
		statusText.setLocation(8, 52);
		resourceScrollPanel.setBounds(8, 64, Math.max(1, panelWidth - 16), Math.max(1, panelHeight - 72));
	}
	
	private void cycleType() {
		List<EnumResource> types = new ArrayList<>(supportedTypes);
		int index = types.indexOf(selectedType);
		if (index < 0) {
			selectedType = EnumResource.IMAGE;
		} else {
			selectedType = types.get((index + 1) % types.size());
		}
		typeButton.text = selectedType.name();
	}
	
	private void addResourceFromForm() {
		if (world == null || keyField == null || selectedType == null) {
			setStatus("World not available", 0xff722f2f);
			return;
		}
		
		File selectedFile = openFilePicker(selectedType);
		if (selectedFile == null) {
			setStatus("Import cancelled", 0xff5c4e2a);
			return;
		}
		
		String key = keyField.getText() != null ? keyField.getText().trim() : "";
		if (key.isBlank()) {
			key = removeExtension(selectedFile.getName());
			keyField.setText(key);
		}
		if (key.isBlank()) {
			setStatus("Key cannot be empty", 0xff722f2f);
			return;
		}
		if (RESERVED_EMPTY_RESOURCE.equals(key)) {
			setStatus("'empty' is reserved", 0xff722f2f);
			return;
		}
		
		IResource resource = loadResourceFromFile(selectedFile, selectedType);
		if (resource == null) {
			setStatus("Load failed: " + selectedFile.getName(), 0xff722f2f);
			return;
		}
		
		boolean replacing = world.resources.containsKey(key);
		world.addResource(key, resource);
		selectedKey = key;
		
		rebuildResourceList();
		setStatus(replacing ? "Replaced: " + key : "Added: " + key, 0xff243028);
	}
	
	private IResource loadResourceFromFile(File file, EnumResource type) {
		if (file == null || type == null) {
			return null;
		}
		
		try (InputStream stream = IOResource.createFileStream(file)) {
			if (stream == null) {
				return null;
			}
			return IOResource.loadResource(stream, type);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private File openFilePicker(EnumResource type) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(type.name() + " file select");
		
		if (type == EnumResource.IMAGE || type == EnumResource.CURSOR) {
			chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "bmp", "gif"));
		} else if (type == EnumResource.SOUND) {
			chooser.setFileFilter(new FileNameExtensionFilter("Wave Files", "wav"));
		} else if (type == EnumResource.ANIMATION) {
			chooser.setFileFilter(new FileNameExtensionFilter("Animation Files", "bin", "anim"));
		}
		
		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
	
	private String removeExtension(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return "";
		}
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex <= 0) {
			return fileName;
		}
		return fileName.substring(0, dotIndex);
	}
	
	private void removeResourceByKey(String key) {
		if (world == null) {
			setStatus("World not available", 0xff722f2f);
			return;
		}
		if (key == null || key.isBlank()) {
			setStatus("Select a resource first", 0xff722f2f);
			return;
		}
		if (RESERVED_EMPTY_RESOURCE.equals(key)) {
			setStatus("'empty' cannot be removed", 0xff722f2f);
			return;
		}
		if (!world.resources.containsKey(key)) {
			setStatus("Not found: " + key, 0xff722f2f);
			return;
		}
		
		world.removeResource(key);
		if (key.equals(selectedKey)) {
			selectedKey = null;
		}
		rebuildResourceList();
		setStatus("Removed: " + key, 0xff243028);
	}
	
	private void rebuildResourceList() {
		if (resourceListPanel == null || resourceScrollPanel == null || world == null) {
			return;
		}
		
		resourceListPanel.clear();
		
		int listWidth = Math.max(96, resourceScrollPanel.getWidth() - 22);
		int rowHeight = 18;
		int gap = 2;
		int y = 0;
		
		List<String> keys = new ArrayList<>(world.resources.keySet());
		keys.sort(String.CASE_INSENSITIVE_ORDER);
		
		if (keys.isEmpty()) {
			resourceListPanel.add(new Text("No resources", 4, 4, 0xff333333));
			resourceListPanel.setSize(listWidth, 20);
			resourceScrollPanel.updateBars();
			return;
		}
		
		for (String key : keys) {
			IResource resource = world.resources.get(key);
			String typeText = resource != null ? resource.getResourceType().name() : "UNKNOWN";
			boolean selected = key.equals(selectedKey);
			
			Button selectButton = new Button(key + " (" + typeText + ")", 0, y, Math.max(44, listWidth - 36), rowHeight).setRunnable(() -> {
				selectedKey = key;
				rebuildResourceList();
			});
			selectButton.setButtonColor(selected ? 0xff5f89c5 : 0xffffffff);
			selectButton.setTextColor(selected ? 0xffffffff : 0xff000000);
			resourceListPanel.add(selectButton);
			
			Button deleteButton = new Button("X", listWidth - 34, y, 34, rowHeight);
			if (RESERVED_EMPTY_RESOURCE.equals(key)) {
				deleteButton.active = false;
			} else {
				deleteButton.setButtonColor(0xffd76666);
				deleteButton.setTextColor(0xffffffff);
				deleteButton.setRunnable(() -> {
					removeResourceByKey(key);
				});
			}
			resourceListPanel.add(deleteButton);
			
			y += rowHeight + gap;
		}
		
		resourceListPanel.setSize(listWidth, Math.max(1, y));
		resourceScrollPanel.updateBars();
	}
	
	private void setStatus(String text, int color) {
		if (statusText == null) {
			return;
		}
		
		statusText.text = text != null ? text : "";
		statusText.color = color;
	}
}
