/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wikifont.wikifontinterface;

import java.io.Serializable;

public class Font implements Serializable{

    public String family;
    public String version;
    public String lastModified;
    public String category;
    public String kind;
    public String menu;

    //Costruttori
    public Font(String family, String version, String lastModified, String category, String kind, String menu) {
        this.family = family;
        this.version = version;
        this.lastModified = lastModified;
        this.category = category;
        this.kind = kind;
        this.menu = menu;
    }

    public Font() {
    }

    //Metodi getter e setter
    public String getFamily() {
        return family;
    }

    public String getVersion() {
        return version;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getCategory() {
        return category;
    }

    public String getKind() {
        return kind;
    }

    public String getMenu() {
        return menu;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }
    
    
}
