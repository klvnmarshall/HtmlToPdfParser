package dev.marshalll.htmltopdfparser.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;


public interface PdfService {

    ResponseEntity<byte[]> downloadPDF(HttpServletRequest request, HttpServletResponse response);
}
