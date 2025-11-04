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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Handles the creation of payslips pages.
 */
@Component(roles = PayslipDocumentProcessor.class)
@Singleton
public class PayslipDocumentProcessor
{
    private static final String PAYSLIPS_CLASS_NAME = "Payslips.Code.PayslipClass";

    private static final String NAME_KEY = "name";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> documentReferenceResolver;

    /**
     * Saves the given pdf to the document.
     *
     * @param pdfDoc PDF document
     * @throws IOException if any error occurs
     */
    public InputStream getPdfDocumentStream(PDDocument pdfDoc) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdfDoc.save(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Generate the payslips page if missing
     *
     * @param date payslips date
     * @return the generated document
     * @throws XWikiException if any error occurs
     */
    public XWikiDocument generateDocumentsRoot(String date) throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        XWiki wiki = context.getWiki();
        String documentName = String.format("Payslips %s", date);
        List<String> spaces = List.of("Payslips");
        LocalDocumentReference payslipReference = new LocalDocumentReference(spaces, documentName);
        DocumentReference documentReference = new DocumentReference(payslipReference, context.getWikiReference());
        XWikiDocument payslipDoc = wiki.getDocument(documentReference, context);
        if (!wiki.exists(documentReference, context)) {
            addPayslipClass(payslipDoc, documentName);
            wiki.saveDocument(payslipDoc, context);
        }
        return payslipDoc;
    }

    private void addPayslipClass(XWikiDocument payslipDoc, String payslipName) throws XWikiException
    {
        XWikiContext wikiContext = contextProvider.get();
        DocumentReference eventClassRef = documentReferenceResolver.resolve(PAYSLIPS_CLASS_NAME);
        BaseObject eventObj = payslipDoc.newXObject(eventClassRef, wikiContext);
        eventObj.set(NAME_KEY, payslipName, wikiContext);
    }
}
