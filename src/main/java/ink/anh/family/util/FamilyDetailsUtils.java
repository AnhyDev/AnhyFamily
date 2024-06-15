package ink.anh.family.util;

import org.bukkit.Location;

import ink.anh.family.fdetails.FamilyDetails;

public class FamilyDetailsUtils {

    /**
     * Перевіряє, чи входить задана точка в радіус сімейного хому і чи знаходиться вона в тому ж світі.
     * @param location Локація, яку потрібно перевірити.
     * @return Істина, якщо локація знаходиться в радіусі сімейного хому і в тому ж світі.
     */
    public static boolean isLocationWithinHomeRadius(FamilyDetails details, Location location) {
        if (details == null || location == null) {
            return false;
        }
        
        Location homeLocation = details.getHomeLocation();
        
        if (homeLocation == null) {
            return false;
        }

        //FamilyConfig config = GlobalManager.getInstance().getFamilyConfig();
        int radius = 20;
        //boolean sameWorldRequired = config.isChestWorld();

        if (!homeLocation.getWorld().equals(location.getWorld())) {
            return false;
        }

        double distance = homeLocation.distance(location);
        return distance <= radius;
    }

}
