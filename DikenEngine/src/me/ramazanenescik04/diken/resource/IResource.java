package me.ramazanenescik04.diken.resource;

import java.io.*;

/**
 * Defines the `IResource` type within the DikenEngine `resource` package.
 */
public interface IResource extends java.io.Serializable, Cloneable {
	public EnumResource getResourceType();
	
	default boolean resourceIs(EnumResource res) {
		EnumResource thisRes = this.getResourceType();
		
		if(thisRes == res) return true; else return false;
	}
	
	public default void saveResource(DataOutputStream out) throws IOException {}
	public default void loadResource(DataInputStream in) throws IOException {}
	
	public static IResource loadResource(DataInputStream in, String clazzName)
	        throws IOException, ReflectiveOperationException {
		Class<?> clazz = Class.forName(
			clazzName,
		 	true,
		    Thread.currentThread().getContextClassLoader()
		);
		
		return loadResource(in, clazz);
	}
	
	public static IResource loadResource(DataInputStream in, Class<?> clazz)
	        throws IOException, ReflectiveOperationException {

	    Object obj = clazz.getDeclaredConstructor().newInstance();

	    if (!(obj instanceof IResource resource)) {
	        throw new IllegalArgumentException(clazz.getName() + " does not implement IResource");
	    }

	    resource.loadResource(in);
	    return resource;
	}
	
	public default void reload() {}
	public default void disponse() {}
	
	IResource clone();
}
