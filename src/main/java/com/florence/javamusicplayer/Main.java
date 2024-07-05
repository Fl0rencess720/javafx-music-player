package com.florence.javamusicplayer;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DB db = DB.getInstance();
        StackPane root = new StackPane();
        //创建背景图层
        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("image-background");
        //创建覆盖在背景图上的模糊层
        StackPane overlayPane = new StackPane();
        overlayPane.getStyleClass().add("overlay-background");

        root.getChildren().addAll(imagePane, overlayPane);
        // 创建中间音乐信息
        Middle mid = new Middle();
        VBox midBox = mid.getMid();
        // 创建左侧音乐列表
        LeftList leftList = new LeftList(mid);
        VBox leftListView = leftList.getLeftList();
        // 使用 BorderPane 将左侧列表添加到左侧，并将音乐信息显示在中心
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(leftListView);
        borderPane.setCenter(midBox);

        // 将 BorderPane 添加到 overlayPane 中
        overlayPane.getChildren().add(borderPane);
        Scene scene = new Scene(root, 1055, 610);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("javaFX音乐播放器");
        stage.setScene(scene);
        stage.setMinWidth(1055);
        stage.setMinHeight(610);
        Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
        stage.getIcons().add(icon);
        stage.show();

        slideInFromLeft(leftListView);
    }

    // 左侧滑入动画
    private void slideInFromLeft(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(700), node);
        transition.setFromX(-200);
        transition.setToX(0);
        transition.play();
    }

    public static void main(String[] args) {
        launch();
    }
}
