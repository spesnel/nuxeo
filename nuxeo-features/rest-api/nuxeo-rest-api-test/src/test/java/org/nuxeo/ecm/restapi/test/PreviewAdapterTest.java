/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *
 *      Nelson Silva <nsilva@nuxeo.com>
 */
package org.nuxeo.ecm.restapi.test;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.Jetty;
import org.nuxeo.runtime.transaction.TransactionHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;

/**
 * @since 8.2
 */
@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class })
@Jetty(port = 18090)
@RepositoryConfig(cleanup = Granularity.METHOD, init = RestServerInit.class)
@Deploy({ "org.nuxeo.ecm.core.cache", "org.nuxeo.ecm.platform.convert", "org.nuxeo.ecm.platform.preview" })
public class PreviewAdapterTest extends BaseTest {

    @Test
    public void testFilePreview() {
        DocumentModel doc = session.createDocumentModel("/", "adoc", "File");
        Blob blob = Blobs.createBlob("Dummy txt", "text/plain", null, "dummy.txt");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        assertEquals(200, getPreview(doc).getStatus());
        assertEquals(200, getPreview(doc, "file:content").getStatus());
    }

    @Test
    public void testFileAttachmentPreview() {
        DocumentModel doc = session.createDocumentModel("/", "adoc", "File");
        Blob attachment = Blobs.createBlob("Dummy attachment", "text/plain", null, "attachment.txt");
        List<Map<String, Serializable>> fileList = new ArrayList<>();
        fileList.add(Collections.singletonMap("file", (Serializable) attachment));
        doc.setPropertyValue("files:files", (Serializable) fileList);
        doc = session.createDocument(doc);
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        assertEquals(200, getPreview(doc, "files/0/file").getStatus());
    }

    @Test
    public void testNotePreview() {
        DocumentModel doc = session.createDocumentModel("/", "anote", "Note");
        doc.setPropertyValue("note:note", "Dummy note");
        doc.setPropertyValue("note:mime_type", "text/html");
        doc = session.createDocument(doc);
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        assertEquals(200, getPreview(doc).getStatus());
        assertEquals(200, getPreview(doc, "note:note").getStatus());
    }

    protected ClientResponse getPreview(DocumentModel doc) {
        return getPreview(doc, null);
    }

    protected ClientResponse getPreview(DocumentModel doc, String xpath) {
        StringJoiner path = new StringJoiner("/").add("id").add(doc.getId());
        if (xpath != null) {
            path.add("@blob").add(xpath);
        }
        path.add("@preview");
        ClientResponse response = getResponse(RequestType.GET, path.toString());
        assertEquals(200, response.getStatus());
        return response;
    }
}