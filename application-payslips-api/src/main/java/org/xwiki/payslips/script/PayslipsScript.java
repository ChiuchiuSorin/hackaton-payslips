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
package org.xwiki.payslips.script;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.payslips.internal.ExcelParser;
import org.xwiki.payslips.internal.PDFGenerator;
import org.xwiki.payslips.internal.PayslipDocumentSaver;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
@Named("payslips")
@Singleton
public class PayslipsScript implements ScriptService
{
    @Inject
    private ExcelParser excelParser;

    @Inject
    private PDFGenerator pdfGenerator;

    @Inject
    private PayslipDocumentSaver payslipDocumentSaver;

    public void generatePayslips(AttachmentReference reference, String date) throws XWikiException, IOException
    {
        String officialDate = date;
        if (officialDate == null || officialDate.isEmpty()) {
            Locale romanianLocale = new Locale("ro", "RO");
            SimpleDateFormat formatter = new SimpleDateFormat("LLLL yyyy", romanianLocale);
            officialDate = formatter.format(new Date());
        }
        Map<String, Map<String, String>> payslips = excelParser.payslipProcess(reference, officialDate);
        XWikiDocument wikiDocument = payslipDocumentSaver.generateDocumentsRoot(officialDate);

        for (Map.Entry<String, Map<String, String>> entry : payslips.entrySet()) {
            PDDocument pdfDocument = pdfGenerator.genereatePDF(entry.getValue());
            payslipDocumentSaver.saveDocumentPDF(String.format("%s-%s", entry.getKey(), officialDate), pdfDocument,
                wikiDocument);
            pdfDocument.close();
        }

        payslipDocumentSaver.documentSave(wikiDocument);
    }
}
