package com.course.langchain.util;

import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class DocumentParserUtil {
    // ==================== 文件解析器 ====================
    private static final DocumentParser TEXT_PARSER = new TextDocumentParser();
    private static final DocumentParser PDF_PARSER = new ApachePdfBoxDocumentParser();
    private static final DocumentParser MD_PARSER = new MarkdownDocumentParser();
    private static final DocumentParser OFFICE_PARSER = new ApachePoiDocumentParser();

    private static final Set<String> OFFICE_EXTENSIONS = Set.of(
            "doc", "docx", "ppt", "pptx", "xls", "xlsx"
    );

    public DocumentParser resolveParser(String fileName) {
        String ext = getExtension(fileName);
        if (ext == null) throw new RuntimeException("不支持的文件格式");
        if ("pdf".equals(ext)) return PDF_PARSER;
        if ("md".equals(ext)) return MD_PARSER;
        if (OFFICE_EXTENSIONS.contains(ext)) return OFFICE_PARSER;
        return TEXT_PARSER;
    }

    public String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) return null;
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}
