package su.terrafirmagreg.core.compat.kjs.attachment;

import com.google.gson.JsonObject;

@SuppressWarnings("unused")
public class PressAttachmentJS implements AttachmentJS {

    private Integer circuit = null;

    public PressAttachmentJS circuit(int circuit) {
        this.circuit = circuit;
        return this;
    }

    @Override
    public String getType() {
        return "press";
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getType());
        if (circuit != null) {
            obj.addProperty("circuit", circuit);
        }
        return obj;
    }
}
