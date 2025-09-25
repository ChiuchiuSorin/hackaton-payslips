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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.xwiki.component.annotation.Component;

@Component(roles = PDFGenerator.class)
@Singleton
public class PDFGenerator
{
    public PDDocument genereatePDF(Map<String, String> payslip) throws IOException
    {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            URL data = getClass().getClassLoader().getResource("logo.png");
            PDImageXObject headerImage;
            try (InputStream in = data.openStream()) {
                 headerImage = PDImageXObject.createFromByteArray(
                    document, in.readAllBytes(), "logo.png"
                );
            }
            float imageStartingPoint = PDRectangle.A4.getWidth() / 2 - 350 / 2;
            contentStream.drawImage(headerImage, imageStartingPoint, 775, 175, 52.5f); // x, y, width, height
            float yPosition = 750;


            float margin = 50;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20;
            float colWidth = tableWidth / 2;
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);

            for (Map.Entry<String, String> entry : payslip.entrySet()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(entry.getKey());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(margin + colWidth + 150, yPosition);
                contentStream.showText(entry.getValue());
                contentStream.endText();

                yPosition -= rowHeight;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }
}
