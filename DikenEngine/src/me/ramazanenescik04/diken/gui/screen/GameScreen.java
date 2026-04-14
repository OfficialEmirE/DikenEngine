package me.ramazanenescik04.diken.gui.screen;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.entity.MovementPlayer;
import me.ramazanenescik04.diken.game.entity.SoloPlayer;
import me.ramazanenescik04.diken.game.nodes.Tool;
import me.ramazanenescik04.diken.game.world.World;
import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.gui.window.SettingsWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class GameScreen extends Screen {
	
	private Panel pausePanel, invertoryShortcutPanel;
	private TextField chatBar;
	private ProgressBar healthBar;
	private boolean chatBarEnabled, pauseMenuEnabled;
	
	public World theWorld;
	private SoloPlayer thePlayer = new SoloPlayer(0, 0);
	private MovementPlayer movementPlayer = new MovementPlayer(thePlayer);;
	
	private Screen parent;
	
	private List<String> chatMessageList;
	private boolean initIsFinished = false;
	
	public GameScreen(Screen parent) {
		this.parent = parent;
	}
	
	public GameScreen(Screen parent, World world) {
		this(parent, world, true);
	}
	
	public GameScreen(Screen parent, World world, boolean copyWorld) {
		this.theWorld = world != null ? (copyWorld ? world.copy() : world) : null;
		this.parent = parent;
	}
	
	public void openScreen() {
		System.gc();
		
		chatMessageList = new ArrayList<>();
		chatBar = new TextField(2, engine.getScaledHeight() - 22, engine.getScaledWidth() - 2, 20);
		pausePanel = new Panel(0, 0, engine.getScaledWidth(), engine.getScaledHeight());
		
		// TODO: EN BAŞTAN YAPILACAK!
		invertoryShortcutPanel = new Panel(engine.getScaledWidth() / 2 - 2 / 2, (engine.getScaledHeight() - 26) - 36, 2, 34) {
		    // Tool ve Buton eşleşmesini tutmak için (gerekirse sonradan erişmek için)
		    private Map<Tool, ImageButton> buttons = new HashMap<>();
		    private ImageButton selectedButton;

		    @Override
		    public void tick(DikenEngine engine) {
		        super.tick(engine);
		        
		        // Oyuncunun tool listesini al
		        List<Tool> currentTools = thePlayer.findFirstChild("Tools").findByClass(Tool.class);
		        int currentSize = currentTools.size();
		        
		        // Panel genişliğini dinamik ayarla
		        int slotWidth = currentSize * 34;
		        // Not: setBounds işlemini sadece boyut değiştiyse yapmak performansı artırır
		        if (this.getWidth() != slotWidth + 2) {
		             this.setBounds(engine.getScaledWidth() / 2 - (slotWidth + 2) / 2, (engine.getScaledHeight() - 26) - 36, slotWidth + 2, 34);
		        }

		        // Eğer araç sayısı değiştiyse listeyi güncelle
		        if (currentSize != buttons.size()) {
		            this.updateInventoryList(currentTools);
		        }
		    }

		    // Listeyi yenileyen metod
		    private void updateInventoryList(List<Tool> tools) {
		        // 1. Önceki butonları temizle (Panelin kendi component listesinden ve map'ten)
		        this.clear(); // Panelin çocuklarını silen bir metodun olduğunu varsayıyorum
		        buttons.clear();
		        thePlayer.setSelectedTool(null);

		        int xOffset = 2; // Soldan boşluk
		        int yOffset = 1; // Üstten boşluk (ortalama için)
		        int btnSize = 32;
		        int gap = 2; // Butonlar arası boşluk

		        // 2. Yeni listeyi döngüye al
		        for (int i = 0; i < tools.size(); i++) {
		            Tool tool = tools.get(i);

		            // Butonun X konumu: Başlangıç + (Sıra * (Boyut + Boşluk))
		            int xPos = xOffset + (i * (btnSize + gap));
		            
		            // ImageButton oluştur (Tool'un ikonunu kullanarak)
		            // Varsayım: tool.getIcon() veya tool.getImage() bir görsel dönüyor.
		            ImageButton btn = new ImageButton(tool.getIconBitmap(), xPos, yOffset, btnSize, btnSize) {
						@Override
						public Bitmap render() {
							Bitmap bitmap = super.render();
							if (selectedButton == this) {
								bitmap.box(0, 0, width - 1, height - 1, 0xffffff00);
							}
							return bitmap;
						}
		            };

		            // Tıklama olayı (Lambda expression örneği)
		            btn.setRunnable(() -> {
		            	if (thePlayer.getSelectedTool() == tool) {
		            		thePlayer.setSelectedTool(null);
		            	} else {		            		
		            		tool.y = 30;
		            		tool.x = -16;
		            		thePlayer.setSelectedTool(tool);
		            	}
		            	
		            	if (selectedButton == btn) {
		            		selectedButton = null;
		            	} else {
		            		selectedButton = btn;
		            	}
		            });

		            // Panele ve Map'e ekle
		            this.add(btn); 
		            buttons.put(tool, btn);
		        }
		    }
		};
		healthBar = new ProgressBar(engine.getScaledWidth() / 2 - 110 / 2, engine.getScaledHeight() - 26, 110, 16);
		healthBar.text = "Health";
		healthBar.color = 0xff4fff4f;
		healthBar.color2 = 0xff33a633;
		
		if (this.theWorld == null) {
			OptionWindow.showMessageNoWait("World object null! Please report the error!", "Error", OptionWindow.ERROR_MESSAGE, 0, _ -> this.engine.setCurrentScreen(this.parent));
			return;
		}
		
		theWorld.setZoom(1.0f);
		theWorld.setBounds(0, 0, engine.getScaledWidth(), engine.getScaledHeight());
		theWorld.addNode(thePlayer);
		
		thePlayer.setFollowCamera(true);
		
		this.getContentPane().add(this.theWorld);
		this.getContentPane().add(healthBar);
		this.getContentPane().add(invertoryShortcutPanel);
		
		initPausePanel();
		
		ArrayBitmap menuButtonTextures = (ArrayBitmap) ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "menu_buttons"));
		ImageButton pauseButton = new ImageButton(menuButtonTextures.getBitmap(0, 0), 2, 2, 20, 20);
		pauseButton.setRunnable(() -> {
			if (this.chatBarEnabled)
				this.closeChatMenu();
			
			this.openPauseMenu();
		});
		this.getContentPane().add(pauseButton);
		
		ImageButton chatButton = new ImageButton(menuButtonTextures.getBitmap(1, 0), 24, 2, 20, 20);
		chatButton.setRunnable(() -> {
			if (this.chatBarEnabled) {
				this.closeChatMenu();
			} else {
				this.openChatMenu();
			}
		});
		this.getContentPane().add(chatButton);
		
		initIsFinished = true;
	}
	
	private void initPausePanel() {
		pausePanel.clear();
		Button resumeButton = new Button("Resume The Game", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) - 25, 120, 22).setRunnable(() -> {			
			this.closePauseMenu();
		}).setButtonColor(0xff005cff).setTextColor(0xffffffff);
		Button settingsButton = new Button("Settings", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2), 120, 22).setRunnable(() -> {
			if (!this.engine.wManager.isWindowActive(SettingsWindow.class)) {
				this.engine.wManager.addWindow(new SettingsWindow());
			};
		}).setButtonColor(0xff005cff).setTextColor(0xffffffff);
		Button exitButton = new Button("Exit The Game", pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) + 25, 120, 22).setRunnable(() -> {
			if (parent == null) {
				this.engine.stop();
				System.exit(0);
				return;
			}
			this.engine.setCurrentScreen(parent);
			System.gc();
		}).setButtonColor(0xff005cff).setTextColor(0xffffffff);
		pausePanel.add(resumeButton);
		pausePanel.add(settingsButton);
		pausePanel.add(exitButton);
	}
	
	public void resized() {
		if (this.theWorld == null)
			return;
		
		chatBar.setBounds(2, engine.getScaledHeight() - 22, engine.getScaledWidth() - 2, 20);
		pausePanel.setSize(engine.getScaledWidth(), engine.getScaledHeight());
		theWorld.setSize(engine.getScaledWidth(), engine.getScaledHeight());
		healthBar.setBounds(engine.getScaledWidth() / 2 - 110 / 2, engine.getScaledHeight() - 26, 110, 16);
		
		invertoryShortcutPanel.setLocation(engine.getScaledWidth() / 2 - invertoryShortcutPanel.getWidth() / 2, (engine.getScaledHeight() - 26) - 36);
		
		pausePanel.get(0).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) - 25);
		pausePanel.get(1).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2));
		pausePanel.get(2).setLocation(pausePanel.width / 2 - 120 / 2, (pausePanel.height / 2 - 20 / 2) + 25);
	}
	
	@Override
	public void render(Bitmap bitmap) {				
		super.render(bitmap);
		
		if (!initIsFinished) {
			return;
		}
		
		bitmap.draw(this.invertoryShortcutPanel.render(), this.invertoryShortcutPanel.x, this.invertoryShortcutPanel.y);
		
		if (pauseMenuEnabled) {
			bitmap.blendFill(0, 0, engine.getScaledWidth(), engine.getScaledHeight(), 0xaa000000);
			
			bitmap.draw(this.pausePanel.render(), 0, 0);
		}
		
		for (int i = 0; i < this.chatMessageList.size(); i++) {
			String text = this.chatMessageList.get(i);
			bitmap.drawText(text, 2, this.engine.getScaledHeight() - (i * 9) - 35, false);
		}
	}
	
	public void openPauseMenu() {
		if (pauseMenuEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		pauseMenuEnabled = true;
		theWorld.active = false;
		initPausePanel();
		this.getContentPane().add(pausePanel);
		this.getContentPane().get(1).setActive(false);
		this.getContentPane().get(2).setActive(false);
	}
	
	public void openChatMenu() {
		if (chatBarEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		chatBar.setFocused(true);
		this.getContentPane().add(chatBar);
		chatBarEnabled = true;
	}
	
	public void closePauseMenu() {
		if (!pauseMenuEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		pauseMenuEnabled = false;
		theWorld.active = true;
		this.getContentPane().getCompoments().removeIf(e -> e == this.pausePanel);
		this.getContentPane().get(1).setActive(true);
		this.getContentPane().get(2).setActive(true);
	}
	
	public void closeChatMenu() {
		if (!chatBarEnabled) return; // Zaten açıksa tekrar açma kodunu çalıştırma!
		
		chatBar.setFocused(false);
		chatBarEnabled = false;
		chatBar.text = "";
		this.getContentPane().getCompoments().removeIf(e -> e == this.chatBar);
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		super.keyDown(eventCharacter, eventKey);
		
		if (!chatBarEnabled && !pauseMenuEnabled) {
			if (eventKey == KeyEvent.VK_DIVIDE) {
				this.openChatMenu();
	
			} else if (eventKey == KeyEvent.VK_ESCAPE) {
				this.openPauseMenu();
			}
		} else if (chatBarEnabled && !pauseMenuEnabled) {
			if (eventKey == KeyEvent.VK_ESCAPE || eventKey == KeyEvent.VK_ENTER) {
				if (eventKey == KeyEvent.VK_ENTER && !chatBar.text.trim().isEmpty()) {
					sendMessage("TestUser" + ": " + chatBar.text.trim());
				}
				
				this.closeChatMenu();
			}
		} else if (!chatBarEnabled && pauseMenuEnabled) {
			if (eventKey == KeyEvent.VK_ESCAPE) {
				this.closePauseMenu();
			}
		}
		
		if (eventKey == KeyEvent.VK_R && this.thePlayer.canMove) {
			this.thePlayer.damage(100);
		}
		
		if (eventKey == KeyEvent.VK_P) {
			System.out.println("-=-=- YAKINDA BU KARDIRILACAK! -=-=-");
			theWorld.root.printTree(true);
		}
	}
	
	public void tick() {	
		if (!initIsFinished) {
			return;
		}
		
		if (this.thePlayer.canMove) {
			movementPlayer.tick(engine);
			
			thePlayer.isMoving = movementPlayer.isMoving;
			thePlayer.setViewType(movementPlayer.viewType);
		}
		
		if (this.thePlayer.followCamera) {
			int playerWidth = 57;
			int playerHeight = 64;
			
			int centerX = (playerWidth / 2) - (engine.getScaledWidth() / 2);
			int centerY = (playerHeight / 2) - (engine.getScaledHeight() / 2);
			
			theWorld.camera.x = centerX + thePlayer.x;
			theWorld.camera.y = centerY + thePlayer.y;
		}
		
		if (this.theWorld == null) {
			super.tick();
			return;
		}
		
		boolean busy = (engine.wManager != null ? engine.wManager.activeWindow != null : false) || pauseMenuEnabled || chatBarEnabled || !this.thePlayer.isAlive() || !this.theWorld.isActive();
		
		if (thePlayer.canMove && busy) {
			thePlayer.canMove = false;
		} else if (!thePlayer.canMove && !busy) {
			thePlayer.canMove = true;
		}
		
		super.tick();

		if (this.healthBar != null) {
			healthBar.value = thePlayer.health;
			healthBar.maxValue = 100;
		}
	}

	public void sendMessage(String message) {
		this.chatMessageList.add(0, message);

		while(this.chatMessageList.size() > 50) {
			this.chatMessageList.remove(this.chatMessageList.size() - 1);
		}
	}
}
