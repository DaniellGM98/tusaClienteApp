package net.ddsmedia.tusa.tusamovil.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ivan on 27/03/2017.
 */

public class Orden {

    private String ID;
    private String VIN;
    private int estado;
    private int tipo;
    private String estadoStr;
    private String origen;
    private String destino;
    private int operador;
    private String nombreOperador;
    private String celOperador;
    private String latitud;
    private String longitud;
    private String fecha;
    private int calificacion;
    private int fk_compartido;
    private double velocidad;
    private String geoDestino;

    private int fotos;

    public Orden(ResultSet info){
        super();

        try {
            this.setID(info.getString("ID_orden"));
            this.setVIN(info.getString("VIN"));
            //this.setEstadoStr(info.getString("Instrucciones_operador"));
            this.setEstado(info.getInt("estado"));
            this.setTipo(info.getInt("tipo"));
            this.setOperador(info.getInt("fk_matricula"));
            this.setCalificacion(info.getInt("calificacion"));
            this.setOrigen(info.getString("nomOrigen"));
            this.setDestino(info.getString("nomDestino"));
            this.setNombreOperador(info.getString("nombreOperador"));
            this.setCelOperador(info.getString("celOperador"));
            this.setLatitud(info.getString("latitud"));
            this.setLongitud(info.getString("longitud"));
            this.setFecha(info.getString("fecha"));
            this.setFk_compartido(info.getInt("fk_compartido"));
            this.setVelocidad(Double.parseDouble(info.getString("velocidad")));
            this.setGeoDestino(info.getString("geoDestino"));
            //this.setCelAlerta(info.getString("celAlerta"));
            //this.setPobDestino(info.getString("pobDestino"));

            this.setFotos(info.getInt("fotos"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Orden(JSONObject info) {
        super();

        try {
            this.setID(info.getString("ID"));
            this.setVIN(info.getString("VIN"));
            this.setEstadoStr(info.getString("estadoStr"));
            this.setEstado(info.getInt("estado"));
            this.setTipo(info.getInt("tipo"));
            this.setOperador(info.getInt("operador"));
            this.setCalificacion(info.getInt("calificacion"));
            this.setOrigen(info.getString("origen"));
            this.setNombreOperador(info.getString("nombreOperador"));
            this.setCelOperador(info.getString("celOperador"));
            this.setLatitud(info.getString("latitud"));
            this.setDestino(info.getString("destino"));
            this.setLongitud(info.getString("longitud"));
            this.setFecha(info.getString("fecha"));
            this.setFk_compartido(info.getInt("fk_compartido"));
            this.setVelocidad(Double.parseDouble(info.getString("velocidad")));
            this.setGeoDestino(info.getString("geoDestino"));
            //this.setCelAlerta(info.getString("celAlerta"));
            //this.setPobDestino(info.getString("pobDestino"));

            this.setFotos(info.getInt("fotos"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("ID",this.getID());
        obj.put("VIN",this.getVIN());
        obj.put("estadoStr",this.getEstadoStr());
        obj.put("estado",this.getEstado());
        obj.put("tipo",this.getTipo());
        obj.put("origen",this.getOrigen());
        obj.put("destino",this.getDestino());
        obj.put("operador",this.getOperador());
        obj.put("calificacion",this.getCalificacion());
        obj.put("nombreOperador",this.getNombreOperador());
        obj.put("celOperador",this.getCelOperador());
        obj.put("latitud",this.getLatitud());
        obj.put("longitud",this.getLongitud());
        obj.put("fecha",this.getFecha());
        obj.put("fk_compartido",this.getFk_compartido());
        obj.put("velocidad", this.getVelocidad());
        obj.put("geoDestino", this.getGeoDestino());
        //obj.put("celAlerta",this.getCelAlerta());
        //obj.put("pobDestino",this.getPobDestino());

        obj.put("fotos",this.getFotos());
        return obj;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String VIN) {
        this.VIN = VIN;
    }

    public String getEstadoStr() {
        return estadoStr;
    }

    public void setEstadoStr(String estadoStr) {
        this.estadoStr = estadoStr;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getNombreOperador() {
        return nombreOperador;
    }

    public void setNombreOperador(String nombreOperador) {
        this.nombreOperador = nombreOperador;
    }

    public String getCelOperador() {
        return celOperador;
    }

    public void setCelOperador(String celOperador) {
        this.celOperador = celOperador;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    /*public String getPobDestino() {
        return pobDestino;
    }

    public void setPobDestino(String pobDestino) {
        this.pobDestino = pobDestino;
    }*/

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getOperador() {
        return operador;
    }

    public void setOperador(int operador) {
        this.operador = operador;
    }



    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public int getFk_compartido() {
        return fk_compartido;
    }

    public void setFk_compartido(int fk_compartido) {
        this.fk_compartido = fk_compartido;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }

    public String getGeoDestino() {
        return geoDestino;
    }

    public void setGeoDestino(String geoDestino) {
        this.geoDestino = geoDestino;
    }

    public int getFotos() {
        return fotos;
    }

    public void setFotos(int fotos) {
        this.fotos = fotos;
    }
}
