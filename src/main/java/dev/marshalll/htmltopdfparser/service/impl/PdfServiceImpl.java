package dev.marshalll.htmltopdfparser.service.impl;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.layout.font.FontProvider;
import dev.marshalll.htmltopdfparser.service.PdfService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;


@Service
public class PdfServiceImpl implements PdfService {

    @Autowired
    private final TemplateEngine templateEngine;

    public PdfServiceImpl() {
        this.templateEngine = new TemplateEngine();
    }

    @Override
    public ResponseEntity<byte[]> downloadPDF(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange exchange = application.buildExchange(request, response);
        WebContext context = new WebContext(exchange);

        String invoiceTemplate = templateEngine.process("sample", context);

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        ConverterProperties converterProperties = new ConverterProperties();
        var fontProvider = new FontProvider();
        converterProperties.setBaseUri("http://localhost:8080");

        ClassLoader classLoader = getClass().getClassLoader();

        final var fontStream = classLoader.getResourceAsStream("static/fonts/WorkSans.ttf");

        var fontProgram = FontProgramFactory.createFont(fontStream.readAllBytes());
        fontProvider.addFont(fontProgram);
        converterProperties.setFontProvider(fontProvider);

        HtmlConverter.convertToPdf(invoiceTemplate, target, converterProperties);

        byte[] bytes = target.toByteArray();
        String fileName = "INVOICE";

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".pdf");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(header)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
