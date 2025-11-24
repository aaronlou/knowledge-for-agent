package ai.innergrow.knowledge.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI Alibaba Agent
 */
@Configuration
public class AgentConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Bean
    public ChatModel chatModel() {
        // DashScope Chat Model will be auto-configured by Spring Boot
        // This bean is for explicit configuration if needed
        return new DashScopeChatModel(dashscopeApiKey);
    }
}
