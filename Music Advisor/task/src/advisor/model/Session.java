package advisor.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Session {
    private boolean isActive = true;
    private boolean isAuthenticated = false;
    @Setter private String authorizationCode;
    @Setter private String accessToken;
    @Setter private Pageable<Playlist> playlistCache;
    @Setter private Pageable<Album> albumCache;
    @Setter private Pageable<Category> categoriesCache;
    @Setter private String lastCommand;
    public void setValidAuthentication() {
        isAuthenticated = true;
    }
    public void endSession() {
        isActive = false;
    }
}
