package su.terrafirmagreg.core.compat.kjs.attachment;

import com.google.gson.JsonObject;

@SuppressWarnings("unused")
public class VacuumChamberAttachmentJS implements AttachmentJS {

    private boolean mode = false;

    public VacuumChamberAttachmentJS mode(boolean mode) {
        this.mode = mode;
        return this;
    }

    public VacuumChamberAttachmentJS mode(String mode) {
        this.mode = "pressure".equalsIgnoreCase(mode) || "pressurizing".equalsIgnoreCase(mode);
        return this;
    }

    public VacuumChamberAttachmentJS vacuum() {
        this.mode = false;
        return this;
    }

    public VacuumChamberAttachmentJS pressure() {
        this.mode = true;
        return this;
    }

    public VacuumChamberAttachmentJS vacuum(boolean vacuum) {
        this.mode = !vacuum;
        return this;
    }

    public VacuumChamberAttachmentJS pressure(boolean pressure) {
        this.mode = pressure;
        return this;
    }

    public VacuumChamberAttachmentJS vacuumMode() {
        return vacuum();
    }

    public VacuumChamberAttachmentJS pressureMode() {
        return pressure();
    }

    @Override
    public String getType() {
        return "vacuum_chamber";
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getType());
        obj.addProperty("mode", mode);
        return obj;
    }
}
