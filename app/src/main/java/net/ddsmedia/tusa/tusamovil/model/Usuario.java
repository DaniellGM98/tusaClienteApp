package net.ddsmedia.tusa.tusamovil.model;

import net.ddsmedia.tusa.tusamovil.Utils.Globals;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ivan on 25/03/2017.
 */

public class Usuario {
    private int matricula;
    private String usuario;
    private String nombre;
    //private String razon;
    //private String contacto;
    //private String celular;
    private String email;
    private int temporal;
    private int tipo;
    //private String orden;
    private int pendientes;
    private int salud;
    private int cliente;

    public Usuario(ResultSet info){
        super();
        try {
            //this.setMatricula(info.getInt("fk_cliente"));
            this.setCliente(info.getInt("fk_cliente"));
            this.setNombre(info.getString("usuario"));
            this.setUsuario(info.getString("usuario"));
            /*this.setRazon(info.getString("Razon_social"));
            this.setContacto(info.getString("Contacto"));
            this.setCelular(info.getString("Celular"));*/
            this.setEmail(info.getString("email"));
            this.setTemporal(info.getInt("temporal"));
            this.setTipo(info.getInt("tipo_cliente"));
            this.setPendientes(info.getInt("pendientes"));
            this.setSalud(info.getInt("salud"));
            /*if(info.getString("orden").equals("")){
                this.setOrden("");
                this.setPendientes(0);
            }else{
                this.setOrden(info.getString("orden").substring(1));
                this.setPendientes(Integer.parseInt(info.getString("orden").substring(0,1)));
            }*/
            if(info.getInt("tipo_cliente") == Globals.CLIENTE_ADMINIS ||
                    info.getInt("tipo_cliente") == Globals.CLIENTE_BASE ||
                    info.getInt("tipo_cliente") == Globals.CLIENTE_USUARIO)
                this.setMatricula(info.getInt("fk_matricula"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Usuario(JSONObject info) {
        super();

        try {
            this.setMatricula(info.getInt("matricula"));
            this.setNombre(info.getString("nombre"));
            this.setUsuario(info.getString("usuario"));
            /*this.setRazon(info.getString("razon"));
            this.setContacto(info.getString("contacto"));
            this.setCelular(info.getString("celular"));*/
            this.setEmail(info.getString("email"));
            //this.setOrden(info.getString("orden"));
            this.setPendientes(info.getInt("pendientes"));
            this.setTipo(info.getInt("tipo"));
            this.setSalud(info.getInt("salud"));
            this.setCliente(info.getInt("cliente"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("matricula",this.getMatricula());
        obj.put("nombre",this.getNombre());
        obj.put("usuario",this.getUsuario());
        /*obj.put("razon",this.getRazon());
        obj.put("contacto",this.getContacto());
        obj.put("celular",this.getCelular());*/
        obj.put("email",this.getEmail());
        //obj.put("orden",this.getOrden());
        obj.put("pendientes",this.getPendientes());
        obj.put("tipo",this.getTipo());
        obj.put("salud",this.getSalud());
        obj.put("cliente",this.getCliente());
        return obj;
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /*public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }*/

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTemporal() {
        return temporal;
    }

    public void setTemporal(int temporal) {
        this.temporal = temporal;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    /*public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }*/

    public int getPendientes() {
        return pendientes;
    }

    public void setPendientes(int pendientes) {
        this.pendientes = pendientes;
    }

    public int getSalud() {
        return salud;
    }

    public void setSalud(int salud) {
        this.salud = salud;
    }

    public int getCliente() {
        return cliente;
    }

    public void setCliente(int cliente) {
        this.cliente = cliente;
    }
}
