package org.aksw.simba.lsq.cli.main.cmd;

import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

public abstract class VersionProviderFromClasspathProperties
    implements IVersionProvider {

    abstract String getResourceName();
    abstract Collection<String> getStrings(Properties properties);

    @Override
    public String[] getVersion() throws Exception {
        String resourceName = getResourceName();

        Properties properties = new Properties();
        try (InputStream in = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(resourceName),
                "Resource not found: " + resourceName)) {
            properties.load(in);
        }

        String[] result = getStrings(properties).toArray(new String[0]);
        return result;
    }

}
