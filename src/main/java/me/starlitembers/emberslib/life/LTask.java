package me.starlitembers.emberslib.life;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LTask {
    private List<String> text = new ArrayList<>();
    private final int[] bound = new int[]{0, 0};
    private final String id;
    private boolean hard;
    public LTask(String id, boolean hard, int minValue, int maxValue, String... text){
        this.id = id;
        setHard(hard);
        setText(text);
        setMinValue(minValue);
        setMaxValue(maxValue);
    }
    public String getId(){
        return id;
    }
    public boolean isHard(){
        return hard;
    }
    public void setHard(boolean hard){
        this.hard = hard;
    }
    public int getMinValue(){
        return bound[0];
    }
    public int getMaxValue(){
        return bound[1];
    }
    public void setMinValue(int minValue){
        bound[0] = minValue;
    }
    public void setMaxValue(int maxValue){
        bound[1] = maxValue;
    }
    public void addTaskMeta(ItemStack book, Player owner){
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("The Secret Keeper");
        meta.setGeneration(BookMeta.Generation.TATTERED);
        if(owner == null){
            meta.setTitle(ChatColor.RED+getId()+" Task");
        } else {
            meta.setTitle(ChatColor.RED+owner.getName()+"'s Task");
        }
        meta.setPages(text);
        meta.setFireResistant(true);
        book.setItemMeta(meta);
    }
    public String[] getText(){
        return text.toArray(new String[0]);
    }
    public String getText(int page){
        return text.size() >= page + 1 ? text.get(page) : null;
    }
    public void setText(String... text){
        this.text = new ArrayList<>();
        this.text.addAll(Arrays.asList(text));
    }
    public void setText(List<String> text){
        this.text = text;
    }
    public void setText(BookMeta meta){
        Optional.of(meta).ifPresent(m -> this.text = m.getPages());
    }
    public void setText(int page, String text){
        if(this.text.size() >= page + 1){
            this.text.set(page, text);
            return;
        }
        else for(int i = Math.max(0, this.text.size() - 1); i < page; i++){
            this.text.set(i, "");
        }
        this.text.set(page, text);
    }
    public int getPages(){
        return text.size();
    }
}
