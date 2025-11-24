package ai.innergrow.knowledge.service;

import ai.innergrow.knowledge.model.KnowledgeDocument;
import ai.innergrow.knowledge.model.PdfParseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service to save parsed PDF knowledge to file system
 */
@Service
public class KnowledgeStorageService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeStorageService.class);
    private final ObjectMapper objectMapper;
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Value("${knowledge.storage.directory:data/knowledge}")
    private String storageDirectory;

    public KnowledgeStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Initialize storage directory
     */
    private void initializeStorage() {
        try {
            Path storagePath = Paths.get(storageDirectory);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                logger.info("Created storage directory: {}", storageDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create storage directory: {}", storageDirectory, e);
        }
    }

    /**
     * Save a single parse result to file system
     * @param parseResult The PDF parse result
     * @return Saved knowledge document
     */
    public KnowledgeDocument saveKnowledge(PdfParseResult parseResult) {
        initializeStorage();
        
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(idGenerator.getAndIncrement());
        document.setFileName(parseResult.getFileName());
        document.setFilePath(parseResult.getFilePath());
        document.setContent(parseResult.getContent());
        document.setProcessingSuccess(parseResult.isSuccess());
        document.setErrorMessage(parseResult.getErrorMessage());
        document.setExtractedAt(LocalDateTime.now());
        
        // Convert metadata map to JSON string if exists
        if (parseResult.getMetadata() != null) {
            try {
                String metadataJson = objectMapper.writeValueAsString(parseResult.getMetadata());
                document.setMetadata(metadataJson);
            } catch (Exception e) {
                logger.warn("Failed to serialize metadata for file: {}", parseResult.getFileName(), e);
            }
        }

        // Save to JSON file
        try {
            String fileName = String.format("knowledge_%d_%s.json", 
                document.getId(), 
                parseResult.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_"));
            Path filePath = Paths.get(storageDirectory, fileName);
            
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(document);
            Files.writeString(filePath, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            logger.info("Saved knowledge document to file: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to save knowledge document for: {}", parseResult.getFileName(), e);
        }

        return document;
    }

    /**
     * Save multiple parse results to file system
     * @param parseResults List of parse results
     * @return Count of successfully saved documents
     */
    public int saveKnowledgeBatch(List<PdfParseResult> parseResults) {
        int savedCount = 0;
        
        for (PdfParseResult result : parseResults) {
            try {
                saveKnowledge(result);
                savedCount++;
            } catch (Exception e) {
                logger.error("Failed to save knowledge for file: {}", result.getFileName(), e);
            }
        }
        
        logger.info("Saved {} out of {} documents to file system", savedCount, parseResults.size());
        return savedCount;
    }

    /**
     * Get all knowledge documents from storage
     * @return List of all knowledge documents
     */
    public List<KnowledgeDocument> getAllKnowledge() {
        List<KnowledgeDocument> documents = new ArrayList<>();
        initializeStorage();
        
        try {
            Path storagePath = Paths.get(storageDirectory);
            Files.list(storagePath)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        KnowledgeDocument doc = objectMapper.readValue(content, KnowledgeDocument.class);
                        documents.add(doc);
                    } catch (IOException e) {
                        logger.error("Failed to read knowledge file: {}", path, e);
                    }
                });
        } catch (IOException e) {
            logger.error("Failed to list knowledge files", e);
        }
        
        return documents;
    }

    /**
     * Get knowledge document by ID
     * @param id Document ID
     * @return Knowledge document if found
     */
    public KnowledgeDocument getKnowledgeById(Long id) {
        return getAllKnowledge().stream()
            .filter(doc -> doc.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}
