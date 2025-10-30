package net.ddsmedia.tusa.tusamovil.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ivan on 26/04/2017.
 */

public class PuntoRuta {

    private double latitud;
    private double longitud;
    private double velocidad;
    private String fecha;

    private int iniciadaDiff;
    private int destinoDiff;

    public PuntoRuta(ResultSet info){
        super();

        try {
            this.setLatitud(Double.parseDouble(info.getString("latitud")));
            this.setLongitud(Double.parseDouble(info.getString("longitud")));
            this.setFecha(info.getString("fechaf"));
            this.setIniciadaDiff(info.getInt("iniciada"));
            this.setDestinoDiff(info.getInt("destino"));
            String vel = "0";
            if(!info.getString("velocidad").equals(""))
                vel = info.getString("velocidad");
            this.setVelocidad(Double.parseDouble(vel));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getIniciadaDiff() {
        return iniciadaDiff;
    }

    public void setIniciadaDiff(int iniciadaDiff) {
        this.iniciadaDiff = iniciadaDiff;
    }

    public int getDestinoDiff() {
        return destinoDiff;
    }

    public void setDestinoDiff(int destinoDiff) {
        this.destinoDiff = destinoDiff;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }
}
