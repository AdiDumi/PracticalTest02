package ro.pub.cs.systems.eim.practicaltest02.model;

public class Valuta {
    private String euro;
    private String dolar;
    private String lastUpdate;

    public Valuta() {
        this.euro = null;
        this.dolar = null;
        this.lastUpdate = null;
    }

    public Valuta(String euro, String valuta, String lastUpdate) {
        this.euro = euro;
        this.dolar = dolar;
        this.lastUpdate = lastUpdate;
    }

    public String getEuro() {
        return euro;
    }

    public void setEuro(String euro) {
        this.euro = euro;
    }

    public String getDolar() {
        return dolar;
    }

    public void setDolar(String dolar) {
        this.dolar = dolar;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String printEuro() {
        return "Ultima actualizare{" +
                "euro='" + this.euro + '\'' +
                ", la data='" + this.lastUpdate + '\'' +
                '}';
    }

    public String printDolar() {
        return "Ultima actualizare{" +
                "euro='" + this.dolar + '\'' +
                ", la data='" + this.lastUpdate + '\'' +
                '}';
    }
}
