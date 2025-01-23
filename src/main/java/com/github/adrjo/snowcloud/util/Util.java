package com.github.adrjo.snowcloud.util;

public class Util {

    /**
     * Whether the file should be viewable in the browser or directly downloaded
     * MIGHT BE DANGEROUS, limited to only a few formats for now
     *
     * @param contentType the content type of the file
     * @return true if the file should be viewable in browser, false otherwise
     */
    public static boolean isInlineViewable(String contentType) {
        return contentType.startsWith("image/")
                || contentType.equals("application/pdf")
                || contentType.equals("text/plain");
    }
}
