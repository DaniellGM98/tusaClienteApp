package net.ddsmedia.tusa.tusamovil.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class People {

    private int id;
    private String nombre;

    public People(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public People(ResultSet info){
        super();

        try {
            this.setId(info.getInt("id"));
            this.setNombre(info.getString("nombre"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String toString(){
        return nombre;
    }
}
