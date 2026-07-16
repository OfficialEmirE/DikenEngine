package me.ramazanenescik04.diken.studio.editors;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.studio.builders.Toolbar;
import me.ramazanenescik04.diken.studio.dockables.EditorTabPanel;
import me.ramazanenescik04.diken.tools.Pair;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public abstract class ResourceEditor<R extends IResource> extends BaseEditor {
	private static final long serialVersionUID = 1L;

	protected World theWorld;
	protected Pair<String, R> resource;
	protected Toolbar.Builder builder = new Toolbar.Builder();
	
	@SuppressWarnings("unchecked")
	public ResourceEditor(String title, World world, String resourceKey, R defaultValue) {
		super(title);
		this.theWorld = world;
		
		R loadedValue = defaultValue;
		
		if (this.theWorld != null) {
			R value = (R) this.theWorld.resources.get(resourceKey);
			
			if (value != null) loadedValue = value;
			world.addResource(resourceKey, loadedValue);
		}
		
		this.resource = new Pair<>(resourceKey, loadedValue);
		
		setLayout(new BorderLayout(0, 0));
		
		var toolbar = builder.newToolbar("default");
		builder.addButton(toolbar, "save", 8, 0, "studio.menubar.save", () -> {
			if (resource.second != null) {
				world.addResource(resource);
			}
		});
		builder.addButton(toolbar, "saveAs", 7, 0, "studio.menubar.saveAs", this::openSaveDialog);
	}
	
	@Override
	public void init(EditorTabPanel tabPanel) {
		super.init(tabPanel);
		
		var toolBar = builder.getJToolBar();
		toolBar.setBackground(new Color(82, 82, 82));
		add(toolBar, BorderLayout.NORTH);
	}

	@Override
	public void refreshWorld(World world, boolean playtest) {
		this.theWorld = world;
	}
	
	public void openSaveDialog() {
		IResource theResource = resource.second;
		if (theResource == null) return;
		
		EnumResource selectedType = theResource.getResourceType();
		
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(Lang.get("resources.selectResourceFile"));
        
        if (selectedType == EnumResource.IMAGE || selectedType == EnumResource.CURSOR) {
        	fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "bmp", "gif"));
		} else if (selectedType == EnumResource.SOUND) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Wave Files", "wav"));
		} else if (selectedType == EnumResource.ANIMATION) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Animation Files", "bin", "anim"));
		} else if (selectedType == EnumResource.FONT) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Fonts", "otf", "otc", "ttf", "ttc"));
		}

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        
        if (selectedFile == null) return;
        
        try (var out = new DataOutputStream(new FileOutputStream(selectedFile))) {
        	theResource.saveResource(out);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
