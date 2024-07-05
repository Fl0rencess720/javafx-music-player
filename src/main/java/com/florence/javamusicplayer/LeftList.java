package com.florence.javamusicplayer;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LeftList {
    private static ListView<String> listView;
    private HBox collectionOrSearch;
    private Middle mid;
    public static ArrayList<Music> collectionMusicList;
    public static ArrayList<Music> searchMusicList;
    private static int collectionOrSearchStatus;


    public LeftList(Middle mid) {
        LoadCollectionMusicList();
        LoadSearchMusicList();
        mid.setPlaylist(collectionMusicList);
        this.mid = mid;
        listView = new ListView<>();
        collectionOrSearch = new HBox();
        LoadCollectionOrSearch();
        LoadLeftList(collectionMusicList);
        setupListeners();
    }

    public void LoadCollectionOrSearch() {
        try {
            InputStream collectionInput = getClass().getResourceAsStream("/images/collection.png");
            InputStream searchInput = getClass().getResourceAsStream("/images/search.png");

            ImageView collectionImageView = new ImageView(new Image(collectionInput));
            ImageView searchImageView = new ImageView(new Image(searchInput));

            collectionImageView.setFitHeight(31);
            collectionImageView.setFitWidth(31);
            searchImageView.setFitHeight(31);
            searchImageView.setFitWidth(31);

            collectionImageView.getStyleClass().add("collection-search-image");
            searchImageView.getStyleClass().add("collection-search-image");

//            Button collectionButton = new Button();
//            collectionButton.setGraphic(collectionImageView);

            Button searchButton = new Button();
            searchButton.setGraphic(collectionImageView);

//            collectionButton.getStyleClass().add("collection-search-button");
            searchButton.getStyleClass().add("collection-search-button");

            TextField searchField = new TextField();
            searchField.setPromptText("Search...");
            searchField.setPrefWidth(20);
            searchField.setVisible(false);

            collectionOrSearch.getChildren().addAll(searchButton, searchField);
            collectionOrSearch.setPadding(new Insets(10));
            collectionOrSearch.setSpacing(10);
            collectionOrSearch.setStyle("-fx-alignment: center;");

            searchButton.setOnAction(event -> {
                //关闭搜索框，回到收藏夹
                if (searchField.isVisible()) {
                    collectionOrSearchStatus = 0;
                    KeyValue keyValueWidth = new KeyValue(searchField.prefWidthProperty(), 20);
                    KeyFrame keyFrameWidth = new KeyFrame(Duration.millis(350), keyValueWidth);
                    Timeline timelineWidth = new Timeline(keyFrameWidth);
                    timelineWidth.play();
                    searchField.setVisible(false);

                    searchButton.setGraphic(collectionImageView);
                    TranslateTransition translateSearch = new TranslateTransition(Duration.millis(500), searchButton);
                    translateSearch.setByX(20);

                    translateSearch.play();
                    mid.setPlaylist(collectionMusicList);
                    LoadLeftList(collectionMusicList);
                } else {
                    //打开搜索框，查询全部歌曲
                    collectionOrSearchStatus = 1;

                    TranslateTransition translateSearch = new TranslateTransition(Duration.millis(500), searchButton);
                    translateSearch.setByX(-20);
                    translateSearch.play();

                    searchButton.setGraphic(searchImageView);
                    searchField.setVisible(true);
                    KeyValue keyValueWidth = new KeyValue(searchField.prefWidthProperty(), 100); // Expand width
                    KeyFrame keyFrameWidth = new KeyFrame(Duration.millis(500), keyValueWidth);
                    Timeline timelineWidth = new Timeline(keyFrameWidth);
                    timelineWidth.play();
                    mid.setPlaylist(searchMusicList);
                    LoadLeftList(searchMusicList);
                }
            });
            searchField.setOnAction(event -> {
                String searchText = searchField.getText().toLowerCase();
                searchMusic(searchText);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LoadCollectionMusicList() {
        try {
            var musicDir = Paths.get(getClass().getResource("/music").toURI());
            collectionMusicList = Files.list(musicDir)
                    .filter(path -> path.toString().endsWith(".mp3"))
                    .map(path -> {
                        try {
                            Mp3File mp3file = new Mp3File(path.toFile());
                            String title = "未知标题";
                            String artist = "未知作者";
                            String album = "未知专辑";
                            Image albumImage = null;
                            if (mp3file.hasId3v2Tag()) {
                                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                                title = id3v2Tag.getTitle();
                                artist = id3v2Tag.getArtist();
                                InputStream inputStream = new ByteArrayInputStream(id3v2Tag.getAlbumImage());
                                albumImage = new Image(inputStream);
                                album = id3v2Tag.getAlbum();
                            } else if (mp3file.hasId3v1Tag()) {
                                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                                title = id3v1Tag.getTitle();
                                artist = id3v1Tag.getArtist();
                                album = id3v1Tag.getAlbum();
                            }
                            Music music = new Music(path.toString(), title, artist, album, albumImage);
                            DB db = DB.getInstance();
                            if (db.getMusicIsLikedByHashCode(music.hashCode())) {
                                return music;
                            } else {
                                return null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(music -> music != null) // 过滤掉null值
                    .collect(Collectors.toCollection(ArrayList::new)); // 收集到ArrayList
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void LoadSearchMusicList() {
        try {
            var musicDir = Paths.get(getClass().getResource("/music").toURI());
            searchMusicList = Files.list(musicDir)
                    .filter(path -> path.toString().endsWith(".mp3"))
                    .map(path -> {
                        try {
                            Mp3File mp3file = new Mp3File(path.toFile());
                            String title = "未知标题";
                            String artist = "未知作者";
                            String album = "未知专辑";
                            Image albumImage = null;
                            if (mp3file.hasId3v2Tag()) {
                                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                                title = id3v2Tag.getTitle();
                                artist = id3v2Tag.getArtist();
                                InputStream inputStream = new ByteArrayInputStream(id3v2Tag.getAlbumImage());
                                albumImage = new Image(inputStream);
                                album = id3v2Tag.getAlbum();
                            } else if (mp3file.hasId3v1Tag()) {
                                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                                title = id3v1Tag.getTitle();
                                artist = id3v1Tag.getArtist();
                                album = id3v1Tag.getAlbum();
                            }
                            Music music = new Music(path.toString(), title, artist, album, albumImage);
//                            DB db = DB.getInstance();
//                            db.insertMusicHashAndLiked(music.hashCode(), false);
                            return music;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new)); // 收集到ArrayList

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void LoadLeftList(List<Music> musicList) {
        listView.getItems().clear();  // 先清空现有项目
        for (Music music : musicList) {
            listView.getItems().add(music.getTitle() + " - " + music.getArtist());
        }
    }

    public static ListView<String> getListView() {
        return listView;
    }

    private void setupListeners() {
        listView.setOnMouseClicked(event -> {
                    //双击播放
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                        String selectedItem = listView.getSelectionModel().getSelectedItem();
                        int selectedIdx = listView.getSelectionModel().getSelectedIndex();

                        if (selectedItem != null) {
                            mid.setCurrentSongIndex(selectedIdx);
                            mid.updateTwiceClick(mid.getPlaylist().get(selectedIdx));
                        }
                    }
                    //单击查看信息
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                        String selectedItem = listView.getSelectionModel().getSelectedItem();
                        int selectedIdx = listView.getSelectionModel().getSelectedIndex();
                        if (selectedItem != null) {
                            mid.updateOnceClick(mid.getPlaylist().get(selectedIdx));
                        }
                    }
                }
        );
    }

    public void searchMusic(String searchText) {
        // 使用stream对搜索音乐列表进行过滤，保留标题中包含搜索文本的音乐
        List<Music> searchMusicListFiltered = searchMusicList.stream()
                .filter(music -> music.getTitle().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        // 加载左侧列表，显示筛选后的音乐
        LoadLeftList(searchMusicListFiltered);
        // 设置中间区域的播放列表为筛选后的音乐
        mid.setPlaylist(searchMusicListFiltered);
    }


    public static List<Music> getCollectionMusicList() {
        return collectionMusicList;
    }

    public static List<Music> getSearchMusicList() {
        return searchMusicList;
    }

    public static int getCollectionOrSearchStatus() {
        return collectionOrSearchStatus;
    }

    public VBox getLeftList() {
        VBox vbox = new VBox();
        vbox.getChildren().add(collectionOrSearch);
        vbox.getChildren().add(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.getStyleClass().add("list-view");
        vbox.setPadding(new Insets(10));
        return vbox;
    }
}
