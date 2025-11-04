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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(roles = ArchiveGenerator.class)
@Singleton
public class ArchiveGenerator
{
    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    @Inject
    private PayslipDocumentProcessor payslipDocumentProcessor;

    @Inject
    private PDFGenerator pdfGenerator;

    public String generateArchive(Map<String, Map<String, String>> payslips, String payslipDate)
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWiki wiki = wikiContext.getWiki();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
            XWikiDocument document = payslipDocumentProcessor.generateDocumentsRoot(payslipDate);
            String archiveName = String.format("%s.zip", document.getDocumentReference().getName());
            XWikiAttachment archive = document.getAttachment(archiveName);
            if (archive != null) {
                document.removeAttachment(archive);
            }
            for (Map.Entry<String, Map<String, String>> entry : payslips.entrySet()) {
                populateZipEntry(payslipDate, entry.getValue(), entry.getKey(), zipOutputStream);
            }
            zipOutputStream.flush();
            zipOutputStream.close();
            baos.flush();
            baos.close();
            document.setAttachment(archiveName, new ByteArrayInputStream(baos.toByteArray()), wikiContext);
            wiki.saveDocument(document, wikiContext);
            return document.getAttachmentURL(archiveName, "download", wikiContext);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating the files archive.", e);
        }
    }

    private void populateZipEntry(String payslipDate, Map<String, String> value, String key, ZipOutputStream zos)
        throws IOException
    {
        PDDocument pdfDocument = pdfGenerator.generatePDF(value);
        ZipEntry zipEntry = new ZipEntry("payslips/" + String.format("%s-%s.pdf", key, payslipDate));
        zos.putNextEntry(zipEntry);
        try (InputStream in = payslipDocumentProcessor.getPdfDocumentStream(pdfDocument)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }
        zos.closeEntry();
    }
}
