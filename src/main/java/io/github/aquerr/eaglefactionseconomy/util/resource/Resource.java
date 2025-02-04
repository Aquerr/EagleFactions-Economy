package io.github.aquerr.eaglefactionseconomy.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Resource
{
    private final String path;
    private final URL url;

    Resource(String path, URL url)
    {
        this.path = path;
        this.url = url;
    }

    public InputStream getInputStream() throws IOException
    {
        return this.url.openStream();
    }

    public String getPath()
    {
        return path;
    }
}
