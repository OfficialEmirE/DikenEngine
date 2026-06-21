local world, DikenBridge = ...

Instance = {}

local orijinalPrint = print

print = function(...)
    local args = {...}
    local textList = {}
    
    for i = 1, select("#", ...) do
        local v = args[i]
        
        if v == nil then
            textList[i] = "nil"
        elseif type(v) == "table" and v._javaRef ~= nil then
            textList[i] = tostring(v)
        else
            textList[i] = tostring(v)
        end
    end
    
    local lastText = table.concat(textList, " ")
    
    pcall(function()
        DikenBridge:log(lastText)
    end)
end

local function modifyMetatable(javaObj)
    if javaObj == nil then return nil end
    
    if type(javaObj) == "table" and javaObj._javaRef ~= nil then
        return javaObj
    end

    local proxy = {
        _javaRef = javaObj
    }
    
    local mt = {}
	
	mt.__tostring = function(self)
	    local rawJava = self._javaRef
	    local success, name = pcall(function() return rawJava:getName() end)
	    if success and name then
	        return name
	    end

		return "Instance: " .. tostring(rawJava)
	end
    
    mt.__index = function(self, key)
        local rawJava = self._javaRef
        
        local success, val = pcall(function() return rawJava[key] end)
        if success and val ~= nil then 
            if type(val) == "number" or type(val) == "string" or type(val) == "boolean" then
                return val
            end
            
			if type(val) == "function" then    -- "userdata" kaldırıldı
			    return function(p1, p2, p3) 
			        local target = p1
			        if type(p1) == "table" and p1._javaRef ~= nil then target = p1._javaRef end
			        if p2 and type(p2) == "table" and p2._javaRef ~= nil then p2 = p2._javaRef end
			        if p3 and type(p3) == "table" and p3._javaRef ~= nil then p3 = p3._javaRef end

			        local mSuccess, mRes = pcall(val, target, p2, p3)
			        if mSuccess then
			            if type(mRes) == "number" or type(mRes) == "string" or type(mRes) == "boolean" or mRes == nil then
			                return mRes
			            end
			            return modifyMetatable(mRes)
			        end
			    end
			end

			return modifyMetatable(val)
        end
        
        -- 3. Alt Nesne Arama (game.Workspace.Player gibi)
        if type(key) == "string" and rawJava.findFirstChild then
            local cSuccess, child = pcall(rawJava.findFirstChild, rawJava, key)
            if cSuccess and child ~= nil then
                return modifyMetatable(child) -- Bulunan alt nesneyi de sarmalayıp dönüyoruz!
            end
        end
        
        return nil
    end

    -- DEĞER YAZMA (__newindex)
    mt.__newindex = function(self, key, value)
        local rawJava = self._javaRef
        
        -- Eğer atanacak değer de sarmalanmış bir tabloysa, Java'ya saf halini gönder
        local actualValue = value
        if type(value) == "table" and value._javaRef ~= nil then
            actualValue = value._javaRef
        end

        if key == 'Parent' then
            if actualValue ~= nil then 
                actualValue:addChild(rawJava) 
            end
        else
            -- Java nesnesindeki değeri doğrudan güvenle güncelle
            local gSuccess, oldValue = pcall(function() return rawJava[key] end)
            if not gSuccess then oldValue = nil end
            
            pcall(function() rawJava[key] = actualValue end)
            
            -- Event Tetikleme
            if oldValue ~= actualValue and rawJava.triggerEvent then
                pcall(rawJava.triggerEvent, rawJava, 'OnAnyPropertyChanged', key, oldValue, actualValue)
                pcall(rawJava.triggerEvent, rawJava, 'OnPropertyChanged_' .. key, oldValue, actualValue)
            end
        end
    end
    
    setmetatable(proxy, mt)
    return proxy
end

-- Ana dünyayı sarmalayıp atıyoruz
game = modifyMetatable(world:getRoot())

rawset(game, "GetService", function(self, service) 
    return modifyMetatable(world:getService(service))
end)

rawset(game, "HttpGet", function(self, url)
    return DikenBridge:httpGet(url)
end)

script = modifyMetatable(DikenBridge:getCurrentScript())

hex = function(str)
    if string.sub(str, 1, 2) == '0x' then
        str = string.sub(str, 3)
    end
    return tonumber(str, 16)
end

-- Instance.new çıktısını otomatik sarmalıyoruz
Instance.new = function(className)
    local rawNewObj = DikenBridge:create(className)
    return modifyMetatable(rawNewObj)
end

Instance.clone = function(object)
    local rawNewObj = DikenBridge:clone(object)
    return modifyMetatable(rawNewObj)
end