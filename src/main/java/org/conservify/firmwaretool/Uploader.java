package org.conservify.firmwaretool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uploader {
    private static final Logger logger = LoggerFactory.getLogger(Uploader.class);

    public boolean upload(File binary, String port, UploaderConfig config) {
        String command = config.getCommandLine();
        Properties properties = new Properties();
        properties.put("bossac", "tools/bossac.exe");
        properties.put("binary", binary.toString().replace("\\", "/"));
        properties.put("port", port);

        String populated = replace(properties, command);
        logger.info(populated);
        RunCommand.run(populated, new File("."), true);
        return true;
    }


    private String getKey(Properties props, String key) {
        if (!props.containsKey(key)) {
            return "{" + key + "}";
        }
        return this.replace(props, (String)props.get(key));
    }

    private String replace(Properties props, String value) {
        Pattern p = Pattern.compile("\\{([\\w\\.-]+)\\}");
        Matcher m = p.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, getKey(props, m.group(1)));
        }
        m.appendTail(sb);

        return sb.toString();
    }
}
