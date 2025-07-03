package com.aicourse.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Placeholder图片控制器
 */
@RestController
@RequestMapping("/placeholder")
public class PlaceholderController {

    @GetMapping("/{width}/{height}")
    public ResponseEntity<byte[]> getPlaceholderImage(
            @PathVariable int width, 
            @PathVariable int height,
            @RequestParam(defaultValue = "000000") String bg,
            @RequestParam(defaultValue = "ffffff") String color) {
        
        try {
            // 创建图片
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 设置背景色
            Color backgroundColor = Color.decode("#" + bg);
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, width, height);
            
            // 设置文字
            Color textColor = Color.decode("#" + color);
            g2d.setColor(textColor);
            g2d.setFont(new Font("Arial", Font.BOLD, Math.min(width, height) / 8));
            
            String text = width + "×" + height;
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            
            int x = (width - textWidth) / 2;
            int y = (height + textHeight) / 2;
            
            g2d.drawString(text, x, y);
            g2d.dispose();
            
            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("public, max-age=31536000"); // 缓存1年
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 