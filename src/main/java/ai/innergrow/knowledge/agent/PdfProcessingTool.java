package ai.innergrow.knowledge.agent;

import ai.innergrow.knowledge.model.PdfParseResult;
import ai.innergrow.knowledge.service.KnowledgeStorageService;
import ai.innergrow.knowledge.service.PdfParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Tool function for PDF processing that can be used by the AI agent
 */
@Component
public class PdfProcessingTool implements Function<PdfProcessingTool.Request, PdfProcessingTool.Response> {

    private static final Logger logger = LoggerFactory.getLogger(PdfProcessingTool.class);
    
    private final PdfParserService pdfParserService;
    private final KnowledgeStorageService knowledgeStorageService;

    public PdfProcessingTool(PdfParserService pdfParserService, 
                            KnowledgeStorageService knowledgeStorageService) {
        this.pdfParserService = pdfParserService;
        this.knowledgeStorageService = knowledgeStorageService;
    }

    @Override
    public Response apply(Request request) {
        logger.info("Processing PDF directory: {}", request.pdfDirectory());
        
        try {
            // Parse PDFs using Python script
            List<PdfParseResult> parseResults = pdfParserService.parsePdfDirectory(request.pdfDirectory());
            
            // Save results to database
            int savedCount = knowledgeStorageService.saveKnowledgeBatch(parseResults);
            
            return new Response(
                true,
                String.format("Successfully processed %d PDFs and saved %d to database", 
                    parseResults.size(), savedCount),
                parseResults.size(),
                savedCount
            );
            
        } catch (Exception e) {
            logger.error("Error processing PDFs from directory: {}", request.pdfDirectory(), e);
            return new Response(
                false,
                "Error: " + e.getMessage(),
                0,
                0
            );
        }
    }

    /**
     * Request record for PDF processing
     */
    public record Request(String pdfDirectory) {
    }

    /**
     * Response record for PDF processing result
     */
    public record Response(
        boolean success,
        String message,
        int totalProcessed,
        int totalSaved
    ) {
    }
}
