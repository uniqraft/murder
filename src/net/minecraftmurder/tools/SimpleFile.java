package net.minecraftmurder.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SimpleFile {
	public static boolean exists (String path) {
		File file = new File(path);
		return file.exists();
	}
	/**
	 * Tries to load a YamlConfiguration from the specified path.
	 * @param path Where to load the file from.
	 * @param create If true the file will be created if it can't be found
	 * @return If the file could be found it will be returned. If create is true but the file wasn't found an empty file will be created and returned.
	 */
	public static YamlConfiguration loadConfig (String path, boolean create) {
		try {
			if (!create && !exists(path))
				return null;
			File file = new File(path);
			file.createNewFile();
			FileConfiguration fileConfig = new YamlConfiguration();
			fileConfig.load(file);
			return (YamlConfiguration) fileConfig;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static YamlConfiguration loadConfig(String path) {
		return loadConfig(path, true);
	}
	
	public static boolean saveConfig (YamlConfiguration config, String path) {
		try {
			File file = new File(path);
			file.createNewFile();
			config.save(file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static List<YamlConfiguration> getYamlConfigsInPath (String path) {
		String[] filesArray = getFilesInPath(path, ".yml");
		if (filesArray == null)
			return null;
		
		List<String> files = Arrays.asList(filesArray);
		
		List<YamlConfiguration> configs = new ArrayList<YamlConfiguration>();
		for (String file: files) {
			configs.add(loadConfig(file, false));
		}
		
		return configs;
	}
	public static String[] getFilesInPath (String path, final String ending) {
		try {
			File[] files = new File(path).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.endsWith(ending);
				}
			});
			String[] paths = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				paths[i] = files[i].getPath();
			}
			if (paths.length == 0) return null;
			return paths;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}