package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;

/**
 * Represents the `ScrollPanel` type within the DikenEngine `gui.compoment` package.
 */
public class ScrollPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Hitbox viewportSize;

    protected ScrollBar horizontalScrollBar, verticalScrollBar;
    protected GuiComponent scrollComponent;
    protected Button scrollLock;

    private int originalContentWidth, originalContentHeight;
    private boolean initialized = false;
    private boolean isTouching;

    private final int BAR_SIZE = 18;
    
    private final IGuiListener panelListener = new IGuiListener() {
		@Override
		public void changedBounds(IHitbox newBounds) {
		}

		@Override
		public void changedSize(IHitbox newBounds, int width, int height) {
			handlePanelResize(width, height);
		}

		@Override
		public void changedLocation(IHitbox newBounds, int x, int y) {
		}
	};
    
    private final IGuiListener contentListener = new IGuiListener() {
		@Override
		public void changedBounds(IHitbox newBounds) {
		}

		@Override
		public void changedSize(IHitbox newBounds, int width, int height) {
			handleContentResize(width, height);
		}

		@Override
		public void changedLocation(IHitbox newBounds, int x, int y) {
		}
	};

    public ScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height);

        this.viewportSize = new Hitbox(0, 0, width - BAR_SIZE, height - BAR_SIZE);

        this.scrollComponent = new Panel(0, 0, width - BAR_SIZE, height - BAR_SIZE);
        this.scrollComponent.parent = this;
        this.scrollComponent.addGuiListener(contentListener);
        this.originalContentWidth = this.scrollComponent.width;
        this.originalContentHeight = this.scrollComponent.height;

        this.horizontalScrollBar = new ScrollBar(0, height - BAR_SIZE, width - BAR_SIZE, BAR_SIZE, 1)
        	    .addDraggedListener((percent) -> {
        	        int maxScrollX = this.scrollComponent.width - this.viewportSize.width;
        	        
        	        if (maxScrollX > 0) {
        	            this.scrollComponent.setX((int) -(percent * maxScrollX));
        	        } else {
        	            this.scrollComponent.setX(0);
        	        }
        	    });
        this.horizontalScrollBar.parent = this;

        this.verticalScrollBar = new ScrollBar(width - BAR_SIZE, 0, BAR_SIZE, height - BAR_SIZE, 0)
        	    .addDraggedListener((percent) -> {
        	        // Toplam taşan miktarı hesapla
        	        int maxScrollY = this.scrollComponent.height - this.viewportSize.height;
        	        
        	        // Eğer içerik pencereden büyükse kaydır
        	        if (maxScrollY > 0) {
        	            // Negatif yönde kaydırıyoruz çünkü içerik yukarı gitmeli
        	            this.scrollComponent.setY((int) -(percent * maxScrollY));
        	        } else {
        	            this.scrollComponent.setY(0);
        	        }
        	    });
        this.verticalScrollBar.parent = this;
        
        this.scrollLock = new Button("", width - BAR_SIZE, height - BAR_SIZE, BAR_SIZE, BAR_SIZE);
        this.scrollLock.parent = this;

        updateBars();
        this.addGuiListener(panelListener);
    }

    @Override
    public void init(DikenEngine engine) {
        initialized = true;
        
        if (!isVaild(scrollComponent)) {
        	this.add(scrollComponent);
        }
        if (!isVaild(horizontalScrollBar)) {
        	this.add(horizontalScrollBar);
        }
        if (!isVaild(verticalScrollBar)) {
        	this.add(verticalScrollBar);
        }
        if (!isVaild(scrollLock)) {
        	this.add(scrollLock);
        }

        syncContentSizeToViewport();
        updateBars();
    }

    public ScrollPanel setScrollComponent(GuiComponent gc) {
    	if (gc == null) {
    		return this;
    	}
    	
    	this.scrollComponent.removeGuiListener(contentListener);
    	int previousX = this.scrollComponent.x;
    	int previousY = this.scrollComponent.y;
    	
    	if (isVaild(this.scrollComponent)) {
    		this.remove(this.scrollComponent);
    	}

        this.scrollComponent = gc;
        this.scrollComponent.parent = this;
        this.scrollComponent.addGuiListener(contentListener);
        this.scrollComponent.setLocation(previousX, previousY);
        
        this.originalContentWidth = gc.width;
        this.originalContentHeight = gc.height;

        if (initialized) {
        	this.getCompoments().add(0, gc);
        }
        
        syncContentSizeToViewport();
        updateBars();
        refreshScrollOffsets();
        
        return this;
    }

    @Override
    public void tick(DikenEngine engine) {
        super.tick(engine);

        if (this.scrollComponent.width != Math.max(originalContentWidth, viewportSize.width)
        		|| this.scrollComponent.height != Math.max(originalContentHeight, viewportSize.height)) {
        	syncContentSizeToViewport();
        	updateBars();
        	refreshScrollOffsets();
        }

        int wheel = engine.input.getWheelValue();
        if (this.active && this.mouseTouchingScrollPanel()) {
        	onMouseWheel(wheel);
        }
    }

    public boolean mouseTouchingScrollPanel() {
		return this.isTouching;
	}

	public void updateBars() {
        int viewW = this.viewportSize.width;
        int viewH = this.viewportSize.height;

        int contentW = this.scrollComponent.width;
        int contentH = this.scrollComponent.height;

        this.horizontalScrollBar.updateHandleSize(viewW, contentW);
        this.verticalScrollBar.updateHandleSize(viewH, contentH);
    }
    
    public void onMouseWheel(int direction) {
        float currentPos = this.verticalScrollBar.getScrollValue();
        this.verticalScrollBar.setScrollValue(currentPos + (direction * 0.05f)); 
    }

    private void handlePanelResize(int width, int height) {
		this.viewportSize.setSize(Math.max(1, width - BAR_SIZE), Math.max(1, height - BAR_SIZE));
        this.horizontalScrollBar.setBounds(0, height - BAR_SIZE, Math.max(1, width - BAR_SIZE), BAR_SIZE);
        this.verticalScrollBar.setBounds(width - BAR_SIZE, 0, BAR_SIZE, Math.max(1, height - BAR_SIZE));
        this.scrollLock.setBounds(width - BAR_SIZE, height - BAR_SIZE, BAR_SIZE, BAR_SIZE);
        syncContentSizeToViewport();
        updateBars();
        refreshScrollOffsets();
	}

    private void handleContentResize(int width, int height) {
    	this.originalContentWidth = Math.max(1, width);
    	this.originalContentHeight = Math.max(1, height);
    	syncContentSizeToViewport();
        updateBars();
        refreshScrollOffsets();
    }
    
    private void syncContentSizeToViewport() {
    	int targetWidth = Math.max(originalContentWidth, viewportSize.width);
        int targetHeight = Math.max(originalContentHeight, viewportSize.height);
        
        if (this.scrollComponent.width != targetWidth || this.scrollComponent.height != targetHeight) {
        	this.scrollComponent.setSize(targetWidth, targetHeight);
        }
    }
    
    private void refreshScrollOffsets() {
    	this.horizontalScrollBar.setScrollValue(this.horizontalScrollBar.getScrollValue());
    	this.verticalScrollBar.setScrollValue(this.verticalScrollBar.getScrollValue());
    }

	@Override
	public void mouseGetInfo(int relMouseX, int relMouseY, boolean isTouch2) {
		super.mouseGetInfo(relMouseX, relMouseY, isTouch2);
		
		isTouching = isTouch2;
	}
}
