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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Parse the given payslip content from .xlsx file format.
 */
@Component(roles = ExcelParser.class)
@Singleton
public class ExcelParser
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    public Map<String, Map<String, String>> payslipProcess(AttachmentReference ref, String date) throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        XWiki xwiki = context.getWiki();
        XWikiDocument xWikiDocument = xwiki.getDocument(ref.getDocumentReference(), contextProvider.get());

        try (InputStream fis = xWikiDocument.getAttachment(ref.getName())
            .getContentInputStream(context); Workbook workbook = new XSSFWorkbook(fis))
        {
            Sheet sheet = workbook.getSheetAt(0);
            List<Integer> startingIndexes = getStartingColumns(sheet, date);
            return getIndividualPayslips(sheet, startingIndexes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Integer> getStartingColumns(Sheet sheet, String startsWithPattern)
    {
        List<Integer> startingIndexes = new ArrayList<>();
        int index = 0;
        for (Row row : sheet) {
            if (row.getCell(0) == null) {
                startingIndexes.add(index + 1);
                break;
            }
            if (row.getCell(0).toString().toLowerCase().startsWith(startsWithPattern)) {
                startingIndexes.add(index);
            }
            index++;
        }
        return startingIndexes;
    }

    private Map<String, Map<String, String>> getIndividualPayslips(Sheet sheet, List<Integer> indexes)
    {
        Map<String, Map<String, String>> payslips = new HashMap<>();
        for (int i = 0; i < indexes.size() - 1; i++) {
            payslips.put(getIntranetUsername(sheet, indexes.get(i) + 1, 0),
                getPayslip(indexes.get(i), indexes.get(i + 1) - 1, 0, 1, sheet));
            payslips.put(getIntranetUsername(sheet, indexes.get(i) + 1, 3),
                getPayslip(indexes.get(i), indexes.get(i + 1) - 1, 3, 4, sheet));
        }
        return payslips;
    }

    private String getIntranetUsername(Sheet sheet, int rowIndex, int columnIndex)
    {
        String name = sheet.getRow(rowIndex).getCell(columnIndex).toString();
        String[] parts = name.toLowerCase().split(" ");
        return parts[2].charAt(0) + parts[1];
    }

    private Map<String, String> getPayslip(int start, int end, int keyColumn, int valueColumn, Sheet sheet)
    {
        Map<String, String> paySlip = new LinkedHashMap<>();
        for (int index = start; index < end; index++) {
            Row row = sheet.getRow(index);
            String key = row.getCell(keyColumn).toString();
            String value = row.getCell(valueColumn).toString();
            paySlip.put(key, value);
        }
        return paySlip;
    }
}
