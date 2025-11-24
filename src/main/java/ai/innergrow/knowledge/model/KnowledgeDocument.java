package ai.innergrow.knowledge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing extracted knowledge stored in PostgreSQL
 */
@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime extractedAt;

    @Column(nullable = false)
    private boolean processingSuccess;

    @Column(columnDefinition = "TEXT")
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

    public String