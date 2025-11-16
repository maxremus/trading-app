package org.example.invoiceservice.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoicePdfService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public byte[] generateInvoicePdf(UUID id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found."));

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Зареждаме Unicode шрифт (DejaVuSans)
            InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");
            if (fontStream == null) {
                throw new RuntimeException("Font DejaVuSans.ttf not found in resources/fonts/");
            }
            var font = PDType0Font.load(document, fontStream);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // Заглавие
            content.beginText();
            content.setFont(font, 18);
            content.newLineAtOffset(70, 750);
            content.showText("Фактура № " + invoice.getId());
            content.endText();

            // Подробности
            int y = 710;
            content.beginText();
            content.setFont(font, 12);
            content.newLineAtOffset(70, y);
            content.showText("Поръчка: " + invoice.getOrderId());
            content.newLineAtOffset(0, -20);
            content.showText("Клиент: " + invoice.getCustomerName());
            content.newLineAtOffset(0, -20);
            content.showText("ЕИК:" + invoice.getEik());
            content.newLineAtOffset(0, -20);
            content.showText("Дата на издаване: " +
                    invoice.getIssuedOn().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            content.newLineAtOffset(0, -20);
            content.showText("Обща сума: " + invoice.getTotalAmount() + " лв.");
            content.endText();

            content.close();

            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }
}
