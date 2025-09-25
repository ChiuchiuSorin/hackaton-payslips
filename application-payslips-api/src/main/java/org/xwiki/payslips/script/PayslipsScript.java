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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.payslips.internal.ExcelParser;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiException;

@Component
@Named("payslips")
@Singleton
public class PayslipsScript implements ScriptService
{
    @Inject
    private ExcelParser excelParser;

    public void genereatePayslips(AttachmentReference reference, String date ) throws XWikiException
    {
        String officialDate = date;
        if (officialDate == null || officialDate.isEmpty()) {
            Locale romanianLocale = new Locale("ro", "RO");
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, romanianLocale);
            SimpleDateFormat formatter = new SimpleDateFormat("LLLL yyyy", romanianLocale);
            officialDate = formatter.format(new Date());
        }
        excelParser.payslipProcess(reference, officialDate);
    }
}
