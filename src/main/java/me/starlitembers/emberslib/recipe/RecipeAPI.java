package me.starlitembers.emberslib.recipe;

import me.starlitembers.emberslib.EmbersLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class RecipeAPI {
    final Plugin p;
    List<ShapedRecipe> shapedRecipes;
    List<ShapelessRecipe> shapelessRecipes;
    List<Material> itemsToBlacklist;
    public RecipeAPI(Plugin plugin){
        p = plugin;
        shapedRecipes = new ArrayList<>();
        shapelessRecipes = new ArrayList<>();
        itemsToBlacklist = new ArrayList<>();
    }

    public void addShapelessRecipe(String recipeName, Material result, RecipeChoice.MaterialChoice... items){
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));

        for(RecipeChoice.MaterialChoice choice : items){
            shapelessRecipe.addIngredient(choice);
        }

        shapelessRecipes.add(shapelessRecipe);
    }
    public void addShapelessRecipe(String recipeName, Material result, RecipeChoice.ExactChoice... items){
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));

        for(RecipeChoice.ExactChoice choice : items){
            shapelessRecipe.addIngredient(choice);
        }

        shapelessRecipes.add(shapelessRecipe);
    }
    public void addShapelessRecipe(String recipeName, ItemStack result, RecipeChoice.MaterialChoice... items){
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));

        for(RecipeChoice.MaterialChoice choice : items){
            shapelessRecipe.addIngredient(choice);
        }

        shapelessRecipes.add(shapelessRecipe);
    }
    public void addShapelessRecipe(String recipeName, ItemStack result, RecipeChoice.ExactChoice... items){
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));

        for(RecipeChoice.ExactChoice choice : items){
            shapelessRecipe.addIngredient(choice);
        }

        shapelessRecipes.add(shapelessRecipe);
    }
    public void addShapedRecipe(String recipeName, Material result, String row1, String row2, String row3, RecipeChoice.MaterialChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.MaterialChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);

        shapedRecipes.add(shapedRecipe);
    }
    public void addShapedRecipe(String recipeName, Material result, String row1, String row2, String row3, RecipeChoice.ExactChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.ExactChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);

        shapedRecipes.add(shapedRecipe);
    }
    public void addShapedRecipe(String recipeName, ItemStack result, String row1, String row2, String row3, RecipeChoice.MaterialChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.MaterialChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);

        shapedRecipes.add(shapedRecipe);
    }
    public void addShapedRecipe(String recipeName, ItemStack result, String row1, String row2, String row3, RecipeChoice.ExactChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.ExactChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);

        shapedRecipes.add(shapedRecipe);
    }
    public void addDefaultRecipe(DefaultRecipe recipe){
        switch (recipe){
            case TNT -> addShapedRecipe(
                    "tnt",
                    Material.TNT,
                    "PSP",
                    "SGS",
                    "PSP",
                    new RecipeChoice.MaterialChoice(Material.PAPER),
                    new RecipeChoice.MaterialChoice(Material.SAND),
                    new RecipeChoice.MaterialChoice(Material.GUNPOWDER)
            );
            case NAME_TAG -> addShapelessRecipe(
                    "name_tag",
                    Material.NAME_TAG,
                    new RecipeChoice.MaterialChoice(Material.PAPER),
                    new RecipeChoice.MaterialChoice(Material.STRING),
                    new RecipeChoice.MaterialChoice(Material.IRON_INGOT)
            );
        }
    }
    public void removeRecipe(Material item){
        itemsToBlacklist.add(item);
    }

    public void registerRecipes(){
        for(ShapedRecipe s : shapedRecipes){
            try {
                p.getServer().addRecipe(s);
            } catch (Exception e){
                EmbersLib.plugin().getLogger().severe("Could not create recipe "+ s.getKey() +"! does it already exist?");
            }
        }
        for(ShapelessRecipe s : shapelessRecipes){
            try {
                p.getServer().addRecipe(s);
            } catch (Exception e){
                EmbersLib.plugin().getLogger().severe("Could not create recipe "+ s.getKey() +"! does it already exist?");
            }
        }
        Iterator<Recipe> iter = getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe r = iter.next();
            if (itemsToBlacklist.contains(r.getResult().getType())) {
                iter.remove();
            }
        }
    }
    public enum DefaultRecipe{
        TNT,
        NAME_TAG
    }
}
