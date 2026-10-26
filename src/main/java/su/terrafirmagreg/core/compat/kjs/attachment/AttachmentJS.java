package su.terrafirmagreg.core.compat.kjs.attachment;

import com.google.gson.JsonObject;

public interface AttachmentJS {

    String getType();

    JsonObject toJson();
}
