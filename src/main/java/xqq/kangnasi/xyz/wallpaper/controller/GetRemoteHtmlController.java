package xqq.kangnasi.xyz.wallpaper.controller;

import cn.hutool.http.HttpUtil;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/remoteHtml")
@RestController
@CrossOrigin
public class GetRemoteHtmlController {
    @GetMapping("/wallPapersCraft")
    public String getWallPapersCraft(@RequestParam("pageNum") Integer pageNum){
        return HttpUtil.get("https://wallpaperscraft.com/all/page"+pageNum);
    }

    @GetMapping("/wallpaper")
    public String getWallPaper(@RequestParam("str") String str){
        return HttpUtil.get("https://wallpaperscraft.com/wallpaper/"+str);
    }
}
