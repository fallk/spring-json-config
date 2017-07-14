package com.example;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProprietySourceConfigHandler {

    @Autowired
    private ConfigurableEnvironment env;

    @Bean
    @Lazy(false)
    public JSONPropertySource importPropertySource() {
        // debug!
        System.out.println("Injecting.");

        JSONPropertySource props = new JSONPropertySource(new File("./res/"));
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(props);


        return props;
    }

    private class JSONPropertySource extends PropertySource<Object> {
        private final File jsonPath;

        private final Map<String, JSONObject> jsonCache = new HashMap<>();

        /**
         * Create a new {@code JSONPropertySource} with the given name and source object.
         */
        public JSONPropertySource(File jsonPath) {
            super("json-prop", new Object());
            if (!jsonPath.isDirectory()) {
                throw new IllegalArgumentException("jsonpath must point to a directory");
            }
            this.jsonPath = jsonPath;
        }

        /**
         * Return the value associated with the given name,
         * or {@code null} if not found.
         *
         * @param name the property to find
         * @see PropertyResolver#getRequiredProperty(String)
         */
        @Override
        public Object getProperty(String name) {
            if (!name.startsWith("json.")) {
                return null;
            }
            String[] args = name.substring("json.".length()).split("\\.");

            // assume config if no json name present.
            if (args.length == 1) {
                args = new String[] { "config", args[0] };
            }
            // debug!
            System.out.println(Arrays.toString(args));

            // final reference for lambda body
            final String[] _args = args;
            JSONObject obj = jsonCache.computeIfAbsent(args[0], k -> {
                try {
                    return new JSONObject(new JSONTokener(new FileInputStream(new File(jsonPath, _args[0] + ".json"))));
                } catch (IOException e) {
                    throw new RuntimeException("could not parse json at " + jsonPath.getPath() + File.separator + _args[0] + ".json", e);
                }
            });

            // TODO add proper body support

            return obj.get(args[1]);
        }
    }
}