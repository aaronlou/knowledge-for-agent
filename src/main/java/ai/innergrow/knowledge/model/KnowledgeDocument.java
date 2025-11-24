package ai.innergrow.knowledge.model;

import java.time.LocalDateTime;

/**
 * Model representing extracted knowledge
 */
public class KnowledgeDocument {
    
    private Long id;

    private String fileName;

    private String filePath;

    private String content;

    private String metadata;

    private LocalDateTime extractedAt;

    private boolean processingSuccess;

    private String errorMessage;

    public KnowledgeDocument() {
        this.extractedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(LocalDateTime extractedAt) {
        this.extractedAt = extractedAt;
    }

    public boolean isProcessingSuccess() {
        return processingSuccess;
    }

    public void setProcessingSuccess(boolean processingSuccess) {
        this.processingSuccess = processingSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}