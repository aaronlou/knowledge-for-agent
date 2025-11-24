package ai.innergrow.knowledge.agent;

import com.alibaba.cloud.ai.agent.AgentExecutor;
import com.alibaba.cloud.ai.agent.SimpleAgentPromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI Agent for PDF knowledge extraction using Spring AI Alibaba
 */
@Component
public class PdfProcessingAgent {

    private static final Logger logger = LoggerFactory.getLogger(PdfProcessingAgent.class);
    
    private final AgentExecutor agentExecutor;
    private final PdfProcessingTool pdfProcessingTool;

    public PdfProcessingAgent(ChatModel chatModel, PdfProcessingTool pdfProcessingTool) {
        this.pdfProcessingTool = pdfProcessingTool;
        
        // Create agent executor with tools
        this.agentExecutor = AgentExecutor.builder()
                .chatModel(chatModel)
                .tools(List.of(pdfProcessingTool))
                .build();
        
        logger.info("PdfProcessingAgent initialized with tools");
    }

    /**
     * Process PDF directory using AI agent
     * @param pdfDirectory Directory containing PDF files
     * @return Processing result message
     */
    public String processPdfDirectory(String pdfDirectory) {
        logger.info("Agent processing PDF directory: {}", pdfDirectory);
        
        try {
            // Create prompt for the agent
            String userPrompt = String.format(
                "Please process all PDF files in the directory: %s. " +
                "Extract knowledge from these PDFs and save them to the database.",
                pdfDirectory
            );

            // Execute agent with the prompt
            String result = agentExecutor.execute(userPrompt);
            
            logger.info("Agent execution completed successfully");
            return result;
            
        } catch (Exception e) {
            logger.error("Error during agent execution", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Process PDF directory directly without AI agent (for simple execution)
     * @param pdfDirectory Directory containing PDF files
     * @return Processing result
     */
    public PdfProcessingTool.Response processDirectly(String pdfDirectory) {
        logger.info("Direct processing of PDF directory: {}", pdfDirectory);
        return pdfProcessingTool.apply(new PdfProcessingTool.Request(pdfDirectory));
    }
}
