/*
 * (C) Copyright 2017 Nuxeo (http://nuxeo.com/) and others.
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
 *     Funsho David
 *     Kevin Leturc
 */
package org.nuxeo.ecm.core.versioning;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 9.1
 */
public class VersioningListener implements EventListener {

    private static final Log log = LogFactory.getLog(VersioningListener.class);

    @Override
    public void handleEvent(Event event) {
        if (!(event.getContext() instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) event.getContext();

        VersioningService service = Framework.getService(VersioningService.class);
        if (service == null) {
            log.error("VersioningService service not found ... !");
            return;
        }
        DocumentModel previousDocument = (DocumentModel) docCtx.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
        DocumentModel currentDocument = docCtx.getSourceDocument();

        VersioningOption option = (VersioningOption) currentDocument.getContextData(
                VersioningService.VERSIONING_OPTION);

        // In case of a manual versioning, the versioning option is not null
        if (option == null) {
            option = service.getOptionForAutoVersioning(previousDocument, currentDocument);
            if (option != null && option != VersioningOption.NONE) {
                currentDocument.putContextData(VersioningService.VERSIONING_OPTION, option);
            }
        }
    }

}