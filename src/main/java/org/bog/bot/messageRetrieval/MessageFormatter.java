package org.bog.bot.messageRetrieval;

import org.bog.bot.POJOs.DiscordQuote;

public class MessageFormatter {

    public String formatMessageDetails(DiscordQuote discordQuote) {

        StringBuilder sb = new StringBuilder();
        String rawMessageContent = boldenText(discordQuote.getContentRaw());
        appendIfNotNullOrEmpty(sb, rawMessageContent);

        sb
                .append("- Posted by: ").append(discordQuote.getAuthor()).append("\n")
                .append("- Date: ").append(discordQuote.getDateOfMessage()).append("\n");

        appendIfNotNullOrEmpty(sb, discordQuote.getConditionalImage());

        sb.append(discordQuote.getJumpUrl());

        return sb.toString();
    }

    private void appendIfNotNullOrEmpty(StringBuilder builder, String text) {
        if (text != null && !text.isEmpty()) {
            builder.append(text);
        }
    }

    private String boldenText(String text) {
        if (text != null && !text.isEmpty()) {
            return "**" + text + "**";
        } else return null;
    }
}
