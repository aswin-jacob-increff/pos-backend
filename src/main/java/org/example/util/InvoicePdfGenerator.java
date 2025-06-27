package org.example.util;

import org.apache.fop.apps.*;
import org.example.pojo.InvoicePojo;
import org.example.pojo.InvoiceItemPojo;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

public class InvoicePdfGenerator {

    public static byte[] generatePdf(InvoicePojo invoice) throws Exception {
        // Generate FO XML string
        String foXml = generateFoXml(invoice);

        // Set up FOP
        FopFactory fopFactory = FopFactory.newInstance(new java.io.File(".").toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        Source src = new StreamSource(new StringReader(foXml));
        Result res = new SAXResult(fop.getDefaultHandler());

        transformer.transform(src, res);

        return out.toByteArray();
    }

    private static String generateFoXml(InvoicePojo invoice) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin="1cm">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body">
        """);

        sb.append("<fo:block font-size='14pt' font-weight='bold'>Invoice #" + invoice.getId() + "</fo:block>");
        sb.append("<fo:block>Order ID: " + invoice.getOrder().getId() + "</fo:block>");
//        sb.append("<fo:block>Client: " + invoice.getOrder().getClient().getName() + "</fo:block>");
        sb.append("<fo:block>Date: " + invoice.getOrder().getDate() + "</fo:block>");
        sb.append("<fo:block space-after='1cm'/>");

        sb.append("""
            <fo:table border='solid 1px black' width='100%'>
              <fo:table-header>
                <fo:table-row>
                  <fo:table-cell><fo:block>Product</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>Qty</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>Price</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>Total</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-header>
              <fo:table-body>
        """);

        for (InvoiceItemPojo item : invoice.getInvoiceItemList()) {
            sb.append("<fo:table-row>");
            sb.append("<fo:table-cell><fo:block>" + item.getName() + "</fo:block></fo:table-cell>");
            sb.append("<fo:table-cell><fo:block>" + item.getQuantity() + "</fo:block></fo:table-cell>");
            sb.append("<fo:table-cell><fo:block>" + item.getPrice() + "</fo:block></fo:table-cell>");
            sb.append("<fo:table-cell><fo:block>" + item.getAmount() + "</fo:block></fo:table-cell>");
            sb.append("</fo:table-row>");
        }

        sb.append("""
              </fo:table-body>
            </fo:table>
        """);

        sb.append("<fo:block space-before='1cm'>Total Quantity: " + invoice.getTotalQuantity() + "</fo:block>");
        sb.append("<fo:block>Total Amount: ₹" + invoice.getTotal() + "</fo:block>");

        sb.append("</fo:flow></fo:page-sequence></fo:root>");
        return sb.toString();
    }
}
