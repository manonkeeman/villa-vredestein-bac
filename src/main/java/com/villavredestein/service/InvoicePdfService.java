package com.villavredestein.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.villavredestein.model.Invoice;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoicePdfService {

    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);
    private static final Color BRAND_COLOR = new Color(30, 64, 100);

    public byte[] generate(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addHeader(doc, invoice);
            doc.add(Chunk.NEWLINE);
            addInvoiceTable(doc, invoice);
            doc.add(Chunk.NEWLINE);
            addFooter(doc);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generatie mislukt", e);
        }
    }

    private void addHeader(Document doc, Invoice invoice) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND_COLOR);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);

        Paragraph title = new Paragraph("Villa Vredestein", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        doc.add(title);

        doc.add(new Paragraph("Factuur", FontFactory.getFont(FontFactory.HELVETICA, 14, Color.GRAY)));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Factuurnummer: #" + invoice.getId(), subFont));
        doc.add(new Paragraph("Factuurdatum:  " + fmt(invoice.getIssueDate()), subFont));
        doc.add(new Paragraph("Vervaldatum:   " + fmt(invoice.getDueDate()), subFont));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Aan:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        doc.add(new Paragraph(invoice.getStudent().getUsername(), subFont));
        doc.add(new Paragraph(invoice.getStudent().getEmail(), subFont));
    }

    private void addInvoiceTable(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{4f, 1.5f});
        table.setWidthPercentage(100);

        addHeaderCell(table, "Omschrijving");
        addHeaderCell(table, "Bedrag");

        String desc = invoice.getTitle();
        if (invoice.getDescription() != null && !invoice.getDescription().isBlank()) {
            desc += "\n" + invoice.getDescription();
        }
        addBodyCell(table, desc);
        addBodyCell(table, EUR.format(invoice.getAmount()));

        // Total row
        PdfPCell totalLabel = new PdfPCell(new Phrase("Totaal",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        totalLabel.setBorder(Rectangle.TOP);
        totalLabel.setPadding(8);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell totalValue = new PdfPCell(new Phrase(EUR.format(invoice.getAmount()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_COLOR)));
        totalValue.setBorder(Rectangle.TOP);
        totalValue.setPadding(8);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(totalLabel);
        table.addCell(totalValue);

        doc.add(table);
    }

    private void addFooter(Document doc) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Paragraph footer = new Paragraph("Bedankt voor het op tijd betalen van de huur. Bij vragen kunt u contact opnemen via villavredestein@gmail.com", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        cell.setBackgroundColor(BRAND_COLOR);
        cell.setPadding(8);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private String fmt(java.time.LocalDate date) {
        return date != null ? date.format(DATE_NL) : "-";
    }
}