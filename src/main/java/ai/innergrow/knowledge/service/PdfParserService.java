package ai.innergrow.knowledge.service;

import ai.innergrow.knowledge.model.PdfParseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to execute Python script for PDF parsing
 */
@Service
public class PdfParserService {

    private static final Logger logger = LoggerFactory.getLogger(PdfParserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${knowledge.python.script.path}")
    private String pythonScriptPath;

    @Value("${knowledge.python.executable:python3}")
    private String pythonExecutable;

    /**
     * Parse all PDF files in the given directory
     * @param pdfDirectory Directory containing PDF files
     * @return List of parse results
     */
    public List<PdfParseResult> parsePdfDirectory(String pdfDirectory) {
        List<PdfParseResult> results = new ArrayList<>();
        
        try {
            Path dirPath = Paths.get(pdfDirectory);
            
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                logger.error("Invalid PDF directory: {}", pdfDirectory);
                PdfParseResult errorResult = new PdfParseResult();
                errorResult.setSuccess(false);
                errorResult.setErrorMessage("Invalid directory: " + pdfDirectory);
                results.add(errorResult);
                return results;
            }

            // Find all PDF files in directory
            List<File> pdfFiles = Files.walk(dirPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            logger.info("Found {} PDF files in directory: {}", pdfFiles.size(), pdfDirectory);

            // Parse each PDF file
            for (File pdfFile : pdfFiles) {
                PdfParseResult result = parseSinglePdf(pdfFile.getAbsolutePath());
                results.add(result);
            }

        } catch (Exception e) {
            logger.error("Error processing PDF directory: {}", pdfDirectory, e);
            PdfParseResult errorResult = new PdfParseResult();
            errorResult.setSuccess(false);
            errorResult.setErrorMessage("Error: " + e.getMessage());
            results.add(errorResult);
        }

        return results;
    }

    /**
     * Parse a single PDF file using Python script
     * @param pdfFilePath Path to the PDF file
     * @return Parse result
     */
    public PdfParseResult parseSinglePdf(String pdfFilePath) {
        PdfParseResult result = new PdfParseResult();
        result.setFilePath(pdfFilePath);
        result.setFileName(Paths.get(pdfFilePath).getFileName().toString());

        try {
            // Build Python command
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonExecutable,
                    pythonScriptPath,
                    pdfFilePath
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Parse JSON output from Python script
                String jsonOutput = output.toString().trim();
                try {
                    // Parse the JSON response from Python script
                    @SuppressWarnings("unchecked")
                    var jsonResult = objectMapper.readValue(jsonOutput, java.util.Map.class);
                    
                    result.setContent((String) jsonResult.get("content"));
                    result.setSuccess((Boolean) jsonResult.getOrDefault("success", true));
                    
                    // Set metadata if present
                    @SuppressWarnings("unchecked")
                    var metadata = (java.util.Map<String, Object>) jsonResult.get("metadata");
                    if (metadata != null) {
                        result.setMetadata(metadata);
                    }
                    
                    logger.info("Successfully parsed PDF: {}", pdfFilePath);
                } catch (Exception e) {
                    // Fallback: use raw output if JSON parsing fails
                    result.setContent(jsonOutput);
                    result.setSuccess(true);
                    logger.warn("Could not parse JSON output, using raw content for: {}", pdfFilePath);
                }
            } else {
                result.setSuccess(false);
                result.setErrorMessage("Python script failed with exit code: " + exitCode + "\n" + output);
                logger.error("Failed to parse PDF: {}, exit code: {}", pdfFilePath, exitCode);
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Exception: " + e.getMessage());
            logger.error("Error executing Python script for PDF: {}", pdfFilePath, e);
        }

        return result;
    }
}
