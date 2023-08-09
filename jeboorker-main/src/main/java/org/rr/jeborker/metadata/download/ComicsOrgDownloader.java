package org.rr.jeborker.metadata.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtil;

public class ComicsOrgDownloader implements MetadataDownloader
{

    private static final String MAIN_URL = "http://www.comics.org";

    private static final String QUERY_URL = MAIN_URL + "/searchNew/?q={0}&search_object=issue&page={1}";

    private static final int PAGES_TO_LOAD = 1;
    private static final int START_PAGE = 1;

    @Override
    public List<MetadataDownloadEntry> search(String phrase)
    {
        try
        {
            String encodesSearchPhrase = URLEncoder.encode(phrase, StringUtil.UTF_8);
            String searchUrl = MessageFormat.format(QUERY_URL, encodesSearchPhrase, START_PAGE);

            List<byte[]> pageHtmlContent = MetadataDownloadUtils.loadPages(Collections.singletonList(new URL(searchUrl)), PAGES_TO_LOAD);
            List<Document> htmlDocs = MetadataDownloadUtils.getDocuments(pageHtmlContent, MAIN_URL);
            List<String> searchResultLinks = findSearchResultLinks(htmlDocs);
            List<byte[]> metadataHtmlContent = MetadataDownloadUtils.loadLinkContent(searchResultLinks, MAIN_URL);
            return getMetadataDownloadEntries(metadataHtmlContent);
        }
        catch (IOException e)
        {
            LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to fetch metadata for search '" + phrase + "'", e);
        }
        return null;
    }

    private List<MetadataDownloadEntry> getMetadataDownloadEntries(List<byte[]> metadataHtmlContent) throws IOException
    {
        List<MetadataDownloadEntry> result = new ArrayList<>(metadataHtmlContent.size());
        for (byte[] html : metadataHtmlContent)
        {
            if (html != null)
            {
                Document htmlDoc = Jsoup.parse(new ByteArrayInputStream(html), StringUtil.UTF_8, MAIN_URL);
                result.add(new ComicsOrgDownloadEntry(htmlDoc, MAIN_URL));
            }
        }
        return result;
    }

    private List<String> findSearchResultLinks(List<Document> docs)
    {
        List<String> result = new ArrayList<>();
        for (Document doc : docs)
        {
            Elements links = doc.getElementsByTag("a");
            for (int i = 0; i < links.size(); i++)
            {
                String href = links.get(i).attr("href");
                if (href.startsWith("/issue/"))
                {
                    result.add(href);
                }
            }
        }
        return result;
    }

}
