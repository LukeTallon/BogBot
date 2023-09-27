package org.bog.bot.POJOs;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "contentraw")
    private String contentRaw;

    @Column(name = "author")
    private String author;

    @Column(name = "dateOfMessage")
    private String dateOfMessage;

    @Column(name = "image")
    private String conditionalImage;

    @Column(name = "jumpurl")
    private String jumpUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordQuote that = (DiscordQuote) o;
        return Objects.equals(id, that.id) && Objects.equals(contentRaw, that.contentRaw) && Objects.equals(author, that.author) && Objects.equals(dateOfMessage, that.dateOfMessage) && Objects.equals(conditionalImage, that.conditionalImage) && Objects.equals(jumpUrl, that.jumpUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, contentRaw, author, dateOfMessage, conditionalImage, jumpUrl);
    }
}
