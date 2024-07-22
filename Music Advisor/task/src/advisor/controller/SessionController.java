package advisor.controller;

import advisor.model.*;
import advisor.services.SessionService;
import advisor.view.CommandLineView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;


public class SessionController {
    private static final Set<String> VALID_COMMANDS = new HashSet<>(List.of("auth", "featured", "new", "categories", "playlists", "exit", "next", "prev"));
    private final Session session;
    private final AppConfig config;
    private final Scanner inputScanner;
    private final CommandLineView view;
    private final SessionService sessionService;

    public SessionController(Scanner inputScanner, CommandLineView view) {
        this.session = new Session();
        this.sessionService = new SessionService(session, AppConfig.getAppConfig());
        this.config = AppConfig.getAppConfig();
        this.inputScanner = inputScanner;
        this.view = view;
    }

    public void run() {
        while (session.isActive() && inputScanner.hasNextLine()) {
            String commandQuery = inputScanner.nextLine();
            List<String> tokens = new ArrayList<>(List.of(commandQuery.split(" ")));
            if (tokens.isEmpty() || !VALID_COMMANDS.contains(tokens.get(0))) {
                view.displayUsage();
                continue;
            }

            String command = tokens.remove(0);
            if (command.equals("exit")) {
                view.exit();
//                session.endSession();
            }

            if (!session.isAuthenticated() && !command.equals("auth")) {
                view.requestUserAuthorization();
                continue;
            }

            if (command.equals("auth") && !session.isAuthenticated())  {
                auth();
                continue;
            }

            if (command.equals("prev") || command.equals("next")) {
                changePage(command);
                continue;
            }
            fetchResults(command, tokens);
        }
    }

    private void fetchResults(String command, List<String> args) {
        switch (command) {
            case "featured":
                List<Playlist> featured = getFeaturedPlaylists();
                session.setPlaylistCache(new Pageable<>(featured, config.getPAGE_SIZE()));
                session.setLastCommand(command);
                view.displayFeaturedPlaylists(session.getPlaylistCache().getPage());
                view.displayPagePosition(session.getPlaylistCache().getPageNumber(), session.getPlaylistCache().getTotalPages());
                break;
            case "new":
                List<Album> newAlbums = getNewAlbums();
                session.setAlbumCache(new Pageable<>(newAlbums, config.getPAGE_SIZE()));
                session.setLastCommand(command);
                view.displayNewAlbums(session.getAlbumCache().getPage());
                view.displayPagePosition(session.getAlbumCache().getPageNumber(), session.getAlbumCache().getTotalPages());
                break;
            case "categories":
                List<Category> availableCategories = getAvailableCategories();
                session.setCategoriesCache(new Pageable<>(availableCategories, config.getPAGE_SIZE()));
                session.setLastCommand(command);
                view.displayAvailableCategories(session.getCategoriesCache().getPage());
                view.displayPagePosition(session.getCategoriesCache().getPageNumber(), session.getCategoriesCache().getTotalPages());
                break;
            case "playlists":
                if (args.isEmpty()) {
                    view.displayUsage();
                }
                List<Playlist> playlists = getPlaylistsByCategory(String.join(" ", args));
                session.setPlaylistCache(new Pageable<>(playlists, config.getPAGE_SIZE()));
                session.setLastCommand(command);
                view.displayFeaturedPlaylists(session.getPlaylistCache().getPage());
                view.displayPagePosition(session.getPlaylistCache().getPageNumber(), session.getPlaylistCache().getTotalPages());
                break;
        }

    }

    private void changePage(String direction){
        switch (session.getLastCommand()) {
            case "featured":
                if (direction.equals("next") && session.getPlaylistCache().hasNext()) {
                    session.getPlaylistCache().inc();
                    view.displayFeaturedPlaylists(session.getPlaylistCache().getPage());
                    view.displayPagePosition(session.getPlaylistCache().getPageNumber(), session.getPlaylistCache().getTotalPages());
                } else if (direction.equals("prev") && session.getPlaylistCache().hasPrev()) {
                    session.getPlaylistCache().dec();
                    view.displayFeaturedPlaylists(session.getPlaylistCache().getPage());
                    view.displayPagePosition(session.getPlaylistCache().getPageNumber(), session.getPlaylistCache().getTotalPages());
                } else {
                    view.displayInformativeMessage("No more pages.");
                }
            break;
            case "new":
                if (direction.equals("next") && session.getAlbumCache().hasNext()) {
                    session.getAlbumCache().inc();
                    view.displayNewAlbums(session.getAlbumCache().getPage());
                    view.displayPagePosition(session.getAlbumCache().getPageNumber(), session.getAlbumCache().getTotalPages());
                } else if (direction.equals("prev") && session.getAlbumCache().hasPrev()) {
                    session.getAlbumCache().dec();
                    view.displayNewAlbums(session.getAlbumCache().getPage());
                    view.displayPagePosition(session.getAlbumCache().getPageNumber(), session.getAlbumCache().getTotalPages());
                } else {
                    view.displayInformativeMessage("No more pages.");
                }
                break;
            case "categories":
                if (direction.equals("next") && session.getCategoriesCache().hasNext()) {
                    session.getCategoriesCache().inc();
                    view.displayAvailableCategories(session.getCategoriesCache().getPage());
                    view.displayPagePosition(session.getCategoriesCache().getPageNumber(), session.getCategoriesCache().getTotalPages());
                } else if (direction.equals("prev") && session.getCategoriesCache().hasPrev()) {
                    session.getCategoriesCache().dec();
                    view.displayAvailableCategories(session.getCategoriesCache().getPage());
                    view.displayPagePosition(session.getCategoriesCache().getPageNumber(), session.getCategoriesCache().getTotalPages());
                } else {
                    view.displayInformativeMessage("No more pages.");
                }
                break;
            case "playlists":
                if (direction.equals("next") && session.getPlaylistCache().hasNext()) {
                    session.getPlaylistCache().inc();
                    view.displayPlaylistByCategory(session.getPlaylistCache().getPage());
                    view.displayPagePosition(session.getPlaylistCache().getPageNumber(), session.getPlaylistCache().getTotalPages());
                } else if (direction.equals("prev") && session.getPlaylistCache().hasPrev()) {
                    session.getPlaylistCache().dec();
                    view.displayPlaylistByCategory(session.getPlaylistCache().getPage());
                    view.displayPagePosition(session.getPlaylistCache().getPageNumber(), session.getPlaylistCache().getTotalPages());
                } else {
                    view.displayInformativeMessage("No more pages.");
                }
                break;
        }
    }

    private void auth() {
        view.displayAuthLink(config.getAUTH_LINK());
        view.displayInformativeMessage("waiting for code...");
        sessionService.createCallbackServer();
        view.displayInformativeMessage("code received");
        view.displaySuccessAuthentication();
        view.displayInformativeMessage("making http request for access_token...");
        String token = sessionService.requestAccessToken();
        session.setAccessToken(token);
        view.displayInformativeMessage("token: " + token);
    }

    public List<Playlist> getFeaturedPlaylists() {
        List<Playlist> featured = new ArrayList<>();
        String res = sessionService.getResource("featured-playlists");
        if (res.isEmpty()) {
            view.displayInformativeMessage("Not found");
            return featured;
        }
        JsonObject root = JsonParser.parseString(res).getAsJsonObject();
        JsonObject playlists = root.getAsJsonObject("playlists");
        JsonArray items = playlists.getAsJsonArray("items");

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String name = item.get("name").getAsString();
            String url = item.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            featured.add(new Playlist(name, url));
        }
        return featured;
    }

    public List<Album> getNewAlbums() {
        List<Album> news = new ArrayList<>();
        String res = sessionService.getResource("new-releases");
        if (res.isEmpty()) return news;

        JsonObject root = JsonParser.parseString(res).getAsJsonObject();
        JsonObject albums = root.getAsJsonObject("albums");
        JsonArray items = albums.getAsJsonArray("items");

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String name = item.get("name").getAsString();
            String url = item.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            JsonArray artistsJson = item.get("artists").getAsJsonArray();
            String[] artists = new String[artistsJson.size()];
            for (int j = 0; j < artistsJson.size(); j++) {
                artists[j] = artistsJson.get(j).getAsJsonObject().get("name").getAsString();
            }
            news.add(new Album(name, url, artists));
        }
        return news;
    }

    public List<Category> getAvailableCategories() {
        List<Category> categories = new ArrayList<>();
        String res = sessionService.getResource("categories");
        JsonObject root = JsonParser.parseString(res).getAsJsonObject();
        JsonObject categoriesResponse = root.getAsJsonObject("categories");
        JsonArray items = categoriesResponse.getAsJsonArray("items");

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String name = item.get("name").getAsString();
            String id = item.get("id").getAsString();
            categories.add(new Category(name, id));
        }

        return categories;
    }

    public List<Playlist> getPlaylistsByCategory(String C_NAME) {
        List<Playlist> playlists = new ArrayList<>();
        List<Category> availableCategories = getAvailableCategories();
        Optional<Category> category = availableCategories.stream().filter(c -> c.getName().equals(C_NAME)).findAny();
        if (category.isEmpty()) {
            view.displayInformativeMessage("Unknown category name");
            return playlists;
        }

        String id = category.get().getId();
        String res = sessionService.getResource("categories/" + id + "/playlists");
        JsonObject root = JsonParser.parseString(res).getAsJsonObject();
        if (root.has("error"))  {
            JsonObject error = root.get("error").getAsJsonObject();
            String message = error.get("message").getAsString();
            view.displayInformativeMessage(message);
            return playlists;
        }

        JsonObject playlistsResponse = root.getAsJsonObject("playlists");
        JsonArray items = playlistsResponse.getAsJsonArray("items");

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String name = item.get("name").getAsString();
            String url = item.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            playlists.add(new Playlist(name, url));
        }

        return playlists;
    }



    public void setCustomParamsFromArgs(String[] args) {
        HashMap<String, String> params = _parseParams(args);
        if (params.containsKey("-access")) {
            config.setCustomAuthorizationServer(params.get("-access"));
        }
        if (params.containsKey("-resource")) {
            config.setCustomApiServer(params.get("-resource"));
        }
        if (params.containsKey("-page")) {
            config.setCustomPageSize(Integer.parseInt(params.get("-page")));
        }
    }

    private static HashMap<String, String> _parseParams(String[] args) {
        HashMap<String, String> params = new HashMap<>();
        String currParam = "";
        for (String token : args) {
            if (token.contains("-")) {
                currParam = token;
                continue;
            }
            if (!currParam.isEmpty()) {
                params.put(currParam, token);
                currParam = "";
            }
        }
        return params;
    }
}
