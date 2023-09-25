package org.bog.bot.POJOs;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode
public class MessageRetrievalOutput {

    private Map<String, DiscordQuote> outputMap;
}
