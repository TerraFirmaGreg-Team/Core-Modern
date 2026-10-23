package net.dries007.tfc.compat.vivecraft;

public class VivecraftIntegration {
	public static boolean isVREnabled()
	{
		try
		{
			Class<?> vrStateClass = Class.forName("org.vivecraft.client_vr.VRState");
			java.lang.reflect.Field vrRunningField = vrStateClass.getDeclaredField("VR_RUNNING");
			return vrRunningField.getBoolean(null);
		}
		catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoClassDefFoundError e)
		{
			return false;
		}
	}

}
