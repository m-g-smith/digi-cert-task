package co.za.smithers.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class ListResponse<T> {
    private final List<T> items;

    public ListResponse(@JsonProperty("items") List<T> items) {
        this.items = items;
    }
}
