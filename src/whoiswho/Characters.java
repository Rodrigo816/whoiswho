package whoiswho;

public class Characters {
    private String name;
    private boolean selected = false;

    public Characters(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
