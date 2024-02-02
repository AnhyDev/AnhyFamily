package ink.anh.family.common;

import org.bukkit.inventory.ItemStack;
import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;

public class Prices {

    private Currency currency;
    private Map<FamilyService, BigInteger> amountByService;
    private Map<FamilyService, ItemStack> itemByService;

    // Constructor
    public Prices(Currency currency, BigInteger amountMarriage, ItemStack itemMarriage, 
                  BigInteger amountDivorce, ItemStack itemDivorce, 
                  BigInteger amountAdoption, ItemStack itemAdoption) {
        this.currency = currency;
        this.amountByService = new EnumMap<>(FamilyService.class);
        this.itemByService = new EnumMap<>(FamilyService.class);

        amountByService.put(FamilyService.MARRIAGE, amountMarriage);
        itemByService.put(FamilyService.MARRIAGE, itemMarriage);
        
        amountByService.put(FamilyService.DIVORCE, amountDivorce);
        itemByService.put(FamilyService.DIVORCE, itemDivorce);

        amountByService.put(FamilyService.ADOPTION, amountAdoption);
        itemByService.put(FamilyService.ADOPTION, itemAdoption);
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigInteger getAmountForService(FamilyService service) {
        return amountByService.getOrDefault(service, BigInteger.ZERO);
    }

    public ItemStack getItemForService(FamilyService service) {
        return itemByService.getOrDefault(service, new ItemStack(org.bukkit.Material.AIR));
    }

    // Setters
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setAmountForService(FamilyService service, BigInteger amount) {
        amountByService.put(service, amount);
    }

    public void setItemForService(FamilyService service, ItemStack item) {
        itemByService.put(service, item);
    }
}
