package ai.innergrow.knowledge.runner;

import ai.innergrow.knowledge.agent.PdfProcessingAgent;
import ai.innergrow.knowledge.agent.PdfProcessingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner to execute PDF knowledge extraction on startup
 * Usage: java -jar knowledge.jar /path/to/pdf/directory
 */
@Component
public class KnowledgeExtractionRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeExtractionRunner.class);
    
    private final PdfProcessingAgent pdfProcessingAgent;

    public KnowledgeExtractionRunner(PdfProcessingAgent pdfProcessingAgent) {
        this.pdfProcessingAgent = pdfProcessingAgent;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Knowledge Extraction Application Started");
        
        if (args.length == 0) {
            logger.warn("No PDF directory provided. Usage: java -jar knowledge.jar <pdf-directory-path>");
            logger.info("Please provide a PDF directory path as command-line argument");
            return;
        }

        String pdfDirectory = args[0];
        logger.info("PDF Directory: {}", pdfDirectory);
        
        try {
            // Option 1: Use AI Agent for processing (intelligent decision-making)
            // String result = pdfProcessingAgent.processPdfDirectory(pdfDirectory);
            // logger.info("Agent Result: {}", result);
            
            // Option 2: Direct processing (simpler, faster)
            PdfProcessingTool.Response response = pdfProcessingAgent.processDirectly(pdfDirectory);
            
            if (response.success()) {
                logger.info("✓ Processing completed successfully!");
                logger.info("  Total PDFs processed: {}", response.totalProcessed());
                logger.info("  Total saved to database: {}", response.totalSaved());
            } else {
                logger.error("✗ Processing failed: {}", response.message());
            }
            
        } catch (Exception e) {
            logger.error("Error during knowledge extraction", e);
            throw e;
        }
        
        logger.info("Knowledge Extraction Application Finished");
    }
}
