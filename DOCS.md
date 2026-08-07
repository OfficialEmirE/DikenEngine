# DikenEngine Documentation

## Overview
DikenEngine, Java ile yazılmış bir 2D oyun motoru ve stüdyo editörüdür. Bu doküman:
- projenin nasıl derleneceğini ve çalıştırılacağını,
- DikenEngine Java plugin API'sini,
- Lua script API'sini,
- plugin yapısını ve yükleme sürecini.

## Hızlı Başlangıç
### Derleme ve Çalıştırma
Proje kökünde `run.bat` dosyası bulunmaktadır.

- Studio modunda başlatmak için:
  - `run.bat --studio`
- Oyun modu ile başlatmak için:
  - `run.bat`
  - Bu durumda çalıştırma sırasında bir `.dwf` dosyası seçmeniz istenir.

### Gereksinimler
- Java 25 ve üzeri önerilir.
- Proje, `DikenEngine\libs` altındaki bağımlılıkları ve `DikenEngine\res` kaynaklarını kullanır.

## Çalıştırma Alternatifleri
`DikenEngine/src/me/ramazanenescik04/diken/DikenEngine.java` içinde ana giriş noktası bulunur:
- `public static void main(String[] args)`
- `--studio` parametresi ile stüdyo modu açılır.
- Parametre yoksa kullanıcı bir `.dwf` dosyası seçer ve oyunu yükler.

## Studio Modu
Studio modunda `StudioPanel` sınıfı ana denetleyicidir.
- `StudioPanel.loadWorld(World world)` : Dünyayı stüdyoya yükler.
- `StudioPanel.startPlayTest()` : mevcut düzenleme dünyasını test modunda oynatır.
- `StudioPanel.stopPlayTest()` : playtest modunu durdurur.
- `StudioPanel.stop()` : stüdyoyu kapatır.

### Plugin Studio Entegrasyonu
- `PluginManager.instance.allPluginsLoaded` olayı pluginler yüklendikten sonra toolbar ve menüleri yeniden oluşturur.
- Plugin `generateToolbar` ve `generateMenubar` kullanarak stüdyo arabirimine özel butonlar ekleyebilir.

## Plugin Sistemi
DikenEngine pluginleri `DikenEngine/src/me/ramazanenescik04/diken/plugin` içindeki `Plugin` sınıfı üzerinden yönetir.

### Plugin Yükleme
- `PluginManager.instance.loadPlugins(DikenEngine engine, StudioPanel studio)`
  - `./plugins/` klasöründeki `.jar` dosyalarını tarar.
  - Her `.jar` içindeki `plugin.json` dosyasını okur.
  - `mainClass` alanıyla belirtilen sınıfı yükler.
  - Sınıf `Plugin` alt sınıfı olmalıdır.
- `PluginManager.instance.loadLocalPlugin(Class<? extends Plugin> pluginClass, DikenEngine engine, StudioPanel studio)`
  - Proje içinden doğrudan bir plugin sınıfını yerel olarak ekler.
- `PluginManager.instance.enableAll(DikenEngine engine, StudioPanel studio)`
  - yüklü tüm pluginlerin `enable(...)` metodunu çağırır.
- `PluginManager.instance.disableAll()`
  - pluginleri devre dışı bırakılır ve sınıf yükleyici kapatılır.

### Plugin Sınıfı Yapısı
Her plugin `me.ramazanenescik04.diken.plugin.Plugin` sınıfını genişletmelidir.

```java
public abstract class Plugin {
    protected DikenEngine engine;
    protected StudioPanel studio;

    public abstract String getName();
    public abstract String getVersion();
    public abstract String getAuthor();
    public abstract String getDescription();
    public abstract Bitmap getIcon();

    protected abstract void onEnable();
    protected abstract void onDisable();

    public final void enable(DikenEngine engine, StudioPanel studio) { ... }
    public final void disable() { ... }
    public boolean isEnabled() { ... }

    public void generateToolbar(Toolbar.Builder builder) {}
    public void generateMenubar(Menubar.Builder builder) {}
    public void playTestMode(boolean b) {}
    public List<Setting<?>> getPluginSettings() { return List.of(); }
}
```

### Örnek Plugin
`ExamplePlugin` projenin içinde basit bir örnektir:
```java
public class ExamplePlugin extends Plugin {
    @Override
    public String getName() { return "Example Plugin"; }
    @Override
    public String getVersion() { return "0.1"; }
    @Override
    public String getAuthor() { return "Ramazanenescik04"; }
    @Override
    public String getDescription() { return "An example plugin."; }
    @Override
    public Bitmap getIcon() { return null; }
    @Override
    protected void onEnable() {}
    @Override
    protected void onDisable() {}
}
```

### Plugin Jar Yapısı
Bir plugin jar dosyası içinde:
- `plugin.json`
- plugin sınıfı ve ilgili bağımlılıkları.

Örnek `plugin.json`:
```json
{
  "mainClass": "com.example.MyPlugin"
}
```

### Plugin Studio UI Entegrasyonu
Pluginler aşağıdaki yöntemlerle stüdyo arabirimine ekleme yapabilir:
- `generateToolbar(Toolbar.Builder builder)`
- `generateMenubar(Menubar.Builder builder)`
- `playTestMode(boolean b)` : playtest moduna girildiğinde/çıktığında çağrılır.
- `getPluginSettings()` : plugin ayarlarının stüdyo ayarları listesine eklenmesi için `Setting<?>` listesi döndürür.

`Toolbar.Builder` ve `Menubar.Builder` ile stüdyo araç çubuğu ve menü öğeleri oluşturabilirsiniz.

#### Toolbar Örneği
```java
public void generateToolbar(Toolbar.Builder builder) {
    var toolbar = builder.newToolbar("myPluginToolbar");
    builder.addButton(toolbar, "doSomething", 0, 0, "myplugin.buttonTooltip", () -> {
        // buton tıklama işlemi
    });
}
```

#### Menubar Örneği
```java
public void generateMenubar(Menubar.Builder builder) {
    var menu = builder.newMenu("myPluginMenu", "myplugin.menu.title");
    builder.addMenuItem(menu, "myplugin.action", 1, 1, () -> {
        // menü tıklama işlemi
    });
}
```

## Java API
Aşağıda DikenEngine içinde pluginler veya modüller tarafından kullanılabilecek temel API parçaları listelenmiştir.

### DikenEngine
`me.ramazanenescik04.diken.DikenEngine`

- `static DikenEngine getEngine()` : ana engine örneğini döndürür.
- `boolean isStudioMode()` : stüdyo modunda mı çalıştığını bildirir.
- `StudioPanel getStudio()` : stüdyo panelini döndürür (stüdyo modunda değilse `null` olabilir).
- `World getWorld()` : şu anki dünya.
- `void setWorld(World world)` : aktif dünyayı değiştirir.
- `void setCursor(CursorResource cursor)` : fare imlecini ayarlar.
- `int getScaledWidth()`, `int getScaledHeight()` : render edilmiş çözünürlüğü döndürür.
- `int getScale()` : ölçek değerini döndürür.
- `void log(String message)` : konsola normal mesaj yazar.
- `void errorLog(String message)` : hata mesajı yazdırır.
- `void errorLog(String message, Throwable e)` : hata mesajını istisna detaylarıyla yazar.

### StudioPanel
`me.ramazanenescik04.diken.studio.StudioPanel`

- `void loadWorld(World world)`
- `void startPlayTest()`
- `void stopPlayTest()`
- `void stop()`
- `void tick()` : stüdyo güncellemeleri için.
- `Event generatingToolbar`, `Event generatingMenubar` : toolbar/menü oluşturulurken tetiklenen olaylar.

### World
`me.ramazanenescik04.diken.game.World`

- `World(String gameName)` : yeni dünya yaratır.
- `<T extends Service> T getService(Class<T> serviceClass)` : servisleri alır.
- `<T extends Service> T getService(String serviceName)` : servis adından alır.
- `List<Service> getServices()` : tüm servisler.
- `List<Node> getAllNodes()` : dünyadaki tüm düğümler.
- `void startScripts()` : dünyadaki tüm `Script` düğümlerini başlatır.
- `Camera getCamera()`
- `Workspace getWorkspace()`
- `RunService getRunService()`
- `IResource[] getResources(EnumResource animation)`
- `String[] getResourceKeys(EnumResource animation)`
- `<T extends IResource> T getResource(String key, EnumResource expectedType)`

### Node
`me.ramazanenescik04.diken.game.Node`

- `void addChild(Node child)`
- `void insertChild(int index, Node child)`
- `void removeChild(Node child)`
- `void replaceChild(Node oldChild, Node newChild)`
- `Node getParent()`
- `void setParent(Node newParent)`
- `List<Node> getChildren()`
- `String getName()`
- `void setName(String name)`
- `Node getRootNode()`
- `boolean isRemoved()`
- `boolean isArchivable()`
- `int getZIndex()`
- `void setZIndex(int zIndex)`
- `List<Node> find(Predicate<Node> condition)`
- `List<Node> findByName(String name)`
- `<T> List<T> findByClass(Class<T> clazz)`
- `List<Node> findByNetId(UUID target)`
- `Node findFirstChild(String name)`
- `Node findFirstChildByNetId(UUID netId)`
- `<T extends Node> T findFirstChildOfClass(Class<T> clazz)`
- `List<Node> getDescendants()`

#### Node Olayları
`Node` içinde birçok `Event` alanı vardır, örneğin:
- `OnAddChild`, `OnRemoveChild`, `OnUpdate`, `OnDestroy`, `OnPropertyChanged`, `OnPostRender`, vb.

### InstanceList
`me.ramazanenescik04.diken.game.InstanceList`

- `List<Node> getNodeList()` : kayıtlı node şablonları.
- `int registeredNodeCount()`
- `Node getRegisteredNode(int index)`
- `Node getRegisteredNode(Class<? extends Node> nodeClass)`
- `boolean isRegistered(Node node)`
- `Map<CategoryKey, List<Node>> getTypedNodes()`

Bu liste stüdyo editöründeki "Basic Objects" panelini oluşturur.

## Lua API
DikenEngine, Lua scriptlerini `me.ramazanenescik04.diken.scripting.Script` kullanarak çalıştırır.

### Script Yürütme
- `Script.initialize(World theWorld)` : Lua scriptini başlatır.
- `Script.stop()` : scripti durdurur.
- `Script.update(World world, DikenEngine engine)` : her frame güncellemesi için çağrılır.
- `Script.getSource()`, `Script.setSource(String source)` : Lua kodunu okur/yazar.
- `Script.isEnabled()`, `Script.setEnabled(boolean enabled)` : script etkinliği.

### Lua Köprüsü
`me.ramazanenescik04.diken.scripting.LuaBridge`

- `Object create(String className)`
  - `InstanceList` içinden verilen sınıf adına karşılık gelen node türünü bulur.
  - `Node.new("SpriteSheet")` gibi çağrıların arka planını oluşturur.
- `Object clone(Object object)`
  - bir `Node` veya `Cloneable` nesnesini klonlar.
- `Object getCurrentScript()`
  - şu anki script nesnesini döner.
- `void log(String message)`
  - DikenEngine konsoluna log yazar.

### Lua Global Nesneleri
`res/scripts/init.lua` içinde şu nesneler sağlanır:
- `world` -> `World` kök düğümünü içerir.
- `DikenBridge` -> `LuaBridge` örneği.

Script çalışırken aşağıdaki global nesneler hazır olur:
- `game` : `world:getRoot()` kök düğümü.
- `script` : mevcut script nesnesi.
- `Node.new(className)` : kayıtlı node tiplerinden yeni bir örnek oluşturur.
- `Node.clone(object)` : verilen nesnenin kopyasını alır.
- `hex(str)` : onaltılık dizgeyi sayıya çevirir.
- `print(...)` : DikenEngine loguna yazar.

### Lua İçin Desteklenen Java Sınıfları
`LuaInit.initClasses()` aşağıdaki sınıfları global olarak sunar:
- `UDim2`
- `KeyEvent`
- `MouseEvent`
- `NodeResource`
- `Point`
- `Event`
- `Signal`

### Lua İçin Desteklenen Enumlar
`LuaInit.initEnums()` aşağıdaki enum tiplerini `Enum` tablosu altında sağlar:
- `Enum.CameraType`
- `Enum.LightType`
- `Enum.Surface`
- `Enum.BorderStyle`
- `Enum.ImageType`
- `Enum.ResourceType`
- `Enum.TextPosition`
- `Enum.RenderType`

### Lua API Kullanım Örnekleri
```lua
-- Bir node oluşturma
local sprite = Node.new("SpriteSheet")
sprite.ImageType = Enum.ImageType.Single
sprite.X = 100
sprite.Y = 50

-- Oyundaki servisi alma
local workspace = game:GetService("Workspace")

-- Script loglama
print("Hello from Lua script")

-- Klon oluşturma
local clone = Node.clone(sprite)
```

### Lua'da Java Nesne Erişimi
`init.lua` içinde Java nesnelerine metatable sarmalayıcı uygulanır:
- `rawJava:method()` çağrıları `pcall` ile güvenli şekilde yapılır.
- `game.ChildName` şeklinde erişimler önce normal alanlara, sonra `findFirstChild` yöntemine bakar.
- Java nesnelerine `__index` ve `__newindex` ile özellik yazma desteği sağlanır.

## Plugin Geliştirme Önerileri
### Yeni Plugin Başlatma
1. `Plugin` sınıfını extend edin.
2. `getName()`, `getVersion()`, `getAuthor()`, `getDescription()` gibi zorunlu metodları doldurun.
3. `onEnable()` içinde gerekli başlatma işlerini yapın.
4. `generateToolbar()` veya `generateMenubar()` ile stüdyoye kontrol ekleyin.
5. `getPluginSettings()` ile `Setting<?>` listesi döndürerek ayarları kaydedin.
6. `plugin.json` oluşturun ve jar içine ekleyin.

### Örnek Plugin Akışı
```java
public class MyPlugin extends Plugin {
    @Override public String getName() { return "MyPlugin"; }
    @Override public String getVersion() { return "1.0"; }
    @Override public String getAuthor() { return "YourName"; }
    @Override public String getDescription() { return "A custom DikenEngine plugin."; }
    @Override public Bitmap getIcon() { return null; }

    @Override protected void onEnable() {
        DikenEngine.log("MyPlugin enabled");
    }

    @Override protected void onDisable() {
        DikenEngine.log("MyPlugin disabled");
    }

    @Override public void generateToolbar(Toolbar.Builder builder) {
        var toolbar = builder.newToolbar("myplugin");
        builder.addButton(toolbar, "sayHello", 0, 0, "myplugin.hello", () -> {
            DikenEngine.log("Hello from MyPlugin toolbar button");
        });
    }
}
```

### `plugin.json`
```json
{
  "mainClass": "com.example.MyPlugin"
}
```

## Önemli Dosya ve Paketler
- `DikenEngine/src/me/ramazanenescik04/diken/DikenEngine.java`
- `DikenEngine/src/me/ramazanenescik04/diken/plugin/Plugin.java`
- `DikenEngine/src/me/ramazanenescik04/diken/plugin/PluginManager.java`
- `DikenEngine/src/me/ramazanenescik04/diken/scripting/Script.java`
- `DikenEngine/src/me/ramazanenescik04/diken/scripting/LuaBridge.java`
- `DikenEngine/src/me/ramazanenescik04/diken/scripting/LuaInit.java`
- `DikenEngine/src/me/ramazanenescik04/diken/game/Node.java`
- `DikenEngine/src/me/ramazanenescik04/diken/game/World.java`

## Notlar
- `PluginManager` sadece `./plugins/` klasöründeki `.jar` dosyalarını tarar.
- Eğer `plugin.json` eksikse veya `mainClass` `Plugin` sınıfını genişletmiyorsa plugin yükleme atlanır.
- Stüdyo modunda `StudioUtils.registerPluginSettings(plugin)` çağrısı plugin ayarlarını stüdyo menüsüne ekler.
- `LuaBridge` ve `init.lua` bir arada çalışarak Lua scriptlerine oyun dünyasını ve log mekanizmasını bağlar.

---
Bu doküman, DikenEngine projesinin temel kullanımını, Java plugin API'sini ve Lua script API'sini kapsayacak şekilde hazırlandı.
