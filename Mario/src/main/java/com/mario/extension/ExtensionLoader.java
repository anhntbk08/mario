package com.mario.extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.nhb.common.BaseLoggable;
import com.nhb.common.utils.FileSystemUtils;

final class ExtensionLoader extends BaseLoggable {

	private ExtensionConfigReader configReader;
	private File extFolder;
	private ClassLoader classLoader;
	private String name;

	ExtensionLoader(File extFolder) {
		this.extFolder = extFolder;
	}

	ExtensionLoader(String path) {
		this(new File(path));
	}

	public void load() throws Exception {
		if (extFolder.exists() && extFolder.isDirectory()) {
			// read config
			this.configReader = new ExtensionConfigReader();
			this.configReader.read(FileSystemUtils.createPathFrom(extFolder.getAbsolutePath(), "extension.xml"));
			this.name = configReader.getExtensionName();

			// load jar files
			File libFolder = new File(extFolder.getAbsolutePath(), "lib");
			if (libFolder.exists() && libFolder.isDirectory()) {
				List<File> jars = FileSystemUtils.scanFolder(libFolder);
				if (jars != null && jars.size() > 0) {
					URL[] urls = new URL[jars.size()];
					for (int i = 0; i < jars.size(); i++) {
						File jar = jars.get(i);
						try {
							urls[i] = jar.toURI().toURL();
						} catch (MalformedURLException e) {
							throw new RuntimeException("error while getting url for jar file " + jar.getAbsolutePath(),
									e);
						}
					}
					this.classLoader = new URLClassLoader(urls);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <ClassType> ClassType loadClass(String className) throws ClassNotFoundException {
		if (className != null && className.trim().length() > 0 && this.classLoader != null) {
			Class<?> clazz = this.classLoader.loadClass(className);
			return (ClassType) clazz;
		}
		return null;
	}

	public <T> T newInstance(String className) throws Exception {
		Class<T> clazz = this.loadClass(className);
		if (clazz != null) {
			return clazz.newInstance();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public ExtensionConfigReader getConfigReader() {
		return this.configReader;
	}
}
