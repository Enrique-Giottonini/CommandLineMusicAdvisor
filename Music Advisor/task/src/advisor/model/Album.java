package advisor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Album {
    private String name;
    private String url;
    private String[] artists;
}
