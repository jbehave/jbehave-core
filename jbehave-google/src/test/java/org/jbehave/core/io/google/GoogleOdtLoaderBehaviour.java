package org.jbehave.core.io.google;

import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.util.ServiceException;
import org.hamcrest.Matchers;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.google.LoadOdtFromGoogle.GoogleAccessFailed;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoogleOdtLoaderBehaviour {

    @Test
    void shouldGetResourceFromDocsService() throws IOException, ServiceException {
        DocsService service = mock(DocsService.class);
        DocumentListFeed feed = mock(DocumentListFeed.class);
        DocumentListEntry entry = mock(DocumentListEntry.class);
        MediaSource mediaSource = mock(MediaSource.class);
        InputStream inputStream = mock(InputStream.class);
        final MediaContent content = mock(MediaContent.class);
        final DocumentQuery query = mock(DocumentQuery.class);
        when(service.getFeed(query, DocumentListFeed.class)).thenReturn(feed);
        when(service.getMedia(content)).thenReturn(mediaSource);
        when(feed.getEntries()).thenReturn(asList(entry));
        when(entry.getContent()).thenReturn(content);
        when(content.getUri()).thenReturn("http://docs.google.com");
        when(mediaSource.getInputStream()).thenReturn(inputStream);

        LoadOdtFromGoogle storyLoader = new LoadOdtFromGoogle("user", "password", "https://docs.google.com/feeds/default/private/full/", service){

            @Override
            DocumentQuery documentQuery(String title) {
                return query;
            }

            @Override
            protected MediaContent mediaContent(String url) {
                return content;
            }
            
        };
        InputStream resourceStream = storyLoader.resourceAsStream("a_story");
        assertThat(resourceStream, Matchers.equalTo(inputStream));
    }

    @Test
    void shouldNotLoadInexistingResourceFromGoogleDocs() {
        LoadOdtFromGoogle googleOdtLoader = new LoadOdtFromGoogle("user", "password",
                "https://docs.google.com/feeds/default/private/full/", mock(DocsService.class));
        assertThrows(InvalidStoryResource.class, () -> googleOdtLoader.loadStoryAsText("an_unexisting_story"));
    }

    @Test
    void shouldNotAllowInvalidAccess() {
        assertThrows(GoogleAccessFailed.class, () -> new LoadOdtFromGoogle("DUMMY", "DUMMY"));
    }

}
