package dev.marshalll.htmltopdfparser.controller;

import dev.marshalll.htmltopdfparser.service.PdfService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/download")
public class PdfController {

    @Autowired
    private PdfService pdfService;


    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPDF(HttpServletRequest request, HttpServletResponse response) {
        return pdfService.downloadPDF(request, response);
    }
}
