package org.yaji.debugger;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.yaji.json.Json;
import org.yaji.json.ParseException;

import FESI.Data.ESValue;
import FESI.Interpreter.Evaluator;

class Command {
    private boolean readingHeader = true;
    private byte [] partial = new byte[1024];
    private int partialIndex = 0;
    private SortedMap<String,String>  headers = new TreeMap<String, String>();
    private byte[] content;
    private int contentIndex;
    private boolean complete;
    private boolean cr;

    public boolean construct(ByteBuffer buffer) {
        if (complete) {
            reset();
        }
        while (buffer.hasRemaining() && !complete) {
            if (readingHeader) {
                String header = readUntilNewline(buffer);
                if (header == null) {
                    return false;
                }
                if (header.length() == 0) {
                    readingHeader = false;
                    int contentLength = getContentLength();
                    content = new byte[contentLength];
                    contentIndex = 0;
                } else {
                    String[] headerParts = header.split(":");
                    headers.put(headerParts[0],headerParts[1]);
                }
            } else {
                byte b = buffer.get();
                System.out.append((char)b);
                content[contentIndex++] = b;
                complete = contentIndex == content.length;
            }
        }
        System.out.append('\n');
        return complete;
    }

    private void reset() {
        readingHeader = true;
        partialIndex = 0;
        contentIndex = 0;
        cr = false;
        headers.clear();
        complete = false;
    }

    private int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }

    private String readUntilNewline(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            System.out.append((char)b);
            if (cr && b == '\n') {
                cr = false;
                String string;
                try {
                    string = new String(partial,0,partialIndex,"UTF-8");
                    partialIndex = 0;
                    return string;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                if (cr) {
                    partial[partialIndex++] = '\r';
                    cr = false;
                }
                if (b == '\r') {
                    cr = true;
                } else {
                    partial[partialIndex++] = b;
                }
            }
        }
        return null;
    }

    public String getHeader(String headerField) {
        return headers.get(headerField);
    }

    public String getContentAsString() {
        try {
            return new String(content,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ESValue getContentAsJson(Evaluator evaluator) {
        Json json = new Json(new StringReader(getContentAsString()));
        json.setEvaluator(evaluator);
        try {
            return json.Value();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void appendHeaders(StringBuilder builder, int contentLength) {
        Set<Entry<String, String>> entrySet = headers.entrySet();
        for (Entry<String, String> entry : entrySet) {
            String key = entry.getKey();
            builder.append(key).append(":");
            if (entry.getKey().equals("Content-Length")) {
                builder.append(contentLength);
            } else {
                builder.append(entry.getValue());
            }
            builder.append("\r\n");
        }
        builder.append("\r\n");
    }
}