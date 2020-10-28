package com.cc.server.worker.services;

import java.io.ByteArrayOutputStream;
import java.io.File; 
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.awt.image.BufferedImage; 
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service; 
@Service
  public class ImageProcessing{ 
	public int start(String key)throws IOException {
	try {
		BufferedImage img = null; 
		File f = null; 
		f = new File("src/main/resources/img/original/"+key); 

		img = ImageIO.read(f);
		int width = img.getWidth(); 
		int height = img.getHeight(); 
		for (int y = 0; y < height; y++){ 
			for (int x = 0; x < width; x++) { 
				int p = img.getRGB(x,y); 
				int a = (p>>24)&0xff; 
				int r = (p>>16)&0xff; 
				p = (a<<24) | (r<<16) | (0<<8) | 0; 
				img.setRGB(x, y, p); 
			} 
		} 
		f=new File("src/main/resources/img/edited/"+key);
		ImageIO.write(img, "jpg", f); 
		return 1;
	} catch (Exception e) {
		e.printStackTrace();
		return 0;
	}
	} 
	
	public byte[] startV2(InputStream is)throws IOException {
		try {
			BufferedImage img = null; 
			img = ImageIO.read(is);
			int width = img.getWidth(); 
			int height = img.getHeight(); 
			for (int y = 0; y < height; y++){ 
				for (int x = 0; x < width; x++) { 
					int p = img.getRGB(x,y); 
					int a = (p>>24)&0xff; 
					int r = (p>>16)&0xff; 
					p = (a<<24) | (r<<16) | (0<<8) | 0; 
					img.setRGB(x, y, p); 
				} 
			} 
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( img, "jpg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			return imageInByte;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		}
} 
