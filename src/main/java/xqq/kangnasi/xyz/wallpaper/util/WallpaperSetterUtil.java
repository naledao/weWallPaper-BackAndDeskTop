package xqq.kangnasi.xyz.wallpaper.util;

import java.io.IOException;

public class WallpaperSetterUtil {

    // 设置壁纸的方法
    /**
     * 根据传入的 option 设置壁纸。
     *
     * @param imagePath 壁纸图片的绝对路径
     * @param option    "桌面" 表示设置桌面壁纸，"锁屏" 表示设置锁屏壁纸
     * @return 操作是否成功
     */
    public static boolean setWallpaper(String imagePath, String option) throws IOException, InterruptedException {
        if ("桌面".equals(option)) {
            // 设置桌面壁纸：使用 gsettings 设置 GNOME 桌面壁纸
            return setDesktopWallpaper(imagePath);
        } else if ("锁屏".equals(option)) {
            // 设置锁屏壁纸：更新 Gnome 配置文件
            return setLockScreenWallpaper(imagePath);
        } else {
            System.err.println("未知的 option 参数，请使用 \"桌面\" 或 \"锁屏\"");
            return false;
        }
    }

    // 设置桌面壁纸
    private static boolean setDesktopWallpaper(String imagePath) throws IOException, InterruptedException {
        // 使用 gsettings 设置 GNOME 桌面壁纸
        String command = String.format("gsettings set org.gnome.desktop.background picture-uri file://%s", imagePath);
        return executeCommand(command);
    }

    // 设置锁屏壁纸（GNOME 锁屏壁纸）
    private static boolean setLockScreenWallpaper(String imagePath) throws IOException, InterruptedException {
        // 使用 gsettings 设置锁屏壁纸
        String command = String.format("gsettings set org.gnome.desktop.screensaver picture-uri file://%s", imagePath);
        return executeCommand(command);
    }

    // 执行命令的方法
    private static boolean executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command });
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    // 测试方法
    public static void main(String[] args) throws IOException, InterruptedException {
        setWallpaper("/home/ksdy/Pictures/petronas_towers_kuala_lumpur_malaysia_1360675_3840x2400.jpg", "桌面");
        setWallpaper("/home/ksdy/Pictures/petronas_towers_kuala_lumpur_malaysia_1360675_3840x2400.jpg", "锁屏");
    }
}
