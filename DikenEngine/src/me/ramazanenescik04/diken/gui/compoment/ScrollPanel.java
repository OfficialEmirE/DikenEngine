package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;

public class ScrollPanel extends Panel {
    private static final long serialVersionUID = 1L;

    // Önceki boyutları saklayıp değişim olup olmadığını kontrol edeceğiz
    private Hitbox prevBounds; 

    // Görünen alanın boyutları (Scroll barlar hariç alan)
    private Hitbox viewportSize;

    protected ScrollBar horizontalScrollBar, verticalScrollBar;
    protected GuiComponent scrollComponent;
    
    protected Button scrollLock;
    
    private int originalContentWidth, originalContentHeight;

    // Scroll bar kalınlığı (kod içinde magic number olmasın diye sabit)
    private final int BAR_SIZE = 18;

    public ScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height);

        // 1. Viewport (Görünen Kısım) boyutu hesapla
        this.viewportSize = new Hitbox(0, 0, width - BAR_SIZE, height - BAR_SIZE);

        // 2. ScrollComponent (İçerik) oluştur
        this.scrollComponent = new Panel(0, 0, width - BAR_SIZE, height - BAR_SIZE);
        this.scrollComponent.parent = this;

        // 3. ScrollBar'ları oluştur
        // Yatay Bar (En altta)
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

        // 4. İlk boyutları kaydet
        this.prevBounds = new Hitbox(x, y, width, height);

        // 5. Barları ilk kez güncelle
        updateBars();
    }

    @Override
    public void init(DikenEngine engine) {
        // Alt elemanları sisteme ekle
        this.add(scrollComponent);
        this.add(horizontalScrollBar);
        this.add(verticalScrollBar);
        this.add(scrollLock);
        
        // Başlangıçta barları tekrar hesapla
        updateBars();
    }

    /**
     * Scroll edilecek içeriği değiştirir.
     */
    public ScrollPanel setScrollComponent(GuiComponent gc) {
        // Eski bileşeni listeden bul ve yenisiyle değiştir
        int index = this.getCompoments().indexOf(this.scrollComponent);
        if (index != -1) {
            this.getCompoments().set(index, gc);
        } else {
            this.add(gc); // Eğer listede yoksa ekle
        }

        this.scrollComponent = gc;
        this.scrollComponent.parent = this;
        
        this.originalContentWidth = gc.width;
        this.originalContentHeight = gc.height;
        
        // Yeni içerik geldiği için barları güncelle
        updateBars();
        
        return this;
    }

    @Override
    public void tick(DikenEngine engine) {
        super.tick(engine);

        // 1. Önce içeriğin o an olması gereken boyutunu hesapla
        // Kural: İçerik, orijinal boyutundan veya pencere boyutundan hangisi büyükse o kadar olur.
        int targetWidth = Math.max(originalContentWidth, viewportSize.width);
        int targetHeight = Math.max(originalContentHeight, viewportSize.height);

        // 2. Boyutları uygula (Sadece değiştiyse güncelle)
        if (this.scrollComponent.width != targetWidth || this.scrollComponent.height != targetHeight) {
            this.scrollComponent.width = targetWidth;
            this.scrollComponent.height = targetHeight;
            updateBars();
        }

        // 3. Pencere boyutu değişim kontrolü (Barların yerini korumak için)
        if (this.width != this.prevBounds.width || this.height != this.prevBounds.height) {
            this.prevBounds.width = this.width;
            this.prevBounds.height = this.height;
            
            this.viewportSize.setSize(width - BAR_SIZE, height - BAR_SIZE);
            this.horizontalScrollBar.setBounds(0, height - BAR_SIZE, width - BAR_SIZE, BAR_SIZE);
            this.verticalScrollBar.setBounds(width - BAR_SIZE, 0, BAR_SIZE, height - BAR_SIZE);
            this.scrollLock.setBounds(width - BAR_SIZE, height - BAR_SIZE, BAR_SIZE, BAR_SIZE);
            
            updateBars();
        }
        
        double wheel = engine.getScrollY();
        if (this.active) {
        	onMouseWheel(wheel);
        }
    }

	/**
     * Scroll barların "handle" (tutma yeri) boyutlarını 
     * Viewport ve Content oranına göre hesaplar.
     */
    public void updateBars() {
        // Viewport (Görünen Alan) genişlik ve yüksekliği
        int viewW = this.viewportSize.width;
        int viewH = this.viewportSize.height;

        // Content (Toplam İçerik) genişlik ve yüksekliği
        int contentW = this.scrollComponent.width;
        int contentH = this.scrollComponent.height;

        // Yatay Bar Güncellemesi: (Görünen Genişlik vs Toplam Genişlik)
        this.horizontalScrollBar.updateHandleSize(viewW, contentW);

        // Dikey Bar Güncellemesi: (Görünen Yükseklik vs Toplam Yükseklik)
        this.verticalScrollBar.updateHandleSize(viewH, contentH);
    }
    
    public void onMouseWheel(double direction) {
        // direction genellikle yukarı için -1, aşağı için 1 döner
        // Scroll bar'ın değerini biraz arttır/azalt
        float currentPos = this.verticalScrollBar.getScrollValue();
        this.verticalScrollBar.setScrollValue((float) (currentPos + (direction * 0.05f))); 
        
        // Değer değiştikçe listener tetikleneceği için içerik otomatik kayacaktır
    }
}