package com.florence.javamusicplayer;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Middle {
    private Label titleLabelInMusicInfo;
    private Label artistLabelInMusicInfo;
    private Label albumLabelInMusicInfo;
    private Label titleLabelInMusicPlayer;
    private Label artistLabelInMusicPlayer;
    private ImageView albumImageInMusicInfo;
    private ImageView albumImageInMusicPlayer;
    private Button pathDisplayButton;
    private Button playButton;
    private ProgressBar progress;
    private VBox info;
    private HBox player;
    private MediaPlayer mediaPlayer;
    private List<Music> playlist = new ArrayList<>();
    private int currentSongIndex = -1;

    public Middle() {
        loadMusicInfo();
        dropDownAnimation(info);
        loadPlayer();
    }

    public void updateOnceClick(Music music) {
        albumImageInMusicInfo.setImage(music.getAlbumImage());
        albumImageInMusicInfo.setFitWidth(150);
        albumImageInMusicInfo.setFitHeight(150);
        albumLabelInMusicInfo.setText("《" + music.getAlbum() + "》");
        titleLabelInMusicInfo.setText(music.getTitle());
        artistLabelInMusicInfo.setText("—— " + music.getArtist());
        pathDisplayButton.setVisible(true);
        Button likeButton = new Button();
        likeButton.getStyleClass().add("like-button");
        if (!pathDisplayButton.getText().equals("点击显示音源位置")) {
            pathDisplayButton.setText(music.getPath());
            ;
        }
        pathDisplayButton.setOnAction(e -> {
            if (pathDisplayButton.getText().equals("点击显示音源位置")) {
                pathDisplayButton.setText(music.getPath());
            } else {
                pathDisplayButton.setText("点击显示音源位置");
            }
        });


        InputStream unLikedButtonInput = getClass().getResourceAsStream("/images/unliked.png");
        InputStream likedButtonInput = getClass().getResourceAsStream("/images/liked.png");

        ImageView unLikedButtonImageView = new ImageView(new Image(unLikedButtonInput));
        ImageView likedButtonImageView = new ImageView(new Image(likedButtonInput));

        unLikedButtonImageView.setFitHeight(50);
        unLikedButtonImageView.setFitWidth(50);
        likedButtonImageView.setFitHeight(50);
        likedButtonImageView.setFitWidth(50);

        DB db = DB.getInstance();
        if (db.getMusicIsLikedByHashCode(music.hashCode())) {
            likeButton.setGraphic(likedButtonImageView);
        } else {
            likeButton.setGraphic(unLikedButtonImageView);
        }

        // 加载按钮动画
        addButtonAnimation(likeButton);

        likeButton.setOnAction(e -> {
            //已经收藏了就取消收藏
            if (db.getMusicIsLikedByHashCode(music.hashCode())) {
                likeButton.setGraphic(unLikedButtonImageView);
                db.updateMusicIsLikedByHashCode(music.hashCode(), false);

                if (LeftList.getCollectionOrSearchStatus() == 0) {
                    int indexToRemove = -1;
                    for (int i = 0; i < LeftList.getCollectionMusicList().size(); i++) {
                        if (LeftList.getCollectionMusicList().get(i).hashCode() == music.hashCode()) {
                            indexToRemove = i;
                            break;
                        }
                    }
                    if (indexToRemove >= 0) {
                        LeftList.getListView().getItems().remove(indexToRemove);
                    }
                }
                LeftList.getCollectionMusicList().removeIf(m -> m.hashCode() == music.hashCode());

            } else {//未收藏就收藏
                likeButton.setGraphic(likedButtonImageView);
                db.updateMusicIsLikedByHashCode(music.hashCode(), true);
                LeftList.getCollectionMusicList().add(music);
                likeAnimation(likeButton);
            }
        });
        if (info.getChildren().size() == 5) {
            info.getChildren().set(4, likeButton);
            info.getChildren().add(pathDisplayButton);
        } else {
            info.getChildren().set(4, likeButton);
        }
        addButtonAnimation(pathDisplayButton);
        //加载渐进动画
        titleAnimation(titleLabelInMusicInfo);
        artistAnimation(artistLabelInMusicInfo);
        dropDownAnimation(albumImageInMusicInfo);
        upAnimationtion(likeButton).play();
    }


    public void updateTwiceClick(Music music) {
        // 获取音乐标题和艺术家信息
        playerUpAnimation(player);

        String title = music.getTitle();
        String artist = music.getArtist();

        // 调用 updateOnceClick 方法更新基础信息
        updateOnceClick(music);

        // 加载暂停按钮图标
        InputStream pauseButtonInput = getClass().getResourceAsStream("/images/pause.png");
        ImageView pauseButtonImageView = new ImageView(new Image(pauseButtonInput));
        pauseButtonImageView.setFitHeight(47);
        pauseButtonImageView.setFitWidth(47);

        // 设置播放器中标题、艺术家信息和专辑图片
        titleLabelInMusicPlayer.setText(title);
        artistLabelInMusicPlayer.setText(artist);
        albumImageInMusicPlayer.setImage(albumImageInMusicInfo.getImage());


        // 停止并释放当前的 mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            // 创建新的 Media 和 MediaPlayer 对象并播放音乐
            File file = new File(music.getPath());
            URI uri = file.toURI();
            Media media = new Media(uri.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
            // 将播放按钮图标更改为暂停图标
            playButton.setGraphic(pauseButtonImageView);
            // 使专辑图片旋转
            albumImageInPlayerRotate();
            // 绑定进度条以显示播放进度
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                double progressValue = newValue.toMillis() / mediaPlayer.getTotalDuration().toMillis();
                progress.setProgress(progressValue);
            });
        } catch (Exception e) {
            System.out.println("Error loading media: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public VBox getMid() {
        VBox vbox = new VBox();
        vbox.getChildren().add(info);
        vbox.getChildren().add(player);
        VBox.setVgrow(info, Priority.ALWAYS);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    public void setPlaylist(List<Music> playlist) {

        this.playlist = playlist;
    }

    public List<Music> getPlaylist() {
        return playlist;
    }

    public void setCurrentSongIndex(int currentSongIndex) {
        this.currentSongIndex = currentSongIndex;
    }

    private void loadMusicInfo() {
        info = new VBox(10);

        titleLabelInMusicInfo = new Label();
        titleLabelInMusicInfo.setId("music-info-label-title");

        artistLabelInMusicInfo = new Label();
        artistLabelInMusicInfo.setId("music-info-label-artist");

        albumLabelInMusicInfo = new Label();
        albumLabelInMusicInfo.setId("music-info-label-album");
        albumImageInMusicInfo = new ImageView();

        pathDisplayButton = new Button("点击显示音源位置");
        pathDisplayButton.setVisible(false);
        pathDisplayButton.getStyleClass().add("path-display-button");

        info.getChildren().addAll(albumImageInMusicInfo, titleLabelInMusicInfo, artistLabelInMusicInfo, albumLabelInMusicInfo, pathDisplayButton);
        info.getStyleClass().add("music-info");
        info.setPadding(new Insets(20, 0, 20, 0));
        info.setAlignment(Pos.CENTER);
    }

    private void loadPlayer() {
        player = new HBox(20);
        player.getStyleClass().add("player");


        VBox musicImgInPlayer = new VBox();
        albumImageInMusicPlayer = new ImageView();
        albumImageInMusicPlayer.setFitWidth(90);
        albumImageInMusicPlayer.setFitHeight(90);

        Circle clip = new Circle(45, 45, 45);
        albumImageInMusicPlayer.setClip(clip);

        albumImageInMusicPlayer.setEffect(new DropShadow(20, Color.BLACK));

        musicImgInPlayer.getChildren().add(albumImageInMusicPlayer);
        musicImgInPlayer.getStyleClass().add("music-img-player");
        musicImgInPlayer.setAlignment(Pos.CENTER);


        // 创建包含音乐信息的 VBox
        VBox musicInfoInPlayer = new VBox();
        titleLabelInMusicPlayer = new Label();
        titleLabelInMusicPlayer.getStyleClass().add("music-info-player-label");

        artistLabelInMusicPlayer = new Label();
        artistLabelInMusicPlayer.getStyleClass().add("music-info-player-label");

        musicInfoInPlayer.getChildren().addAll(titleLabelInMusicPlayer, artistLabelInMusicPlayer);
        musicInfoInPlayer.getStyleClass().add("music-info-player");
        musicInfoInPlayer.setPadding(new Insets(10));
        musicInfoInPlayer.setAlignment(Pos.CENTER);

        // 创建播放器控制区域的 VBox
        VBox musicPlayer = new VBox(10);
        musicPlayer.setPadding(new Insets(10));
        musicPlayer.setAlignment(Pos.CENTER);

        // 创建播放按钮
        InputStream playButtonInput = getClass().getResourceAsStream("/images/play.png");
        InputStream pauseButtonInput = getClass().getResourceAsStream("/images/pause.png");

        ImageView playButtonImageView = new ImageView(new Image(playButtonInput));
        ImageView pauseButtonImageView = new ImageView(new Image(pauseButtonInput));

        playButtonImageView.setFitHeight(47);
        playButtonImageView.setFitWidth(47);
        pauseButtonImageView.setFitHeight(47);
        pauseButtonImageView.setFitWidth(47);

        //创建播放按钮的点击监听
        playButton = new Button();
        playButton.getStyleClass().add("player-button");
        playButton.setGraphic(playButtonImageView);
        playButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playButton.setGraphic(playButtonImageView);
                } else {
                    mediaPlayer.play();
                    playButton.setGraphic(pauseButtonImageView);
                }
            }
        });

        //创建上下一首按钮
        InputStream lastButtonInput = getClass().getResourceAsStream("/images/last.png");
        InputStream nextButtonInput = getClass().getResourceAsStream("/images/next.png");

        ImageView lastButtonImageView = new ImageView(new Image(lastButtonInput));
        ImageView nextButtonImageView = new ImageView(new Image(nextButtonInput));

        lastButtonImageView.setFitHeight(40);
        lastButtonImageView.setFitWidth(40);
        nextButtonImageView.setFitHeight(40);
        nextButtonImageView.setFitWidth(40);

        Button nextButton = new Button();
        Button lastButton = new Button();
        nextButton.setGraphic(nextButtonImageView);
        lastButton.setGraphic(lastButtonImageView);

        nextButton.getStyleClass().add("last-next-button");
        lastButton.getStyleClass().add("last-next-button");

        // 创建上一首按钮的点击监听
        lastButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                // 计算上一首曲目的索引
                int lastIdx = currentSongIndex - 1;
                if (lastIdx == -1) { // 如果当前是第一首歌，则循环到最后一首
                    lastIdx = getPlaylist().size() - 1;
                }
                currentSongIndex = lastIdx; // 更新当前曲目的索引
                updateTwiceClick(getPlaylist().get(lastIdx)); // 更新并播放上一首曲目
            }
        });

        // 创建下一首按钮的点击监听
        nextButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                // 计算下一首曲目的索引
                int nextIdx = currentSongIndex + 1;
                if (nextIdx == getPlaylist().size()) { // 如果当前是最后一首歌，则循环到第一首
                    nextIdx = 0;
                }
                currentSongIndex = nextIdx; // 更新当前曲目的索引
                updateTwiceClick(getPlaylist().get(nextIdx)); // 更新并播放下一首曲目
            }
        });


        HBox playBox = new HBox();
        playBox.getChildren().addAll(lastButton, playButton, nextButton);
        playBox.setAlignment(Pos.CENTER);
        playBox.setSpacing(10);


        // 创建进度条
        HBox progressBar = new HBox();
        progressBar.setPadding(new Insets(10));
        progressBar.setAlignment(Pos.CENTER);

        // 添加进度条
        progress = new ProgressBar();
        progress.setMinWidth(250);
        progressBar.getChildren().add(progress);
        progress.getStyleClass().add("progress-bar");
        // 将播放按钮和进度条添加到播放器控制区域的 VBox 中
        musicPlayer.getChildren().addAll(playBox, progressBar);

        // 将所有部分添加到播放器的 HBox 中
        player.getChildren().addAll(musicImgInPlayer, musicInfoInPlayer, musicPlayer);

        HBox.setHgrow(musicImgInPlayer, Priority.ALWAYS);
        HBox.setHgrow(musicInfoInPlayer, Priority.ALWAYS);
        HBox.setHgrow(musicPlayer, Priority.ALWAYS);

        musicImgInPlayer.prefWidthProperty().bind(player.widthProperty().multiply(0.15));
        musicInfoInPlayer.prefWidthProperty().bind(player.widthProperty().multiply(0.25));
        musicPlayer.prefWidthProperty().bind(player.widthProperty().multiply(0.60));
        player.setPadding(new Insets(10));

        // 添加进度条绑定
        if (mediaPlayer != null) {
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                double progressValue = newValue.toMillis() / mediaPlayer.getTotalDuration().toMillis();
                progress.setProgress(progressValue);
            });
        }
    }

    private void addButtonAnimation(Button button) {
        // 定义悬停时的缩放动画
        ScaleTransition scaleTransition;
        if (button.getGraphic() != null) {
            scaleTransition = new ScaleTransition(Duration.millis(350), button);
            scaleTransition.setToX(1.3);
            scaleTransition.setToY(1.3);
        } else {
            scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
        }


        // 定义取消悬停时的缩放动画
        ScaleTransition scaleTransitionReverse = new ScaleTransition(Duration.millis(350), button);
        scaleTransitionReverse.setToX(1.0);
        scaleTransitionReverse.setToY(1.0);

        // 添加鼠标进入事件
        button.setOnMouseEntered(event -> {
            scaleTransition.stop(); // 停止可能正在进行的反向动画
            scaleTransition.play(); // 播放悬停动画
        });

        // 添加鼠标离开事件
        button.setOnMouseExited(event -> {
            scaleTransitionReverse.stop(); // 停止可能正在进行的悬停动画
            scaleTransitionReverse.play(); // 播放反向动画
        });
    }


    private void dropDownAnimation(Node node) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(700), node);
        translateTransition.setFromY(-200);
        translateTransition.setToY(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(700), node);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);

        // 并行执行
        translateTransition.play();
        scaleTransition.play();
    }

    private TranslateTransition upAnimationtion(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(800), node);
        transition.setFromY(100);
        transition.setToY(0);
        return transition;
    }

    private void titleAnimation(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(650), node);
        transition.setFromX(-200);
        transition.setToX(0);
        transition.play();
    }

    private void artistAnimation(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(650), node);
        transition.setFromX(200);
        transition.setToX(0);
        transition.play();
    }

    private void likeAnimation(Node node) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), node);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setOnFinished(e -> {
            ScaleTransition scaleTransition2 = new ScaleTransition(Duration.millis(100), node);
            scaleTransition2.setToX(1.0);
            scaleTransition2.setToY(1.0);
        });
    }

    private void playerUpAnimation(Node node) {
        TranslateTransition transition1 = new TranslateTransition(Duration.millis(550), node);
        transition1.setFromY(0);
        transition1.setToY(200);
        // 当transition1完成时，开始transition2
        transition1.setOnFinished(e -> {
            TranslateTransition transition2 = new TranslateTransition(Duration.millis(850), node);
            transition2.setFromY(200);
            transition2.setToY(0);
            transition2.play();
        });
        transition1.play();
    }

    private void albumImageInPlayerRotate() {
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(8), albumImageInMusicPlayer);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
        mediaPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == MediaPlayer.Status.PLAYING) {
                rotateTransition.play();
            } else {
                rotateTransition.pause();
            }
        });
    }


}
