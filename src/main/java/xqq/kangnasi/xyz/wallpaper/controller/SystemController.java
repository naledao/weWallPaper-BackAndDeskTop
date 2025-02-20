package xqq.kangnasi.xyz.wallpaper.controller;

import cn.hutool.Hutool;
import cn.hutool.crypto.SecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xqq.kangnasi.xyz.wallpaper.domain.vo.LocalImage;
import xqq.kangnasi.xyz.wallpaper.util.WallpaperSetterUtil;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/system")
@CrossOrigin
public class SystemController {

    @Value("${app.saveImageDir}")
    private String saveImageDir;

    @PostMapping("/setLocalImage")
    public Integer setLocalImage(@RequestParam("image") MultipartFile multipartFile, @RequestParam("option") String option) throws IOException, InterruptedException {
        String fileName= SecureUtil.md5(multipartFile.getOriginalFilename());
        System.out.println(fileName);
        Path path=Paths.get(saveImageDir,fileName);
        if(!Files.exists(path)){
            multipartFile.transferTo(path);
        }
        return getSetMsg(option, path);
    }

    private Integer getSetMsg(@RequestParam("option") String option, Path path) throws IOException, InterruptedException {
        String absolutePath = path.toAbsolutePath().toString();
        int i = absolutePath.indexOf(".");
        String subPath=absolutePath.substring(0,i)+absolutePath.substring(i+2);
        boolean b = WallpaperSetterUtil.setWallpaper(subPath,option);
        return b ?1:0;
    }


    @PostMapping("/setImage")
    public Integer setImage(@RequestParam("url") String url,@RequestParam("option") String option) throws IOException, InterruptedException {
        // 从URL提取文件名
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Path path = Paths.get(saveImageDir, fileName);
        if(!Files.exists(path)){
            // 处理可能包含的查询参数
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf('?'));
            }

            // 创建保存目录（如果不存在）
            Path dirPath = Paths.get(saveImageDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 下载并保存图片
            Path filePath = dirPath.resolve(fileName);
            try (InputStream inputStream = new URL(url).openStream()) {
                Files.copy(inputStream, filePath);
            }
        }
        return getSetMsg(option, path);
    }

    @GetMapping("/openSaveDir")
    public void openSaveDir() throws IOException {
        Path path = Paths.get(saveImageDir);
        String absolutePath = path.toAbsolutePath().toString();
        int i = absolutePath.indexOf(".");
        String subPath=absolutePath.substring(0,i)+absolutePath.substring(i+2);
        File folder = new File(subPath);
        if (!folder.exists()) {
            Path path1=Paths.get(subPath);
            Files.createDirectory(path1);
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;

            if (os.contains("win")) {
                // Windows
                process = Runtime.getRuntime().exec("explorer.exe /select," + subPath);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux/macOS
                process = Runtime.getRuntime().exec(new String[]{"xdg-open", subPath});
            } else {
                return;
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    @GetMapping("/getLocalImages")
    public List<LocalImage> getLocalImages() throws IOException {
        Path path=Paths.get("./saveImageDir");
        List<LocalImage> res=new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String absolutePath = file.toAbsolutePath().toString().replace(".\\","");
                LocalImage localImage=new LocalImage();
                localImage.setLocalAddress(absolutePath);
                localImage.setBase64(encodeImageToBase64(absolutePath));
                res.add(localImage);
                return super.visitFile(file, attrs);
            }
        });
        return res;
    }

    private String encodeImageToBase64(String imagePath){
        try {
            // 读取图片文件
            File file = new File(imagePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] imageBytes = new byte[(int) file.length()];
            fileInputStream.read(imageBytes);
            fileInputStream.close();

            // 将字节数组转换为Base64编码
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
