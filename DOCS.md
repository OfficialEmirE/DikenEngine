# DikenEngine Dokümantasyonu

Bu belge, depodaki mevcut DikenEngine 3.x kaynak koduna göre hazırlanmıştır. DikenEngine Java ile yazılmış 2D bir oyun motoru ve Studio editörüdür. Oyun sahnesi `Node` ağacından oluşur; Lua scriptleri bu ağacı çalışırken kontrol eder, Java pluginleri ise Studio'yu genişletir.

## İçindekiler

- [Kurulum ve çalıştırma](#kurulum-ve-çalıştırma)
- [Proje ve çalışma modeli](#proje-ve-çalışma-modeli)
- [Lua API](#lua-api)
- [Java plugin API](#java-plugin-api)
- [Dünya ve dosya işlemleri](#dünya-ve-dosya-işlemleri)
- [Kaynaklar ve notlar](#kaynaklar-ve-notlar)

## Kurulum ve çalıştırma

### Gereksinimler

- Java 25 veya üzeri önerilir. Kaynak kodu virtual thread ve güncel Java sözdizimi kullanır.
- Windows'ta `run.bat`, LWJGL native dosyalarını ve `DikenEngine/libs` altındaki JAR dosyalarını otomatik classpath'e ekler.
- Linux ve macOS'ta aynı işlemler Java classpath'i elle verilerek yapılmalıdır. Native dosyalar `DikenEngine/res/natives/<OS>` altındadır.

### Windows

```bat
run.bat
run.bat --studio
```

Parametresiz komut oyunu başlatır. `--studio` Studio arayüzünü açar. Derleme çıktısı `DikenEngine/bin` klasörüne yazılır. `run.bat` her çalıştırıldığında `src` altındaki Java dosyalarını derler.

### Elle derleme

Proje kökünden:

```powershell
$cp = "DikenEngine\res;" + ((Get-ChildItem DikenEngine\libs\*.jar).FullName -join ";")
New-Item -ItemType Directory -Force DikenEngine\bin
javac -d DikenEngine\bin -cp $cp (Get-ChildItem DikenEngine\src -Recurse -Filter *.java).FullName
java -cp "$cp;DikenEngine\bin" me.ramazanenescik04.diken.DikenEngine --studio
```

### `.dwf` dünyası

3.0.0 ile `.dwf` dosyaları açılıp oynatılabilir. Studio'da kaynakları ve node ağacını oluşturup dünyayı kaydedin. Runtime tarafında dünya yükleme işlemi `World.loadWorld(...)` ile yapılır.

## Proje ve çalışma modeli

Kaynak kökü `DikenEngine/src/me/ramazanenescik04/diken` klasörüdür.

| Paket | Sorumluluk |
|---|---|
| `game` | `World`, `Node`, `Instance`, event, setting ve oyun modeli |
| `game.nodes` | Kamera, parça, sprite, ses, ışık ve diğer sahne node'ları |
| `game.services` | `Workspace`, `RunService`, input, UI, player ve lighting servisleri |
| `gui` | Runtime GUI bileşenleri ve `UDim2` |
| `resource` | Bitmap, ses, cursor ve kaynak kayıtları |
| `scripting` | LuaJ, script yaşam döngüsü ve Lua bridge |
| `plugin` | Studio plugin yükleme ve yaşam döngüsü |
| `studio` | Editör panelleri, menüler, toolbar ve ayarlar |

Her node bir parent ve child listesine sahiptir. Root node üzerinden tipik erişim:

```lua
local workspace = game:GetService("Workspace")
local run = game:GetService("RunService")
local part = workspace:FindFirstChild("Part")
```

Lua proxy'si Java getter/metot adlarını doğrudan kullanır; ancak pratikte Java isimleri büyük-küçük harf duyarlıdır. `game.Workspace.Part` biçimi child adıyla arama yapar.

## Lua API

### Script yaşam döngüsü

Bir `Script` node'unun `source` alanı çalıştırılır. Script içinde global `update` fonksiyonu varsa her engine update turunda çağrılır.

```lua
function update()
    -- Her frame çağrılır.
end
```

Script üzerindeki kaynak API'si:

| API | Açıklama |
|---|---|
| `script.source` | Lua kaynak metni |
| `script.enabled` | Script çalıştırılabilir mi |
| `script:getSource()` | Kaynağı döndürür |
| `script:setSource(source)` | Kaynağı değiştirir |
| `script:isEnabled()` | Etkinlik durumunu döndürür |
| `script:setEnabled(enabled)` | Etkinlik durumunu değiştirir |

### Global Lua değerleri

`res/scripts/init.lua` tarafından kurulan global değerler:

| Global | Açıklama |
|---|---|
| `game` | Dünya root node proxy'si |
| `script` | Çalışan `Script` node proxy'si |
| `Node` | `new` ve `clone` yardımcılarını içeren tablo |
| `Enum` | Engine enum sınıflarını içeren Java proxy tablosu |
| `UDim2` | `UDim2` Java sınıfı |
| `KeyEvent`, `MouseEvent` | AWT input sınıfları |
| `NodeResource`, `Point`, `Event`, `Signal` | Lua'ya açılan Java sınıfları |
| `hex(value)` | `0x` önekli veya normal hexadecimal string'i sayıya çevirir |
| `print(...)` | Engine console log'una yazar |

`print` birden fazla argümanı boşlukla birleştirir. `print` yerine Java `DikenBridge:log` çağrılır.

### Node oluşturma ve kopyalama

```lua
local part = Node.new("Part")
part.Name = "PlayerBody"
part.Parent = game:GetService("Workspace")

local copy = Node.clone(part)
copy.Parent = part.Parent
```

- `Node.new(className)` `InstanceList` içinde simple class adına göre arar. Örnek adlar: `Part`, `Texture`, `SpriteSheet`, `Folder`, `Script`.
- `Node.clone(object)` yalnızca kopyalanabilir/archivable node'larda çalışır.
- Bulunamayan sınıf veya `nil` nesne için Java console'a hata yazılır ve `nil` dönebilir.
- Child eklemek için `node:addChild(child)` veya `child.Parent = parent` kullanılabilir.

### Node temel API'si

Aşağıdaki metotlar tüm `Node` türevlerine kalıtılır. Java proxy, getter/setter ve public field erişimini Lua'ya taşır. Lua tarafında Java camelCase adları kullanın.

| API | Açıklama |
|---|---|
| `getName()`, `setName(name)` | Node adı |
| `getParent()`, `setParent(parent)` | Parent erişimi |
| `getRootNode()` | Ağacın root node'u |
| `getChildren()` | Doğrudan child kopya listesi |
| `addChild(child)` | Sona child ekler |
| `insertChild(index, child)` | Belirli indekse child ekler |
| `removeChild(child)` | Child'ı ayırır |
| `replaceChild(old, new)` | Child değiştirir |
| `getChildIndex(child)` | Child indeksini döndürür |
| `isDescendantOf(other)` | Soy ağacı kontrolü |
| `getDescendants()` | Tüm alt node'lar |
| `findByName(name)` | İsim eşleşen node listesi |
| `findByClass(clazz)` | Java class tipine göre arama |
| `findByNetId(uuid)` | UUID ile arama |
| `findFirstChild(name)` | Doğrudan child adına göre arama |
| `findFirstChildByNetId(uuid)` | İlk UUID eşleşmesi |
| `findFirstChildOfClass(clazz)` | İlk class eşleşmesi |
| `getFullName()` | Root'tan itibaren noktalı ad |
| `getGlobalX()`, `getGlobalY()` | Global koordinat |
| `toPoint()` | Global `Point` |
| `getNetId()` | UUID kimliği |
| `isRemoved()` | Silinme durumu |
| `removeNode()` | Node'ı kaldırılmak üzere işaretler ve `OnDestroy` ateşler |
| `printTree(printConsole)` | Ağaç metni üretir |
| `getZIndex()`, `setZIndex(value)` | Render sırası |
| `isDebugRenderer()`, `setDebugRenderer(value)` | Debug hitbox çizimi |
| `isArchivable()`, `setArchivable(value)` | Kopyalanabilir/kaydedilebilir durum |

Node event alanları: `OnAddChild`, `OnRemoveChild`, `OnInsertChild`, `OnReplaceChild`, `OnAddDescendant`, `OnRemoveDescendant`, `OnInsertDescendant`, `OnReplaceDescendant`, `OnParentChangedDescendant`, `OnUpdate`, `OnDispose`, `OnReload`, `OnDestroy`, `OnParentChanged`, `OnPropertyChanged`, `OnPreRender`, `OnPostRender`.

### Event API

```lua
local signal = part.OnCollision:Connect(function(other)
    print("Çarpışma", other)
end)

part.OnCollision:FireEvent(other)
part.OnCollision:Disconnect(signal)
```

| API | Açıklama |
|---|---|
| `event:Connect(function(...))` | Lua callback bağlar, `Signal` döndürür |
| `event:Disconnect(signal)` | Bağlantıyı kaldırır |
| `event:FireEvent(...)` | Tüm listener'ları çağırır |
| `BindableEvent:Connect(function(...))` | Sahneye eklenebilen event node'u |
| `BindableEvent:FireEvent(...)` | BindableEvent listener'larını çağırır |

### Instance API

`Instance` tüm render edilebilir node'ların temelidir:

| API | Açıklama |
|---|---|
| `getX()`, `setX(x)`, `getY()`, `setY(y)` | Local konum |
| `getGlobalX()`, `getGlobalY()` | Parent'lar dahil konum |
| `setLocation(x, y)` | Local konumu birlikte ayarlar |
| `getScaleX()`, `setScaleX(v)`, `getScaleY()`, `setScaleY(v)` | Ölçek |
| `getRotation()`, `setRotation(degrees)` | Derece cinsinden dönüş |
| `getColor()`, `setColor(argb)` | ARGB tint |
| `isSolid()`, `setSolid(value)` | Çarpışmaya katılma |
| `isAnchored()`, `setAnchored(value)` | Fizik çözümünde sabitlik |
| `getRenderType()`, `setRenderType(type)` | Render kapsamı |
| `getAABB()`, `getGlobalAABB()`, `hasAABB()` | Hitbox erişimi |
| `setAABB(width, height)` | Yerel AABB oluşturur |
| `getAABBWidth()`, `getAABBHeight()` | AABB boyutu |
| `setAABBSize(width, height)` | AABB boyutunu günceller |
| `findInArea(area)` | Alan içindeki instance'lar |
| `onCollision(other)` | Çarpışma callback'i |

`Enum.RenderType` değerleri: `InVisible`, `OnlyRenderThis`, `OnlyRenderChildrens`, `RenderAll`.

### Oyun node'ları

| Sınıf | Public API / temel alanlar |
|---|---|
| `Part` | `getSurface`, `setSurface`; `Surface` enum: `Top`, `Bottom`, `Left`, `Right`, `Front`, `Back` |
| `Texture` | `getTexture`, `setTexture`, `getTextureBitmap` |
| `ImageNode` | `getTexture`, `setTexture`, `getTextureBitmap` |
| `Decal` | ImageNode API'si; parent instance yüzeyine texture çizer |
| `SpriteSheet` | `getAnimationID`, `setAnimationID`, `getAnimation`, `isPlaying`, `setPlaying`, `getImageType`, `setImageType` |
| `Audio` | `getSound`, `setSound`, `playAudio`, `isPlaying`, `setLoop`, `isLoop`, `setVolume`, `getVolume`, `setPosition`, `getPosition`, `setPitch`, `getPitch` |
| `Light` | `getLightColor`, `setLightColor`, `getRadius`, `setRadius`, `getIntensity`, `setIntensity`, `getType`, `setType`, `getDirection`, `setDirection`, `getConeAngle`, `setConeAngle`, `isShadows`, `setShadows` |
| `Camera` | `getCameraType`, `setCameraType`, `getFollowingInstance`, `setFollowingInstance`, `getPosition`, `setPosition`, `getX`, `setX`, `getY`, `setY`, `addX`, `addY`, `getZoom`, `setZoom`, `reset` |
| `Sky` | `getTexture`, `setTexture`, `syncToCamera` |
| `Tool` | `getIcon`, `setIcon`, `getIconBitmap` |
| `Folder` | Sadece Node hiyerarşi API'si |
| `Model` | Instance API'si ve child render gruplaması |
| `SpawnLocation` | Part API'si; oyuncu spawn noktası |
| `BooleanValue`, `FloatValue`, `IntegerValue`, `StringValue`, `ObjectValue` | `getValue`, `setValue`, `getTypeClass` |

### Servisler

Servisler `game:GetService("ServisAdı")` ile alınır. Varsayılan world servisleri `Workspace`, `PlayerService`, `UIService`, `InputService`, `RunService`, `Lighting` ve `Game`'dir.

| Servis | API |
|---|---|
| `Workspace` | `findInArea(area)`, Node/Instance API'si |
| `RunService` | `isRunning`, `run`, `stop`, `restart`; `OnUpdate` event'i |
| `InputService` | `isKeyDown(key)`, `isKeyPressed(key)`, `isKeyReleased(key)`, `setCursor(resource)` |
| `PlayerService` | `getUsername`, `setUsername`, `getCharacter`, `setCharacter` |
| `Lighting` | `getSky`, `setSky`, `getAmbientColor`, `setAmbientColor`, `isLightingEnabled`, `setLightingEnabled` |
| `UIService` | UI Node ağacını günceller ve çizer |
| `Game` | `HttpSend(url, method, data)`, `HttpGet(url)`, `HttpPost(url, data)` |

`InputService` event'leri: `OnKeyHandled`, `OnKeyDown`, `OnMouseHandled`, `OnMouseClicked`. Input callback argümanları engine'in input modu, key/mouse kodu, karakter ve buton değerleridir.

### GUI API

GUI node'ları `ScreenGui` altına eklenir. Ortak `GuiComponent` API'si:

| API | Açıklama |
|---|---|
| `getPosition`, `setPosition(UDim2 veya x,y)` | UI konumu |
| `getSize`, `setSize(UDim2 veya w,h)` | UI boyutu |
| `getGlobalX`, `getGlobalY`, `getLocalX`, `getLocalY` | Piksel konumları |
| `getWidth`, `getHeight`, `getAbsoluteBounds` | Hesaplanan boyut/bounds |
| `setVisible`, `isVisible` | Görünürlük |
| `setActive`, `isActive` | Input etkinliği |
| `addGuiListener`, `removeGuiListener` | GUI listener yönetimi |

GUI sınıfları ve kendilerine özgü API'ler:

| Sınıf | API |
|---|---|
| `ScreenGui` | `isEnabled`, `setEnabled`, `create`, `createFramePool`, `drawBitmap`, `keyHandled`, `mouseHandled` |
| `Panel` | `isClipsDescendants`, `setClipsDescendants`, `get/setBorderStyle`, `get/setBackgroundColor`, `get/setBorderColor`, `get/setBorderSize` |
| `Text` | `get/setText`, `get/setColor`, `get/setTextPosition`, `get/setFont`, `calculateTextCoordinates` |
| `TextField` | `get/setText`, `setTextChanged`, `setPressedEnter`, `setFocused`, `isFocused`, `setNumberic`, `isNumberField`, `setNumberField` |
| `PasswordField` | TextField API'si |
| `TextLine` | `get/setTextLines`, `add`, `remove`, `clear`, `get/setText`, `get/setFont`, `get/setColor`, `get/setBgColor`, `autoSetSize`, `get/setEditable`, `get/setFocused` |
| `Button` | `get/setText`, `setTextColor`, `setButtonColor`, `setButtonIcon`, `getButtonIcon`, `setButtonIconLeft`, `isButtonIconLeft`, `setRunnable`, `isTouchingMouse` |
| `ImageButton` | `getIcon`, `setIcon` |
| `CheckBox` | `isChecked`, `setChecked`, `get/setText`, `setConsumer` |
| `ProgressBar` | `get/setValue`, `get/setMaxValue`, `get/setColor`, `get/setColor2`, `get/setBackgroundColor`, `get/setText` |
| `RenderImage` | `getTexture`, `setTexture` |
| `ScrollBar` | `addDraggedListener`, `get/setScrollValue`, `updateHandleSize` |
| `ColorPickBox` | `setConsumer`, `get/setSelectedColor`, `get/setHueColor`, `getSelectedPosColor` |
| `ColorPickBar` | `setConsumer`, `get/setSelectedColor` |
| `AlphaPickBar` | `setConsumer`, `setSelectedAlpha`, `setBaseColor` |

### `UDim2` ve enum'lar

```lua
local size = UDim2.of(0, 320, 0, 180)
local panel = Node.new("Panel")
panel.Position = UDim2.zero
panel.Size = size
panel.BorderStyle = Enum.BorderStyle.Line
```

`UDim2` constructor: `UDim2(scaleX, offsetX, scaleY, offsetY)`. Hazır değerler: `UDim2.zero`, `UDim2.defaultV`, `UDim2.fullscreen`. `UDim2:clone()` ve `UDim2:getGlobalPosition(width, height)` kullanılabilir.

Lua'ya açılan enum tabloları:

| Enum tablosu | Kaynak enum |
|---|---|
| `Enum.CameraType` | `Camera.CameraType` |
| `Enum.LightType` | `Light.LightType` |
| `Enum.Surface` | `Part.Surface` |
| `Enum.BorderStyle` | `Panel.BorderStyle` |
| `Enum.ImageType` | `SpriteSheet.ImageType` |
| `Enum.ResourceType` | `EnumResource` |
| `Enum.TextPosition` | `Text.TextPosition` |
| `Enum.RenderType` | `Instance.RenderType` |

## Java plugin API

Pluginler yalnızca Studio sürecinde yüklenir. Plugin ana sınıfı `me.ramazanenescik04.diken.plugin.Plugin` sınıfını extend etmeli ve public boş constructor sağlamalıdır.

### JAR yapısı

JAR dosyasını proje kökündeki `plugins/` klasörüne koyun:

```text
plugins/
  ExamplePlugin.jar
```

JAR root'unda `plugin.json` bulunmalıdır:

```json
{
  "mainClass": "com.example.MyPlugin"
}
```

`PluginManager` her `.jar` dosyasını tarar, `mainClass` sınıfını yükler, `Plugin` alt sınıfı olduğunu doğrular ve public boş constructor ile örnekler.

### Minimum plugin

```java
package com.example;

import me.ramazanenescik04.diken.plugin.Plugin;
import me.ramazanenescik04.diken.resource.Bitmap;

public final class MyPlugin extends Plugin {
    @Override public String getName() { return "My Plugin"; }
    @Override public String getVersion() { return "1.0.0"; }
    @Override public String getAuthor() { return "Author"; }
    @Override public String getDescription() { return "Studio extension."; }
    @Override public Bitmap getIcon() { return null; }

    @Override protected void onEnable() {
        System.out.println("Plugin enabled");
    }

    @Override protected void onDisable() {
        System.out.println("Plugin disabled");
    }
}
```

### `Plugin` sınıfı

Zorunlu abstract metotlar:

| Metot | Açıklama |
|---|---|
| `getName()` | Görünen plugin adı |
| `getVersion()` | Plugin sürümü |
| `getAuthor()` | Yazar |
| `getDescription()` | Açıklama |
| `getIcon()` | `Bitmap` ikon; yoksa `null` |
| `onEnable()` | Etkinleştirme callback'i; subclass'ta `protected` |
| `onDisable()` | Devre dışı bırakma callback'i; subclass'ta `protected` |

Public yaşam döngüsü ve erişim metotları:

| Metot | Açıklama |
|---|---|
| `final enable(DikenEngine, StudioPanel)` | Engine/studio referanslarını set eder ve bir kez `onEnable` çağırır |
| `final disable()` | Etkinse `onDisable` çağırır |
| `isEnabled()` | Durum |
| `info()` | `PluginInfo` metadata'sı |
| `setInfo(PluginInfo)` | Manager tarafından atanır |
| `generateToolbar(Toolbar.Builder)` | Toolbar üretme extension point'i |
| `generateMenubar(Menubar.Builder)` | Menü üretme extension point'i |
| `playTestMode(boolean)` | Play/test modu callback'i |
| `getPluginSettings()` | Studio ayarları; varsayılan `List.of()` |

`enable` ve `disable` final olduğu için plugin doğrudan yaşam döngüsünü override edemez. Engine ve Studio referansları subclass tarafından `protected` `engine` ve `studio` alanlarından kullanılabilir.

### Toolbar builder

```java
@Override
public void generateToolbar(Toolbar.Builder builder) {
    Toolbar toolbar = builder.getToolbar("my-tools");
    builder.addButton(toolbar, "hello", 0, 0, "plugin.hello", () -> {
        System.out.println("Hello");
    });
}
```

`Toolbar` API'si: `addButton(key, AbstractButton)`, `removeButton(key)`, `getToolbarID()`, `getButton(key)`, `getButtons()`.

`Toolbar.Builder` API'si: `newToolbar(id)`, `getToolbar(id)`, `addButton(toolbar, key, iconX, iconY, tooltip, Runnable, Object...)`, `setButtonChecked`, `getButtonChecked`, `getJToolBar()`, `convertCButton(DefaultSingleCDockable)`.

İkon koordinatları `editor_icons` sprite kaynağından alınır. Tooltip metni `Lang.get` üzerinden çevrilir.

### Menubar builder

```java
@Override
public void generateMenubar(Menubar.Builder builder) {
    Menubar menu = builder.newMenu("my-menu", "plugin.menu");
    String id = builder.addMenuItem(menu, "plugin.open", 0, 1,
        () -> System.out.println("Open"));
    builder.setButtonEnabled(menu, id, true);
}
```

`Menubar` API'si: `addSeperator()`, `addButton(key, JMenuItem)`, `removeButton(key)`, `getName()`, `setName(name)`, `getMenubarID()`, `getButton(key)`, `getButtons()`.

`Menubar.Builder` API'si: `newMenu`, `getMenu`, `addMenuItem`, `addMenuItemCheckBox`, `addMenuAccelerator`, `addMenuSeparator`, `setButtonChecked`, `getButtonChecked`, `setButtonEnabled`, `isButtonEnabled`, `getJMenuBar`, `getJPopupMenu`.

İkon parametreleri negatifse ikon eklenmez. `KeyStroke` overload'u menü accelerator'ı ekler.

### Plugin ayarları

`getPluginSettings()` içinde `Setting<?>` listesi döndürerek Studio ayar paneline ayar eklenir. `Setting` üzerinde kullanılan temel metotlar:

```java
Setting<Boolean> setting = new Setting<>(
    "Enabled", true, Boolean.class, EnumSettingType.CHECK_BOX
).addChangeListener(value -> {
    // Yeni değer
});
```

Önemli `Setting` API'si: `getName`, `setName`, `getValue`, `setValue`, `getTypeClass`, `getSettingType`, `addChangeListener`, `getChangeListeners`, `isSaveable`, `setSaveable`.

### `PluginInfo` ve manager

`PluginInfo` bir Java record'dur:

```java
Class<? extends Plugin> pluginClass();
File pluginFile();
URLClassLoader classLoader();
```

`PluginManager.instance` singleton'ının public API'si:

| Metot/alan | Açıklama |
|---|---|
| `loadPlugins(engine, studio)` | `./plugins/*.jar` dosyalarını yükler |
| `loadLocalPlugin(pluginClass, engine, studio)` | Classpath içindeki plugin'i yükler |
| `enableAll(engine, studio)` | Tüm pluginleri etkinleştirir; sonra `allPluginsLoaded` event'ini ateşler |
| `disableAll()` | Pluginleri kapatır, URL classloader'ları kapatır ve listeyi temizler |
| `getPlugins()` | Değiştirilemez plugin listesi |
| `allPluginsLoaded` | Tüm pluginler etkinleştirildikten sonra ateşlenen `Event` |

Pluginler sıralı veya bağımlılık kontrollü yüklenmez; klasördeki JAR sırasına göre yüklenir. Hatalı bir plugin loglanır ve diğer pluginlerin yüklenmesi sürer.

## Dünya ve dosya işlemleri

Java tarafında world serialization API'si:

```java
World world = World.loadWorld(file);
World.saveWorld(world, file);
byte[] bytes = World.saveWorldToBytes(world);
World copy = World.loadWorldFromBytes(bytes);
```

Overload'lar `File`, `InputStream` ve `OutputStream` kabul eder. `.dwf` içeriği GZIP ile sıkıştırılmış binary node verisidir; dosyayı elle JSON olarak düzenlemeyin.

Kaynaklar `ResourceLocator` ile yönetilir:

```java
IResource resource = ResourceLocator.getResource("background");
ResourceLocator.addResource("my-resource", resource);
```

Kaynak türleri `Bitmap`, `ArrayBitmap`, `SoundResource`, `CursorResource`, `UniFont` ve `EnumResource` tarafından desteklenir. Lua tarafı normalde node property'leri üzerinden resource ID string'i kullanır.

## Kaynaklar ve notlar

- Lua entegrasyonu LuaJ 3.0.2 kullanır.
- Scriptler ayrı virtual thread üzerinde başlar; `Script.stop()` thread'i keser ve Lua update callback'ini temizler.
- Lua `update` fonksiyonunda uzun bloklayan işlemler runtime performansını etkileyebilir.
- Event callback'leri bağlantı sırasına göre çağrılır. Aynı callback'i birden fazla bağlarsanız birden fazla çağrı alırsınız.
- `setParent` için `nil` vermek mevcut kaynakta özel bir null-parent yolu kullanır; node ayırırken doğrudan `parent:removeChild(node)` tercih etmek daha güvenlidir.
- `DikenEngine` Java 25 önerisiyle derlenir; bağımlılık JAR'ları `DikenEngine/libs` klasöründedir.
- Bu belge kaynakta gerçekten bulunan public API'leri kapsar. Yeni bir public method eklendiğinde bu dosya da güncellenmelidir.
