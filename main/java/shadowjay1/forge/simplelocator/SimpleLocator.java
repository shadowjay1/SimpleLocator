package shadowjay1.forge.simplelocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.lwjgl.input.Keyboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Mod(modid="simplelocator", name="SimpleLocator", version=SimpleLocator.version)
public class SimpleLocator {
	@Instance(value = "simplelocator")
	public static SimpleLocator instance;
	public static LocatorSettings settings;
	public static NetworkThread networkThread;
	public static File configFile;
	public static File offlineLocationsFile;
	static List<String> previousPlayerList = new ArrayList<String>();
	public static final String version = "v4.0";
	public static KeyBinding binding;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		File directory = event.getModConfigurationDirectory();
		
		if(!directory.isDirectory()) {
			directory.mkdir();
		}
		
		configFile = new File(directory, "simplelocator.json");
		offlineLocationsFile = new File(directory, "simplelocator-offline.json");
		
		if(!offlineLocationsFile.isFile()) {
			try {
				offlineLocationsFile.createNewFile();
				
				FileWriter writer = new FileWriter(offlineLocationsFile);
				Gson gson = new GsonBuilder().create();
				
				writer.write(gson.toJson(new HashMap<String, LocatorLocation>()));
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(offlineLocationsFile));
				String line;
				String content = "";
				
				while((line = reader.readLine()) != null) {
					content += line;
				}
				
				reader.close();
				
				Gson gson = new GsonBuilder().create();
				LocatorListener.offlineLocations = gson.fromJson(content, OfflineLocationsJson.class);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!configFile.isFile()) {
			try {
				configFile.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			settings = new LocatorSettings();
			settings.save(configFile);
		}
		else {
			settings = LocatorSettings.load(configFile);
			
			if(settings == null)  {
				settings = new LocatorSettings();
			}
			
			settings.save(configFile);
		}
		
		try {
			LaunchClassLoader l = (LaunchClassLoader) this.getClass().getClassLoader();
			
			Field field = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");

			field.setAccessible(true);
			Set<String> exclusions = (Set<String>) field.get(l);

			exclusions.remove("org.apache.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		AuthenticationThread authThread = new AuthenticationThread();
		authThread.start();
		networkThread = new NetworkThread();
		networkThread.start();
		LocalLocationNetwork locationThread = new LocalLocationNetwork();
		locationThread.start();
		
		for(GroupConfiguration group : settings.getGroups()) {
			if(group.getUpdateURL() != null && !group.getUpdateURL().trim().isEmpty()) {
				new GroupUpdateThread(group).start();
			}
		}
	}
	
	public static ArrayList<Field> doubles = new ArrayList<Field>();
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		GroupingThread groupingThread = new GroupingThread();
		groupingThread.start();
		
		LocatorListener listener = new LocatorListener();
		
		MinecraftForge.EVENT_BUS.register(listener);
		FMLCommonHandler.instance().bus().register(listener);
		
		ClientRegistry.registerKeyBinding(binding = new KeyBinding("Locator settings", Keyboard.KEY_L, "SimpleLocator"));
	}
	
	public static void saveConfiguration() {
		settings.save(configFile);
	}
	
	public static String filterChatColors(String s) {
		return EnumChatFormatting.getTextWithoutFormattingCodes(s);
	}
	
	public static void onPlayerLeave(String player) {
		GroupConfiguration group = settings.getGroups().getByUsername(player);
		
		if(group != null && group.isTrackingOnline()) {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + player + " left the server."));;
		}
	}
	
	public static void onPlayerJoin(String player) {
		GroupConfiguration group = settings.getGroups().getByUsername(player);
		
		if(group != null && group.isTrackingOnline()) {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + player + " joined the server."));
		}
	}
}
