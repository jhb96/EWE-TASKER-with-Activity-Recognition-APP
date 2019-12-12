package es.dit.gsi.rulesframework;

import java.time.LocalDateTime;

public class DetectedActivityInfo {

    private int confidence;
    private int type;
    private LocalDateTime fecha;

    public DetectedActivityInfo(){}

    public DetectedActivityInfo(int type, int confidence, LocalDateTime fecha){
        this.confidence = confidence;
        this.type = type;
        this.fecha = fecha;
    }

    public int getConfidence() {
        return confidence;
    }
    public int getType(){
        return type;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
