package whoiswho;

public class Characters {
    private String name;
    private boolean selected = false;
    private int id;
    private String idString;
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public Characters(String name, int id){
        this.name = ":"+name;
        this.id = id;
        this.idString = Integer.toString(id);
    }

    public String getName() {
        return name;
    }

    public void setRedName(){
        this.name = ANSI_RED+name+ANSI_RESET;
        idString = ANSI_RED + idString + ANSI_RESET;
    }

    public String getIdString() {
        return idString;
    }

    public int getId() {
        return id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
