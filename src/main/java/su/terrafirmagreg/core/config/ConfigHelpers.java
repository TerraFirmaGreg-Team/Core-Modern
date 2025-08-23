package su.terrafirmagreg.core.config;

public final class ConfigHelpers {
    public static String toTitleCase(String input, boolean omitSpaces) {
        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            result.append(part.substring(0, 1).toUpperCase())
                    .append(part.substring(1).toLowerCase())
                    .append(omitSpaces? "" : " ");
        }
        return result.toString().trim();
    }

    public static String toTitleCase(String input) {
        return toTitleCase(input, false);
    }
}
