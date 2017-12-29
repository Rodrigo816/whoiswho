package whoiswho;

public class Characters {
    private String name;
    private boolean selected = false;
    private String gender;

    public Characters(String name, String gender){
        this.name = name;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public String getGender(){
        return gender;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
