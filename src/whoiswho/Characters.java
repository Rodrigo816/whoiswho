package whoiswho;

public class Characters {
    private String name;
    private boolean selected = false;
    private String gender;
    private int id;

    public Characters(String name, String gender, int id){
        this.name = name;
        this.gender = gender;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getGender(){
        return gender;
    }

    public int getId() {
        return id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
