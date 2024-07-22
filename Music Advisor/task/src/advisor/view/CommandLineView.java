package advisor.view;

import advisor.model.Album;
import advisor.model.Category;
import advisor.model.Playlist;

import java.util.Arrays;
import java.util.List;

public class CommandLineView {
    public void displayUsage() {
        System.out.println(
                """
                usage: > [command] where command can be:
                       > auth - authenticate first to run other commands
                       > new - a list of new albums with artists and links on Spotify;
                       > featured - a list of Spotify-featured playlists with their links fetched from API;
                       > categories - a list of all available categories on Spotify (just their names);
                       > playlist C_NAME - where C_NAME is the name of category. The list contains playlists of this category and their links on Spotify;
                       > exit - shuts down the application;
                """
        );
    }

    public void exit() {
        System.out.println("---GOODBYE!---");
    }

    public void displayFeaturedPlaylists(List<Playlist> featured) {
        if (featured.isEmpty()) {
            return;
        }
        for (Playlist playlist : featured) {
            System.out.println(playlist.getName());
            System.out.println(playlist.getUrl());
        }
    }

    public void displayNewAlbums(List<Album> newAlbums) {
        if (newAlbums.isEmpty()) {
            return;
        }
        for (Album album : newAlbums) {
            System.out.println(album.getName());
            System.out.println(Arrays.toString(album.getArtists()));
            System.out.println(album.getUrl());
        }
    }

    public void displayAvailableCategories(List<Category> categories) {
        if (categories.isEmpty()) {
            return;
        }
        for (Category category : categories) {
            System.out.println(category.getName());
        }
    }

    public void displayPlaylistByCategory(List<Playlist> playlists) {
        if (playlists.isEmpty()) return;
        for (Playlist playlist : playlists) {
            System.out.println(playlist.getName());
            System.out.println(playlist.getUrl());
        }
    }

    public void requestUserAuthorization() {
        System.out.println("Please, provide access for application.");
    }

    public void displayAuthLink(String authLink) {
        System.out.println("use this link to request the access code:");
        System.out.println(authLink);
    }

    public void displaySuccessAuthentication() {
        System.out.println("---SUCCESS---");
    }

    public void displayInformativeMessage(String message) {
        System.out.println(message);
    }


    public void displayPagePosition(int pageNumber, int totalPages) {
        System.out.println("---PAGE " + pageNumber + " OF " + totalPages + "---");
    }
}
