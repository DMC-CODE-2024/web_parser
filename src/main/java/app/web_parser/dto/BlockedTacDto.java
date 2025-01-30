package app.web_parser.dto;

import lombok.Data;

@Data
public class BlockedTacDto {
    String tac;

    public BlockedTacDto(String[] record) {
        this.tac = record[0].trim();

    }
}
