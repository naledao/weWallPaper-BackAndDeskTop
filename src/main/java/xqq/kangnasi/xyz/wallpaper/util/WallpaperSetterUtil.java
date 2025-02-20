package xqq.kangnasi.xyz.wallpaper.util;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class WallpaperSetterUtil {

    // 桌面壁纸接口
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean SystemParametersInfo(int uiAction, int uiParam, String pvParam, int fWinIni);
    }

    // 常量定义
    private static final int SPI_SETDESKWALLPAPER = 20;
    private static final int SPIF_UPDATEINIFILE   = 0x01;
    private static final int SPIF_SENDWININICHANGE = 0x02;

    /**
     * 根据传入的 option 设置壁纸。
     *
     * @param imagePath 壁纸图片的绝对路径
     * @param option    "桌面" 表示设置桌面壁纸，"锁屏" 表示设置锁屏壁纸
     * @return 操作是否成功
     */
    public static boolean setWallpaper(String imagePath, String option) throws IOException, InterruptedException {
        if ("桌面".equals(option)) {
            // 设置桌面壁纸
            return User32.INSTANCE.SystemParametersInfo(
                    SPI_SETDESKWALLPAPER,
                    0,
                    imagePath,
                    SPIF_UPDATEINIFILE | SPIF_SENDWININICHANGE);
        } else if ("锁屏".equals(option)) {
            // 构建PowerShell脚本内容
            String script = String.format(
                    "$imagePath = \"%s\"; " +
                            "$registryPath = \"HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\PersonalizationCSP\"; " +
                            "New-Item -Path $registryPath -Force; " +
                            "New-ItemProperty -Path $registryPath -Name LockScreenImagePath -Value $imagePath -PropertyType String -Force; " +
                            "New-ItemProperty -Path $registryPath -Name LockScreenImageStatus -Value 1 -PropertyType DWORD -Force; " +
                            "New-ItemProperty -Path $registryPath -Name LockScreenImageUrl -Value $imagePath -PropertyType String -Force;",
                    imagePath.replace("\"", "`\"") // 转义双引号
            );

            // 将脚本转换为UTF-16LE格式并Base64编码
            byte[] scriptBytes = script.getBytes(StandardCharsets.UTF_16LE);
            String encodedScript = Base64.getEncoder().encodeToString(scriptBytes);

            // 构建提升权限执行的PowerShell命令
            String command = String.format(
                    "powershell -Command \"Start-Process powershell -ArgumentList '-EncodedCommand %s' -Verb RunAs\"",
                    encodedScript
            );

            // 执行命令
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});

            // 等待执行完成
            process.waitFor();
            return true;
        } else {
            System.err.println("未知的 option 参数，请使用 \"桌面\" 或 \"锁屏\"");
            return false;
        }
    }
    // 测试方法
    public static void main(String[] args) throws IOException, InterruptedException {
        setWallpaper("B:\\springboot项目\\wallPaper\\saveImageDir\\petronas_towers_kuala_lumpur_malaysia_1360675_3840x2400.jpg","锁屏");
    }
}
