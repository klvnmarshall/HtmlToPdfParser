package dev.marshalll.htmltopdfparser.service.impl;

import com.itextpdf.commons.actions.IEvent;
import com.itextpdf.commons.actions.IEventHandler;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.svg.converter.SvgConverter;
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
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.*;
import java.net.MalformedURLException;


@Service
public class PdfServiceImpl implements PdfService {

    @Autowired
    private final TemplateEngine templateEngine;

    public PdfServiceImpl() {
        this.templateEngine = new TemplateEngine();
    }

    public static final String IMAGE = "./src/main/resources/static/images/acme.svg";

    @Override
    public ResponseEntity<byte[]> downloadPDF(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange exchange = application.buildExchange(request, response);
        WebContext context = new WebContext(exchange);

        String invoiceTemplate = templateEngine.process("sample", context);

        ConverterProperties converterProperties = new ConverterProperties();
        var fontProvider = new FontProvider();
        converterProperties.setBaseUri("http://localhost:8080");

        ClassLoader classLoader = getClass().getClassLoader();
        final String[] FONTS = {
                "static/fonts/WorkSans-Bold.ttf",
                "static/fonts/WorkSans-Regular.ttf",
                "static/fonts/WorkSans-SemiBold.ttf",
        };

        for(String font: FONTS) {
            final var fontStream = classLoader.getResourceAsStream(font);
            var fontProgram = FontProgramFactory.createFont(fontStream.readAllBytes());
            fontProvider.addFont(fontProgram);
        }

        converterProperties.setFontProvider(fontProvider);

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(target);
        PdfDocument pdfDocument = new PdfDocument(writer);
        //Header headerHandler = new Header(IMAGE);
        Footer footerHandler = new Footer(IMAGE);

        //pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, headerHandler);
        pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, footerHandler);

        HtmlConverter.convertToPdf(invoiceTemplate, pdfDocument, converterProperties);

        //footerHandler.writeTotal(pdfDocument);
        pdfDocument.close();
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

    protected class Footer extends AbstractPdfDocumentEventHandler {
        private String imagePath;

        public Footer(String imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        protected void onAcceptedEvent(AbstractPdfDocumentEvent event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();

            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();

            Canvas canvas = new Canvas(new PdfCanvas(page), pageSize);

            try (FileInputStream svgStream = new FileInputStream(imagePath)) {
                // Convert SVG to a form XObject and wrap it as an Image
                PdfFormXObject xObject = SvgConverter.convertToXObject(svgStream, pdf);
                Image img = new Image(xObject);

                // Center horizontally and place at fixed Y (footer)
                float imgWidth = img.getImageScaledWidth();
                float x = (pageSize.getWidth() - imgWidth) / 2f;

                img.setFixedPosition(x, img.getImageScaledHeight());
                canvas.add(img);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("SVG file not found: " + imagePath, e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                canvas.close();
            }


//            Image img = null;
//            try {
//                img = new Image(ImageDataFactory.create(imagePath));
//            } catch (MalformedURLException e) {
//                throw new RuntimeException(e);
//            }
//            float imgWidth = img.getImageScaledWidth();
//            float x = (pageSize.getWidth() - imgWidth) / 2;
//
//            img.setFixedPosition(x, img.getImageScaledHeight());
//            canvas.add(img);
//            canvas.close();
        }
    }


}
