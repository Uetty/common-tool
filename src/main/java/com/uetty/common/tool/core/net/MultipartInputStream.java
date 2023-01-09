package com.uetty.common.tool.core.net;

import com.uetty.common.tool.core.Mimetypes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MultipartInputStream extends InputStream {

    private static final String DEFAULT_MIMETYPE = "application/octet-stream";

    private static final String PREFIX = "--";

    private final String boundary;

    private static final String SUFFIX = "--";

    private static final String LINE_END = "\r\n";

    private final LinkedHashMap<String, Object> data = new LinkedHashMap<>();

    private boolean finished = false;

    private int pos = 0;

    private InputStream currentStream;

    private List<InputStream> inputStreamList;

    public MultipartInputStream(Map<String, Object> params) {
        this(null, params);
    }

    public MultipartInputStream(String boundary, Map<String, Object> params) {
        String defaultBoundary = "----" + "Boundary" + UUID.randomUUID().toString().replace("-", "");
        this.boundary = boundary == null || "".equals(boundary.trim()) ? defaultBoundary : boundary;
        Map<String, InputStreamWrapper> streamMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof InputStreamWrapper) {
                streamMap.put(key, (InputStreamWrapper) value);
            } else if (value instanceof InputStream) {
                String name = UUID.randomUUID().toString().replace("-", "");
                streamMap.put(key, new InputStreamWrapper(name, (InputStream) value, DEFAULT_MIMETYPE));
            } else if (value instanceof File) {
                File file = (File) value;
                if (!file.exists() || !file.isFile()) {
                    continue;
                }
                String mimetype = Mimetypes.getInstance().getMimetype(file);
                try {
                    streamMap.put(key, new InputStreamWrapper(file.getName(), new FileInputStream(file), mimetype));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String str = String.valueOf(value);
                data.put(key, str);
            }
        }

        data.putAll(streamMap);
    }

    private String escapeName(String name) {
        return name.replace("\"", "%22");
    }

    private ByteArrayInputStream newByteArrayInputStream(StringBuilder sb) {
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    private void initInputStream() throws IOException {
        if (finished) {
            return;
        }
        if (inputStreamList != null) {
            return;
        }
        inputStreamList = new ArrayList<>();

        Set<String> keySet = data.keySet();

        for (String key : keySet) {

            Object o = data.get(key);

            if (o instanceof InputStreamWrapper) {

                InputStreamWrapper wrapper = (InputStreamWrapper) o;
                StringBuilder sb = new StringBuilder(128);

                // ------WebKitFormBoundaryYa8WyvC2Uu5oOvCU
                // Content-Disposition: form-data; name="file"; filename="filename.txt"
                // Content-Type: application/octet-stream
                //
                sb.append(PREFIX);
                sb.append(boundary);
                sb.append(LINE_END);

                String name = escapeName(key);
                String filename = escapeName(wrapper.getName());
                String str = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", name, filename);
                sb.append(str);
                sb.append(LINE_END);

                str = String.format("Content-Type: %s", wrapper.getMimetype());
                sb.append(str);
                sb.append(LINE_END);

                sb.append(LINE_END);

                inputStreamList.add(newByteArrayInputStream(sb));

                // file input stream
                inputStreamList.add(wrapper.getInputStream());

                sb.delete(0, sb.length());
                sb.append(LINE_END);
                inputStreamList.add(newByteArrayInputStream(sb));

            } else {

                // ------WebKitFormBoundaryYa8WyvC2Uu5oOvCU
                // Content-Disposition: form-data; name="dataName"
                //
                // data string

                StringBuilder sb = new StringBuilder(128);
                sb.append(PREFIX);
                sb.append(boundary);
                sb.append(LINE_END);

                String name = escapeName(key);
                String str = String.format("Content-Disposition: form-data; name=\"%s\"", name);
                sb.append(str);
                sb.append(LINE_END);

                sb.append(LINE_END);

                String dataString = String.valueOf(o);
                sb.append(dataString);

                sb.append(LINE_END);

                inputStreamList.add(newByteArrayInputStream(sb));
            }
        }

        StringBuilder sb = new StringBuilder(64);

        sb.append(PREFIX);
        sb.append(boundary);
        sb.append(SUFFIX);

        inputStreamList.add(newByteArrayInputStream(sb));

        currentStream = inputStreamList.get(pos);
    }

    @Override
    public int available() throws IOException {
        if (finished) {
            return 0;
        }

        initInputStream();

        int available = currentStream.available();
        for (int i = pos + 1; i < inputStreamList.size(); i++) {
            available += inputStreamList.get(i).available();
        }

        return available;
    }

    private void updateStream() {
        pos++;
        if (pos < inputStreamList.size()) {
            currentStream = inputStreamList.get(pos);
        } else {
            currentStream = null;
            finished = true;
        }
    }

    @Override
    public int read() throws IOException {
        if (finished) {
            return -1;
        }

        initInputStream();

        int read = currentStream.read();
        if (read == -1) {
            updateStream();
            if (finished) {
                return -1;
            }
            read = currentStream.read();
        }

        return read;
    }

    public String getBoundary() {
        return boundary;
    }

    public static class InputStreamWrapper {

        private final InputStream inputStream;

        private final String name;

        private final String mimetype;

        public InputStreamWrapper(String name, InputStream inputStream, String mimetype) {
            this.inputStream = inputStream;
            this.name = name;
            this.mimetype = mimetype;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getName() {
            return name;
        }

        public String getMimetype() {
            return mimetype;
        }
    }
}
