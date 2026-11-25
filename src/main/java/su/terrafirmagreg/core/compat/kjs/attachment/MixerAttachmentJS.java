package su.terrafirmagreg.core.compat.kjs.attachment;

import com.google.gson.JsonObject;

@SuppressWarnings("unused")
public class MixerAttachmentJS implements AttachmentJS {

    private Float minRpm = null;
    private Integer circuit = null;

    public MixerAttachmentJS minRpm(float minRpm) {
        this.minRpm = minRpm;
        return this;
    }

    public MixerAttachmentJS rpm(float minRpm) {
        return minRpm(minRpm);
    }

    public MixerAttachmentJS circuit(int circuit) {
        this.circuit = circuit;
        return this;
    }

    @Override
    public String getType() {
        return "mixer";
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getType());
        if (minRpm != null) {
            obj.addProperty("min_rpm", minRpm);
        }
        if (circuit != null) {
            obj.addProperty("circuit", circuit);
        }
        return obj;
    }
}
