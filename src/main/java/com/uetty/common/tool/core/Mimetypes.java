package com.uetty.common.tool.core;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
public class Mimetypes {

    /* The default MIME type */
    public static final String DEFAULT_MIMETYPE = "application/octet-stream";

    private static Mimetypes mimetypes = null;

    private final HashMap<String, String> extensionToMimetypeMap = new HashMap<String, String>();

    private Mimetypes() {
    }

    private static InputStream getResource() {
        InputStream inputStream = Mimetypes.class.getResourceAsStream("/META-INF/mime.types");
        if (inputStream == null) {
            inputStream = Mimetypes.class.getResourceAsStream("/mime.types");
        }
        return inputStream;
    }

    public synchronized static Mimetypes getInstance() {
        if (mimetypes != null)
            return mimetypes;

        mimetypes = new Mimetypes();
        InputStream is = getResource();
        if (is != null) {
            log.debug("Loading mime types from file in the classpath: mime.types");

            try {
                mimetypes.loadMimetypes(is);
            } catch (IOException e) {
                log.error("Failed to load mime types from file in the classpath: mime.types", e);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        } else {
            log.warn("Unable to find 'mime.types' file in classpath");
        }
        return mimetypes;
    }

    public void loadMimetypes(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("#") || line.length() == 0) {
                // Ignore comments and empty lines.
            } else {
                StringTokenizer st = new StringTokenizer(line, " \t");
                if (st.countTokens() > 1) {
                    String extension = st.nextToken();
                    if (st.hasMoreTokens()) {
                        String mimetype = st.nextToken();
                        extensionToMimetypeMap.put(extension.toLowerCase(), mimetype);
                    }
                }
            }
        }
    }

    public String getMimetype(String fileName) {
        String mimeType = getMimetypeByExt(fileName);
        if (mimeType != null) {
            return mimeType;
        }
        return DEFAULT_MIMETYPE;
    }

    public String getMimetype(File file) {
        return getMimetype(file.getName());
    }

    public String getMimetype(File file, String key) {
        return getMimetype(file.getName(), key);
    }

    public String getMimetype(String primaryObject, String secondaryObject) {
        String mimeType = getMimetypeByExt(primaryObject);
        if (mimeType != null) {
            return mimeType;
        }

        mimeType = getMimetypeByExt(secondaryObject);
        if (mimeType != null) {
            return mimeType;
        }

        return DEFAULT_MIMETYPE;
    }

    private String getMimetypeByExt(String fileName) {
        int lastPeriodIndex = fileName.lastIndexOf(".");
        if (lastPeriodIndex > 0 && lastPeriodIndex + 1 < fileName.length()) {
            String ext = fileName.substring(lastPeriodIndex + 1).toLowerCase();
            if (extensionToMimetypeMap.containsKey(ext)) {
                return (String) extensionToMimetypeMap.get(ext);
            }
        }
        return null;
    }

    public List<String> getExtNamesByMimetype(String mimetype) {
        List<String> extNames = new ArrayList<>();
        if (mimetype == null) {
            return extNames;
        }
        for (Map.Entry<String, String> entry : extensionToMimetypeMap.entrySet()) {
            if (mimetype.equalsIgnoreCase(entry.getValue())) {
                extNames.add(entry.getKey());
            }
        }
        return extNames;
    }
}
