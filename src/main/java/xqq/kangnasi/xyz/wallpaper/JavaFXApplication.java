package xqq.kangnasi.xyz.wallpaper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class JavaFXApplication extends Application {
    private Stage splashStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 创建并显示启动窗口
        createSplashStage();
        splashStage.show();

        // 在后台线程启动Spring Boot
        new Thread(() -> {
            try {
                SpringApplication app = new SpringApplication(WallPaperApplication.class);
                // 添加监听器，当Spring Boot启动完成后执行
                app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
                    // 在JavaFX线程中关闭启动窗口并显示主窗口
                    Platform.runLater(() -> {
                        splashStage.close();
                        try {
                            configureMainStage(primaryStage);
                            primaryStage.show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                        Timeline timeline = new Timeline(
//                                new KeyFrame(Duration.seconds(3), e -> {
//                                    splashStage.close();
//                                    // 配置主窗口但不立即显示
//                                    try {
//                                        configureMainStage(primaryStage);
//                                    } catch (IOException ex) {
//                                        throw new RuntimeException(ex);
//                                    }
//                                    primaryStage.show();
//                                })
//                        );
//                        timeline.play();
                    });
                });
                app.run(getParameters().getRaw().toArray(new String[0]));
            } catch (Exception e) {
                e.printStackTrace();
                // 启动失败时显示错误信息
                Platform.runLater(() -> {
                    splashStage.close();
                    showErrorAlert("Spring Boot启动失败: " + e.getMessage());
                });
            }
        }).start();
    }
    private void createSplashStage() {
        splashStage = new Stage();

        // 创建WebView加载HTML
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        try {
            URL htmlUrl = getClass().getResource("/static/start.html");
            if (htmlUrl != null) {
                engine.load(htmlUrl.toExternalForm());
            } else {
                throw new IOException("Cannot find splash screen HTML");
            }
        } catch (Exception e) {
            e.printStackTrace();
            webView.getEngine().loadContent("<h1 style='color:red;text-align:center'>启动界面加载失败</h1>");
        }

        // 配置窗口属性
        Scene splashScene = new Scene(webView, 400, 300);
        splashStage.setScene(splashScene);
        splashStage.setTitle("系统启动中");
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setResizable(false);

        // 关键配置：窗口行为控制
        splashStage.setAlwaysOnTop(true); // 始终保持最前
        splashStage.setIconified(false);  // 初始非最小化状态

        // 禁用最小化事件
        splashStage.iconifiedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(() -> splashStage.setIconified(false));
            }
        });

        // 禁用窗口失焦
        splashStage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(() -> splashStage.requestFocus());
            }
        });

        // 设置图标
        try {
            URL iconUrl = getClass().getResource("/ico.jpg");
            if (iconUrl != null) {
                Image icon = new Image(iconUrl.openStream());
                splashStage.getIcons().add(icon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 窗口位置和显示配置
        splashStage.centerOnScreen();
        splashStage.setOnShown(event -> {
            splashStage.toFront();    // 确保显示在最前
            splashStage.requestFocus(); // 保持焦点
        });
    }

    private void configureMainStage(Stage mainStage) throws IOException {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.load("http://127.0.0.1:16316/");
        BorderPane root = new BorderPane();
        root.setCenter(webView);

        Screen screen = Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();
        Scene scene = new Scene(root, screenWidth*0.8, screenHeight*0.9);
        mainStage.setTitle("远程壁纸共享");
        mainStage.setScene(scene);
//        mainStage.setResizable(false);

        // 设置主窗口图标
        URL iconUrl = getClass().getResource("/ico.jpg");
        if (iconUrl != null) {
            Image icon = new Image(iconUrl.openStream());
            mainStage.getIcons().add(icon);
        }

        // 处理关闭事件
        mainStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("请选择");
            alert.setHeaderText("你确定要退出吗？");

            // 设置对话框图标
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            URL dialogIconUrl = getClass().getResource("/ico.jpg");
            if (dialogIconUrl != null) {
                try {
                    dialogStage.getIcons().add(new Image(dialogIconUrl.openStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // 自定义按钮
            ButtonType yesButton = new ButtonType("是的", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("取消", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                System.exit(0);
            } else {
                event.consume(); // 取消关闭操作
            }
        });
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("启动失败");
        alert.setContentText(message);
        // 设置错误对话框图标
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        URL dialogIconUrl = getClass().getResource("/ico.jpg");
        if (dialogIconUrl != null) {
            try {
                dialogStage.getIcons().add(new Image(dialogIconUrl.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        alert.showAndWait();
    }
}
