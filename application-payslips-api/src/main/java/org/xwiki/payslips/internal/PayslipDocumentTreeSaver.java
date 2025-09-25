/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.payslips.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.fop.pdf.PDFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(roles = PayslipDocumentTreeSaver.class)
@Singleton
public class PayslipDocumentTreeSaver
{
    private static final List<String> SPACES = List.of("Payslips", "Documents");

    private static final LocalDocumentReference ROOT_SPACE = new LocalDocumentReference(SPACES, "WebHome");

    @Inject
    private Provider<XWikiContext> contextProvider;


    public void saveDocumentPDF(String fileName, PDDocument PDF, XWikiDocument document) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDF.save(baos);
        InputStream pdfStream = new ByteArrayInputStream(baos.toByteArray());
        document.setAttachment(fileName, pdfStream, contextProvider.get());
    }


    public XWikiDocument generateDocumentsRoot() throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        XWiki wiki = context.getWiki();
        DocumentReference documentReference = new DocumentReference(ROOT_SPACE, context.getWikiReference());
        if (!wiki.exists(documentReference, context)) {
            wiki.saveDocument(wiki.getDocument(documentReference, context), context);
        }
        return wiki.getDocument(documentReference, context);
    }

    public void documentSave(XWikiDocument duku) throws XWikiException
    {
        contextProvider.get().getWiki().saveDocument(duku, contextProvider.get());
    }
}
