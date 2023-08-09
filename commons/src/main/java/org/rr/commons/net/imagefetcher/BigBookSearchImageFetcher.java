package org.rr.commons.net.imagefetcher;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BigBookSearchImageFetcher extends AImageFetcher
{

    /**
     * Perform a google image search and returns the result.
     *
     * @param searchTerm The search phrase for the search.
     * @return All images found by the search.
     * @throws IOException
     * @see https://developers.google.com/image-search/v1/jsondevguide#json_snippets_java
     */
    private List<IImageFetcherEntry> searchImages(String searchTerm) throws IOException
    {
        final String encodesSearchPhrase = URLEncoder.encode(searchTerm, "UTF-8");

        String urlString =
            "http://bigbooksearch.com/STOP!please-dont-scrape-my-site-you-will-put-my-api-key-over-the-usage-limit-and-the-site-will-break!AGAIN/books/"
                + encodesSearchPhrase;
        LoggerFactory.getLogger(this).log(Level.INFO, "Loading... " + urlString);
        final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(urlString);

        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(resourceLoader.getContent());

            JsonNode resultsNode = rootNode.get("results");
            if (resultsNode.isArray())
            {
                final ArrayList<IImageFetcherEntry> result = new ArrayList<>();

                for (JsonNode resultNode : resultsNode)
                {
                    String link = resultNode.get("link").asText();
                    String title = resultNode.get("title").asText();
                    String img = resultNode.get("image").asText();
                    int width = resultNode.get("width").asInt();
                    int height = resultNode.get("height").asInt();

                    IImageFetcherEntry image = new BigBookImageFetcherEntry(link, img, width, height, title);
                    result.add(image);
                }
                return result;
            }
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(this).log(Level.WARNING, "Failed loading... " + urlString, e);
        }
        finally
        {
            resourceLoader.dispose();
        }

        return Collections.emptyList();
    }

    public List<IImageFetcherEntry> getNextEntries() throws IOException
    {
        if (getSearchTerm() == null || getSearchTerm().isEmpty())
        {
            return Collections.emptyList();
        }
        return searchImages(getSearchTerm());
    }

    /**
     * The implementation for one image search result entry.
     */
    private static class BigBookImageFetcherEntry extends AImageFetcherEntry
    {

        private final String link;
        private final String image;
        private final int width;
        private final int height;
        private final String title;

        private BigBookImageFetcherEntry(String link, String image, int width, int height, String title)
        {
            this.link = link;
            this.image = image;
            this.width = width;
            this.height = height;
            this.title = title;
        }

        @Override
        public URL getThumbnailURL()
        {
            try
            {
                return getImageURL();
            }
            catch (Exception e)
            {
                return null;
            }
        }

        @Override
        public URL getImageURL()
        {
            try
            {
                return new URL(image);
            }
            catch (Exception e)
            {
                return null;
            }
        }

        @Override
        public int getImageWidth()
        {
            try
            {
                return width;
            }
            catch (Exception e)
            {
                return 0;
            }
        }

        @Override
        public int getImageHeight()
        {
            try
            {
                return height;
            }
            catch (Exception e)
            {
                return 0;
            }
        }

        @Override
        public String getTitle()
        {
            try
            {
                return title;
            }
            catch (Exception e)
            {
                return EMPTY;
            }
        }
    }

}
